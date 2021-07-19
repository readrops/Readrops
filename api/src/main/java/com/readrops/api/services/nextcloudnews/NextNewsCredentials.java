package com.readrops.api.services.nextcloudnews;

import com.readrops.api.services.Credentials;

public class NextNewsCredentials extends Credentials {

    public NextNewsCredentials(String login, String password, String url) {
        super(login != null && password != null ? okhttp3.Credentials.basic(login, password) : null, url);
    }
}
