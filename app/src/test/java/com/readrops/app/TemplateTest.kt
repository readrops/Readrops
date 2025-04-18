package com.readrops.app

import com.readrops.app.util.FrenchTypography
import com.readrops.app.util.RemoveAuthorFilter
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TemplateTest {

    @Test
    fun testRemoveAuthorFilter() = runTest {
        assertEquals("My title", RemoveAuthorFilter.filter("My title - Author", "Author"))
        assertEquals("My title", RemoveAuthorFilter.filter("Author | My title", "Author"))
        assertEquals(
            "Series | My title",
            RemoveAuthorFilter.filter("Series | Author | My title", "Author")
        )
        // Test casing too
        assertEquals("My title", RemoveAuthorFilter.filter("My title - AUTHOR", "Author"))
        assertEquals("My title", RemoveAuthorFilter.filter("AUTHOR | My title", "Author"))
        assertEquals(
            "Series | My title",
            RemoveAuthorFilter.filter("Series | AUTHOR | My title", "Author")
        )
    }

    @Test
    fun testFrenchTypography() = runTest {
        assertEquals(" ?", FrenchTypography.filter("   ?"))
        assertEquals(" !", FrenchTypography.filter("   !"))
        assertEquals(" !?", FrenchTypography.filter("   !?"))
        assertEquals(" :", FrenchTypography.filter("  :"))
        assertEquals(" ;", FrenchTypography.filter("  ;"))
    }
}