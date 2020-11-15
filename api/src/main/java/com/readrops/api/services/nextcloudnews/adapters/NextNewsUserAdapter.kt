package com.readrops.api.services.nextcloudnews.adapters

import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText
import java.io.InputStream

class NextNewsUserAdapter : XmlAdapter<String> {

    override fun fromXml(inputStream: InputStream): String {
        val konsumer = inputStream.konsumeXml()
        var displayName: String? = null

        return try {
            konsumer.child("ocs") {
                allChildrenAutoIgnore("data") {
                    allChildrenAutoIgnore("displayname") {
                        displayName = nonNullText()
                    }
                }
            }

            konsumer.close()
            displayName!!
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }
}