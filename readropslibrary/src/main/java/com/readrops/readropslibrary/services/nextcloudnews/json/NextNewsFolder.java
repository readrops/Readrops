package com.readrops.readropslibrary.services.nextcloudnews.json;

import com.google.gson.annotations.SerializedName;

public class NextNewsFolder {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    public NextNewsFolder() {
    }

    public NextNewsFolder(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public NextNewsFolder(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
