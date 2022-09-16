package com.example.pos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : AppCompatActivity() {
    // init webview
    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hilangkan header dan status bar
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        webView=findViewById(R.id.WV)
        webView.webViewClient= WebViewClient()
        webView.loadUrl("https://github.com/rahmadipi")

        //web setting
        val webSettings=webView.settings

        //aktifkan js
        webSettings.javaScriptEnabled=true

        //bootstrap
        webSettings.domStorageEnabled=true
    }

    override fun onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }
    }
}