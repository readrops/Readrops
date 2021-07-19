package com.readrops.api.services.freshrss.adapters

import com.readrops.api.TestUtils
import com.squareup.moshi.Moshi
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class FreshRSSUserInfoAdapterTest {

    private val adapter = Moshi.Builder()
            .add(FreshRSSUserInfoAdapter())
            .build()
            .adapter(FreshRSSUserInfo::class.java)

    @Test
    fun userInfoTest() {
        val stream = TestUtils.loadResource("services/freshrss/adapters/user_info.json")

        val userInfo = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(userInfo.userName, "test")
    }
}