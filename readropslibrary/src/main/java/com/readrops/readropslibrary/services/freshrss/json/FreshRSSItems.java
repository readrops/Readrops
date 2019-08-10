package com.readrops.readropslibrary.services.freshrss.json;

import java.util.List;

public class FreshRSSItems {

    private String id;

    private Long updated;

    private List<FreshRSSItem> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public List<FreshRSSItem> getItems() {
        return items;
    }

    public void setItems(List<FreshRSSItem> items) {
        this.items = items;
    }
}
