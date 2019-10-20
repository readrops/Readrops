package com.readrops.app.activities

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.readrops.app.R
import com.readrops.app.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = ""
        setWebViewSettings()

        val url: String = intent.getStringExtra(WEB_URL)
        binding.webView.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setWebViewSettings() {
        val settings: WebSettings = binding.webView.settings

        settings.javaScriptEnabled = true
        settings.setSupportZoom(true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                binding.webView.loadUrl(request?.url.toString())
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                title = view?.title
                supportActionBar?.subtitle = Uri.parse(view?.url).host

                super.onPageFinished(view, url)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (binding.webView.canGoBack())
                    binding.webView.goBack()
                else
                    finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val WEB_URL = "webUrl"
    }
}
