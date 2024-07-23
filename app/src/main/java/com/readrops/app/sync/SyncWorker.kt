package com.readrops.app.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.readrops.api.services.SyncResult
import com.readrops.app.R
import com.readrops.app.ReadropsApp
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.util.FeedColors
import com.readrops.app.util.putSerializable
import com.readrops.db.Database
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val notificationManager = NotificationManagerCompat.from(appContext)
    private val database = get<Database>()

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val sharedPreferences = get<SharedPreferences>()
        var workResult = Result.success(workDataOf(END_SYNC_KEY to true))

        try {
            require(notificationManager.areNotificationsEnabled())

            val notificationBuilder =
                NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                    .setProgress(0, 0, true)
                    .setSmallIcon(R.drawable.ic_notifications) // TODO use better icon
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT) // for Android 7.1 and earlier
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setOnlyAlertOnce(true)

            val accountId = inputData.getInt(ACCOUNT_ID_KEY, 0)
            val accounts = if (accountId == 0) {
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
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

                if (account.isLocal) {
                    val result = refreshLocalAccount(repository, account)

                    if (result.second.isNotEmpty()) {
                        workResult = Result.success(
                            workDataOf(END_SYNC_KEY to true)
                                .putSerializable(LOCAL_SYNC_ERRORS_KEY, result.second)
                        )
                    }
                } else {
                    val syncResult = repository.synchronize()
                    fetchFeedColors(syncResult, notificationBuilder)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            workResult = Result.failure(
                workDataOf(SYNC_FAILURE_KEY to true)
                    .putSerializable(SYNC_FAILURE_EXCEPTION_KEY, e)
            )
        } finally {
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
        }

        return workResult
    }

    private suspend fun refreshLocalAccount(
        repository: BaseRepository,
        account: Account
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
                setProgress(
                    workDataOf(
                        FEED_NAME_KEY to feed.name,
                        FEED_MAX_KEY to feedMax,
                        FEED_COUNT_KEY to ++feedCount
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

    @SuppressLint("MissingPermission")
    private suspend fun fetchFeedColors(
        syncResult: SyncResult,
        notificationBuilder: NotificationCompat.Builder
    ) {
        notificationBuilder.setContentTitle(applicationContext.getString(R.string.get_feeds_colors))

        for ((index, feedId) in syncResult.newFeedIds.withIndex()) {
            val feedName = syncResult.feeds.first { it.id == feedId.toInt() }.name

            notificationBuilder.setContentText(feedName)
                .setProgress(syncResult.newFeedIds.size, index + 1, false)
            notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

            try {
                val color =
                    FeedColors.getFeedColor(syncResult.feeds.first { it.id == feedId.toInt() }.iconUrl!!)
                database.feedDao().updateFeedColor(feedId.toInt(), color)
            } catch (e: Exception) {
                Log.e(TAG, "$feedName: ${e.message}")
            }
        }
    }

    companion object {
        private val TAG: String = SyncWorker::class.java.simpleName

        private const val SYNC_NOTIFICATION_ID = 2
        private const val SYNC_RESULT_NOTIFICATION_ID = 3

        const val END_SYNC_KEY = "END_SYNC"
        const val SYNC_FAILURE_KEY = "SYNC_FAILURE"
        const val SYNC_FAILURE_EXCEPTION_KEY = "SYNC_FAILURE_EXCEPTION"
        const val ACCOUNT_ID_KEY = "ACCOUNT_ID"
        const val FEED_ID_KEY = "FEED_ID"
        const val FOLDER_ID_KEY = "FOLDER_ID"
        const val FEED_NAME_KEY = "FEED_NAME"
        const val FEED_MAX_KEY = "FEED_MAX"
        const val FEED_COUNT_KEY = "FEED_COUNT"
        const val LOCAL_SYNC_ERRORS_KEY = "LOCAL_SYNC_ERRORS"

        suspend fun startNow(context: Context, data: Data, onUpdate: (WorkInfo) -> Unit) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(TAG)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).apply {
                enqueue(request)
                getWorkInfoByIdFlow(request.id)
                    .collect { workInfo -> onUpdate(workInfo) }
            }
        }
    }
}