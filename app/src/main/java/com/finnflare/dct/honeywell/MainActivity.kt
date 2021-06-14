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

        //TODO add scan res type check

        val result = when (code) {
            "d" -> {
                (data + controlNumberGTIN(data))
            }
            "w" -> {
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

    private fun controlNumberGTIN(str: String): String {
        var ch = 0
        var nch = 0

        str.forEachIndexed { index, c ->
            when {
                index % 2 == 0 -> ch += Character.digit(c, 10)
                else -> nch += Character.digit(c, 10)
            }
        }

        return ((10 - (ch + 3 * nch) % 10) % 10).toString()
    }
}