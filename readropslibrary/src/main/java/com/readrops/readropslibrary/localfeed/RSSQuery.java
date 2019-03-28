package com.readrops.readropslibrary.localfeed;

import android.accounts.NetworkErrorException;
import android.util.Log;

import com.google.gson.Gson;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.Utils.LibUtils;
import com.readrops.readropslibrary.Utils.UnknownFormatException;
import com.readrops.readropslibrary.localfeed.atom.ATOMFeed;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSLink;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RSSQuery {

    private static final String TAG = RSSQuery.class.getSimpleName();

    private static final String RSS_CONTENT_TYPE_REGEX = "([^;]+)";

    private static final String RSS_2_REGEX = "rss.*version=\"2.0\"";

    /**
     * Request the url given in parameter.
     * This method is synchronous, <b>it has to be called from another thread than the main one</b>.
     * @param url url to request
     * @throws Exception
     */
    public RSSQueryResult queryUrl(String url, Map<String, String> headers) throws Exception {
        Response response = query(url, headers);

        if (response.isSuccessful()) {
            String header = response.header(LibUtils.CONTENT_TYPE_HEADER);
            RSSType type = getRSSType(header);

            if (type == null)
                return new RSSQueryResult(new UnknownFormatException("bad content type : " + header + "for " + url));

            return parseFeed(response.body().byteStream(), type, response);
        } else if (response.code() == 304)
            return null;
        else
            return new RSSQueryResult(new NetworkErrorException("Error " + response.code() + " when requesting url " + url));
    }

    public boolean isUrlFeedLink(String url) throws IOException {
        Response response = query(url, new HashMap<String, String>());

        if (response.isSuccessful()) {
            String header = response.header(LibUtils.CONTENT_TYPE_HEADER);
            RSSType type = getRSSType(header);

            if (type == RSSType.RSS_UNKNOWN) {
                RSSType contentType = getContentRSSType(response.body().string());
                return contentType != RSSType.RSS_UNKNOWN;
            } else return type != null;
        } else
            return false;
    }

    private Response query(String url, Map<String, String> headers) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder().url(url);
        for (String header : headers.keySet()) {
            String value = headers.get(header);
            builder.addHeader(header, value);
        }

        Request request = builder.build();
        return okHttpClient.newCall(request).execute();
    }

    private RSSType getRSSType(String contentType) {
        Pattern pattern = Pattern.compile(RSS_CONTENT_TYPE_REGEX);
        Matcher matcher = pattern.matcher(contentType);

        String header;
        if (matcher.find())
            header = matcher.group(0);
        else
            header = contentType;

        switch (header) {
            case LibUtils.RSS_DEFAULT_CONTENT_TYPE:
                return  RSSType.RSS_2;
            case LibUtils.RSS_TEXT_CONTENT_TYPE:
                return RSSType.RSS_UNKNOWN;
            case LibUtils.RSS_APPLICATION_CONTENT_TYPE:
                return RSSType.RSS_UNKNOWN;
            case LibUtils.ATOM_CONTENT_TYPE:
                return RSSType.RSS_ATOM;
            case LibUtils.JSON_CONTENT_TYPE:
                return RSSType.RSS_JSON;
            case LibUtils.HTML_CONTENT_TYPE:
                return RSSType.RSS_UNKNOWN;
            default:
                Log.d(TAG, "bad content type : " + contentType);
                return null;
        }
    }



    /**
     * Parse input feed
     * @param stream source to parse
     * @param type rss type, important to know the feed format
     * @param response query response
     * @throws Exception
     */
    private RSSQueryResult parseFeed(InputStream stream, RSSType type, Response response) throws Exception {
        String xml = LibUtils.inputStreamToString(stream);
        Serializer serializer = new Persister();

        if (type == RSSType.RSS_UNKNOWN) {
            RSSType contentType = getContentRSSType(xml);
            if (contentType == RSSType.RSS_UNKNOWN) {
                return new RSSQueryResult(new UnknownFormatException("Unknown content format"));
            } else
                type = contentType;
        }

        String etag = response.header(LibUtils.ETAG_HEADER);
        String lastModified = response.header(LibUtils.LAST_MODIFIED_HEADER);
        AFeed feed;
        RSSQueryResult queryResult = new RSSQueryResult();

        switch (type) {
            case RSS_2:
                feed = serializer.read(RSSFeed.class, xml);
                if (((RSSFeed)feed).getChannel().getFeedUrl() == null) // workaround si the channel does not have any atom:link tag
                    ((RSSFeed)feed).getChannel().getLinks().add(new RSSLink(null, response.request().url().toString()));
                feed.setEtag(etag);
                feed.setLastModified(lastModified);

                queryResult.setFeed(feed);
                queryResult.setRssType(type);
                break;
            case RSS_ATOM:
                feed = serializer.read(ATOMFeed.class, xml);
                ((ATOMFeed)feed).setWebsiteUrl(response.request().url().scheme() + "://" + response.request().url().host());
                ((ATOMFeed)feed).setUrl(response.request().url().toString());
                feed.setEtag(etag);
                feed.setLastModified(etag);

                queryResult.setFeed(feed);
                queryResult.setRssType(type);
                break;
            case RSS_JSON:
                Gson gson = new Gson();
                feed = gson.fromJson(xml, JSONFeed.class);

                feed.setEtag(etag);
                feed.setLastModified(lastModified);

                queryResult.setFeed(feed);
                queryResult.setRssType(type);
                break;
        }

        return queryResult;
    }

    private RSSType getContentRSSType(String content) {
        RSSType type;

        if (Pattern.compile(RSS_2_REGEX).matcher(content).find())
            type = RSSType.RSS_2;
        else if (content.contains("<feed xmlns=\"http://www.w3.org/2005/Atom\""))
            type = RSSType.RSS_ATOM;
        else
            type = RSSType.RSS_UNKNOWN;

        return type;
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
