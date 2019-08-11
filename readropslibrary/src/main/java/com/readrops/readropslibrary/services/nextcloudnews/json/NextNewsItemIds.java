package com.readrops.readropslibrary.services.nextcloudnews.json;

import java.util.List;

public class NextNewsItemIds {

    private List<String> items;

    public NextNewsItemIds(List<String> items) {
        this.items = items;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
