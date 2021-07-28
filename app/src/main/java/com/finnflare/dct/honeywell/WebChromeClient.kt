package com.finnflare.dct.honeywell

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient


class BaseWebChromeClient(private val context: Context) : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Log.d(
            "MyApplication", consoleMessage.message() + " -- From line "
                    + consoleMessage.lineNumber() + " of "
                    + consoleMessage.sourceId()
        )
        return super.onConsoleMessage(consoleMessage)
    }
}