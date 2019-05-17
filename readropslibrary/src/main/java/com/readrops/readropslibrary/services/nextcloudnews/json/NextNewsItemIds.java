package com.readrops.readropslibrary.services.nextcloudnews.json;

import java.util.List;

public class NextNewsItemIds {

    private List<Integer> items;

    public NextNewsItemIds(List<Integer> items) {
        this.items = items;
    }

    public List<Integer> getItems() {
        return items;
    }

    public void setItems(List<Integer> items) {
        this.items = items;
    }
}
