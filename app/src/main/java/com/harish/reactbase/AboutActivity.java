package com.harish.reactbase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {
    WebView about;
    WebSettings webSettings;
    String HTMLContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("About");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        about=findViewById(R.id.webView);
        webSettings=about.getSettings();
        webSettings.setJavaScriptEnabled(true);
        about.loadUrl("file:///android_asset/about.html");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
