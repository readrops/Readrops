package com.readrops.readropslibrary.services;

import com.readrops.readropslibrary.services.nextcloudnews.Folders;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface NextCloudNewsAPI {

    @GET("folders")
    Observable<Folders> getFolders();
}
