package com.readrops.db.entities.account

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.readrops.db.R

enum class AccountType(
    @DrawableRes val iconRes: Int,
    @StringRes val nameRes: Int,
    val config: AccountConfig
) {
    LOCAL(R.mipmap.ic_launcher, R.string.local_account, AccountConfig.LOCAL),
    NEXTCLOUD_NEWS(R.drawable.ic_nextcloud_news, R.string.nextcloud_news, AccountConfig.NEXTCLOUD_NEWS),
    FRESHRSS(R.drawable.ic_freshrss, R.string.freshrss, AccountConfig.GREADER),
    FEVER(R.drawable.ic_fever, R.string.fever, AccountConfig.FEVER),
    GREADER(R.drawable.ic_google_reader, R.string.greader, AccountConfig.GREADER)
}

val ACCOUNT_APIS = listOf(AccountType.GREADER, AccountType.FEVER)