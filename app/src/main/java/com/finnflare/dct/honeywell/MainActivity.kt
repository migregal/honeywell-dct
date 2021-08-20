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
            binding.webview.url?.let { url ->
                if (url.substringBefore("/#").removeSuffix("/") != it.removeSuffix("/"))
                    binding.webview.loadUrl(it)
            }
        }
    }

    override fun onScanResult(data: String, code: String) {
        super.onScanResult(data, code)

        val result = when (code) {
            EAN13 -> checkDigitTransmitEan13(data)
            CODE128 -> data
            GS1_128 -> data
            DATAMATRIX -> getDataMatrixData(data)
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
                            GS1_128 -> "gs1_128"
                            DATAMATRIX -> "dm"
                            else -> "unknown"
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

    private fun checkDigitTransmitEan13(str: String): String {
        if (str.length == 13)
            return str;

        var ch = 0
        var nch = 0

        str.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> ch += Character.digit(c, 10)
                else -> nch += Character.digit(c, 10)
            }
        }

        return str + ((10 - (ch + 3 * nch) % 10) % 10).toString()
    }

    private fun getDataMatrixData(str: String): String {
        val startIndex = str.indexOf("01") + 2

        with (str) {
            return "01" + substring(startIndex, startIndex + 14) +
                    "21" + substring(startIndex + 16, startIndex + 29)
        }
    }
}