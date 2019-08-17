package com.readrops.app.repositories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.FeedMatcher;
import com.readrops.app.utils.ItemMatcher;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.freshrss.FreshRSSAPI;
import com.readrops.readropslibrary.services.freshrss.FreshRSSCredentials;
import com.readrops.readropslibrary.services.freshrss.FreshRSSService;
import com.readrops.readropslibrary.services.freshrss.FreshRSSSyncData;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFolder;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class FreshRSSRepository extends ARepository {

    public FreshRSSRepository(@NonNull Application application, @Nullable Account account) {
        super(application, account);
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        FreshRSSAPI api = new FreshRSSAPI(new FreshRSSCredentials(null, account.getUrl()));

        return api.login(account.getLogin(), account.getPassword())
                .flatMap(token -> {
                    account.setToken(token);
                    api.buildAPI(new FreshRSSCredentials(token, account.getUrl()), FreshRSSService.class, FreshRSSService.END_POINT);

                    return api.getUserInfo();
                })
                .flatMap(userInfo -> {
                    account.setDisplayedName(userInfo.getUserName());

                    if (insert)
                        account.setId((int) database.accountDao().insert(account));

                    return Single.just(true);
                });
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds) {
        FreshRSSAPI api = new FreshRSSAPI(account.toCredentials());

        FreshRSSSyncData syncData = new FreshRSSSyncData();
        SyncType syncType;

        if (account.getLastModified() != 0) {
            syncType = SyncType.CLASSIC_SYNC;
            syncData.setLastModified(account.getLastModified());
        } else
            syncType = SyncType.INITIAL_SYNC;

        return api.sync(syncType, syncData)
                .flatMapObservable(syncResult -> {
                    insertFolders(syncResult.getFolders(), account);
                    insertFeeds(syncResult.getFeeds(), account);
                    insertItems(syncResult.getItems(), account, syncType == SyncType.INITIAL_SYNC);

                    account.setLastModified(syncResult.getLastUpdated());
                    database.accountDao().updateLastModified(account.getId(), syncResult.getLastUpdated());

                    return Observable.empty();
                });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        FreshRSSAPI api = new FreshRSSAPI(account.toCredentials());

        List<Completable> completableList = new ArrayList<>();

        for (ParsingResult result : results) {
            completableList.add(api.createFeed(account.getWriteToken(), result.getUrl()));
        }

        // TODO : see how to handle exceptions/errors like the others repositories
        return Completable.concat(completableList)
                .andThen(Single.just(new ArrayList<>()));
    }

    @Override
    public Completable updateFeed(Feed feed) {
        return null;
    }

    @Override
    public Completable deleteFeed(Feed feed) {
        FreshRSSAPI api = new FreshRSSAPI(account.toCredentials());

        return api.deleteFeed(account.getWriteToken(), feed.getUrl())
                .andThen(super.deleteFeed(feed));
    }

    @Override
    public Completable addFolder(Folder folder) {
        return null;
    }

    @Override
    public Completable updateFolder(Folder folder) {
        return null;
    }

    @Override
    public Completable deleteFolder(Folder folder) {
        return null;
    }

    @Override
    public Completable setItemReadState(Item item, boolean read) {
        FreshRSSAPI api = new FreshRSSAPI(account.toCredentials());

        if (account.getWriteToken() == null) {
            return api.getWriteToken()
                    .flatMapCompletable(writeToken -> {
                        database.accountDao().updateWriteToken(account.getId(), writeToken);

                        return api.markItemReadUnread(read, item.getRemoteId(), writeToken)
                                .concatWith(super.setItemReadState(item, read));
                    });
        } else {
            return api.markItemReadUnread(read, item.getRemoteId(), account.getWriteToken())
                    .andThen(super.setItemReadState(item, read));
        }
    }

    private List<Feed> insertFeeds(List<FreshRSSFeed> freshRSSFeeds, Account account) {
        List<Feed> feeds = new ArrayList<>();

        for (FreshRSSFeed freshRSSFeed : freshRSSFeeds) {
            feeds.add(FeedMatcher.freshRSSFeedToFeed(freshRSSFeed, account));
        }

        List<Long> insertedFeedsIds = database.feedDao().feedsUpsert(feeds, account);

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFaviconUtils(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<FreshRSSFolder> freshRSSFolders, Account account) {
        List<Folder> folders = new ArrayList<>();

        for (FreshRSSFolder freshRSSFolder : freshRSSFolders) {
            if (freshRSSFolder.getType() != null && freshRSSFolder.getType().equals("folder")) {
                List<Object> tokens = Collections.list(new StringTokenizer(freshRSSFolder.getId(), "/"));

                Folder folder = new Folder((String) tokens.get(tokens.size() - 1));
                folder.setRemoteId(freshRSSFolder.getId());
                folder.setAccountId(account.getId());

                folders.add(folder);
            }
        }

        database.folderDao().foldersUpsert(folders, account);
    }

    private void insertItems(List<FreshRSSItem> items, Account account, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (FreshRSSItem freshRSSItem : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(String.valueOf(freshRSSItem.getOrigin().getStreamId()), account.getId());

            if (!initialSync && feedId > 0) {
                if (database.itemDao().remoteItemExists(freshRSSItem.getId(), feedId))
                    break;
            }

            Item item = ItemMatcher.freshRSSItemtoItem(freshRSSItem, feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            newItems.add(item);
        }

        Collections.sort(newItems, Item::compareTo);
        database.itemDao().insert(newItems);
    }
}
