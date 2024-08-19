package com.readrops.app.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
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
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.readrops.api.services.Credentials
import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.MainActivity
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.repositories.SyncResult
import com.readrops.app.util.FeedColors
import com.readrops.app.util.putSerializable
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
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
                    workDataOf(
                        SYNC_FAILURE_KEY to true,
                    )
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
            val (workResult, syncResults) = refreshAccounts(notificationBuilder)
            notificationManager.cancel(SYNC_NOTIFICATION_ID)

            if (!isManual) {
                displaySyncResults(syncResults)
            }

            workResult
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

    private suspend fun refreshAccounts(notificationBuilder: Builder): Pair<Result, Map<Account, SyncResult>> {
        val sharedPreferences = get<SharedPreferences>()
        var workResult = Result.success(workDataOf(END_SYNC_KEY to true))
        val syncResults = mutableMapOf<Account, SyncResult>()

        val accountId = inputData.getInt(ACCOUNT_ID_KEY, -1)
        val accounts = if (accountId == -1) {
            database.accountDao().selectAllAccounts().first()
        } else {
            listOf(database.accountDao().select(accountId))
        }

        for (account in accounts) {
            if (!account.isLocal) {
                account.login = sharedPreferences.getString(account.loginKey, null)
                account.password = sharedPreferences.getString(account.passwordKey, null)
            }

            val repository = get<BaseRepository> { parametersOf(account) }

            notificationBuilder.setContentTitle(
                applicationContext.resources.getString(
                    R.string.updating_account,
                    account.accountName
                )
            )

            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
            }

            if (account.isLocal) {
                val result = refreshLocalAccount(repository, account, notificationBuilder)

                if (result.second.isNotEmpty() && tags.contains(WORK_MANUAL)) {
                    workResult = Result.success(
                        workDataOf(END_SYNC_KEY to true)
                            .putSerializable(LOCAL_SYNC_ERRORS_KEY, result.second)
                    )
                }

                syncResults[account] = result.first
            } else {
                get<AuthInterceptor>().credentials = Credentials.toCredentials(account)
                val syncResult = repository.synchronize()

                if (syncResult.favicons.isNotEmpty()) {
                    loadFeverFavicons(syncResult.favicons, account, notificationBuilder)
                } else {
                    fetchFeedColors(syncResult, notificationBuilder)
                }

                syncResults[account] = syncResult
            }
        }

        return workResult to syncResults
    }

    private suspend fun refreshLocalAccount(
        repository: BaseRepository,
        account: Account,
        notificationBuilder: Builder
    ): Pair<SyncResult, ErrorResult> {
        val feedId = inputData.getInt(FEED_ID_KEY, 0)
        val folderId = inputData.getInt(FOLDER_ID_KEY, 0)

        val feeds = when {
            feedId > 0 -> listOf(database.feedDao().selectFeed(feedId))
            folderId > 0 -> database.feedDao().selectFeedsByFolder(folderId)
            else -> listOf()
        }

        var feedCount = 0
        val feedMax = if (feeds.isNotEmpty()) {
            feeds.size
        } else {
            database.feedDao().selectFeedCount(account.id)
        }

        val result = repository.synchronize(
            selectedFeeds = feeds,
            onUpdate = { feed ->
                if (notificationManager.areNotificationsEnabled()) {
                    notificationBuilder.setContentText(feed.name)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(feed.name))
                        .setProgress(feedMax, ++feedCount, false)

                    notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
                }

                setProgress(
                    workDataOf(
                        FEED_NAME_KEY to feed.name,
                        FEED_MAX_KEY to feedMax,
                        FEED_COUNT_KEY to feedCount
                    )
                )
            }
        )

        if (result.second.isNotEmpty()) {
            Log.e(
                TAG,
                "refreshing local account ${account.accountName}: ${result.second.size} errors"
            )
        }

        return result
    }

    private suspend fun fetchFeedColors(
        syncResult: SyncResult,
        notificationBuilder: Builder
    ) = with(syncResult) {
        notificationBuilder.setContentTitle(applicationContext.getString(R.string.get_feeds_colors))

        for ((index, feed) in feeds.withIndex()) {
            notificationBuilder.setContentText(feed.name)
                .setStyle(NotificationCompat.BigTextStyle().bigText(feed.name))
                .setProgress(feeds.size, index + 1, false)

            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
            }

            try {
                if (feed.iconUrl != null) {
                    val color = FeedColors.getFeedColor(feed.iconUrl!!)
                    database.feedDao().updateFeedColor(feed.id, color)
                }
            } catch (e: Exception) {
                Log.e(TAG, "${feed.name}: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private suspend fun loadFeverFavicons(
        favicons: Map<Feed, Favicon>,
        account: Account,
        notificationBuilder: Builder
    ) {
        if (notificationManager.areNotificationsEnabled()) {
            // can't make detailed progress as the favicon might already exist in cache
            notificationBuilder.setContentTitle("Loading icons and colors")
                .setProgress(0, 0, true)
            notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
        }

        val diskCache = applicationContext.imageLoader.diskCache!!

        for ((feed, favicon) in favicons) {
            val key = "account_${account.id}_feed_${feed.name!!.replace(" ", "_")}"
            val snapshot = diskCache.openSnapshot(key)

            if (snapshot == null) {
                try {
                    diskCache.openEditor(key)!!.apply {
                        diskCache.fileSystem.write(data) {
                            write(favicon.data)
                        }
                        commit()
                    }

                    database.feedDao().updateFeedIconUrl(feed.id, key)
                    val bitmap =
                        BitmapFactory.decodeByteArray(favicon.data, 0, favicon.data.size)

                    if (bitmap != null) {
                        val color = FeedColors.getFeedColor(bitmap)
                        database.feedDao().updateFeedColor(feed.id, color)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "${feed.name}: ${e.message}")
                }
            }

            snapshot?.close()
        }
    }

    private suspend fun displaySyncResults(syncResults: Map<Account, SyncResult>) {
        val notificationContent = SyncAnalyzer(applicationContext, database)
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

        val pendingIntent =
            PendingIntent.getBroadcast(
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

        val pendingIntent =
            PendingIntent.getBroadcast(
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

        private const val SYNC_NOTIFICATION_ID = 2
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