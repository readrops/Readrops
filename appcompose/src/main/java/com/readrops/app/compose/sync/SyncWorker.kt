package com.readrops.app.compose.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.readrops.api.services.SyncResult
import com.readrops.app.compose.R
import com.readrops.app.compose.ReadropsApp
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.util.FeedColors
import com.readrops.db.Database
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
        var result = Result.success(workDataOf(END_SYNC_KEY to true))

        try {
            require(notificationManager.areNotificationsEnabled())

            val notificationBuilder =
                NotificationCompat.Builder(applicationContext, ReadropsApp.SYNC_CHANNEL_ID)
                    .setProgress(0, 0, true)
                    .setSmallIcon(R.drawable.ic_notifications) // TODO use better icon
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT) // for Android 7.1 and earlier
                    .setStyle(NotificationCompat.BigTextStyle())
                    .setOnlyAlertOnce(true)

            val accounts = database.newAccountDao().selectAllAccounts().first()

            for (account in accounts) {
                account.login = sharedPreferences.getString(account.loginKey, null)
                account.password = sharedPreferences.getString(account.passwordKey, null)

                val repository = get<BaseRepository> { parametersOf(account) }

                notificationBuilder.setContentTitle(
                    applicationContext.resources.getString(
                        R.string.updating_account,
                        account.accountName
                    )
                )
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

                val syncResult = repository.synchronize()
                fetchFeedColors(syncResult, notificationBuilder)
            }
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            result = Result.failure(workDataOf(SYNC_FAILURE_KEY to true))
        } finally {
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
        }

        return result
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFeedColors(syncResult: SyncResult, notificationBuilder: NotificationCompat.Builder) {
        notificationBuilder.setContentTitle(applicationContext.getString(R.string.get_feeds_colors))

        for ((index, feedId) in syncResult.newFeedIds.withIndex()) {
            val feedName = syncResult.feeds.first { it.id == feedId.toInt() }.name

            notificationBuilder.setContentText(feedName)
                .setProgress(syncResult.newFeedIds.size, index + 1, false)
            notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())

            val color = try {
                FeedColors.getFeedColor(syncResult.feeds.first { it.id == feedId.toInt() }.iconUrl!!)
            } catch (e: Exception) {
                Log.e(TAG, "${e.message}")
                0
            }

            database.newFeedDao().updateFeedColor(feedId.toInt(), color)
        }
    }

    companion object {
        private val TAG: String = SyncWorker::class.java.simpleName

        private const val SYNC_NOTIFICATION_ID = 2
        private const val SYNC_RESULT_NOTIFICATION_ID = 3

        const val END_SYNC_KEY = "END_SYNC"
        const val SYNC_FAILURE_KEY = "SYNC_FAILURE"

        suspend fun startNow(context: Context, onUpdate: (WorkInfo) -> Unit) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).apply {
                enqueue(request)
                getWorkInfoByIdFlow(request.id)
                    .collect { workInfo -> onUpdate(workInfo) }
            }
        }
    }
}