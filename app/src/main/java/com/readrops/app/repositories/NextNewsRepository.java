package com.readrops.app.repositories;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropsdb.entities.account.Account;
import com.readrops.readropslibrary.services.Credentials;
import com.readrops.readropslibrary.services.SyncResult;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsAPI;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsSyncData;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;
import com.readrops.readropslibrary.utils.UnknownFormatException;

import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class NextNewsRepository extends ARepository<NextNewsAPI> {

    private static final String TAG = NextNewsRepository.class.getSimpleName();

    public NextNewsRepository(@NonNull Context context, @Nullable Account account) {
        super(context, account);
    }

    @Override
    protected NextNewsAPI createAPI() {
        if (account != null)
            return new NextNewsAPI(Credentials.toCredentials(account));

        return null;
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        return Single.<NextNewsUser>create(emitter -> {
            if (api == null)
                api = new NextNewsAPI(Credentials.toCredentials(account));
            else
                api.setCredentials(Credentials.toCredentials(account));

            NextNewsUser user = api.login();

            emitter.onSuccess(user);
        }).flatMap(user -> {
            if (user != null) {
                account.setDisplayedName(user.getDisplayName());
                account.setCurrentAccount(true);

                if (insert) {
                    return database.accountDao().insert(account)
                            .flatMap(id -> {
                                account.setId(id.intValue());
                                return Single.just(true);
                            });
                }

                return Single.just(true);
            } else
                return Single.just(false);
        });
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds) {
        return Observable.create(emitter -> {
            try {
                long lastModified = LocalDateTime.now().toDateTime().getMillis();
                SyncType syncType;

                if (account.getLastModified() != 0)
                    syncType = SyncType.CLASSIC_SYNC;
                else
                    syncType = SyncType.INITIAL_SYNC;

                NextNewsSyncData syncData = new NextNewsSyncData();

                if (syncType == SyncType.CLASSIC_SYNC) {
                    syncData.setLastModified(account.getLastModified() / 1000L);
                    syncData.setReadItems(database.itemDao().getReadChanges(account.getId()));
                    syncData.setUnreadItems(database.itemDao().getUnreadChanges(account.getId()));
                }

                TimingLogger timings = new TimingLogger(TAG, "nextcloud news " + syncType.name().toLowerCase());
                SyncResult syncResult = api.sync(syncType, syncData);
                timings.addSplit("server queries");

                if (!syncResult.isError()) {

                    insertFolders(syncResult.getFolders());
                    timings.addSplit("insert folders");

                    insertFeeds(syncResult.getFeeds());
                    timings.addSplit("insert feeds");

                    insertItems(syncResult.getItems(), syncType == SyncType.INITIAL_SYNC);
                    timings.addSplit("insert items");
                    timings.dumpToLog();

                    account.setLastModified(lastModified);
                    database.accountDao().updateLastModified(account.getId(), lastModified);
                    database.itemDao().resetReadChanges(account.getId());

                    emitter.onComplete();
                } else
                    emitter.onError(new Throwable());

            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
        });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        return Single.create(emitter -> {
            List<FeedInsertionResult> feedInsertionResults = new ArrayList<>();

            for (ParsingResult result : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    List<Feed> nextNewsFeeds = api.createFeed(result.getUrl(), 0);

                    if (nextNewsFeeds != null) {
                        List<Feed> newFeeds = insertFeeds(nextNewsFeeds);
                        // there is always only one object in the list, see nextcloud news api doc
                        insertionResult.setFeed(newFeeds.get(0));
                    } else
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);

                    insertionResult.setParsingResult(result);
                } catch (Exception e) {
                    if (e instanceof IOException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.NETWORK_ERROR);
                    else if (e instanceof UnknownFormatException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.FORMAT_ERROR);
                    else if (e instanceof SQLiteConstraintException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.DB_ERROR);
                    else
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);
                }

                feedInsertionResults.add(insertionResult);
            }

            emitter.onSuccess(feedInsertionResults);
        });
    }

    @Override
    public Completable updateFeed(Feed feed) {
        return Completable.create(emitter -> {
            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());

            if (folder != null)
                feed.setRemoteFolderId(folder.getRemoteId());
            else
                feed.setRemoteFolderId(String.valueOf(0)); // 0 for no folder

            try {
                if (api.renameFeed(feed) && api.changeFeedFolder(feed)) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error when updating feed"));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).andThen(super.updateFeed(feed));
    }

    @Override
    public Completable deleteFeed(Feed feed) {
        return Completable.create(emitter -> {
            try {
                if (api.deleteFeed(Integer.parseInt(feed.getRemoteId()))) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.deleteFeed(feed));
    }

    @Override
    public Single<Long> addFolder(Folder folder) {
        return Single.<Folder>create(emitter -> {
            try {
                List<Folder> folders = api.createFolder(folder);

                if (folders != null) {
                    Folder nextNewsFolder = folders.get(0); // always only one item returned by the server, see doc
                    folder.setRemoteId(nextNewsFolder.getRemoteId());

                    emitter.onSuccess(folder);
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).flatMap(folder1 -> database.folderDao().insert(folder));
    }

    @Override
    public Completable updateFolder(Folder folder) {
        return Completable.create(emitter -> {
            try {
                if (api.renameFolder(folder)) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.updateFolder(folder));
    }

    @Override
    public Completable deleteFolder(Folder folder) {
        return Completable.create(emitter -> {
            try {
                if (api.deleteFolder(folder)) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.deleteFolder(folder));
    }

    private List<Feed> insertFeeds(List<Feed> nextNewsFeeds) {
        for (Feed nextNewsFeed : nextNewsFeeds) {
            nextNewsFeed.setAccountId(account.getId());
        }

        List<Long> insertedFeedsIds = database.feedDao().feedsUpsert(nextNewsFeeds, account);

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFeedsColors(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<Folder> nextNewsFolders) {
        for (Folder folder : nextNewsFolders) {
            folder.setAccountId(account.getId());
        }

        database.folderDao().foldersUpsert(nextNewsFolders, account);
    }

    private void insertItems(List<Item> items, boolean initialSync) {
        List<Item> itemsToInsert = new ArrayList<>();

        for (Item item : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(item.getFeedRemoteId(), account.getId());

            //if the item already exists, update only its read state
            if (!initialSync && feedId > 0 && database.itemDao().remoteItemExists(String.valueOf(item.getRemoteId()), feedId)) {
                database.itemDao().setReadState(item.getRemoteId(), item.isRead());
                continue;
            }

            item.setFeedId(feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            itemsToInsert.add(item);
        }

        if (!itemsToInsert.isEmpty()) {
            Collections.sort(itemsToInsert, Item::compareTo);
            database.itemDao().insert(itemsToInsert);
        }
    }
}
