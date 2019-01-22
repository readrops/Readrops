package com.readrops.readropslibrary.localfeed.json;

import com.google.gson.annotations.SerializedName;

public class JSONAuthor {

    private String name;

    private String url;

    @SerializedName("avatar")
    private String avatarUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


}
