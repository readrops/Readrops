package com.readrops.api.services

import com.readrops.api.services.freshrss.FreshRSSCredentials
import com.readrops.api.services.freshrss.NewFreshRSSService
import com.readrops.api.services.nextcloudnews.NewNextcloudNewsService
import com.readrops.api.services.nextcloudnews.NextNewsCredentials
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
                AccountType.FRESHRSS -> NewFreshRSSService.END_POINT
                AccountType.NEXTCLOUD_NEWS -> NewNextcloudNewsService.END_POINT
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }
    }
}