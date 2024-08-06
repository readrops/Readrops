package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.readrops.api.apiModule
import com.readrops.api.services.fever.FeverCredentials
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.AuthInterceptor
import com.readrops.api.utils.exceptions.LoginFailedException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class FeverDataSourceTest : KoinTest {

    private lateinit var dataSource: FeverDataSource
    private val mockServer = MockWebServer()

    @Before
    fun before() {
        mockServer.start(8080)
        val url = mockServer.url("")
        dataSource = get(parameters = {
            parametersOf(FeverCredentials(null, null, url.toString()))
        })
    }

    @After
    fun tearDown() {
        mockServer.close()
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(apiModule, module {
            single {
                OkHttpClient.Builder()
                        .callTimeout(1, TimeUnit.MINUTES)
                        .readTimeout(1, TimeUnit.HOURS)
                        .addInterceptor(get<AuthInterceptor>())
                        .build()
            }

        })
    }

    @Test
    fun loginSuccessfulTest() {
        val stream = TestUtils.loadResource("services/fever/successful_auth.json")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/json")
                .setBody(Buffer().readFrom(stream)))

        runBlocking {
            dataSource.login("","")
        }
    }

    @Test
    fun loginFailedTest() {
        val stream = TestUtils.loadResource("services/fever/failed_auth.json")

        mockServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader(ApiUtils.CONTENT_TYPE_HEADER, "application/json")
                .setBody(Buffer().readFrom(stream)))

        assertThrows(LoginFailedException::class.java) {
            runBlocking {
                dataSource.login("","")
            }
        }
    }
}