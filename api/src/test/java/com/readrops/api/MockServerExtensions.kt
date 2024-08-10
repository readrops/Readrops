package com.readrops.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import java.io.InputStream
import java.net.HttpURLConnection

fun MockWebServer.enqueueOK() {
    enqueue(MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
    )
}

fun MockWebServer.enqueueStream(stream: InputStream) {
    enqueue(MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(Buffer().readFrom(stream)))
}

fun MockResponse.Companion.okResponseWithBody(stream: InputStream): MockResponse {
    return MockResponse()
        .setResponseCode(HttpURLConnection.HTTP_OK)
        .setBody(Buffer().readFrom(stream))
}