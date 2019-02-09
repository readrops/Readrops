package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.GlideApp;
import com.readrops.app.utils.ReadropsWebView;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.Utils.LibUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.internal.Util;

public class ItemActivity extends AppCompatActivity {

    private ItemViewModel viewModel;
    private TextView title;
    private TextView author;
    private TextView readTime;

    private RelativeLayout readTimeLayout;

    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private ReadropsWebView webView;

    public static final String ITEM_ID = "itemId";
    public static final String IMAGE_URL = "imageUrl";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra(ITEM_ID, 0);
        String imageUrl = intent.getStringExtra(IMAGE_URL);

        toolbar = findViewById(R.id.collasping_layout_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbarLayout = findViewById(R.id.collapsing_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);

        ImageView imageView = findViewById(R.id.collapsing_layout_image);
        webView = findViewById(R.id.item_webview);
        title = findViewById(R.id.activity_item_title);
        author = findViewById(R.id.activity_item_author);
        readTime = findViewById(R.id.activity_item_readtime);
        readTimeLayout = findViewById(R.id.activity_item_readtime_layout);

        if (imageUrl == null) {
            appBarLayout.setExpanded(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarLayout.setTitleEnabled(false);

            toolbar.setTitleTextColor(Color.WHITE);
        } else {
            appBarLayout.setExpanded(true);
            toolbarLayout.setTitleEnabled(true);
            toolbarLayout.setExpandedTitleColor(Color.WHITE);
            toolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

            GlideApp.with(this)
                    .load(imageUrl)
                    .into(imageView);
        }

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, this::bindUI);
    }

    private void bindUI(ItemWithFeed itemWithFeed) {
        Item item = itemWithFeed.getItem();

        if (item.getImageLink() == null)
            toolbar.setTitle(itemWithFeed.getFeedName());
        else
            toolbarLayout.setTitle(itemWithFeed.getFeedName());

        title.setText(item.getTitle());

        if (item.getAuthor() != null) {
            author.setText(getString(R.string.by_author, item.getAuthor()));
            author.setVisibility(View.VISIBLE);
        }

        if (item.getReadTime() > 0) {
            int minutes = (int)Math.round(item.getReadTime());
            if (minutes < 1)
                readTime.setText(getResources().getString(R.string.read_time_lower_than_1));
            else if (minutes > 1)
                readTime.setText(getResources().getString(R.string.read_time, String.valueOf(minutes)));
            else
                readTime.setText(getResources().getString(R.string.read_time_one_minute));

            readTimeLayout.setVisibility(View.VISIBLE);
        }

        webView.setItem(itemWithFeed, Utils.getDeviceWidth(this));
    }
}
