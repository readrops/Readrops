package com.readrops.api.services

import com.readrops.api.services.freshrss.FreshRSSCredentials
import com.readrops.api.services.freshrss.FreshRSSService
import com.readrops.api.services.nextcloudnews.NextNewsCredentials
import com.readrops.api.services.nextcloudnews.NextNewsService
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

abstract class Credentials(val authorization: String?, val url: String) {

    companion object {
        @JvmStatic
        fun toCredentials(account: Account): Credentials {
            val endPoint = getEndPoint(account.accountType!!)

            return when (account.accountType) {
                AccountType.NEXTCLOUD_NEWS -> NextNewsCredentials(account.login, account.password, account.url + endPoint)
                AccountType.FRESHRSS -> FreshRSSCredentials(account.token, account.url + endPoint)
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }

        private fun getEndPoint(accountType: AccountType): String {
            return when (accountType) {
                AccountType.FRESHRSS -> FreshRSSService.END_POINT
                AccountType.NEXTCLOUD_NEWS -> NextNewsService.END_POINT
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }
    }
}