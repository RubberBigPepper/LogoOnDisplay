package com.albakm.logoondisplay;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.reflect.Method;

public class LogoService extends Service {

    public static String ACTION_START="albakm.StartLogoService";
    public static String ACTION_STOP="albakm.StopLogoService";
    public static String ACTION_UPDATE="albakm.UpdateLogoService";

    private static final String m_channelId = "channel-logoondisplay-01";
    private static final String m_channelName = "logoondisplay";
    private static final int m_importance = NotificationManager.IMPORTANCE_DEFAULT;
    private WindowManager m_cWM=null;
    private ImageView m_cIVLogo=null;

    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private static final Class<?>[] mSetForegroundSignature = new Class[] {boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[] {int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class};
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    public LogoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void invokeMethod(Method method, Object[] args)
    {
        try
        {
            mStartForeground.invoke(this, mStartForegroundArgs);
        }
        catch (Exception e)
        {
        }
    }

    void startForegroundCompat(int id, Notification notification)
    {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null)
        {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    void stopForegroundCompat(int id)
    {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null)
        {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try
            {
                mStopForeground.invoke(this, mStopForegroundArgs);
            }
            catch (Exception e)
            {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    @Override
    public void onCreate()
    {
        try
        {
            mStartForeground = getClass().getMethod("startForeground",mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",mStopForegroundSignature);
        }
        catch (Exception e)
        {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
            //return;
        }
        try
        {
            mSetForeground = getClass().getMethod("setForeground",mSetForegroundSignature);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                    "OS doesn't have Service.startForeground OR Service.setForeground!");
        }
    }



    @Override
    public void onDestroy()
    {
        Log.e("DisplayBrightness","OnDestroy reached");
        CloseChannel(this);
    }

    public void CreateChannel(Context cContext)
    {
        NotificationManager cNM=(NotificationManager)cContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O&&cNM.getNotificationChannel(m_channelId)==null) {
            NotificationChannel cChannel = new NotificationChannel(m_channelId, m_channelName, m_importance);
            cChannel.enableVibration(false);
            cChannel.setSound(null,null);
            cNM.createNotificationChannel(cChannel);
        }
    }

    public void CloseChannel(Context cContext)
    {
        NotificationManager cNM=(NotificationManager)cContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O&&cNM.getNotificationChannel(m_channelId)!=null)
        {
            cNM.deleteNotificationChannel(m_channelId);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        CreateChannel(this);
        handleCommand(intent);
        return Service.START_STICKY;//START_REDELIVER_INTENT;
    }

    public void handleCommand(Intent intent)
    {
        AlarmManager cAM=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent cIntent=new Intent();
        cIntent.setAction("rubberbigpepper.DisplayBrightness.NeverKillingService");
        PendingIntent cPendIntent=PendingIntent.getBroadcast(this,0,cIntent,0);
        cAM.cancel(cPendIntent);
        String strAction="";
        if(intent==null)
        {
            Log.e("DisplayBrightness","Intent is null,service was restarted. Killing");
            stopSelf();
            return;
        }
        else
        {
            strAction=intent.getAction();
        }

        if(strAction!=null&&strAction.equalsIgnoreCase(ACTION_STOP))
        {
            HideIndicator();
            try
            {
                m_cWM.removeView(m_cIVLogo);
            }
            catch(Exception ex)
            {

            }
            stopSelf();
        }
        else
        {//пїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅпїЅ
            if(strAction!=null&&strAction.equalsIgnoreCase(ACTION_START))
            {
                HideIndicator();
                m_cWM=null;
                ShowIndicator();
                //startForegroundCompat(R.string.app_name, new Notification());
                int NOTIFICATION_ID = (int) (System.currentTimeMillis()%10000);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this,m_channelId).build());
                else
                    startForegroundCompat(R.string.app_name, new Notification());
            }
            if(strAction!=null&&strAction.equalsIgnoreCase(ACTION_UPDATE))
            {//обновление положения
                UpdateIndicator();
            }
        }
    }

    private void HideIndicator()
    {
        if(m_cWM!=null)
        {
            try
            {
                m_cWM.removeView(m_cIVLogo);
            }
            catch(Exception ex)
            {

            }
        }
        m_cWM=null;
    }

    private void resize(View view, float scaleX, float scaleY) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) (view.getWidth() * scaleX);
        layoutParams.height = (int) (view.getHeight() * scaleY);
        view.setLayoutParams(layoutParams);
    }

    private void UpdateIndicator()
    {
        try {
            SharedPreferences cPrefs = getSharedPreferences("Common", Context.MODE_PRIVATE);
            boolean bFullScreen = cPrefs.getBoolean("Show on status bar", true);
            Bitmap cBmp = ImageHelper.getImage(this);
            int nWidth = 0;
            int nHeight = 0;
            float fScale = cPrefs.getInt("Size", 15) / 15.0f;
            if (cBmp != null) {
                nWidth = (int) (fScale * cBmp.getWidth());
                nHeight = (int) (fScale * cBmp.getHeight());
            }
            m_cIVLogo.setImageBitmap(cBmp);
            m_cIVLogo.setAlpha(cPrefs.getInt("Transparency", 255) / 255.0f);
            m_cIVLogo.setRotation(cPrefs.getInt("Rotation", 0));
            int nHorzShift = cPrefs.getInt("Horz shift", 0);
            int nVertShift = cPrefs.getInt("Vert shift", 0);

            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) m_cIVLogo.getLayoutParams();

            lp.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            if(bFullScreen)
                lp.flags|=WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR|
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;

            lp.horizontalMargin=0.001f*nHorzShift;
            lp.verticalMargin=0.001f*nVertShift;
            switch (cPrefs.getInt("Gravity",0))
            {
                case 0:
                    lp.gravity= Gravity.CENTER;
                    break;
                case 1://left-top
                    lp.gravity=Gravity.LEFT|Gravity.TOP;
                    break;
                case 2://left-center
                    lp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
                    break;
                case 3://left-bottom
                    lp.gravity=Gravity.LEFT|Gravity.BOTTOM;
                    break;
                case 4://bottom-center
                    lp.gravity=Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
                    break;
                case 5://top-center
                    lp.gravity=Gravity.CENTER_HORIZONTAL|Gravity.TOP;
                    break;
                case 6://right-top
                    lp.gravity=Gravity.RIGHT|Gravity.TOP;
                    break;
                case 7://right-center
                    lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
                    break;
                case 8://right-bottom
                    lp.gravity=Gravity.RIGHT|Gravity.BOTTOM;
                    break;
            }

            lp.horizontalMargin = 0.0025f * nHorzShift;
            lp.verticalMargin = 0.0025f * nVertShift;
            lp.width=nWidth;
            lp.height=nHeight;
            m_cWM.updateViewLayout(m_cIVLogo, lp);
            //resize(m_cIVLogo, fScale, fScale);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ShowIndicator()
    {
        SharedPreferences cPrefs=getSharedPreferences ("Common", Context.MODE_PRIVATE);
        boolean bFullScreen=cPrefs.getBoolean("Show on status bar",true);
        LayoutInflater cInflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_cIVLogo=(ImageView)cInflater.inflate(R.layout.logo_view,null);
        Bitmap cBmp=ImageHelper.getImage(this);
        int nWidth=0;
        int nHeight=0;
        float fScale=cPrefs.getInt("Size", 15)/15.0f;
        if(cBmp!=null)
        {
            nWidth=(int)(fScale*cBmp.getWidth());
            nHeight=(int)(fScale*cBmp.getHeight());
        }
        m_cIVLogo.setImageBitmap(cBmp);
        m_cIVLogo.setAlpha(cPrefs.getInt("Transparency",255)/255.0f);
        m_cIVLogo.setRotation(cPrefs.getInt("Rotation",0));
        int nHorzShift=cPrefs.getInt("Horz shift",0);
        int nVertShift=cPrefs.getInt("Vert shift",0);

        int nFlags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        if(bFullScreen)
            nFlags|=WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR|
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN;
        m_cWM=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(nWidth,nHeight,
                (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O)?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                nFlags, PixelFormat.TRANSLUCENT);
        lp.type|=WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;

        lp.horizontalMargin=0.001f*nHorzShift;
        lp.verticalMargin=0.001f*nVertShift;
        switch (cPrefs.getInt("Gravity",0))
        {
            case 0:
                lp.gravity= Gravity.CENTER;
                break;
            case 1://left-top
                lp.gravity=Gravity.LEFT|Gravity.TOP;
                break;
            case 2://left-center
                lp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
                break;
            case 3://left-bottom
                lp.gravity=Gravity.LEFT|Gravity.BOTTOM;
                break;
            case 4://bottom-center
                lp.gravity=Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
                break;
            case 5://top-center
                lp.gravity=Gravity.CENTER_HORIZONTAL|Gravity.TOP;
                break;
            case 6://right-top
                lp.gravity=Gravity.RIGHT|Gravity.TOP;
                break;
            case 7://right-center
                lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
                break;
            case 8://right-bottom
                lp.gravity=Gravity.RIGHT|Gravity.BOTTOM;
                break;
        }
        m_cWM.addView(m_cIVLogo, lp);
        resize(m_cIVLogo,fScale,fScale);
    }
}
