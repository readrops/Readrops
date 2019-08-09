package com.readrops.readropslibrary.services;

public abstract class Credentials {

    private String authorization;

    private String url;

    public Credentials(String authorization, String url) {
        this.authorization = authorization;
        this.url = url;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getUrl() {
        return url;
    }
}
