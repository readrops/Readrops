package com.readrops.readropslibrary.services.nextcloudnews.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class NextNewsFolders {

    @SerializedName("folders")
    private List<NextNewsFolder> folders;

    public List<NextNewsFolder> getFolders() {
        return folders;
    }
}
