package com.readrops.api.services.greader.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Folder
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import junit.framework.TestCase.assertEquals
import okio.Buffer
import org.junit.Test

class GReaderFoldersAdapterTest {

    private val adapter = Moshi.Builder()
            .add(GReaderFoldersAdapter())
            .build()
            .adapter<List<Folder>>(Types.newParameterizedType(List::class.java, Folder::class.java))

    @Test
    fun validFoldersTest() {
        val stream = TestUtils.loadResource("services/freshrss/adapters/folders.json")

        val folders = adapter.fromJson(Buffer().readFrom(stream))!!

        assertEquals(folders.size, 1)

        with(folders.first()) {
            assertEquals(name, "Blogs")
            assertEquals(remoteId, "user/-/label/Blogs")
        }
    }
}