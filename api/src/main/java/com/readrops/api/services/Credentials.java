package com.readrops.api.services;

import androidx.annotation.Nullable;

import com.readrops.db.entities.account.Account;
import com.readrops.api.services.freshrss.FreshRSSCredentials;
import com.readrops.api.services.nextcloudnews.NextNewsCredentials;

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

    @Nullable
    public static Credentials toCredentials(Account account) {
        switch (account.getAccountType()) {
            case NEXTCLOUD_NEWS:
                return new NextNewsCredentials(account.getLogin(), account.getPassword(), account.getUrl());
            case FRESHRSS:
                return new FreshRSSCredentials(account.getToken(), account.getUrl());
            default:
                return null;
        }
    }
}
