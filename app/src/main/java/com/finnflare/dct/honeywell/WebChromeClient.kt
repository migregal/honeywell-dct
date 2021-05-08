package com.finnflare.dct.honeywell

import android.app.AlertDialog
import android.content.Context
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView


class BaseWebChromeClient(private val context: Context): WebChromeClient() {
    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(context)
            .setTitle("App Titler")
            .setMessage(message)
            .setPositiveButton(
                R.string.ok,
                { dialog, which -> result.confirm() })
            .setNegativeButton(
                R.string.cancel,
                { dialog, which -> result.cancel() })
            .create()
            .show()
        return true
    }
}