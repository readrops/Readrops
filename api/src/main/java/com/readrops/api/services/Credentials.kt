package com.readrops.api.services

import com.readrops.api.services.fever.FeverCredentials
import com.readrops.api.services.freshrss.FreshRSSCredentials
import com.readrops.api.services.freshrss.FreshRSSService
import com.readrops.api.services.nextcloudnews.NextcloudNewsCredentials
import com.readrops.api.services.nextcloudnews.NextcloudNewsService
import com.readrops.api.utils.Utils
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

abstract class Credentials(val authorization: String?, val url: String) {

    companion object {
        fun toCredentials(account: Account): Credentials {
            val endPoint = getEndPoint(account.type!!).removePrefix("/")
            var accountUrl = Utils.normalizeUrl(account.url)

            return when (account.type) {
                AccountType.NEXTCLOUD_NEWS -> NextcloudNewsCredentials(account.login, account.password, accountUrl + endPoint)
                AccountType.FRESHRSS -> FreshRSSCredentials(account.token, accountUrl + endPoint)
                AccountType.FEVER -> FeverCredentials(account.login, account.password, accountUrl + endPoint)
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }

        private fun getEndPoint(accountType: AccountType): String {
            return when (accountType) {
                AccountType.FRESHRSS -> FreshRSSService.END_POINT
                AccountType.NEXTCLOUD_NEWS -> NextcloudNewsService.END_POINT
                AccountType.FEVER -> ""
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }
    }
}