package com.readrops.readropslibrary.services.nextcloudnews;

import com.readrops.readropslibrary.services.Credentials;

public class NextNewsCredentials extends Credentials {

    public NextNewsCredentials(String login, String password, String url) {
        super(okhttp3.Credentials.basic(login, password), url);
    }
}
