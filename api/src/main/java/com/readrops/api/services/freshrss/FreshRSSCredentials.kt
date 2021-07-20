package com.readrops.api.services.freshrss

import com.readrops.api.services.Credentials

class FreshRSSCredentials(token: String?, url: String) :
        Credentials(token?.let { AUTH_PREFIX + it }, url) {

    companion object {
        private const val AUTH_PREFIX = "GoogleLogin auth="
    }
}