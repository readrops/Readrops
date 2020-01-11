package com.readrops.readropslibrary.services.nextcloudnews.adapters

import android.annotation.SuppressLint
import com.readrops.readropsdb.entities.Folder
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

class NextNewsFoldersAdapter {

    @ToJson
    fun toJson(folder: Folder): String = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Folder> {
        val folders = mutableListOf<Folder>()

        reader.beginObject()
        reader.nextName() // "folders", beginning of folders array
        reader.beginArray()

        while (reader.hasNext()) {
            val folder = Folder()
            reader.beginObject()

            while (reader.hasNext()) {
                with(folder) {
                    when (reader.selectName(NAMES)) {
                        0 -> remoteId = reader.nextInt().toString()
                        1 -> name = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
            }

            folders += folder
            reader.endObject()
        }

        reader.endArray()
        reader.endObject()

        return folders
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "name")
    }
}