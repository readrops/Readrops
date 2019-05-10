package com.readrops.readropslibrary.services.nextcloudnews;

public class Credentials {

    private String login;

    private String password;

    private String url;

    public Credentials(String login, String password, String url) {
        this.login = login;
        this.password = password;
        this.url = url;
    }

    public String toBase64() {
        return okhttp3.Credentials.basic(login, password);
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
