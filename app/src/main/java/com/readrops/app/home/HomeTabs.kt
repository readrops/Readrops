package com.readrops.app.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import cafe.adriel.voyager.navigator.tab.Tab
import com.readrops.app.R
import com.readrops.app.account.AccountTab
import com.readrops.app.feeds.FeedTab
import com.readrops.app.more.MoreTab
import com.readrops.app.timelime.TimelineTab

enum class HomeTabs(
    val tab: Tab,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    TIMELINE(TimelineTab, R.string.timeline, R.drawable.ic_timeline),
    FEEDS(FeedTab, R.string.feeds, R.drawable.ic_rss_feed_grey),
    ACCOUNT(AccountTab, R.string.account, R.drawable.ic_account),
    MORE(MoreTab, R.string.more, R.drawable.ic_more_vert)
}