package com.finnflare.dct.honeywell

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import com.finnflare.dct.honeywell.databinding.ActivityMainBinding
import com.finnflare.dct.honeywell.scanner.BaseScannerActivity
import com.finnflare.dct.honeywell.settings.SettingsActivity
import com.finnflare.dct.honeywell.ui.dialog.AppInfoDialog
import com.google.android.material.navigation.NavigationView

@SuppressLint("SetJavaScriptEnabled")
class MainActivity : BaseScannerActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setNavigationItemSelectedListener(this)

        WebView.setWebContentsDebuggingEnabled(true)

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.visibility = View.INVISIBLE

        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.allowFileAccess = true
        binding.webview.settings.mediaPlaybackRequiresUserGesture = false
        binding.webview.webViewClient = BaseWebViewClient(this)
        binding.webview.webChromeClient = BaseWebChromeClient(this)

        PreferenceManager.getDefaultSharedPreferences(this).getString("url", "")?.let {
            binding.webview.loadUrl(it)
        }
    }

    override fun onResume() {
        super.onResume()

        PreferenceManager.getDefaultSharedPreferences(this).getString("url", "")?.let {
            if (binding.webview.url != it)
                binding.webview.loadUrl(it)
        }
    }

    override fun onScanResult(data: String, code: String) {
        super.onScanResult(data, code)

        val result = when (code) {
            EAN13 -> data
            CODE128 -> data
            DATAMATRIX -> {
                val startIndex: Int = data.indexOf("01") + 2
                "01" + data.substring(startIndex, startIndex + 14) +
                        "21" + data.substring(startIndex + 16, startIndex + 29)
            }
            else -> return
        }

        runOnUiThread {
            binding.webview.evaluateJavascript(
                "(function() { " +
                        "const event = new CustomEvent('scan', " +
                        "{ detail: { data: \"$result\", fmt: \"" +
                        when (code) {
                            EAN13 -> "ean13"
                            CODE128 -> "code128"
                            else -> "dm"
                        } + "\"}});" +
                        "window.dispatchEvent(event); " +
                        "})();"
            ) { }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> {
                binding.drawerLayout.closeDrawers()
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_about -> {
                binding.drawerLayout.closeDrawers()
                val fm = supportFragmentManager
                val dialogFragment = AppInfoDialog()
                dialogFragment.show(fm, "dialog_fragment_info")
            }
        }

        binding.drawerLayout.closeDrawers()

        return true
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) ->
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            binding.webview.canGoBack() ->
                binding.webview.goBack()
            else ->
                super.onBackPressed()
        }
    }
}