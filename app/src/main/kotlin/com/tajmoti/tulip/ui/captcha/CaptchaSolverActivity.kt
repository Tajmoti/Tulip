package com.tajmoti.tulip.ui.captcha

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityCaptchaSolverBinding
import com.tajmoti.tulip.ui.BaseActivity

class CaptchaSolverActivity : BaseActivity<ActivityCaptchaSolverBinding>(
    R.layout.activity_captcha_solver
) {
    private lateinit var captchaUrl: Uri
    private lateinit var destinationUrl: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        captchaUrl = intent.getParcelableExtra(EXTRA_CAPTCHA_URL)!!
        destinationUrl = intent.getParcelableExtra(EXTRA_DESTINATION_URL)!!
        setupWebView(binding.webViewCaptchaKiller, captchaUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(wb: WebView, url: Uri) {
        wb.settings.javaScriptEnabled = true
        wb.settings.javaScriptCanOpenWindowsAutomatically = true
        wb.webViewClient = IdiotproofWebClient()
        wb.loadUrl(url.toString())
    }

    private fun onUrlLoaded(url: String) {
        if (url == destinationUrl.toString()) {
            setResult(RESULT_OK)
            finish()
        }
    }

    inner class IdiotproofWebClient : WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            onUrlLoaded(url)
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            onUrlLoaded(request.url.toString())
            return request.url != captchaUrl
        }
    }

    companion object {
        private const val EXTRA_CAPTCHA_URL = "captchaUrl"
        private const val EXTRA_DESTINATION_URL = "destinationUri"

        fun newInstance(context: Context, captchaUri: Uri, destinationUri: Uri): Intent {
            val intent = Intent(context, CaptchaSolverActivity::class.java)
            intent.putExtra(EXTRA_CAPTCHA_URL, captchaUri)
            intent.putExtra(EXTRA_DESTINATION_URL, destinationUri)
            return intent
        }
    }
}