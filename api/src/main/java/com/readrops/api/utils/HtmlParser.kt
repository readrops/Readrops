package com.readrops.api.utils

import android.nfc.FormatException
import com.readrops.api.localfeed.LocalRSSHelper
import com.readrops.api.utils.ApiUtils.isHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

data class ParsingResult(
    val url: String,
    val label: String?,
)

object HtmlParser {

    @Throws(FormatException::class)
    suspend fun getFeedLink(url: String, client: OkHttpClient): List<ParsingResult> {
        val document = getHTMLHeadFromUrl(url, client)

        return document.select("link")
            .filter { element ->
                val type = element.attributes()["type"]
                LocalRSSHelper.isRSSType(type)
            }.map {
                ParsingResult(
                    url = it.absUrl("href"),
                    label = it.attributes()["title"]
                )
            }
    }

    fun getFaviconLink(document: Document): String? {
        val links = document.select("link")
            .filter { element -> element.attributes()["rel"].contains("icon") }
            .sortedWith(compareByDescending<Element> {
                it.attributes()["rel"] == "apple-touch-icon"
            }.thenByDescending { element ->
                val sizes = element.attr("sizes")

                if (sizes.isNotEmpty()) {
                    try {
                        sizes.filter { it.isDigit() }
                            .toInt()
                    } catch (e: Exception) {
                        0
                    }
                } else {
                    0
                }
            })

        return links.firstOrNull()
            ?.absUrl("href")
    }

    fun getFeedImage(document: Document): String? {
        return document.select("meta")
            .firstOrNull { element ->
                val property = element.attr("property")
                listOf("og:image", "twitter:image").any { it == property }
            }
            ?.absUrl("content")
    }

    fun getFeedDescription(document: Document): String? {
        return document.select("meta")
            .firstOrNull { element ->
                val property = element.attr("property")
                listOf("og:title", "twitter:title").any { it == property }
            }
            ?.attr("content")
    }

    suspend fun getHTMLHeadFromUrl(url: String, client: OkHttpClient): Document =
        withContext(Dispatchers.IO) {
            client.newCall(
                Request.Builder()
                    .url(url)
                    .build()
            ).execute()
                .use { response ->
                    val body = response.body
                    if (body?.contentType()?.isHtml == true) {
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

                        if (!stringBuilder.contains("<head>") || !stringBuilder.contains("</head>")) {
                            throw FormatException("Failed to get HTML head from $url")
                        }

                        Jsoup.parse(stringBuilder.toString(), url)
                    } else {
                        throw FormatException("Response from $url is not a html file")
                    }
                }
        }
}