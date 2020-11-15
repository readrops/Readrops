package com.readrops.api.services.nextcloudnews;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.services.SyncResult;
import com.readrops.api.services.SyncType;
import com.readrops.api.services.nextcloudnews.adapters.NextNewsUserAdapter;
import com.readrops.api.utils.ApiUtils;
import com.readrops.api.utils.exceptions.ConflictException;
import com.readrops.api.utils.exceptions.UnknownFormatException;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.pojo.StarItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class NextNewsDataSource {

    private static final String TAG = NextNewsDataSource.class.getSimpleName();

    protected static final int MAX_ITEMS = 5000;

    private NextNewsService api;

    public NextNewsDataSource(NextNewsService api) {
        this.api = api;
    }

    @Nullable
    public String login(String user) throws IOException {
        Response<ResponseBody> response = api.getUser(user).execute();

        if (!response.isSuccessful()) {
            return null;
        }

        String displayName = new NextNewsUserAdapter().fromXml(response.body().byteStream());
        response.body().close();

        return displayName;
    }

    @Nullable
    public List<Feed> createFeed(String url, int folderId) throws IOException, UnknownFormatException {
        Response<List<Feed>> response = api.createFeed(url, folderId).execute();

        if (!response.isSuccessful()) {
            if (response.code() == ApiUtils.HTTP_UNPROCESSABLE)
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
        setReadState(data.getReadItems(), syncResult, StateType.READ);
        setReadState(data.getUnreadItems(), syncResult, StateType.UNREAD);

        setStarState(data.getStarredItems(), syncResult, StateType.STAR);
        setStarState(data.getUnstarredItems(), syncResult, StateType.UNSTAR);
    }

    public List<Folder> createFolder(Folder folder) throws IOException, UnknownFormatException, ConflictException {
        Map<String, String> folderNameMap = new HashMap<>();
        folderNameMap.put("name", folder.getName());

        Response<List<Folder>> foldersResponse = api.createFolder(folderNameMap).execute();

        if (foldersResponse.isSuccessful())
            return foldersResponse.body();
        else if (foldersResponse.code() == ApiUtils.HTTP_UNPROCESSABLE)
            throw new UnknownFormatException();
        else if (foldersResponse.code() == ApiUtils.HTTP_CONFLICT)
            throw new ConflictException();
        else
            return new ArrayList<>();
    }

    public boolean deleteFolder(Folder folder) throws IOException {
        Response response = api.deleteFolder(Integer.parseInt(folder.getRemoteId())).execute();

        if (response.isSuccessful())
            return true;
        else if (response.code() == ApiUtils.HTTP_NOT_FOUND)
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
                case ApiUtils.HTTP_NOT_FOUND:
                    throw new Resources.NotFoundException();
                case ApiUtils.HTTP_UNPROCESSABLE:
                    throw new UnknownFormatException();
                case ApiUtils.HTTP_CONFLICT:
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
        else if (response.code() == ApiUtils.HTTP_NOT_FOUND)
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
        else if (response.code() == ApiUtils.HTTP_NOT_FOUND)
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
        else if (response.code() == ApiUtils.HTTP_NOT_FOUND)
            throw new Resources.NotFoundException();
        else
            return false;
    }

    private void setReadState(List<String> items, SyncResult syncResult, StateType stateType) throws IOException {
        if (!items.isEmpty()) {
            Map<String, List<String>> itemIdsMap = new HashMap<>();
            itemIdsMap.put("items", items);

            Response readItemsResponse = api.setReadState(stateType.name().toLowerCase(),
                    itemIdsMap).execute();

            if (!readItemsResponse.isSuccessful())
                syncResult.setError(true);
        }
    }

    private void setStarState(List<StarItem> items, SyncResult syncResult, StateType stateType) throws IOException {
        if (!items.isEmpty()) {
            List<Map<String, String>> body = new ArrayList<>();
            for (StarItem item : items) {
                Map<String, String> itemBody = new HashMap<>();
                itemBody.put("feedId", item.getFeedRemoteId());
                itemBody.put("guidHash", item.getGuidHash());

                body.add(itemBody);
            }

            Response response = api.setStarState(stateType.name().toLowerCase(),
                    Collections.singletonMap("items", body)).execute();
            if (!response.isSuccessful()) {
                syncResult.setError(true);
            }
        }
    }

    public enum StateType {
        READ,
        UNREAD,
        STAR,
        UNSTAR
    }
}
