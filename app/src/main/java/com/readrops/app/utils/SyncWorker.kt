package com.readrops.app.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.activities.MainActivity
import com.readrops.app.repositories.ARepository
import com.readrops.readropsdb.Database
import com.readrops.readropsdb.entities.Item
import com.readrops.readropsdb.entities.account.Account
import com.readrops.readropslibrary.services.SyncResult
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SyncWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    private lateinit var disposable: Disposable

    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val database = Database.getInstance(applicationContext)

    override fun doWork(): Result {
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
            notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

            val repository = ARepository.repositoryFactory(it, applicationContext)

            disposable = repository.sync(null)
                    .doOnError { throwable ->
                        result = Result.failure()
                        Log.e(TAG, throwable.message!!, throwable)
                    }
                    .subscribe()

            if (repository.syncResult != null) syncResults[it] = repository.syncResult
        }

        notificationManager.cancel(SYNC_NOTIFICATION_ID)
        displaySyncResultNotif(syncResults)

        return result
    }

    override fun onStopped() {
        super.onStopped()

        disposable.dispose()
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }

    private fun displaySyncResultNotif(syncResults: Map<Account, SyncResult>) {
        val notifContent = SyncResultAnalyser(applicationContext, syncResults, database).getSyncNotifContent()

        if (notifContent.title != null) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                if (notifContent.item != null) {
                    putExtra(ReadropsKeys.ITEM_ID, notifContent.item?.id)
                    putExtra(ReadropsKeys.IMAGE_URL, notifContent.item?.imageLink)

                    if (notifContent.accountId != null) putExtra(ReadropsKeys.ACCOUNT_ID, notifContent.accountId!!)
                }
            }

            val notificationBuilder = NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                    .setContentTitle(notifContent.title)
                    .setContentText(notifContent.content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notifContent.content))
                    .setSmallIcon(R.drawable.ic_notif)
                    .setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)

            notifContent.item?.let {
                val feed = database.feedDao().getFeedById(it.feedId)

                notificationBuilder.addAction(buildReadlaterAction(it))
                        .addAction(buildMarkAsRead(it))
                        .setColor(if (feed.backgroundColor != 0) feed.backgroundColor else feed.textColor)
            }

            notifContent.largeIcon?.let {
                notificationBuilder.setLargeIcon(it)
            }

            notificationManager.notify(SYNC_RESULT_NOTIFICATION_ID,
                    notificationBuilder.build())
        }

    }

    private fun buildReadlaterAction(item: Item): NotificationCompat.Action {
        val broadcastIntent = Intent(applicationContext, ReadLaterReceiver::class.java).apply {
            putExtra(ReadropsKeys.ITEM_ID, item.id)
        }

        return NotificationCompat.Action.Builder(R.drawable.ic_read_later, applicationContext.getString(R.string.read_later),
                        PendingIntent.getBroadcast(applicationContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAllowGeneratedReplies(false)
                .build()
    }

    private fun buildMarkAsRead(item: Item): NotificationCompat.Action {
        val broadcastIntent = Intent(applicationContext, MarkReadReceiver::class.java).apply {
            putExtra(ReadropsKeys.ITEM_ID, item.id)
        }

        return NotificationCompat.Action.Builder(R.drawable.ic_read, applicationContext.getString(R.string.read),
                        PendingIntent.getBroadcast(applicationContext, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAllowGeneratedReplies(false)
                .build()
    }

    class MarkReadReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val itemId = intent?.getIntExtra(ReadropsKeys.ITEM_ID, 0)!!

            with(Database.getInstance(context)) {
                itemDao().setReadState(itemId, true, true)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

            with(NotificationManagerCompat.from(context!!)) {
                cancel(SYNC_RESULT_NOTIFICATION_ID)
            }
        }
    }

    class ReadLaterReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val itemId = intent?.getIntExtra(ReadropsKeys.ITEM_ID, 0)!!

            with(Database.getInstance(context)) {
                itemDao().setReadItLater(itemId)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

            with(NotificationManagerCompat.from(context!!)) {
                cancel(SYNC_RESULT_NOTIFICATION_ID)
            }
        }

    }

    companion object {
        val TAG = SyncWorker::class.java.simpleName
        private const val SYNC_NOTIFICATION_ID = 2
        const val SYNC_RESULT_NOTIFICATION_ID = 3
    }
}