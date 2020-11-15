package com.readrops.api.services;

import com.readrops.api.services.freshrss.FreshRSSCredentials;
import com.readrops.api.services.freshrss.FreshRSSService;
import com.readrops.api.services.nextcloudnews.NextNewsCredentials;
import com.readrops.api.services.nextcloudnews.NextNewsService;
import com.readrops.db.entities.account.Account;
import com.readrops.db.entities.account.AccountType;

public abstract class Credentials {

    private final String authorization;

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

    public void setUrl(String url) {
        this.url = url;
    }

    public static Credentials toCredentials(Account account) {
        String endPoint = getEndPoint(account.getAccountType());

        switch (account.getAccountType()) {
            case NEXTCLOUD_NEWS:
                return new NextNewsCredentials(account.getLogin(), account.getPassword(), account.getUrl() + endPoint);
            case FRESHRSS:
                return new FreshRSSCredentials(account.getToken(), account.getUrl() + endPoint);
            default:
                throw new IllegalArgumentException("Unknown account type");
        }
    }

    private static String getEndPoint(AccountType accountType) {
        switch (accountType) {
            case FRESHRSS:
                return FreshRSSService.END_POINT;
            case NEXTCLOUD_NEWS:
                return NextNewsService.END_POINT;
            default:
                throw new IllegalArgumentException("Unknown account type");
        }
    }
}
