package com.readrops.api.services.fever

import com.readrops.api.services.Credentials

class FeverCredentials(login: String?, password: String?, url: String) :
        Credentials(/*(login != null && password != null)
                .let { "api_key=" + ApiUtils.md5hash("$login:p$password") }*/null, url)