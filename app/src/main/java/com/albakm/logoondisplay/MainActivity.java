package com.albakm.logoondisplay;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {
    private static final int PERMISSION_REQUEST_CODE = 5692;
    private static final int OVERLAY_REQUEST_CODE = 5693;
    private static final int PICK_IMAGE = 5694;
    private Spinner m_cGravity;
    private SeekBar m_cSeekBarSize;
    private ImageView m_cImagePng;
    private CheckBox m_cCheckSB;
    private SeekBar m_cTransparent;
    private SeekBar m_cRotation;
    private SeekBar m_cHorShift;
    private SeekBar m_cVertShift;

    private boolean m_bShowIndicator=true;
    private WebView m_cWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckPermissionsOrRun();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(isFinishing())
        {
            try {
                SavePrefs();
            }
            catch (Exception ex){}
            if(m_bShowIndicator)
                StartService(true);
        }
    }

    private void SelectImagePng()
    {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/png");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/png");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void OpenSettingsWindow()
    {
        setContentView(R.layout.activity_main);
        m_cGravity=(Spinner)findViewById(R.id.SpinnerGravity);
        m_cSeekBarSize=(SeekBar)findViewById(R.id.SeekBarSize);
        m_cImagePng=(ImageView)findViewById(R.id.ImageViewPNG);
        m_cTransparent=(SeekBar)findViewById(R.id.SeekBarTransparent);
        m_cCheckSB=(CheckBox)findViewById(R.id.CheckBoxFullScreen);
        m_cRotation=(SeekBar)findViewById(R.id.seekBarRotation);
        m_cHorShift=(SeekBar)findViewById(R.id.seekBarHorShift);
        m_cVertShift=(SeekBar)findViewById(R.id.seekBarVertShift);

        SharedPreferences cPrefs=getSharedPreferences ("Common", Context.MODE_PRIVATE);

        int nGravity=cPrefs.getInt("Gravity", 0);
        m_cTransparent.setProgress(cPrefs.getInt("Transparency",255));
        m_cRotation.setProgress(cPrefs.getInt("Rotation",0));
        m_cCheckSB.setChecked(cPrefs.getBoolean("Show on status bar",true));
        m_cHorShift.setProgress(cPrefs.getInt("Horz shift",0)+100);
        m_cVertShift.setProgress(cPrefs.getInt("Vert shift",0)+100);
        Bitmap cBmp=ImageHelper.getImage(this);
        m_cImagePng.setImageBitmap(cBmp);
        m_cGravity.setSelection(nGravity);
        m_cSeekBarSize.setProgress(cPrefs.getInt("Size", 15));

        m_cGravity.setOnItemSelectedListener(this);
        m_cSeekBarSize.setOnSeekBarChangeListener(this);
        m_cTransparent.setOnSeekBarChangeListener(this);
        m_cRotation.setOnSeekBarChangeListener(this);
        m_cCheckSB.setOnCheckedChangeListener(this);
        m_cHorShift.setOnSeekBarChangeListener(this);
        m_cVertShift.setOnSeekBarChangeListener(this);
        m_cImagePng.setAlpha(m_cTransparent.getProgress()/255.0f);
        m_cImagePng.setRotation(m_cRotation.getProgress());

        findViewById(R.id.btnReset).setOnClickListener(this);
     //   findViewById(R.id.btnDonation).setOnClickListener(this);
        findViewById(R.id.ButtonStart).setOnClickListener(this);
        findViewById(R.id.ButtonStop).setOnClickListener(this);
        m_cImagePng.setOnClickListener(this);
        m_cImagePng.setOnLongClickListener(this);

        m_cWebView=(WebView)findViewById(R.id.webView1);
        // включаем поддержку JavaScript
        m_cWebView.getSettings().setJavaScriptEnabled(true);
        // указываем страницу загрузки
        m_cWebView.loadUrl("file:///android_asset/paypal_donate.html");

        SavePrefs();
        StartService(true);
    }

    private void ResetToDefault()
    {
        m_cGravity.setOnItemSelectedListener(null);
        m_cSeekBarSize.setOnSeekBarChangeListener(null);
        m_cTransparent.setOnSeekBarChangeListener(null);
        m_cRotation.setOnSeekBarChangeListener(null);
        m_cCheckSB.setOnCheckedChangeListener(null);
        m_cHorShift.setOnSeekBarChangeListener(null);
        m_cVertShift.setOnSeekBarChangeListener(null);

        m_cTransparent.setProgress(m_cTransparent.getMax());
        m_cRotation.setProgress(0);
        m_cGravity.setSelection(0);
        m_cSeekBarSize.setProgress(15);
        m_cCheckSB.setChecked(true);
        ImageHelper.RemoveInternalFile(this);
        m_cVertShift.setProgress(100);
        m_cHorShift.setProgress(100);

        Bitmap cBmp=ImageHelper.getImage(this);
        m_cImagePng.setImageBitmap(cBmp);
        m_cImagePng.setAlpha(m_cTransparent.getProgress()/255.0f);
        m_cImagePng.setRotation(m_cRotation.getProgress());

        m_cGravity.setOnItemSelectedListener(this);
        m_cSeekBarSize.setOnSeekBarChangeListener(this);
        m_cTransparent.setOnSeekBarChangeListener(this);
        m_cRotation.setOnSeekBarChangeListener(this);
        m_cCheckSB.setOnCheckedChangeListener(this);
        m_cHorShift.setOnSeekBarChangeListener(this);
        m_cVertShift.setOnSeekBarChangeListener(this);
        SavePrefs();
        StartService(true);
    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.ButtonStart:
                m_bShowIndicator=true;
                SavePrefs();
                StartService(true);
                break;
            case R.id.ButtonStop:
                m_bShowIndicator=false;
                StopService();
                break;
            case R.id.ImageViewPNG:
                SelectImagePng();
                break;
            case R.id.btnReset:
                ResetToDefault();
                break;
        /*    case R.id.btnDonation:
                ShowDonationWnd();
                break;*/
        }
    }

    /*private void ShowDonationWnd()
    {
        Intent cIntent=new Intent(this,DonationActivity.class);
        startActivity(cIntent);
    }*/

    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
    {
        switch (seekBar.getId()) {
            case R.id.SeekBarTransparent:
                m_cImagePng.setAlpha(m_cTransparent.getProgress() / 255.0f);
            break;
            case R.id.seekBarRotation:
                m_cImagePng.setRotation(m_cRotation.getProgress());
                break;
        }
        SavePrefs();
        StartService(false);
    }

    public void onStartTrackingTouch(SeekBar seekBar)
    {
        // TODO Auto-generated method stub

    }

    public void onStopTrackingTouch(SeekBar seekBar)
    {
        // TODO Auto-generated method stub

    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        SavePrefs();
        StartService(false);
    }

    private void SavePrefs()
    {
        SharedPreferences.Editor cPrefs=getSharedPreferences ("Common", Context.MODE_PRIVATE).edit();
        cPrefs.clear();
        cPrefs.putInt("Gravity", m_cGravity.getSelectedItemPosition());
        cPrefs.putInt("Size", m_cSeekBarSize.getProgress());
        cPrefs.putInt("Transparency", m_cTransparent.getProgress());
        cPrefs.putBoolean("Show on status bar",m_cCheckSB.isChecked());
        cPrefs.putInt("Rotation",m_cRotation.getProgress());
        cPrefs.putInt("Horz shift",m_cHorShift.getProgress()-100);
        cPrefs.putInt("Vert shift",m_cVertShift.getProgress()-100);
        cPrefs.commit();
    }

    private void StartService(boolean bStart)
    {
        try
        {
            Intent cIntent=new Intent(this,LogoService.class);
            if(bStart)
                cIntent.setAction(LogoService.ACTION_START);
            else
                cIntent.setAction(LogoService.ACTION_UPDATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(cIntent);
            }
            else
                startService(cIntent);
        }
        catch(Exception ex){}
    }

    private void StopService()
    {
        try
        {
            Intent cIntent=new Intent(this,LogoService.class);
            cIntent.setAction(LogoService.ACTION_STOP);
            startService(cIntent);
        }
        catch(Exception ex){}
    }

    private void CheckPermissionsOrRun() {
        boolean bOverlay = ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED;
        boolean bUsage = ActivityCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
        boolean bInternet = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
        boolean bNetworkState= ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean bWiFiState= ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
        ArrayList<String> strArNeedPermission = new ArrayList<String>();
        if (!bOverlay)
            strArNeedPermission.add(android.Manifest.permission.SYSTEM_ALERT_WINDOW);
        if (!bUsage)
            strArNeedPermission.add(android.Manifest.permission.PACKAGE_USAGE_STATS);
        if (!bInternet)
            strArNeedPermission.add(android.Manifest.permission.INTERNET);
        if (!bNetworkState)
            strArNeedPermission.add(android.Manifest.permission.ACCESS_NETWORK_STATE);
        if (!bWiFiState)
            strArNeedPermission.add(android.Manifest.permission.ACCESS_WIFI_STATE);
        if (strArNeedPermission.size() > 0) {
            //спрашиваем пермишен у пользователя
            requestPermission(strArNeedPermission);
        } else {
            CheckOverlayPermissionOrRun();
        }
    }

    public void requestPermission(ArrayList<String> strArNeedPermission) {
        String[] strArList = new String[strArNeedPermission.size()];
        strArNeedPermission.toArray(strArList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, strArList, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            boolean bOverlay = ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED;
            boolean bInternet = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
            boolean bNetworkState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean bWiFiState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean bWriteSettings = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
            for (int n = 0; n < permissions.length; n++) {
                if (grantResults[n] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[n].equals(android.Manifest.permission.SYSTEM_ALERT_WINDOW))
                        bOverlay = true;
                    if (permissions[n].equals(android.Manifest.permission.INTERNET))
                        bInternet = true;
                    if (permissions[n].equals(android.Manifest.permission.ACCESS_NETWORK_STATE))
                        bNetworkState = true;
                    if (permissions[n].equals(android.Manifest.permission.ACCESS_WIFI_STATE))
                        bWiFiState = true;
                    if (permissions[n].equals(android.Manifest.permission.WRITE_SETTINGS))
                        bWriteSettings = true;
                }
            }
            CheckOverlayPermissionOrRun();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void CheckOverlayPermissionOrRun()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!Settings.canDrawOverlays(this.getApplicationContext()))
            {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
                return;
            }
        }
        OpenSettingsWindow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE:
                if(resultCode==RESULT_OK) {
                    if(data==null)//some error?
                    {

                    }
                    else
                    {
                        Uri uriImage=data.getData();
                        ImageHelper.CopyFileToInternalFileDir(this,uriImage);
                        Bitmap cBmp=ImageHelper.getImage(this);
                        m_cImagePng.setImageBitmap(cBmp);
                        m_cImagePng.setTag(uriImage);
                        SavePrefs();
                        StartService(true);
                    }
                }
                break;
            case OVERLAY_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                    OpenSettingsWindow();
                } else {
                    AlertDialog.Builder cBuilder = new AlertDialog.Builder(this);
                    cBuilder.setTitle(R.string.app_name);
                    cBuilder.setMessage(R.string.PermissionsNeeded);
                    cBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    cBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
                    cBuilder.create().show();
                }
                break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        SavePrefs();
        StartService(false);
    }

    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId())
        {
            case R.id.ImageViewPNG:
                Bitmap cBmp=ImageHelper.getImage(this);
                m_cImagePng.setImageBitmap(cBmp);
                m_cImagePng.setTag(null);
                SavePrefs();
                StartService(true);
                return true;
        }
        return false;
    }
}
