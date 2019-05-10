package com.readrops.readropslibrary.services.nextcloudnews.json;

import java.util.List;

public class NextNewsItems {

    private List<NextNewsItem> items;

    public NextNewsItems(List<NextNewsItem> items) {
        this.items = items;
    }

    public List<NextNewsItem> getItems() {
        return items;
    }
}
