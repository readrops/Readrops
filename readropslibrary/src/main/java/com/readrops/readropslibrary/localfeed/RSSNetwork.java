package com.readrops.readropslibrary.localfeed;

import android.util.Log;

import com.google.gson.Gson;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.Utils.Utils;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSLink;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RSSNetwork {

    private static final String TAG = RSSNetwork.class.getSimpleName();

    private static final String RSS_CONTENT_TYPE_REGEX = "([^;]+)";

    private static final String RSS_2_REGEX = "rss.*version=\"2.0\"";

    /**
     * Request the url given in parameter.
     * This method is synchronous, <b>it has to be called from another thread than the main one</b>.
     * @param url url to request
     * @param callback result callback if success or error
     * @throws Exception
     */
    public void request(String url, final QueryCallback callback) throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();

        Pattern pattern = Pattern.compile(RSS_CONTENT_TYPE_REGEX);
        Matcher matcher = pattern.matcher(response.header("content-type"));

        String header;
        if (matcher.find())
            header = matcher.group(0);
        else
            header = response.header("content-type");

        if (response.isSuccessful()) {
            RSSType type = getRSSType(header);
            if (type == null) {
                callback.onSyncFailure(new IllegalArgumentException("bad content type"));
                return;
            }

            parseFeed(response.body().byteStream(), type, callback, url);
        } else
            callback.onSyncFailure(new Exception("Error " + response.code() + " when requesting url " + url));
    }

    /**
     * Parse input feed
     * @param stream inputStream to parse
     * @param type rss type, important to know the format
     * @param callback success callback
     * @param url feed url
     * @throws Exception
     */
    private void parseFeed(InputStream stream, RSSType type, QueryCallback callback, String url) throws Exception {
        String xml = Utils.inputStreamToString(stream);
        Serializer serializer = new Persister();

        if (type == RSSType.RSS_UNKNOWN) {
            if (Pattern.compile(RSS_2_REGEX).matcher(xml).find())
                type = RSSType.RSS_2;
            else if (xml.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\">"))
                type = RSSType.RSS_ATOM;
            else {
                callback.onSyncFailure(new Exception("Unknown xml format"));
                return;
            }
        }

        switch (type) {
            case RSS_2:
                RSSFeed rssFeed = serializer.read(RSSFeed.class, xml);
                if (rssFeed.getChannel().getFeedUrl() == null) // workaround si the channel does not have any atom:link tag
                    rssFeed.getChannel().getLinks().add(new RSSLink(null, url));

                callback.onSyncSuccess(rssFeed, type);
                break;
            case RSS_ATOM:
                ATOMFeed atomFeed = serializer.read(ATOMFeed.class, xml);
                callback.onSyncSuccess(atomFeed, type);
                break;
            case RSS_JSON:
                Gson gson = new Gson();
                JSONFeed feed = gson.fromJson(xml, JSONFeed.class);
                callback.onSyncSuccess(feed, type);
                break;
        }
    }

    /**
     * Get the rss type according to the content-type header
     * @param contentType content-type header
     * @return rss type according to the content-type header
     */
    private RSSType getRSSType(String contentType) {
        switch (contentType) {
            case Utils.RSS_DEFAULT_CONTENT_TYPE:
                return  RSSType.RSS_2;
            case Utils.RSS_TEXT_CONTENT_TYPE:
                return RSSType.RSS_UNKNOWN;
            case Utils.RSS_APPLICATION_CONTENT_TYPE:
                return RSSType.RSS_UNKNOWN;
            case Utils.ATOM_CONTENT_TYPE:
                return RSSType.RSS_ATOM;
            case Utils.JSON_CONTENT_TYPE:
                return RSSType.RSS_JSON;
            default:
                Log.d(TAG, "bad content type : " + contentType);
                return null;

        }
    }

    public enum RSSType {
        RSS_2("rss"),
        RSS_ATOM("atom"),
        RSS_JSON("json"),
        RSS_UNKNOWN("");

        private String value;

        RSSType(String value) {
            this.value = value;
        }
    }


}
