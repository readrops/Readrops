package com.readrops.api.services.greader.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nextNonEmptyString
import com.readrops.db.entities.Folder
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.util.StringTokenizer

class GReaderFoldersAdapter {

    @ToJson
    fun toJson(folders: List<Folder>): String = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Folder> = with(reader) {
        val folders = mutableListOf<Folder>()

        return try {
            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "tags" -> {
                        beginArray()

                        while (hasNext()) {
                            beginObject()
                            parseFolder(reader)?.let { folders += it }

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()
            folders
        } catch (e: Exception) {
            throw ParseException("GReader folders parsing failure", e)
        }
    }

    private fun parseFolder(reader: JsonReader): Folder? = with(reader) {
        val folder = Folder()
        var type: String? = null

        while (hasNext()) {
            with(folder) {
                when (selectName(NAMES)) {
                    0 -> {
                        val id = nextNonEmptyString()
                        name = StringTokenizer(id, "/")
                            .toList()
                            .last() as String
                        remoteId = id
                    }

                    1 -> type = nextString()
                    else -> skipValue()
                }
            }
        }

        // add only folders and avoid tags
        if (type == "folder") {
            folder
        } else {
            null
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "type")
    }
}