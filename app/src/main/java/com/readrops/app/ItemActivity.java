package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.DateUtils;
import com.readrops.app.utils.GlideApp;
import com.readrops.app.utils.ReadropsWebView;
import com.readrops.app.utils.Utils;

public class ItemActivity extends AppCompatActivity {

    private ItemViewModel viewModel;
    private TextView date;
    private TextView title;
    private TextView author;
    private TextView readTime;

    private RelativeLayout readTimeLayout;

    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private FloatingActionButton actionButton;
    private ReadropsWebView webView;

    public static final String ITEM_ID = "itemId";
    public static final String IMAGE_URL = "imageUrl";

    private ItemWithFeed itemWithFeed;

    private boolean appBarCollapsed;

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
        View scrim = findViewById(R.id.collapsing_layout_scrim);
        actionButton = findViewById(R.id.activity_item_fab);
        webView = findViewById(R.id.item_webview);
        date = findViewById(R.id.activity_item_date);
        title = findViewById(R.id.activity_item_title);
        author = findViewById(R.id.activity_item_author);
        readTime = findViewById(R.id.activity_item_readtime);
        readTimeLayout = findViewById(R.id.activity_item_readtime_layout);


        if (imageUrl == null) {
            appBarLayout.setExpanded(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarLayout.setTitleEnabled(false);
            scrim.setVisibility(View.GONE);

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

        appBarLayout.addOnOffsetChangedListener(((appBarLayout1, i) -> {
            if (Math.abs(i) >= (appBarLayout.getTotalScrollRange() - ((5 * appBarLayout.getTotalScrollRange()) / 100))) {
                appBarCollapsed = true;
                invalidateOptionsMenu();
            } else {
                appBarCollapsed = false;
                invalidateOptionsMenu();
            }
        }));

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, this::bindUI);

        actionButton.setOnClickListener(v -> {
            openLink();
        });
    }

    private void bindUI(ItemWithFeed itemWithFeed) {
        this.itemWithFeed = itemWithFeed;
        Item item = itemWithFeed.getItem();

        date.setText(DateUtils.formatedDateTimeByLocal(item.getPubDate()));

        if (item.getImageLink() == null)
            toolbar.setTitle(itemWithFeed.getFeedName());
        else
            toolbarLayout.setTitle(itemWithFeed.getFeedName());

        title.setText(item.getTitle());

        if (itemWithFeed.getBgColor() != 0)
            title.setTextColor(itemWithFeed.getBgColor());
        else if (itemWithFeed.getColor() != 0)
            title.setTextColor(itemWithFeed.getColor());

        if (item.getAuthor() != null) {
            author.setText(getString(R.string.by_author, item.getAuthor()));
            author.setVisibility(View.VISIBLE);
        }

        if (item.getReadTime() > 0) {
            int minutes = (int) Math.round(item.getReadTime());
            if (minutes < 1)
                readTime.setText(getResources().getString(R.string.read_time_lower_than_1));
            else if (minutes > 1)
                readTime.setText(getResources().getString(R.string.read_time, String.valueOf(minutes)));
            else
                readTime.setText(getResources().getString(R.string.read_time_one_minute));

            readTimeLayout.setVisibility(View.VISIBLE);
        }

        if (itemWithFeed.getBgColor() != 0) {
            toolbarLayout.setBackgroundColor(itemWithFeed.getBgColor());
            toolbarLayout.setContentScrimColor(itemWithFeed.getBgColor());
            toolbarLayout.setStatusBarScrimColor(itemWithFeed.getBgColor());

            actionButton.setBackgroundTintList(ColorStateList.valueOf(itemWithFeed.getBgColor()));
        } else if (itemWithFeed.getColor() != 0) {
            toolbarLayout.setBackgroundColor(itemWithFeed.getColor());
            toolbarLayout.setContentScrimColor(itemWithFeed.getColor());
            toolbarLayout.setStatusBarScrimColor(itemWithFeed.getColor());

            actionButton.setBackgroundTintList(ColorStateList.valueOf(itemWithFeed.getColor()));
        }

        webView.setItem(itemWithFeed, Utils.getDeviceWidth(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_open);

        if (appBarCollapsed)
            item.setVisible(true);
        else
            item.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                shareArticle();
                return true;
            case R.id.item_open:
                openLink();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openLink() {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemWithFeed.getItem().getLink()));
        startActivity(urlIntent);
    }

    private void shareArticle() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, itemWithFeed.getItem().getTitle() + " - " + itemWithFeed.getItem().getLink());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }
}
