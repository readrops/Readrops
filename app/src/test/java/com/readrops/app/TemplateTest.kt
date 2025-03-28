package com.readrops.app

import com.readrops.app.util.FrenchTypography
import com.readrops.app.util.RemoveAuthorFilter
import com.readrops.app.util.ShareIntentTextRenderer
import com.readrops.db.entities.Item
import com.readrops.db.entities.OpenIn
import com.readrops.db.pojo.ItemWithFeed
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

    /** Asserts rendered won't HTML escape */
    @Test
    fun dontEscape() {
        val renderer = ShareIntentTextRenderer(
            ItemWithFeed(
                Item(title = "\"Title\""),
                "",
                0,
                0,
                null,
                null,
                null,
                openIn = OpenIn.EXTERNAL_VIEW
            )
        )
        assertEquals("\"Title\"", renderer.render("{{ title }}"))
    }
}
