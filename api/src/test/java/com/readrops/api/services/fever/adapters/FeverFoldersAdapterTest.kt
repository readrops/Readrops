package com.readrops.api.services.fever.adapters

import com.readrops.api.TestUtils
import com.readrops.db.entities.Folder
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okio.Buffer
import org.junit.Test
import kotlin.test.assertEquals

class FeverFoldersAdapterTest {

    private val adapter = Moshi.Builder()
        .add(FeverFoldersAdapter())
        .build()
        .adapter<List<Folder>>(Types.newParameterizedType(List::class.java, Folder::class.java))

    @Test
    fun validFoldersTest() {
        val stream = TestUtils.loadResource("services/fever/folders.json")

        val folders = adapter.fromJson(Buffer().readFrom(stream))!!

        with(folders.first()) {
            assertEquals(name, "Libre")
            assertEquals(remoteId, "4")
        }


    }
}