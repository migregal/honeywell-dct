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
    }

    override fun onScanResult(data: String, code: String) {
        super.onScanResult(data, code)

        //TODO add scan res type check

        runOnUiThread {
            binding.webview.evaluateJavascript(
                "onScanEvent(\"$data\");"
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