package com.readrops.api.services.nextcloudnews.adapters

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.extensions.nonNullText

class NextcloudNewsUserAdapter : XmlAdapter<String> {

    override fun fromXml(konsumer: Konsumer): String {
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
            throw ParseException("Nextcloud News user parsing failure", e)
        }
    }
}