package com.readrops.app.sync

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import coil3.imageLoader
import com.readrops.api.services.Credentials
import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.api.utils.AuthInterceptor
import com.readrops.app.R
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.repositories.SyncResult
import com.readrops.app.sync.SyncWorker.Companion.SYNC_NOTIFICATION_ID
import com.readrops.app.util.FeedColors
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

data class SyncInputData(
    val accountId: Int,
    val feedId: Int,
    val folderId: Int
)

class Synchronizer(
    private val notificationManager: NotificationManagerCompat,
    private val database: Database,
    private val context: Context,
    private val encryptedPreferences: SharedPreferences,
) : KoinComponent {

    suspend fun synchronizeAccounts(
        notificationBuilder: Builder,
        inputData: SyncInputData,
        onUpdate: suspend (feed: Feed, feedMax: Int, feedCount: Int) -> Unit
    ): Pair<Map<Account, SyncResult>, ErrorResult> {
        val syncResults = mutableMapOf<Account, SyncResult>()
        val errorResult = hashMapOf<Feed, Exception>()

        val accounts = if (inputData.accountId == -1) {
            database.accountDao().selectAllAccounts().first()
        } else {
            listOf(database.accountDao().select(inputData.accountId))
        }

        for (account in accounts) {
            if (!account.isLocal) {
                account.login = encryptedPreferences.getString(account.loginKey, null)
                account.password = encryptedPreferences.getString(account.passwordKey, null)
            }

            val repository = get<BaseRepository> { parametersOf(account) }

            notificationBuilder.setContentTitle(
                context.resources.getString(
                    R.string.updating_account,
                    account.name
                )
            )

            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(SYNC_NOTIFICATION_ID, notificationBuilder.build())
            }

            if (account.isLocal) {
                val result = refreshLocalAccount(
                    repository = repository,
                    account = account,
                    notificationBuilder = notificationBuilder,
                    inputData = inputData,
                    onUpdate = onUpdate
                )

                syncResults[account] = result.first
                errorResult.putAll(result.second)
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

        return syncResults to errorResult
    }

    private suspend fun refreshLocalAccount(
        repository: BaseRepository,
        account: Account,
        notificationBuilder: Builder,
        inputData: SyncInputData,
        onUpdate: suspend (feed: Feed, feedMax: Int, feedCount: Int) -> Unit
    ): Pair<SyncResult, ErrorResult> {
        val feedId = inputData.feedId
        val folderId = inputData.folderId

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

                onUpdate(feed, feedMax, feedCount)
            }
        )

        if (result.second.isNotEmpty()) {
            Log.e(TAG, "refreshing local account ${account.name}: ${result.second.size} errors")
        }

        return result
    }

    private suspend fun fetchFeedColors(
        syncResult: SyncResult,
        notificationBuilder: Builder
    ) = with(syncResult) {
        notificationBuilder.setContentTitle(context.getString(R.string.get_feeds_colors))

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

        val diskCache = context.imageLoader.diskCache!!

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

    companion object {
        private val TAG = Synchronizer::class.java.simpleName
    }
}