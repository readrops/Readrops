package com.readrops.app.notifications.sync

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.readrops.api.services.SyncResult
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.itemslist.MainActivity
import com.readrops.app.repositories.ARepository
import com.readrops.app.utils.ReadropsKeys
import com.readrops.app.utils.SharedPreferencesManager
import com.readrops.db.Database
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class SyncWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters), KoinComponent {

    private var disposable: Disposable? = null

    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val database = get<Database>()

    override fun doWork(): Result {
        var result = Result.success()
        val syncResults = mutableMapOf<Account, SyncResult>()

        try {
            val accounts = database.accountDao().selectAll()

            val notificationBuilder = NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                    .setContentTitle(applicationContext.getString(R.string.auto_synchro))
                    .setProgress(0, 0, true)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setOnlyAlertOnce(true)

            accounts.forEach {
                notificationBuilder.setContentText(it.accountName)
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

                it.login = SharedPreferencesManager.readString(it.loginKey)
                it.password = SharedPreferencesManager.readString(it.passwordKey)

                val repository = get<ARepository>(parameters = { parametersOf(it) })

                disposable = repository.sync(null)
                        .doOnError { throwable ->
                            result = Result.failure()
                            Log.e(TAG, throwable.message!!, throwable)
                        }
                        .subscribe()

                if (repository.syncResult != null) syncResults[it] = repository.syncResult
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            result = Result.failure()
        } finally {
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
            displaySyncResultNotif(syncResults)

            return result
        }
    }

    override fun onStopped() {
        super.onStopped()

        disposable?.dispose()
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }

    private fun displaySyncResultNotif(syncResults: Map<Account, SyncResult>) {
        val notifContent = SyncResultAnalyser(applicationContext, syncResults, database)
                .getSyncNotifContent()

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
                    .setContentIntent(PendingIntent.getActivity(applicationContext, 0,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT))
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

            notificationManager.notify(SYNC_RESULT_NOTIFICATION_ID, notificationBuilder.build())
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

    class MarkReadReceiver : BroadcastReceiver(), KoinComponent {

        override fun onReceive(context: Context?, intent: Intent?) {
            val itemId = intent?.getIntExtra(ReadropsKeys.ITEM_ID, 0)!!

            with(get<Database>()) {
                itemDao().setReadState(itemId, true)
                        .subscribeOn(Schedulers.io())
                        .subscribe()
            }

            with(NotificationManagerCompat.from(context!!)) {
                cancel(SYNC_RESULT_NOTIFICATION_ID)
            }
        }
    }

    class ReadLaterReceiver : BroadcastReceiver(), KoinComponent {

        override fun onReceive(context: Context?, intent: Intent?) {
            val itemId = intent?.getIntExtra(ReadropsKeys.ITEM_ID, 0)!!

            with(get<Database>()) {
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