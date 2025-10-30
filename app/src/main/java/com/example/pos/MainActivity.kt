package com.example.pos

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*
import kotlin.concurrent.schedule

// --- New Imports for Geolocation Bridge ---
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import android.location.LocationManager
import androidx.core.content.getSystemService

import android.annotation.SuppressLint
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity() {
    // init webview
    lateinit var webView: WebView
    // init swipe
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    // --- New Geolocation Bridge Variables ---
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // This handles the result of the location permission request
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, now attempt to get the location
                getLocation()
            } else {
                // Permission denied, inform the web page
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
                sendLocationToWeb("Permission Denied", "Permission Denied")
            }
        }
    // ----------------------------------------

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


        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        webView=findViewById(R.id.WV)
        webView.webViewClient= WebViewClient()

        //web setting
        val webSettings=webView.settings
        //aktifkan js
        webSettings.javaScriptEnabled=true
        //bootstrap
        webSettings.domStorageEnabled=true


        // geo location purpose
        webView.addJavascriptInterface(LocationBridge(this), "Android")
        webView.webChromeClient = WebChromeClient()


        val webUrl = getString(R.string.base_web_url)
        webView.loadUrl(webUrl)

        //swipe listener
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
            Timer().schedule(1500){
                swipeRefreshLayout.isRefreshing=false
            }
        }
    }

    @SuppressLint("MissingPermission") // Suppress warning as permission is checked immediately below
    fun getLocation() {
        // 1. Permission Check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // 2. Location Service Check (Ensures GPS/Network is enabled on the device)
            val locationManager = ContextCompat.getSystemService(this, LocationManager::class.java)
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            if (!isGpsEnabled && !isNetworkEnabled) {
                // Location is disabled in phone settings, tell the user
                Toast.makeText(this, "Please enable Location Services (GPS) on your device settings to get a fix.", Toast.LENGTH_LONG).show()
                sendLocationToWeb("Error", "Location Services Disabled")
                return
            }

            // 3. Proceed with Location Request
            Toast.makeText(this, "Acquiring current GPS fix...", Toast.LENGTH_SHORT).show()

            // Permission is granted, request a fresh location fix instead of last known
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, // Request high accuracy fix
                null // Cancellation token is null for simple request
            )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Success: Send coordinates to the web page
                        sendLocationToWeb(location.latitude.toString(), location.longitude.toString())
                    } else {
                        // Location is null (Failed to get fix)
                        Toast.makeText(this, "Failed to get fresh GPS fix.", Toast.LENGTH_LONG).show()
                        sendLocationToWeb("Error", "Failed to acquire GPS fix.")
                    }
                }
                .addOnFailureListener { e ->
                    // Failure: Send error message to the web page
                    Toast.makeText(this, "GPS Request Error: ${e.message}", Toast.LENGTH_LONG).show()
                    sendLocationToWeb("Error", "GPS Request Failed: ${e.message}")
                }
        } else {
            // Permission not yet granted, request it from the user
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // This function executes JavaScript to send location data back to the web page
    private fun sendLocationToWeb(latitude: String, longitude: String) {
        // Construct the JavaScript call: receiveLocation(latitude, longitude)
        val jsFunction = "javascript:receiveLocation('$latitude', '$longitude')"

        // Execute the function on the WebView's main thread
        webView.post {
            // Replaced evaluateJavascript with loadUrl for API 16 compatibility
            webView.loadUrl(jsFunction)
        }
    }

    // --- JavaScript Interface (The Bridge Class) ---

    // This class exposes methods to the JavaScript environment
    class LocationBridge(private val activity: MainActivity) {

        // This method is callable from JavaScript using: Android.getLocation();
        @JavascriptInterface
        fun getLocation() {
            // Must execute the location request back on the main activity thread
            activity.runOnUiThread {
                activity.getLocation()
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