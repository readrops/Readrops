package com.readrops.api.services.nextcloudnews.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.db.entities.Folder
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

        return try {
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
                            1 -> name = reader.nextNonEmptyString()
                            else -> reader.skipValue()
                        }
                    }
                }

                folders += folder
                reader.endObject()
            }

            reader.endArray()
            reader.endObject()

            folders
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "name")
    }
}