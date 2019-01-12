package com.readrops.readropslibrary.services;

import com.readrops.readropslibrary.services.nextcloudnews.Folders;

import retrofit2.http.GET;

public interface NextCloudNewsAPI {

    @GET("folders")
    Folders getFolders();
}
