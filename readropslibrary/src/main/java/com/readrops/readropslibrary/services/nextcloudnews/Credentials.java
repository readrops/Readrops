package com.readrops.readropslibrary.services.nextcloudnews;

public class Credentials {

    private String base64;

    private String url;

    public Credentials(String login, String password, String url) {
        this.base64 = okhttp3.Credentials.basic(login, password);
        this.url = url;
    }

    public String getBase64() {
        return base64;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
