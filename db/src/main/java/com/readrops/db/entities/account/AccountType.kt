package com.readrops.db.entities.account

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.readrops.db.R

enum class AccountType(@DrawableRes val iconRes: Int,
                       @StringRes val typeName: Int,
                       val accountConfig: AccountConfig?) {
    LOCAL(R.mipmap.ic_launcher, R.string.local_account, AccountConfig.LOCAL),
    NEXTCLOUD_NEWS(R.drawable.ic_nextcloud_news, R.string.nextcloud_news, AccountConfig.NEXTCLOUD_NEWS),
    //FEEDLY(R.drawable.ic_feedly, R.string.feedly, null),
    FRESHRSS(R.drawable.ic_freshrss, R.string.freshrss, AccountConfig.FRESHRSS),
    FEVER(R.mipmap.ic_launcher, R.string.fever, AccountConfig.FEVER)
}