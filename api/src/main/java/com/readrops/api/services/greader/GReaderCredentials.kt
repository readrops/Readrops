package com.readrops.api.services.greader

import com.readrops.api.services.Credentials

class GReaderCredentials(token: String?, url: String) :
        Credentials(token?.let { AUTH_PREFIX + it }, url) {

    companion object {
        private const val AUTH_PREFIX = "GoogleLogin auth="
    }
}