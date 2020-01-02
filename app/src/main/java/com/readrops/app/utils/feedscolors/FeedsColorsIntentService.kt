package com.readrops.app.utils.feedscolors

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Feed
import com.readrops.app.utils.ReadropsKeys.FEEDS

class FeedsColorsIntentService : IntentService("FeedsColorsIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val feeds: List<Feed> = intent!!.getParcelableArrayListExtra(FEEDS)!!
        val database = Database.getInstance(this)

        val notificationBuilder = NotificationCompat.Builder(this, ReadropsApp.FEEDS_COLORS_CHANNEL_ID)
                .setContentTitle(getString(R.string.get_feeds_colors))
                .setProgress(feeds.size, 0, false)
                .setSmallIcon(R.drawable.ic_notif)
                .setOnlyAlertOnce(true)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        val notificationManager = NotificationManagerCompat.from(this)

        var feedsNb = 0
        feeds.forEach {
            notificationBuilder.setContentText(it.name)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            setFeedColors(it)

            database.feedDao().updateColors(it.id, it.textColor, it.backgroundColor)
            notificationBuilder.setProgress(feeds.size, ++feedsNb, false)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        stopForeground(true)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }

}