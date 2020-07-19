package com.readrops.api.services.freshrss.json

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FreshRSSUserInfo(val userEmail: String,
                            val userId: String,
                            val userName: String,
                            val userProfileId: String)