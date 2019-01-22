package com.readrops.readropslibrary;

import com.readrops.readropslibrary.localfeed.AItem;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

import java.util.List;

public interface QueryCallback {

    void onSyncSuccess(List<? extends AItem> items, RSSNetwork.RSSType type, String feedUrl);

    void onSyncFailure(Exception ex);

}
