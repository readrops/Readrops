package com.readrops.db.entities.account

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.readrops.db.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class AccountType(@DrawableRes val iconRes: Int,
                       @StringRes val typeName: Int,
                       val accountConfig: AccountConfig?) : Parcelable {
    LOCAL(R.mipmap.ic_launcher, R.string.local_account, AccountConfig.LOCAL),
    NEXTCLOUD_NEWS(R.drawable.ic_nextcloud_news, R.string.nextcloud_news, AccountConfig.NEXTCLOUD_NEWS),
   /* FEEDLY(R.drawable.ic_feedly, R.string.feedly, null),*/
    FRESHRSS(R.drawable.ic_freshrss, R.string.freshrss, AccountConfig.FRESHRSS);
}