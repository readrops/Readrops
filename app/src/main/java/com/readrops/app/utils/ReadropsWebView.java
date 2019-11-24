package com.readrops.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;

import com.readrops.app.R;
import com.readrops.app.database.pojo.ItemWithFeed;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class ReadropsWebView extends WebView {

    private ItemWithFeed itemWithFeed;

    public ReadropsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setItem(ItemWithFeed itemWithFeed) {
        this.itemWithFeed = itemWithFeed;

        String text = getText();
        String base64Content = null;

        if (text != null)
            base64Content = Base64.encodeToString(text.getBytes(), Base64.NO_PADDING);

        loadData(base64Content, "text/html; charset=utf-8", "base64");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        if (!isInEditMode()) {
            WebSettings settings = getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);
        }

        setVerticalScrollBarEnabled(false);
        setBackgroundColor(getResources().getColor(R.color.colorBackground));
    }

    @Nullable
    private String getText() {
        if (itemWithFeed.getItem().getText() != null) {
            Document document = Jsoup.parse(Parser.unescapeEntities(itemWithFeed.getItem().getText(), false), itemWithFeed.getWebsiteUrl());

            formatDocument(document);

            int color = itemWithFeed.getColor() != 0 ? itemWithFeed.getColor() : getResources().getColor(R.color.colorPrimary);
            return getContext().getString(R.string.webview_html_template,
                    Utils.getCssColor(itemWithFeed.getBgColor() != 0 ? itemWithFeed.getBgColor() :
                            color),
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
