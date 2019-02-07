package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;

import com.readrops.app.utils.GlideApp;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.Utils.LibUtils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ItemActivity extends AppCompatActivity {

    private ItemViewModel viewModel;

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
        toolbarLayout.setTitle("");

        ImageView imageView = findViewById(R.id.collapsing_layout_image);

        GlideApp.with(this)
                .load(imageUrl)
                .into(imageView);

        WebView webView = findViewById(R.id.item_webview);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, item -> {
            webView.loadData(item.getContent(), LibUtils.HTML_CONTENT_TYPE, "utf-8");
        });
    }
}
