package com.readrops.readropslibrary.services.nextcloudnews.json;

public class NextNewsUser {

    private String userId;

    private String displayName;

    private long lastLoginTimestamp;

    private Avatar avatar;

    public class Avatar {

        private String data;

        private String mime;
    }
}
