package com.readrops.app.database;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQueryBuilder;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readrops.app.activities.MainActivity;
import com.readrops.app.viewmodels.MainViewModel;

public class ItemsListQueryBuilder {

    private String [] columns = {"Item.id", "title", "clean_description", "image_link", "pub_date", "read",
            "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time",
            "Feed.id as feedId", "Folder.id as folder_id", "Folder.name as folder_name"};

    private String SELECT_ALL_JOIN = "Item Inner Join Feed, Folder on Item.feed_id = Feed.id And Folder.id = Feed.folder_id";

    private String ORDER_BY_ASC = "Item.id DESC";

    private String ORDER_BY_DESC = "pub_date ASC";

    private SupportSQLiteQueryBuilder queryBuilder;

    private boolean showReaditems;
    private int filterFeedId;
    private MainViewModel.FilterType filterType;
    private MainActivity.ListSortType sortType;

    public ItemsListQueryBuilder() {
        queryBuilder =  SupportSQLiteQueryBuilder.builder(SELECT_ALL_JOIN);
    }

    private String buildWhereClause() {
        StringBuilder stringBuilder = new StringBuilder(50);

        if (!showReaditems)
            stringBuilder.append("read = 0 And ");

        switch (filterType) {
            case FEED_FILTER:
                stringBuilder.append("feed_id = " + filterFeedId + " And read_it_later = 0");
                break;
            case READ_IT_LATER_FILTER:
                stringBuilder.append("read_it_later = 1");
                break;
            case NO_FILTER:
                stringBuilder.append("read_it_later = 0");
                break;
            default:
                stringBuilder.append("read_it_later = 0");
                break;
        }

        return stringBuilder.toString();
    }


    public SupportSQLiteQuery getQuery() {
        queryBuilder.columns(columns);

        queryBuilder.selection(buildWhereClause(), new String[0]);

        if (sortType == MainActivity.ListSortType.NEWEST_TO_OLDEST)
            queryBuilder.orderBy(ORDER_BY_ASC);
        else
            queryBuilder.orderBy(ORDER_BY_DESC);


       return queryBuilder.create();
    }

    public boolean showReaditems() {
        return showReaditems;
    }

    public void setShowReaditems(boolean showReaditems) {
        this.showReaditems = showReaditems;
    }

    public int getFilterFeedId() {
        return filterFeedId;
    }

    public void setFilterFeedId(int filterFeedId) {
        this.filterFeedId = filterFeedId;
    }

    public MainViewModel.FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(MainViewModel.FilterType filterType) {
        this.filterType = filterType;
    }

    public MainActivity.ListSortType getSortType() {
        return sortType;
    }

    public void setSortType(MainActivity.ListSortType sortType) {
        this.sortType = sortType;
    }
}
