package com.readrops.readropslibrary.localfeed;

import android.util.Log;

import com.google.gson.Gson;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.Utils.Utils;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RSSNetwork {

    public static final String TAG = RSSNetwork.class.getName();

    /**
     * Request the url given in parameter.
     * This method is synchronous, <b>it has to be called from another thread than the main one</b>.
     * @param url url to request
     * @param callback result callback if success or error
     * @throws Exception
     */
    public void request(String url, final QueryCallback callback) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();

        Log.d(TAG, "starting request");
        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();

        if (response.isSuccessful())
            parseFeed(response.body().byteStream(), getRSSType(response.header("content-type")), callback, url);
        else
            callback.onSyncFailure(new Exception("Error " + response.code() + " when requesting url " + url));
    }

    private void parseFeed(InputStream stream, RSSType type, QueryCallback callback, String url) throws Exception {
        //String xml = Utils.inputStreamToString(stream);
        Serializer serializer = new Persister();

        switch (type) {
            case RSS_2:
                RSSFeed rssFeed = serializer.read(RSSFeed.class, stream);
                callback.onSyncSuccess(rssFeed.getChannel().getItems(), type, url);
                break;
            case RSS_ATOM:
                ATOMFeed atomFeed = serializer.read(ATOMFeed.class, stream);
                callback.onSyncSuccess(atomFeed.getEntries(), type, url);
                break;
            case RSS_JSON:
                Gson gson = new Gson();
                JSONFeed feed = gson.fromJson(Utils.inputStreamToString(stream), JSONFeed.class);
                callback.onSyncSuccess(feed.getItems(), type, url);
                break;
        }
    }

    private RSSType getRSSType(String contentType) {
        if (contentType.contains(RSSType.RSS_2.value))
            return  RSSType.RSS_2;
        else if (contentType.contains(RSSType.RSS_ATOM.value))
            return RSSType.RSS_ATOM;
        else
            return RSSType.RSS_JSON;
    }

    public enum RSSType {
        RSS_2("rss"),
        RSS_ATOM("atom"),
        RSS_JSON("json");

        private String value;

        RSSType(String value) {
            this.value = value;
        }
    }


}
