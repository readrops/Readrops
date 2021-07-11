package com.readrops.api.utils

import com.readrops.api.services.freshrss.FreshRSSCredentials
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val interceptor = AuthInterceptor()
    private val mockServer = MockWebServer()
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun before() {
        okHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        mockServer.start(8080)
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @Test
    fun credentialsUrlTest() {
        mockServer.enqueue(MockResponse())
        interceptor.credentials = FreshRSSCredentials("token", "http://localhost:8080/rss")

        okHttpClient.newCall(Request.Builder().url(mockServer.url("/url")).build()).execute()
        val request = mockServer.takeRequest()

        assertEquals(request.headers["Authorization"], "GoogleLogin auth=token")
    }

    @Test
    fun nullCredentialsTest() {
        mockServer.enqueue(MockResponse())
        interceptor.credentials = null

        okHttpClient.newCall(Request.Builder().url(mockServer.url("/url")).build()).execute()
        val request = mockServer.takeRequest()

        assertEquals(request.requestUrl.toString(), "http://localhost:8080/url")
        assertNull(request.headers["Authorization"])
    }
}