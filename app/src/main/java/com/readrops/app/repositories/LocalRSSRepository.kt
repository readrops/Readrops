package com.readrops.app.repositories

import android.util.Log
import com.readrops.api.localfeed.LocalRSSDataSource
import com.readrops.api.utils.ApiUtils
import com.readrops.api.utils.HtmlParser
import com.readrops.app.util.FeedColors
import com.readrops.app.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class LocalRSSRepository(
    private val dataSource: LocalRSSDataSource,
    database: Database,
    account: Account
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) { /* useless here */ }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: suspend (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> {
        val errors = hashMapOf<Feed, Exception>()
        val newItems = mutableListOf<Item>()

        val feeds = selectedFeeds.ifEmpty {
            database.feedDao().selectFeeds(account.id)
        }

        for (feed in feeds) {
            onUpdate(feed)

            val headers = Headers.Builder()
            if (feed.etag != null) {
                headers[ApiUtils.IF_NONE_MATCH_HEADER] = feed.etag!!
            }
            if (feed.lastModified != null) {
                headers[ApiUtils.IF_MODIFIED_HEADER] = feed.lastModified!!
            }

            try {
                val pair = dataSource.queryRSSResource(feed.url!!, headers.build())

                pair?.let { newItems.addAll(insertNewItems(it.second, feed)) }
            } catch (e: Exception) {
                errors[feed] = e
            }
        }

        return Pair(SyncResult(items = newItems), errors)
    }

    override suspend fun synchronize(): SyncResult =
        throw NotImplementedError("This method can't be called here")


    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult = withContext(Dispatchers.IO) {
        val errors = hashMapOf<Feed, Exception>()

        for (newFeed in newFeeds) {
            onUpdate(newFeed)

            try {
                val result = dataSource.queryRSSResource(newFeed.url!!, null)!!
                insertFeed(result.first.also { it.folderId = newFeed.folderId })
            } catch (e: Exception) {
                errors[newFeed] = e
            }
        }

        return@withContext errors
    }

    private suspend fun insertNewItems(items: List<Item>, feed: Feed): List<Item> {
        val newItems = mutableListOf<Item>()

        for (item in items) {
            if (!database.itemDao().itemExists(item.remoteId!!, feed.accountId)) {
                if (item.description != null) {
                    item.cleanDescription = Jsoup.parse(item.description).text()
                }

                if (item.content != null) {
                    item.readTime = Utils.readTimeFromString(item.content!!)
                } else if (item.description != null) {
                    item.readTime = Utils.readTimeFromString(item.cleanDescription!!)
                }

                item.feedId = feed.id
                newItems += item
            }
        }

        // sort by date
        newItems.sort()
        database.itemDao().insert(newItems)
            .zip(newItems)
            .forEach { (id, item) -> item.id = id.toInt() }

        return newItems
    }

    private suspend fun insertFeed(feed: Feed): Feed {
        // TODO better handle this case
        require(!database.feedDao().feedExists(feed.url!!, account.id)) {
            "Feed already exists for account ${account.name}"
        }

        return feed.apply {
            accountId = account.id
            // we need empty headers to query the feed just after, without any 304 result
            etag = null
            lastModified = null

            try {
                iconUrl = HtmlParser.getFaviconLink(siteUrl!!, get()).also { feedUrl ->
                    feedUrl?.let { color = FeedColors.getFeedColor(it) }
                }
            } catch (e: Exception) {
                Log.d("LocalRSSRepository", "insertFeed: ${e.message}")
            }

            id = database.feedDao().insert(this).toInt()
        }
    }
}