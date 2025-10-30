package com.example.pos

import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*
import kotlin.concurrent.schedule

// for geo location
import android.webkit.WebChromeClient
import android.webkit.GeolocationPermissions
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private var geolocationCallback: GeolocationPermissions.Callback? = null
    private var geolocationOrigin: String? = null

    // init webview
    lateinit var webView: WebView
    // init swipe
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //konten
        setContentView(R.layout.activity_main)
        //portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //swipe swipe
        swipeRefreshLayout=findViewById(R.id.swiperefresh)

        //hilangkan header dan status bar
        supportActionBar?.title = getString(R.string.app_title)
        supportActionBar?.hide()
        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //ubah warna status bar
        val window: Window = this@MainActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(
            this@MainActivity,
            R.color.status_bar_brand_color,
        )

        webView=findViewById(R.id.WV)
        webView.webViewClient= WebViewClient()

        // geo location purpose
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                // 1. Check if we already have the permission
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                    // Permission is granted, invoke callback immediately
                    callback?.invoke(origin, true, false)
                } else {
                    // 2. Permission is NOT granted, request it from the user

                    // Store the callback details to invoke them later
                    this@MainActivity.geolocationCallback = callback
                    this@MainActivity.geolocationOrigin = origin

                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }

        //web setting
        val webSettings=webView.settings
        //aktifkan js
        webSettings.javaScriptEnabled=true
        //bootstrap
        webSettings.domStorageEnabled=true

        val webUrl = getString(R.string.base_web_url)
//        webView.loadUrl("http://192.168.5.200:5080/LiveApp/play.html?id=709987822697983144783216&autoplay=true&mute=false")
//        webView.loadUrl("http://125.163.162.167:88/distributor-remote/")
//        webView.loadUrl("http://192.168.5.179/distributor-remote/")
//        webView.loadUrl("https://remote.dist.transhome.id/")
//        webView.loadUrl("https://dist.bahanbangunanhemat.com/")
        webView.loadUrl(webUrl)

        //swipe listener
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            Timer().schedule(1500){
                swipeRefreshLayout.isRefreshing=false
            }
        }
    }

    // --- Handle the Runtime Permission Result --- for geo location purpose
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && geolocationCallback != null) {

            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

            // Invoke the stored callback with the result
            geolocationCallback?.invoke(geolocationOrigin, granted, false)

            // Clear stored values
            geolocationCallback = null
            geolocationOrigin = null
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