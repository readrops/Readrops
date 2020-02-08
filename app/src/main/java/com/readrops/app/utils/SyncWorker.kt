package com.readrops.app.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.activities.MainActivity
import com.readrops.app.repositories.ARepository
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropslibrary.services.SyncResult
import io.reactivex.disposables.Disposable

class SyncWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private lateinit var disposable: Disposable
    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    override fun doWork(): Result {
        val database = Database.getInstance(applicationContext)
        val accounts = database.accountDao().selectAll()
        var result = Result.success()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                .setContentTitle(applicationContext.getString(R.string.auto_synchro))
                .setProgress(0, 0, true)
                .setSmallIcon(R.drawable.ic_notif)
                .setOnlyAlertOnce(true)

        val syncResults = mutableMapOf<Account, SyncResult>()
        accounts.forEach {
            notificationBuilder.setContentText(it.accountName)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

            val repository = ARepository.repositoryFactory(it, applicationContext)

            disposable = repository.sync(null)
                    .doOnError { result = Result.failure() }
                    .subscribe()

            syncResults[it] = repository.syncResult
        }

        notificationManager.cancel(NOTIFICATION_ID)
        displaySyncResultNotif(syncResults)

        return result
    }

    override fun onStopped() {
        super.onStopped()

        disposable.dispose()
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun displaySyncResultNotif(syncResults: Map<Account, SyncResult>) {
        val notifContent = SyncResultAnalyser(applicationContext, syncResults).getSyncNotifContent()

        if (notifContent.title != null && notifContent.content != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)

            val notificationBuilder = NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                    .setContentTitle(notifContent.title)
                    .setContentText(notifContent.content)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))

            if (notifContent.largeIcon != null)
                notificationBuilder.setLargeIcon(notifContent.largeIcon)

            notificationManager.notify(ITEMS_NOTIFICATION_ID,
                notificationBuilder.build())
        }

    }

    companion object {
        val TAG = SyncWorker::class.java.simpleName
        private const val NOTIFICATION_ID = 2
        private const val ITEMS_NOTIFICATION_ID = 3
    }
}