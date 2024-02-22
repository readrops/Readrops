package com.readrops.api.utils

import android.nfc.FormatException
import com.readrops.api.localfeed.LocalRSSHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class ParsingResult(
    val url: String,
    val label: String?,
)

object HtmlParser {

    suspend fun getFaviconLink(url: String, client: OkHttpClient): String? {
        val document = getHTMLHeadFromUrl(url, client)
        val elements = document.select("link")

        for (element in elements) {
            if (element.attributes()["rel"].lowercase().contains("icon")) {
                return element.absUrl("href")
            }
        }

        return null
    }

    suspend fun getFeedLink(url: String, client: OkHttpClient): List<ParsingResult> {
        val results = mutableListOf<ParsingResult>()

        val document = getHTMLHeadFromUrl(url, client)
        val elements = document.select("link")

        for (element in elements) {
            val type = element.attributes()["type"]

            if (LocalRSSHelper.isRSSType(type)) {
                results += ParsingResult(
                    url = element.absUrl("href"),
                    label = element.attributes()["title"]
                )
            }
        }

        return results
    }

    private fun getHTMLHeadFromUrl(url: String, client: OkHttpClient): Document {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (response.header(ApiUtils.CONTENT_TYPE_HEADER)!!.contains(ApiUtils.HTML_CONTENT_TYPE)
            ) {
                val body = response.body!!.source()

                val stringBuilder = StringBuilder()
                var collectionStarted = false

                while (!body.exhausted()) {
                    val currentLine = body.readUtf8LineStrict()

                    when {
                        currentLine.contains("<head>") -> {
                            stringBuilder.append(currentLine)
                            collectionStarted = true
                        }
                        currentLine.contains("</head>") -> {
                            stringBuilder.append(currentLine)
                            break
                        }
                        collectionStarted -> {
                            stringBuilder.append(currentLine)
                        }
                    }
                }

                if (!stringBuilder.contains("<head>") || !stringBuilder.contains("</head>"))
                    throw FormatException("Failed to get HTML head")

                body.close()
                return Jsoup.parse(stringBuilder.toString(), url)
            } else {
                throw FormatException("The response is not a html file")
            }
        }
    }

}