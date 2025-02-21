package com.readrops.api.utils

import com.readrops.api.utils.exceptions.HttpException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection

class ErrorInterceptorTest {

    private val interceptor = ErrorInterceptor()
    private val server = MockWebServer()
    private lateinit var client: OkHttpClient

    @Before
    fun before() {
        client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        server.start(8080)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test(expected = HttpException::class)
    fun interceptorErrorTest() {
        server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND))

        client.newCall(Request.Builder().url(server.url("/url")).build()).execute()
    }

    @Test
    fun interceptorSuccessTest() {
        server.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_MODIFIED))

        client.newCall(Request.Builder().url(server.url("/url")).build()).execute()
    }
}