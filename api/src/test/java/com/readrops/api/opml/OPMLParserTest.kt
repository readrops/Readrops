package com.readrops.api.opml

import com.readrops.api.TestUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class OPMLParserTest {

    @Test
    fun readOpmlTest() {
        val stream = TestUtils.loadResource("opml/subscriptions.opml")

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

        stream.close()
    }

    @Test
    fun readLiteSubscriptionsTest() {
        val stream = TestUtils.loadResource("opml/lite_subscriptions.opml")

        var foldersAndFeeds: Map<Folder?, List<Feed>>? = null

        OPMLParser.read(stream)
                .subscribe { result -> foldersAndFeeds = result }

        assertEquals(foldersAndFeeds?.values?.first()?.size, 2)
        assertEquals(foldersAndFeeds?.values?.first()?.first()?.url, "http://www.theverge.com/rss/index.xml")
        assertEquals(foldersAndFeeds?.values?.first()?.get(1)?.url, "https://techcrunch.com/feed/")

        stream.close()
    }

    @Test
    fun opmlVersionTest() {
        val stream = TestUtils.loadResource("opml/wrong_version.opml")

        OPMLParser.read(stream)
                .test()
                .assertError(ParseException::class.java)

        stream.close()
    }

    @Test
    fun writeOpmlTest() {
        val file = File("subscriptions.opml")
        val outputStream = FileOutputStream(file)

        val foldersAndFeeds: Map<Folder?, List<Feed>> = HashMap<Folder?, List<Feed>>().apply {
            put(null, listOf(Feed("Feed1", "", "https://feed1.com"),
                    Feed("Feed2", "", "https://feed2.com")))
            put(Folder("Folder1"), listOf())
            put(Folder("Folder2"), listOf(Feed("Feed3", "", "https://feed3.com"),
                    Feed("Feed4", "", "https://feed4.com")))
        }

        OPMLParser.write(foldersAndFeeds, outputStream)
                .subscribeOn(Schedulers.trampoline())
                .subscribe()

        outputStream.flush()
        outputStream.close()

        val inputStream = file.inputStream()
        var foldersAndFeeds2: Map<Folder?, List<Feed>>? = null
        OPMLParser.read(inputStream).subscribe { result -> foldersAndFeeds2 = result }

        assertEquals(foldersAndFeeds.size, foldersAndFeeds2?.size)
        assertEquals(foldersAndFeeds[Folder("Folder1")]?.size, foldersAndFeeds2?.get(Folder("Folder1"))?.size)
        assertEquals(foldersAndFeeds[Folder("Folder2")]?.size, foldersAndFeeds2?.get(Folder("Folder2"))?.size)
        assertEquals(foldersAndFeeds[null]?.size, foldersAndFeeds2?.get(null)?.size)

        inputStream.close()
    }
}