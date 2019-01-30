package com.readrops.readropslibrary;

import com.readrops.readropslibrary.localfeed.AFeed;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

public interface QueryCallback {

    void onSyncSuccess(AFeed feed, RSSNetwork.RSSType type);

    void onSyncFailure(Exception e);

}
