package com.readrops.api.services.fever

import android.util.Log
import com.readrops.api.utils.ApiUtils
import com.readrops.db.entities.Feed
import okhttp3.MultipartBody

class FeverDataSource(val service: FeverService) {

    suspend fun login(login: String, password: String) {
        val credentials = ApiUtils.md5hash("$login:$password")


        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api_key", credentials)
                .build()

        val response = service.login(requestBody)
        Log.d("TAG", "login: ")
    }

    suspend fun getFeeds(): List<Feed> = service.getFeeds()
}