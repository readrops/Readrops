package com.readrops.api.services

import com.readrops.api.services.freshrss.FreshRSSCredentials
import com.readrops.api.services.nextcloudnews.NextNewsCredentials
import org.junit.Test
import kotlin.test.assertEquals

class CredentialsTest {

    @Test
    fun credentialsTest() {
        val credentials = FreshRSSCredentials("token", "https://freshrss.org")

        assertEquals(credentials.authorization!!, "GoogleLogin auth=token")
        assertEquals(credentials.url, "https://freshrss.org")
    }

    @Test
    fun nextcloudNewsCredentialsTest() {
        val credentials = NextNewsCredentials("login", "password", "https://freshrss.org")

        assertEquals(credentials.authorization!!, "Basic bG9naW46cGFzc3dvcmQ=")
        assertEquals(credentials.url, "https://freshrss.org")
    }
}