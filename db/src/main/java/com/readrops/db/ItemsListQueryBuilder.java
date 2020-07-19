package com.readrops.readropsdb;

import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import com.readrops.readropsdb.filters.FilterType;
import com.readrops.readropsdb.filters.ListSortType;

public class ItemsListQueryBuilder {

    private String[] columns = {"Item.id", "title", "clean_description", "image_link", "pub_date", "read",
            "read_changed", "read_it_later", "Feed.name", "text_color", "background_color", "icon_url", "read_time", "Item.remoteId",
            "Feed.id as feedId", "Feed.account_id", "Folder.id as folder_id", "Folder.name as folder_name"};

    private String SELECT_ALL_JOIN = "Item INNER JOIN Feed on Item.feed_id = Feed.id " +
            "LEFT JOIN Folder on Feed.folder_id = Folder.id";

    private String ORDER_BY_ASC = "Item.id DESC";

    private String ORDER_BY_DESC = "pub_date ASC";

    private SupportSQLiteQueryBuilder queryBuilder;

    private boolean showReadItems;
    private int filterFeedId;
    private int accountId;

    private FilterType filterType;
    private ListSortType sortType;

    public ItemsListQueryBuilder() {
        queryBuilder = SupportSQLiteQueryBuilder.builder(SELECT_ALL_JOIN);
    }

    private String buildWhereClause() {
        StringBuilder stringBuilder = new StringBuilder(80);

        stringBuilder.append("Feed.account_id = ").append(accountId).append(" And ");

        if (!showReadItems)
            stringBuilder.append("read = 0 And ");

        switch (filterType) {
            case FEED_FILTER:
                stringBuilder.append("feed_id = ").append(filterFeedId).append(" And read_it_later = 0");
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

        if (sortType == ListSortType.NEWEST_TO_OLDEST)
            queryBuilder.orderBy(ORDER_BY_ASC);
        else
            queryBuilder.orderBy(ORDER_BY_DESC);

        return queryBuilder.create();
    }

    public boolean showReadItems() {
        return showReadItems;
    }

    public void setShowReadItems(boolean showReadItems) {
        this.showReadItems = showReadItems;
    }

    public int getFilterFeedId() {
        return filterFeedId;
    }

    public void setFilterFeedId(int filterFeedId) {
        this.filterFeedId = filterFeedId;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public ListSortType getSortType() {
        return sortType;
    }

    public void setSortType(ListSortType sortType) {
        this.sortType = sortType;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
}
