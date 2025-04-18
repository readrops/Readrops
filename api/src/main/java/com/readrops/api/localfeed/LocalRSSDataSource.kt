package com.readrops.api.localfeed

import androidx.annotation.WorkerThread
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.json.JSONFeedAdapter
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.AuthInterceptor
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.IOException
import java.net.HttpURLConnection

class LocalRSSDataSource(private val httpClient: OkHttpClient) : KoinComponent {

    /**
     * Query RSS url
     * @param url url to query
     * @param headers request headers
     * @return a Feed object with its items
     */
    @Throws(ParseException::class, UnknownFormatException::class, HttpException::class, IOException::class)
    @WorkerThread
    fun queryRSSResource(url: String, headers: Headers?): Pair<Feed, List<Item>>? {
        get<AuthInterceptor>().credentials = null
        val response = queryUrl(url, headers)

        return when {
            response.isSuccessful -> {
                val pair = parseResponse(response, url)

                response.body?.close()
                pair
            }
            response.code == HttpURLConnection.HTTP_NOT_MODIFIED -> null
            else -> throw HttpException(response)
        }
    }

    /**
     * Checks if the provided url is a RSS resource
     * @param url url to check
     * @return true if [url] is a RSS resource, false otherwise
     */
    @WorkerThread
    fun isUrlRSSResource(url: String): Boolean {
        val response = queryUrl(url, null)

        return if (response.isSuccessful) {
            val header = response.header(ApiUtils.CONTENT_TYPE_HEADER)
                    ?: return false

            val contentType = ApiUtils.parseContentType(header)
                    ?: return false

            var type = LocalRSSHelper.getRSSType(contentType)

            if (type == LocalRSSHelper.RSSType.UNKNOWN) {
                val konsumer = response.body!!.byteStream().konsumeXml().apply {
                    try {
                        val rootKonsumer = nextElement(LocalRSSHelper.RSS_ROOT_NAMES)
                        rootKonsumer?.let { type = LocalRSSHelper.guessRSSType(rootKonsumer) }
                    } catch (e: Exception) {
                        close()
                        return false
                    }

                }

                konsumer.close()
            }

            type != LocalRSSHelper.RSSType.UNKNOWN
        } else false
    }

    @Throws(IOException::class)
    private fun queryUrl(url: String, headers: Headers?): Response {
        val requestBuilder = Request.Builder().url(url)
        headers?.let { requestBuilder.headers(it) }

        return httpClient.newCall(requestBuilder.build()).execute()
    }

    private fun parseResponse(response: Response, url: String): Pair<Feed, List<Item>> {
        val header = response.header(ApiUtils.CONTENT_TYPE_HEADER)
                ?: throw UnknownFormatException("Unable to get $url content-type")

        val contentType = ApiUtils.parseContentType(header)
                ?: throw ParseException("Unable to parse $url content-type")

        var type = LocalRSSHelper.getRSSType(contentType)

        var konsumer: Konsumer? = null
        if (type != LocalRSSHelper.RSSType.JSONFEED)
            konsumer = response.body!!.byteStream().konsumeXml()

        var rootKonsumer: Konsumer? = null
        // if we can't guess type based on content-type header, we use the content
        if (type == LocalRSSHelper.RSSType.UNKNOWN) {
            try {
                rootKonsumer = konsumer?.nextElement(LocalRSSHelper.RSS_ROOT_NAMES)

                if (rootKonsumer != null) {
                    type = LocalRSSHelper.guessRSSType(rootKonsumer)
                }
            } catch (e: Exception) {
                throw UnknownFormatException(e.message)
            }

        }

        // if we can't guess type even with the content, we are unable to go further
        if (type == LocalRSSHelper.RSSType.UNKNOWN) throw UnknownFormatException("Unable to guess $url RSS type")

        val pair = parseFeed(rootKonsumer ?: konsumer, type, response)

        rootKonsumer?.finish()
        konsumer?.close()

        return pair
    }

    private fun parseFeed(konsumer: Konsumer?, type: LocalRSSHelper.RSSType, response: Response): Pair<Feed, List<Item>> {
        val pair = if (type != LocalRSSHelper.RSSType.JSONFEED) {
            val adapter = XmlAdapter.xmlFeedAdapterFactory(type)

            adapter.fromXml(konsumer!!)
        } else {
            val pairType = Types.newParameterizedType(Pair::class.java, Feed::class.java,
                    Types.newParameterizedType(List::class.java, Item::class.java))

            val adapter = Moshi.Builder()
                    .add(pairType, JSONFeedAdapter())
                    .build()
                    .adapter<Pair<Feed, List<Item>>>(pairType)

            adapter.fromJson(Buffer().readFrom(response.body!!.byteStream()))!!
        }

        handleSpecialCases(pair.first, type, response)

        pair.first.etag = response.header(ApiUtils.ETAG_HEADER)
        pair.first.lastModified = response.header(ApiUtils.LAST_MODIFIED_HEADER)

        return pair
    }

    private fun handleSpecialCases(feed: Feed, type: LocalRSSHelper.RSSType, response: Response) =
            with(feed) {
                if (type == LocalRSSHelper.RSSType.RSS_2) {
                    // if an atom:link element was parsed, we still replace its value as it is unreliable,
                    // otherwise we just add the rss url
                    url = response.request.url.toString()
                } else if (type == LocalRSSHelper.RSSType.ATOM || type == LocalRSSHelper.RSSType.RSS_1) {
                    if (url == null) url = response.request.url.toString()
                    if (siteUrl == null) siteUrl = response.request.url.scheme + "://" + response.request.url.host
                }
            }
}