package com.example.pos

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    // init webview
    lateinit var webView: WebView
    // init swipe
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //konten
        setContentView(R.layout.activity_main)
        //portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //swipe swipe
        swipeRefreshLayout=findViewById(R.id.swiperefresh)

        //hilangkan header dan status bar
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        webView=findViewById(R.id.WV)
        webView.webViewClient= WebViewClient()
        webView.loadUrl("https://www.google.com/")

        //web setting
        val webSettings=webView.settings

        //aktifkan js
        webSettings.javaScriptEnabled=true

        //bootstrap
        webSettings.domStorageEnabled=true

        //swipe listener
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            Timer().schedule(1500){
                swipeRefreshLayout.isRefreshing=false
            }
        }
    }

    override fun onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }
    }
}