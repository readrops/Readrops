package com.readrops.app.util

import com.readrops.api.utils.ApiUtils
import com.readrops.app.testutil.ReadropsTestRule
import com.readrops.app.testutil.TestUtils
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import java.net.HttpURLConnection
import kotlin.test.assertTrue

class FeedColorsTest : KoinTest {

    private val mockServer = MockWebServer()

    @get:Rule
    val testRule = ReadropsTestRule()

    @Before
    fun before() {
        mockServer.start()
    }

    @After
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun getFeedColorTest() = runTest {
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