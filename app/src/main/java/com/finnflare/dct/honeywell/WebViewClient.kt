package com.finnflare.dct.honeywell

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.Base64
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.IOException
import java.io.InputStream

open class BaseWebViewClient(private val context: Context) : WebViewClient() {
    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        view.loadUrl(request.url.toString())
        return true
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        view?.let { injectScriptFile(it, listOf(SANITIZER)) }
        view?.evaluateJavascript(SANITIZE_JS_CALL, null)
        view?.visibility = View.VISIBLE
    }

    private fun injectScriptFile(view: WebView, scriptFiles: List<String>) {
        var input: InputStream
        scriptFiles.forEach {
            try {
                input = context.assets.open(it)
                val buffer = ByteArray(input.available())
                input.read(buffer)
                input.close()

                val encoded: String = Base64.encodeToString(buffer, Base64.NO_WRAP)
                view.evaluateJavascript(
                    "(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(script)" +
                            "})()",
                    null
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val SANITIZER = "purify.min.js"
        const val SANITIZE_JS_CALL = "(function () {" +
                "document = DOMPurify.sanitize(document.outerHTML, {" +
                "   WHOLE_DOCUMENT: true, " +
                "   USE_PROFILES: {html: true}, " +
                "   RETURN_DOM: true" +
                "});})();"
    }
}