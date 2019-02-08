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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

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

public class ItemActivity extends AppCompatActivity {

    private ItemViewModel viewModel;
    private TextView title;
    private TextView author;
    private TextView readTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("itemId", 0);
        String imageUrl = intent.getStringExtra("imageUrl");

        Toolbar toolbar = findViewById(R.id.collasping_layout_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.collapsing_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        if (imageUrl == null)
            appBarLayout.setExpanded(false);

        ImageView imageView = findViewById(R.id.collapsing_layout_image);

        GlideApp.with(this)
                .load(imageUrl)
                .into(imageView);

        ReadropsWebView webView = findViewById(R.id.item_webview);
        title = findViewById(R.id.activity_item_title);
        author = findViewById(R.id.activity_item_author);
        readTime = findViewById(R.id.activity_item_readtime);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, itemWithFeed -> {
            Item item = itemWithFeed.getItem();

            toolbarLayout.setTitle(itemWithFeed.getFeedName());
            toolbar.setTitle(itemWithFeed.getFeedName());
            title.setText(item.getTitle());

            webView.setItem(itemWithFeed);
        });
    }
}
