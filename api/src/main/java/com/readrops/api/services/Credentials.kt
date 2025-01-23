package com.readrops.api.services

import com.readrops.api.services.fever.FeverCredentials
import com.readrops.api.services.greader.GReaderCredentials
import com.readrops.api.services.nextcloudnews.NextcloudNewsCredentials
import com.readrops.api.services.nextcloudnews.NextcloudNewsService
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

abstract class Credentials(val authorization: String?, val url: String) {

    companion object {
        fun toCredentials(account: Account): Credentials {
            val endPoint = getEndPoint(account.type!!)

            return when (account.type) {
                AccountType.NEXTCLOUD_NEWS -> NextcloudNewsCredentials(account.login, account.password, account.url + endPoint)
                AccountType.FRESHRSS, AccountType.GREADER -> GReaderCredentials(account.token, account.url + endPoint)
                AccountType.FEVER -> FeverCredentials(account.login, account.password, account.url + endPoint)
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }

        private fun getEndPoint(accountType: AccountType): String {
            return when (accountType) {
                AccountType.NEXTCLOUD_NEWS -> NextcloudNewsService.END_POINT
                AccountType.FRESHRSS -> "api/greader.php/"
                AccountType.FEVER, AccountType.GREADER -> ""
                else -> throw IllegalArgumentException("Unknown account type")
            }
        }
    }
}