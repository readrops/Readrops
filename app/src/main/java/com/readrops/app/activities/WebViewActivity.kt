package com.readrops.app.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.readrops.app.R
import com.readrops.app.databinding.ActivityWebViewBinding
import com.readrops.app.utils.ReadropsKeys
import com.readrops.app.utils.ReadropsKeys.ACTION_BAR_COLOR

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.activityWebViewToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = ""

        val actionBarColor = intent.getIntExtra(ACTION_BAR_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(actionBarColor))
        setWebViewSettings()

        with(binding) {
            activityWebViewSwipe.setOnRefreshListener { binding.webView.reload() }
            activityWebViewProgress.progressTintList = ColorStateList.valueOf(actionBarColor)
            activityWebViewProgress.max = 100

            val url: String = intent.getStringExtra(ReadropsKeys.WEB_URL)!!
            webView.loadUrl(url)
        }

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

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                with(binding) {
                    activityWebViewSwipe.isRefreshing = false
                    activityWebViewProgress.progress = 0
                    activityWebViewProgress.visibility = View.VISIBLE
                }

                super.onPageStarted(view, url, favicon)
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                setTitle(title)
                supportActionBar?.subtitle = Uri.parse(view?.url).host

                super.onReceivedTitle(view, title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                with(binding) {
                    activityWebViewProgress.progress = newProgress
                    if (newProgress == 100) activityWebViewProgress.visibility = View.GONE
                }


                super.onProgressChanged(view, newProgress)
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
            R.id.web_view_refresh -> {
                binding.webView.reload()
            }
            R.id.web_view_share -> {
                shareLink()
            }
        }

        return super.onOptionsItemSelected(item!!)
    }

    private fun shareLink() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, binding.webView.url.toString())
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_url)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return true
    }
}
