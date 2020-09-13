package com.readrops.api

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.readrops.api.opml.OPMLParser
import com.readrops.api.utils.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import io.reactivex.schedulers.Schedulers
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@RunWith(AndroidJUnit4::class)
class OPMLParserTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Test
    fun readOpmlTest() {
        val stream = context.resources.assets.open("opml/subscriptions.opml")

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
        val stream = context.resources.assets.open("opml/lite_subscriptions.opml")

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
        val stream = context.resources.assets.open("opml/wrong_version.opml")

        OPMLParser.read(stream)
                .test()
                .assertError(ParseException::class.java)

        stream.close()
    }

    @Test
    fun writeOpmlTest() {
        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val file = File(filePath, "subscriptions.opml")

        val outputStream: OutputStream = FileOutputStream(file)
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