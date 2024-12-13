package com.readrops.app.item.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannedString
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.text.HtmlCompat
import androidx.core.text.layoutDirection
import com.readrops.app.R
import com.readrops.app.util.Utils
import com.readrops.db.pojo.ItemWithFeed
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class ItemWebView(
    context: Context,
    onUrlClick: (String) -> Unit,
    onImageLongPress: (String) -> Unit,
    attrs: AttributeSet? = null,
) : WebView(context, attrs) {

    init {
        settings.javaScriptEnabled = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(false)
        isVerticalScrollBarEnabled = false

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { onUrlClick(it) }
                return true
            }
        }

        setOnLongClickListener {
            val type = hitTestResult.type
            if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                hitTestResult.extra?.let { onImageLongPress(it) }
            }

            false
        }
    }

    fun loadText(
        itemWithFeed: ItemWithFeed,
        accentColor: Color,
        backgroundColor: Color,
        onBackgroundColor: Color
    ) {
        val direction = if (Locale.getDefault().layoutDirection == LAYOUT_DIRECTION_LTR) {
            "ltr"
        } else {
            "rtl"
        }

        val string = context.getString(
            R.string.webview_html_template,
            Utils.getCssColor(accentColor.toArgb()),
            Utils.getCssColor(onBackgroundColor.toArgb()),
            Utils.getCssColor(backgroundColor.toArgb()),
            direction,
            formatText(itemWithFeed)
        )

        loadDataWithBaseURL(
            "file:///android_asset/",
            string,
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )
    }

    private fun formatText(itemWithFeed: ItemWithFeed): String {
        val text = itemWithFeed.item.text ?: return ""
        val unescapedText = Parser.unescapeEntities(text, false)
        val document = if (itemWithFeed.websiteUrl != null) {
            Jsoup.parse(unescapedText, itemWithFeed.websiteUrl!!)
        } else {
            Jsoup.parse(unescapedText)
        }
        // If body has no tags or all tags are unknown (and therefore likely not HTML tags at all),
        // treat the whole thing as plain text and convert it to HTML turning newlines into <br>/<p> tags
        val body = document.body()
        val isPlainText = body.stream().skip(1).allMatch { !it.tag().isKnownTag }
        return if (isPlainText) {
            HtmlCompat.toHtml(SpannedString(unescapedText), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        } else {
            body.select("div,span").forEach { it.clearAttributes() }
            body.html()
        }
    }
}