package com.finnflare.dct.honeywell

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.finnflare.dct.honeywell.databinding.ActivityMainBinding
import com.finnflare.dct.honeywell.scanner.BaseScannerActivity

@SuppressLint("SetJavaScriptEnabled")
class MainActivity : BaseScannerActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.webview.settings.javaScriptEnabled = true

        binding.webview.visibility = View.INVISIBLE
        binding.webview.loadUrl("some url placeholder")

        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.allowFileAccess = true
        binding.webview.webViewClient = BaseWebViewClient(this)
        binding.webview.webChromeClient = BaseWebChromeClient(this)
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
                        "const event = new CustomEvent('scan', { detail: \"$result\" });" +
                        "window.dispatchEvent(event); " +
                        "})();"
            ) { }
        }
    }

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        } else {
            super.onBackPressed()
        }
    }
}