package com.readrops.api.services

import com.readrops.api.services.greader.GReaderCredentials
import com.readrops.api.services.nextcloudnews.NextcloudNewsCredentials
import org.junit.Test
import kotlin.test.assertEquals

class CredentialsTest {

    @Test
    fun credentialsTest() {
        val credentials = GReaderCredentials("token", "https://freshrss.org")

        assertEquals(credentials.authorization!!, "GoogleLogin auth=token")
        assertEquals(credentials.url, "https://freshrss.org")
    }

    @Test
    fun nextcloudNewsCredentialsTest() {
        val credentials = NextcloudNewsCredentials("login", "password", "https://freshrss.org")

        assertEquals(credentials.authorization!!, "Basic bG9naW46cGFzc3dvcmQ=")
        assertEquals(credentials.url, "https://freshrss.org")
    }
}