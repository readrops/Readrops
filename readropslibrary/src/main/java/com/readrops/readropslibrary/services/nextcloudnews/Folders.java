package com.readrops.readropslibrary.services.nextcloudnews;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Folders {

    @SerializedName("folders")
    private List<Folder> folders;

    public List<Folder> getFolders() {
        return folders;
    }
}
