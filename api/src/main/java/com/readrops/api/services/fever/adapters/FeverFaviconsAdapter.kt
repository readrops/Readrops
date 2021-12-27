package com.readrops.api.services.fever.adapters

import android.annotation.SuppressLint
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.skipField
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

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

            repeat(3) {
                skipField()
            }

            nextName() // beginning of favicon array
            beginArray()

            while (hasNext()) {
                beginObject()

                var id = 0
                var data: ByteArray? = null

                while (hasNext()) {
                    when (selectName(NAMES)) {
                        0 -> id = nextInt()
                        1 -> data = nextString().toByteArray()
                        else -> skipValue()
                    }
                }

                if (id > 0 && data != null) {
                    favicons += Favicon(
                        id = id,
                        data = data,
                    )
                }

                endObject()
            }

            endArray()
            endObject()

            favicons
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    companion object {
        val NAMES: JsonReader.Options = JsonReader.Options.of("id", "data")
    }
}