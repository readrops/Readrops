package com.readrops.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.readrops.api.apiModule
import com.readrops.api.utils.ApiUtils
import com.readrops.app.util.FeedColors
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import java.net.HttpURLConnection
import kotlin.test.assertTrue

class FeedColorsTest {

    private val mockServer = MockWebServer()

    @Before
    fun before() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        KoinTestRule.create {
            modules(apiModule, module {
                single { context }
            })
        }

        mockServer.start()
    }

    @After
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun getFeedColorTest() = runBlocking {
        val stream = TestUtils.loadResource("favicon.ico")

        mockServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "image/jpeg")
                .setBody(Buffer().readFrom(stream))
        )

        val url = mockServer.url("/rss").toString()
        val color = FeedColors.getFeedColor(url)

        assertTrue { color != 0 }
    }
}