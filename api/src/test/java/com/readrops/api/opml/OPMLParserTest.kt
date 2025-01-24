package com.readrops.api.opml

import com.readrops.api.TestUtils
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class OPMLParserTest {

    @Test
    fun readOpmlTest() = runTest {
        val stream = TestUtils.loadResource("opml/subscriptions.opml")
        val foldersAndFeeds = OPMLParser.read(stream)

        assertEquals(foldersAndFeeds.size, 6)

        assertEquals(foldersAndFeeds[Folder(name = "Folder 1")]?.size, 2)
        assertEquals(foldersAndFeeds[Folder(name = "Subfolder 1")]?.size, 4)
        assertEquals(foldersAndFeeds[Folder(name = "Subfolder 2")]?.size, 1)
        assertEquals(foldersAndFeeds[Folder(name = "Sub subfolder 1")]?.size, 2)
        assertEquals(foldersAndFeeds[Folder(name = "Sub subfolder 2")]?.size, 0)
        assertEquals(foldersAndFeeds[null]?.size, 2)

        stream.close()
    }

    @Test
    fun readLiteSubscriptionsTest() = runTest {
        val stream = TestUtils.loadResource("opml/lite_subscriptions.opml")

        val foldersAndFeeds = OPMLParser.read(stream)

        assertEquals(foldersAndFeeds.values.first().size, 2)
        assertEquals(
            foldersAndFeeds.values.first().first().url,
            "http://www.theverge.com/rss/index.xml"
        )
        assertEquals(foldersAndFeeds.values.first()[1].url, "https://techcrunch.com/feed/")

        stream.close()
    }

    @Test(expected = ParseException::class)
    fun opmlVersionTest() = runTest {
        val stream = TestUtils.loadResource("opml/wrong_version.opml")

        OPMLParser.read(stream)
        stream.close()
    }

    @Test
    fun writeOpmlTest() = runTest {
        val file = File("subscriptions.opml")
        val outputStream = FileOutputStream(file)

        val foldersAndFeeds: Map<Folder?, List<Feed>> = HashMap<Folder?, List<Feed>>().apply {
            put(
                null, listOf(
                    Feed(name = "Feed1", url = "https://feed1.com"),
                    Feed(name = "Feed2", url = "https://feed2.com")
                )
            )
            put(Folder(name = "Folder1"), listOf())
            put(
                Folder(name = "Folder2"), listOf(
                    Feed(name = "Feed3", url = "https://feed3.com"),
                    Feed(name = "Feed4", url = "https://feed4.com")
                )
            )
        }

        OPMLParser.write(foldersAndFeeds, outputStream)

        outputStream.flush()
        outputStream.close()

        val inputStream = file.inputStream()
        val foldersAndFeeds2 = OPMLParser.read(inputStream)

        assertEquals(foldersAndFeeds.size, foldersAndFeeds2.size)
        assertEquals(
            foldersAndFeeds[Folder(name = "Folder1")]?.size,
            foldersAndFeeds2[Folder(name = "Folder1")]?.size
        )
        assertEquals(
            foldersAndFeeds[Folder(name = "Folder2")]?.size,
            foldersAndFeeds2[Folder(name = "Folder2")]?.size
        )
        assertEquals(foldersAndFeeds[null]?.size, foldersAndFeeds2[null]?.size)

        inputStream.close()
        file.delete()
    }
}