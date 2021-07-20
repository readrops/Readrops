package com.readrops.api.services.nextcloudnews

import com.readrops.api.services.Credentials

class NextNewsCredentials(login: String?, password: String?, url: String):
        Credentials((login != null && password != null).let {
            okhttp3.Credentials.basic(login!!, password!!)
        }, url)