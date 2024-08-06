package com.readrops.api.services.nextcloudnews

import com.readrops.api.services.Credentials

class NextcloudNewsCredentials(login: String?, password: String?, url: String):
        Credentials(if (login != null && password != null) {
            okhttp3.Credentials.basic(login, password)
        } else null, url)