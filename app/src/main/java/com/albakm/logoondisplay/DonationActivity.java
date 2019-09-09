package com.albakm.logoondisplay;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

public class DonationActivity extends AppCompatActivity {

    private WebView m_cWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
        m_cWebView=(WebView)findViewById(R.id.webView1);
        // включаем поддержку JavaScript
        m_cWebView.getSettings().setJavaScriptEnabled(true);
        // указываем страницу загрузки
        m_cWebView.loadUrl("file:///android_asset/paypal_donate.html");
    }

    @Override
    public void onBackPressed() {
        if(m_cWebView.canGoBack()) {
            m_cWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
