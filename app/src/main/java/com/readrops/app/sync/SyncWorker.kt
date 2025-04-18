package com.readrops.app.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.readrops.app.MainActivity
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.repositories.SyncResult
import com.readrops.app.util.extensions.putSerializable
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit


class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val notificationManager by inject<NotificationManagerCompat>()
    private val database by inject<Database>()

    override suspend fun doWork(): Result {
        val isManual = tags.contains(WORK_MANUAL)

        val infos = WorkManager.getInstance(applicationContext)
            .getWorkInfosByTagFlow(TAG).first()

        if (infos.any { it.state == WorkInfo.State.RUNNING && it.id != id }) {
            return if (isManual) {
                Result.failure(
                    workDataOf(SYNC_FAILURE_KEY to true)
                        .putSerializable(
                            SYNC_FAILURE_EXCEPTION_KEY,
                            Exception(applicationContext.getString(R.string.background_sync_already_running))
                        )
                )
            } else {
                Result.retry()
            }
        }

        val notificationBuilder = Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
            .setProgress(0, 0, true)
            .setSmallIcon(R.drawable.ic_sync)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // for Android 7.1 and earlier
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        return try {
            val synchronizer = get<Synchronizer>()

            val (syncResults, errorResult) = synchronizer.synchronizeAccounts(
                notificationBuilder = notificationBuilder,
                inputData = SyncInputData(
                    accountId = inputData.getInt(ACCOUNT_ID_KEY, -1),
                    feedId = inputData.getInt(FEED_ID_KEY, -1),
                    folderId = inputData.getInt(FOLDER_ID_KEY, -1)
                ),
                onUpdate = { feed, feedMax, feedCount ->
                    setProgress(
                        workDataOf(
                            FEED_NAME_KEY to feed.name,
                            FEED_MAX_KEY to feedMax,
                            FEED_COUNT_KEY to feedCount
                        )
                    )
                }
            )

            notificationManager.cancel(SYNC_NOTIFICATION_ID)

            if (!isManual) {
                displaySyncResults(syncResults)
            }

            return Result.success(workDataOf(END_SYNC_KEY to true).apply {
                if (errorResult.isNotEmpty() && isManual) {
                    putSerializable(LOCAL_SYNC_ERRORS_KEY, errorResult)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "${e.printStackTrace()}")

            notificationManager.cancel(SYNC_NOTIFICATION_ID)
            if (isManual) {
                Result.failure(
                    workDataOf(SYNC_FAILURE_KEY to true)
                        .putSerializable(SYNC_FAILURE_EXCEPTION_KEY, Exception(e.cause))
                )
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun displaySyncResults(syncResults: Map<Account, SyncResult>) {
        val notificationContent = get<SyncAnalyzer>()
            .getNotificationContent(syncResults)

        if (notificationContent != null) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                if (notificationContent.accountId > 0) {
                    putExtra(ACCOUNT_ID_KEY, notificationContent.accountId)
                }

                if (notificationContent.item != null) {
                    putExtra(ITEM_ID_KEY, notificationContent.item.id)
                }
            }

            val notificationBuilder = Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                .setContentTitle(notificationContent.title)
                .setContentText(notificationContent.text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationContent.text))
                .setSmallIcon(R.drawable.ic_notifications)
                .setColor(notificationContent.color)
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setAutoCancel(true)

            notificationContent.item?.let { item ->
                val itemId = item.id

                notificationBuilder
                    .addAction(getMarkReadAction(itemId))
                    .addAction(getMarkFavoriteAction(itemId))
            }

            notificationContent.largeIcon?.let { notificationBuilder.setLargeIcon(it) }

            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(SYNC_RESULT_NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }

    private fun getMarkReadAction(itemId: Int): Action {
        val intent = Intent(applicationContext, SyncBroadcastReceiver::class.java).apply {
            action = SyncBroadcastReceiver.ACTION_MARK_READ
            putExtra(ITEM_ID_KEY, itemId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Action.Builder(
            R.drawable.ic_done_all,
            applicationContext.getString(R.string.mark_read),
            pendingIntent
        )
            .setAllowGeneratedReplies(false)
            .build()
    }

    private fun getMarkFavoriteAction(itemId: Int): Action {
        val intent = Intent(applicationContext, SyncBroadcastReceiver::class.java).apply {
            action = SyncBroadcastReceiver.ACTION_SET_FAVORITE
            putExtra(ITEM_ID_KEY, itemId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Action.Builder(
            R.drawable.ic_favorite_border,
            applicationContext.getString(R.string.add_to_favorite),
            pendingIntent
        )
            .setAllowGeneratedReplies(false)
            .build()
    }

    companion object {
        private val TAG: String = SyncWorker::class.java.simpleName

        private val WORK_AUTO = "$TAG-auto"
        private val WORK_MANUAL = "$TAG-manual"

        const val SYNC_NOTIFICATION_ID = 2
        const val SYNC_RESULT_NOTIFICATION_ID = 3

        const val END_SYNC_KEY = "END_SYNC"
        const val SYNC_FAILURE_KEY = "SYNC_FAILURE"
        const val SYNC_FAILURE_EXCEPTION_KEY = "SYNC_FAILURE_EXCEPTION"
        const val ACCOUNT_ID_KEY = "ACCOUNT_ID"
        const val FEED_ID_KEY = "FEED_ID"
        const val ITEM_ID_KEY = "ITEM_ID"
        const val FOLDER_ID_KEY = "FOLDER_ID"
        const val FEED_NAME_KEY = "FEED_NAME"
        const val FEED_MAX_KEY = "FEED_MAX"
        const val FEED_COUNT_KEY = "FEED_COUNT"
        const val LOCAL_SYNC_ERRORS_KEY = "LOCAL_SYNC_ERRORS"

        suspend fun startNow(context: Context, data: Data, onUpdate: (WorkInfo) -> Unit) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(TAG)
                .addTag(WORK_MANUAL)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).apply {
                enqueueUniqueWork(WORK_MANUAL, ExistingWorkPolicy.KEEP, request)
                getWorkInfoByIdFlow(request.id)
                    .collect { workInfo ->
                        if (workInfo != null) {
                            onUpdate(workInfo)
                        }
                    }
            }
        }

        fun startPeriodically(context: Context, period: String) {
            val workManager = WorkManager.getInstance(context)

            val interval = when (period) {
                "0.30" -> 30L to TimeUnit.MINUTES
                "1" -> 1L to TimeUnit.HOURS
                "2" -> 2L to TimeUnit.HOURS
                "3" -> 3L to TimeUnit.HOURS
                "6" -> 6L to TimeUnit.HOURS
                "12" -> 12L to TimeUnit.HOURS
                "24" -> 1L to TimeUnit.DAYS
                else -> null
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            if (interval != null) {
                val request = PeriodicWorkRequest.Builder(
                    SyncWorker::class.java,
                    interval.first,
                    interval.second
                )
                    .addTag(TAG)
                    .addTag(WORK_AUTO)
                    .setConstraints(constraints)
                    .setInitialDelay(interval.first, interval.second)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, interval.first, interval.second)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_AUTO,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            } else {
                workManager.cancelAllWorkByTag(WORK_AUTO)
            }
        }
    }
}