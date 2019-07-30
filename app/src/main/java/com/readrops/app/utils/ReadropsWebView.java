package com.readrops.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.readrops.app.R;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.readropslibrary.utils.LibUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ReadropsWebView extends WebView {

    private ItemWithFeed itemWithFeed;

    public ReadropsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setItem(ItemWithFeed itemWithFeed) {
        this.itemWithFeed = itemWithFeed;
        loadData(getText(), LibUtils.HTML_CONTENT_TYPE, "UTF-8");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        WebSettings settings = getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        setBackgroundColor(getResources().getColor(R.color.colorBackground));
    }

    private String getText() {
        if (itemWithFeed.getItem().getText() != null) {
            Document document = Jsoup.parse(itemWithFeed.getItem().getText(), itemWithFeed.getWebsiteUrl());

            formatDocument(document);

            return getContext().getString(R.string.webview_html_template,
                    Utils.getCssColor(itemWithFeed.getBgColor() != 0 ? itemWithFeed.getBgColor() :
                            (itemWithFeed.getColor() != 0 ? itemWithFeed.getColor() : getResources().getColor(R.color.colorPrimary))),
                    document.body().html());

        } else
            return null;
    }

    private void formatDocument(Document document) {
        Elements elements = document.select("figure,figcaption");
        for (Element element : elements) {
            element.unwrap();
        }

        elements.clear();
        elements = document.select("div");

        for (Element element : elements) {
            element.clearAttributes();
        }
    }


}
