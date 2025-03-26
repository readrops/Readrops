package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class Favicon(
    val id: Int,
    val data: ByteArray
)

class FeverFaviconsAdapter {

    @ToJson
    fun toJson(favicons: List<Favicon>) = ""

    @SuppressLint("CheckResult")
    @FromJson
    fun fromJson(reader: JsonReader): List<Favicon> = with(reader) {
        return try {
            val favicons = arrayListOf<Favicon>()

            beginObject()
            while (hasNext()) {
                when (nextName()) {
                    "favicons" -> {
                        beginArray()

                        while (hasNext()) {
                            beginObject()
                            parseFavicon(reader)?.let { favicons += it }

                            endObject()
                        }

                        endArray()
                    }
                    else -> skipValue()
                }
            }

            endObject()
            favicons
        } catch (e: Exception) {
            throw ParseException("Fever favicons parsing failure", e)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun parseFavicon(reader: JsonReader): Favicon? = with(reader) {
        var id = 0
        var data: ByteArray? = null

        while (hasNext()) {
            when (selectName(NAMES)) {
                0 -> id = nextInt()
                1 -> data = Base64.decode(nextString().substringAfter("base64,"))
                else -> skipValue()
            }
        }

        if (id > 0 && data != null) {
            return Favicon(
                id = id,
                data = data,
            )
        } else {
            null
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "data")
    }
}