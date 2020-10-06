package com.readrops.app.repositories;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.localfeed.LocalRSSDataSource;
import com.readrops.api.services.SyncResult;
import com.readrops.api.utils.HttpManager;
import com.readrops.api.utils.LibUtils;
import com.readrops.api.utils.ParseException;
import com.readrops.api.utils.UnknownFormatException;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.account.Account;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import kotlin.Pair;
import okhttp3.Headers;

public class LocalFeedRepository extends ARepository<Void> {

    private static final String TAG = LocalFeedRepository.class.getSimpleName();

    private LocalRSSDataSource dataSource;

    public LocalFeedRepository(@NonNull Context context, @Nullable Account account) {
        super(context, account);

        syncResult = new SyncResult();
        dataSource = new LocalRSSDataSource(HttpManager.getInstance().getOkHttpClient());
    }

    @Override
    protected Void createAPI() {
        return null;
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        return null;
    }

    @Override
    public Observable<Feed> sync(@Nullable List<Feed> feeds) {
        return Observable.create(emitter -> {
            List<Feed> feedList;

            if (feeds == null || feeds.isEmpty()) {
                feedList = database.feedDao().getFeeds(account.getId());
            } else {
                feedList = feeds;
            }

            for (Feed feed : feedList) {
                emitter.onNext(feed);

                try {
                    Headers.Builder headers = new Headers.Builder();
                    if (feed.getEtag() != null) {
                        headers.add(LibUtils.IF_NONE_MATCH_HEADER, feed.getEtag());
                    }
                    if (feed.getLastModified() != null) {
                        headers.add(LibUtils.IF_MODIFIED_HEADER, feed.getLastModified());
                    }

                    Pair<Feed, List<Item>> pair = dataSource.queryRSSResource(feed.getUrl(), headers.build());

                    if (pair != null) {
                        insertNewItems(feed, pair.getSecond());
                    }
                } catch (Exception e) {
                    Log.d(TAG, "sync: " + e.getMessage());
                }
            }

            emitter.onComplete();
        });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        return Single.create(emitter -> {
            List<FeedInsertionResult> insertionResults = new ArrayList<>();

            for (ParsingResult parsingResult : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    Pair<Feed, List<Item>> pair = dataSource.queryRSSResource(parsingResult.getUrl(),
                            null);
                    Feed feed = insertFeed(pair.getFirst(), parsingResult);

                    if (feed != null) {
                        insertionResult.setFeed(feed);
                    }
                } catch (ParseException e) {
                    Log.d(TAG, "addFeeds: " + e.getMessage());
                    insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.PARSE_ERROR);
                } catch (UnknownFormatException e) {
                    Log.d(TAG, "addFeeds: " + e.getMessage());
                    insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.FORMAT_ERROR);
                } catch (NetworkErrorException | IOException e) {
                    Log.d(TAG, "addFeeds: " + e.getMessage());
                    insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.NETWORK_ERROR);
                } catch (Exception e) {
                    Log.d(TAG, "addFeeds: " + e.getMessage());
                    insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);
                } finally {
                    insertionResult.setParsingResult(parsingResult);
                    insertionResults.add(insertionResult);
                }
            }

            emitter.onSuccess(insertionResults);
        });
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private void insertNewItems(Feed feed, List<Item> items) {
        database.feedDao().updateHeaders(feed.getEtag(), feed.getLastModified(), feed.getId());

        Collections.sort(items, Item::compareTo);

        int maxItems = Integer.parseInt(SharedPreferencesManager.readString(context,
                SharedPreferencesManager.SharedPrefKey.ITEMS_TO_PARSE_MAX_NB));
        if (maxItems > 0 && items.size() > maxItems) {
            items = items.subList(items.size() - maxItems, items.size());
        }

        items.stream().forEach(item -> item.setFeedId(feed.getId()));
        insertItems(items, feed);
    }

    private Feed insertFeed(Feed feed, ParsingResult parsingResult) {
        feed.setFolderId(parsingResult.getFolderId());

        if (database.feedDao().feedExists(feed.getUrl(), account.getId())) {
            return null; // feed already inserted
        }

        setFeedColors(feed);
        feed.setAccountId(account.getId());

        // we need empty headers to query the feed just after, without any 304 result
        feed.setEtag(null);
        feed.setLastModified(null);

        feed.setId((int) (database.feedDao().compatInsert(feed)));
        return feed;
    }

    private void insertItems(Collection<Item> items, Feed feed) {
        List<Item> itemsToInsert = new ArrayList<>();

        for (Item dbItem : items) {
            if (!database.itemDao().itemExists(dbItem.getGuid(), feed.getAccountId())) {
                if (dbItem.getDescription() != null) {
                    dbItem.setCleanDescription(Jsoup.parse(dbItem.getDescription()).text());
                }

                if (dbItem.getContent() != null) {
                    dbItem.setReadTime(Utils.readTimeFromString(dbItem.getContent()));
                } else if (dbItem.getDescription() != null) {
                    dbItem.setReadTime(Utils.readTimeFromString(dbItem.getCleanDescription()));
                }

                itemsToInsert.add(dbItem);
            }
        }

        syncResult.getItems().addAll(itemsToInsert);
        database.itemDao().insert(itemsToInsert);
    }
}
