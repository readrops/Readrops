package com.readrops.api.services.freshrss;

import com.readrops.api.services.Credentials;

public class FreshRSSCredentials extends Credentials {

    private static final String AUTH_PREFIX = "GoogleLogin auth=";

    public FreshRSSCredentials(String token, String url) {
        super(token != null ? AUTH_PREFIX + token : null, url);

    }
}
