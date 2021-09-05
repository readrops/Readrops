package com.readrops.api.services.nextcloudnews.adapters

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.TestUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class NextNewsUserAdapterTest {

    private val adapter = NextNewsUserAdapter()

    @Test
    fun validXmlTest() {
        val stream = TestUtils.loadResource("services/nextcloudnews/user.xml")

        assertEquals(adapter.fromXml(stream.konsumeXml()), "Shinokuni")
    }
}