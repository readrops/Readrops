package com.readrops.api.services.freshrss.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Folder
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class FreshRSSFoldersAdapterTest {

    private val adapter = Moshi.Builder()
            .add(FreshRSSFoldersAdapter())
            .build()
            .adapter<List<Folder>>(Types.newParameterizedType(List::class.java, Folder::class.java))

    @Test
    fun validFoldersTest() {
        val stream = TestUtils.loadResource("services/freshrss/adapters/folders.json")

        val folders = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(folders.size, 1)

        with(folders[0]) {
            assertEquals(name, "Blogs")
            assertEquals(remoteId, "user/-/label/Blogs")
        }
    }
}