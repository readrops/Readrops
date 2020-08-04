package com.readrops.api

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.readrops.api.opml.OPMLParser
import com.readrops.api.utils.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OPMLParserTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun readOpmlTest() {
        val stream = context.resources.assets.open("subscriptions.opml")

        var foldersAndFeeds: Map<Folder?, List<Feed>>? = null

        OPMLParser.read(stream)
                .observeOn(Schedulers.trampoline())
                .subscribeOn(Schedulers.trampoline())
                .subscribe { result -> foldersAndFeeds = result }

        assertEquals(foldersAndFeeds?.size, 6)

        assertEquals(foldersAndFeeds?.get(Folder("Folder 1"))?.size, 2)
        assertEquals(foldersAndFeeds?.get(Folder("Subfolder 1"))?.size, 4)
        assertEquals(foldersAndFeeds?.get(Folder("Subfolder 2"))?.size, 1)
        assertEquals(foldersAndFeeds?.get(Folder("Sub subfolder 1"))?.size, 2)
        assertEquals(foldersAndFeeds?.get(Folder("Sub subfolder 2"))?.size, 0)
        assertEquals(foldersAndFeeds?.get(null)?.size, 2)
    }

    @Test
    fun opmlVersionTest() {
        val stream = context.resources.assets.open("wrong_version.opml")

        OPMLParser.read(stream)
                .test()
                .assertError(ParseException::class.java)
    }

    @Test
    fun writeOpmlTest() {

    }
}