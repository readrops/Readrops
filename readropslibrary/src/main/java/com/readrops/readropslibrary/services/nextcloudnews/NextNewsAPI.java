package com.readrops.readropslibrary.services.nextcloudnews;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropslibrary.services.API;
import com.readrops.readropslibrary.services.Credentials;
import com.readrops.readropslibrary.services.SyncResult;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.nextcloudnews.adapters.NextNewsFeedsAdapter;
import com.readrops.readropslibrary.services.nextcloudnews.adapters.NextNewsFoldersAdapter;
import com.readrops.readropslibrary.services.nextcloudnews.adapters.NextNewsItemsAdapter;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;
import com.readrops.readropslibrary.utils.ConflictException;
import com.readrops.readropslibrary.utils.LibUtils;
import com.readrops.readropslibrary.utils.UnknownFormatException;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class NextNewsAPI extends API<NextNewsService> {

    private static final String TAG = NextNewsAPI.class.getSimpleName();

    public NextNewsAPI(Credentials credentials) {
        super(credentials, NextNewsService.class, NextNewsService.END_POINT);
    }

    @Override
    protected Moshi buildMoshi() {
        return new Moshi.Builder()
                .add(new NextNewsFeedsAdapter())
                .add(new NextNewsFoldersAdapter())
                .add(Types.newParameterizedType(List.class, Item.class), new NextNewsItemsAdapter())
                .build();
    }

    @Nullable
    public NextNewsUser login() throws IOException {
        Response<NextNewsUser> response = api.getUser().execute();

        if (!response.isSuccessful())
            return null;

        return response.body();
    }

    @Nullable
    public List<Feed> createFeed(String url, int folderId) throws IOException, UnknownFormatException {
        Response<List<Feed>> response = api.createFeed(url, folderId).execute();

        if (!response.isSuccessful()) {
            if (response.code() == LibUtils.HTTP_UNPROCESSABLE)
                throw new UnknownFormatException();
            else
                return null;
        }

        return response.body();
    }

    public SyncResult sync(@NonNull SyncType syncType, @Nullable NextNewsSyncData data) throws IOException {
        SyncResult syncResult = new SyncResult();
        switch (syncType) {
            case INITIAL_SYNC:
                initialSync(syncResult);
                break;
            case CLASSIC_SYNC:
                if (data == null)
                    throw new NullPointerException("NextNewsSyncData can't be null");

                classicSync(syncResult, data);
                break;
        }

        return syncResult;
    }

    private void initialSync(SyncResult syncResult) throws IOException {
        getFeedsAndFolders(syncResult);

        Response<List<Item>> itemsResponse = api.getItems(3, false, MAX_ITEMS).execute();
        List<Item> itemList = itemsResponse.body();

        if (!itemsResponse.isSuccessful())
            syncResult.setError(true);

        if (itemList != null)
            syncResult.setItems(itemList);
    }

    private void classicSync(SyncResult syncResult, NextNewsSyncData data) throws IOException {
        putModifiedItems(data, syncResult);
        getFeedsAndFolders(syncResult);

        Response<List<Item>> itemsResponse = api.getNewItems(data.getLastModified(), 3).execute();
        List<Item> itemList = itemsResponse.body();

        if (!itemsResponse.isSuccessful())
            syncResult.setError(true);

        if (itemList != null)
            syncResult.setItems(itemList);
    }

    private void getFeedsAndFolders(SyncResult syncResult) throws IOException {
        Response<List<Feed>> feedResponse = api.getFeeds().execute();
        List<Feed> feedList = feedResponse.body();

        if (!feedResponse.isSuccessful())
            syncResult.setError(true);

        Response<List<Folder>> folderResponse = api.getFolders().execute();
        List<Folder> folderList = folderResponse.body();

        if (!folderResponse.isSuccessful())
            syncResult.setError(true);

        if (folderList != null)
            syncResult.setFolders(folderList);

        if (feedList != null)
            syncResult.setFeeds(feedList);

    }

    private void putModifiedItems(NextNewsSyncData data, SyncResult syncResult) throws IOException {
        if (!data.getReadItems().isEmpty()) {
            Map<String, List<String>> itemIdsMap = new HashMap<>();
            itemIdsMap.put("items", data.getReadItems());

            Response readItemsResponse = api.setArticlesState(StateType.READ.name().toLowerCase(),
                    itemIdsMap).execute();

            if (!readItemsResponse.isSuccessful())
                syncResult.setError(true);
        }

        if (!data.getUnreadItems().isEmpty()) {
            Map<String, List<String>> itemIdsMap = new HashMap<>();
            itemIdsMap.put("items", data.getUnreadItems());

            Response unreadItemsResponse = api.setArticlesState(StateType.UNREAD.toString().toLowerCase(),
                    itemIdsMap).execute();

            if (!unreadItemsResponse.isSuccessful())
                syncResult.setError(true);
        }
    }

    public List<Folder> createFolder(Folder folder) throws IOException, UnknownFormatException, ConflictException {
        Map<String, String> folderNameMap = new HashMap<>();
        folderNameMap.put("name", folder.getName());

        Response<List<Folder>> foldersResponse = api.createFolder(folderNameMap).execute();

        if (foldersResponse.isSuccessful())
            return foldersResponse.body();
        else if (foldersResponse.code() == LibUtils.HTTP_UNPROCESSABLE)
            throw new UnknownFormatException();
        else if (foldersResponse.code() == LibUtils.HTTP_CONFLICT)
            throw new ConflictException();
        else
            return new ArrayList<>();
    }

    public boolean deleteFolder(Folder folder) throws IOException {
        Response response = api.deleteFolder(Integer.parseInt(folder.getRemoteId())).execute();

        if (response.isSuccessful())
            return true;
        else if (response.code() == LibUtils.HTTP_NOT_FOUND)
            throw new Resources.NotFoundException();
        else
            return false;
    }

    public boolean renameFolder(Folder folder) throws IOException, UnknownFormatException, ConflictException {
        Map<String, String> folderNameMap = new HashMap<>();
        folderNameMap.put("name", folder.getName());

        Response response = api.renameFolder(Integer.parseInt(folder.getRemoteId()), folderNameMap).execute();

        if (response.isSuccessful())
            return true;
        else {
            switch (response.code()) {
                case LibUtils.HTTP_NOT_FOUND:
                    throw new Resources.NotFoundException();
                case LibUtils.HTTP_UNPROCESSABLE:
                    throw new UnknownFormatException();
                case LibUtils.HTTP_CONFLICT:
                    throw new ConflictException();
                default:
                    return false;
            }
        }
    }

    public boolean deleteFeed(int feedId) throws IOException {
        Response response = api.deleteFeed(feedId).execute();

        if (response.isSuccessful())
            return true;
        else if (response.code() == LibUtils.HTTP_NOT_FOUND)
            throw new Resources.NotFoundException();
        else
            return false;
    }

    public boolean changeFeedFolder(Feed feed) throws IOException {
        Map<String, Integer> folderIdMap = new HashMap<>();
        folderIdMap.put("folderId", Integer.parseInt(feed.getRemoteFolderId()));

        Response response = api.changeFeedFolder(Integer.parseInt(feed.getRemoteId()), folderIdMap).execute();

        if (response.isSuccessful())
            return true;
        else if (response.code() == LibUtils.HTTP_NOT_FOUND)
            throw new Resources.NotFoundException();
        else
            return false;
    }

    public boolean renameFeed(Feed feed) throws IOException {
        Map<String, String> feedTitleMap = new HashMap<>();
        feedTitleMap.put("feedTitle", feed.getName());

        Response response = api.renameFeed(Integer.parseInt(feed.getRemoteId()), feedTitleMap).execute();

        if (response.isSuccessful())
            return true;
        else if (response.code() == LibUtils.HTTP_NOT_FOUND)
            throw new Resources.NotFoundException();
        else
            return false;
    }

    public enum StateType {
        READ,
        UNREAD,
        STARRED,
        UNSTARRED
    }
}
