package com.readrops.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.readrops.app.R;
import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.Utils.LibUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ReadropsWebView extends WebView {

    private ItemWithFeed itemWithFeed;

    public ReadropsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setItem(ItemWithFeed itemWithFeed) {
        this.itemWithFeed = itemWithFeed;
        loadData(getText(), LibUtils.HTML_CONTENT_TYPE, "utf-8");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        setBackgroundColor(getResources().getColor(R.color.colorBackground));
    }

    private String getText() {
        Document document = Jsoup.parse(itemWithFeed.getItem().getText());

        document.head().append("<style>img{display: inline;height: auto;max-width: 100%;}</style>");

        return document.toString();
    }


}
