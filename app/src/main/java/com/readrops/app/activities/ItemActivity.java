package com.readrops.app.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.readrops.app.R;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropsdb.pojo.ItemWithFeed;
import com.readrops.app.utils.DateUtils;
import com.readrops.app.utils.GlideApp;
import com.readrops.app.utils.PermissionManager;
import com.readrops.app.utils.ReadropsWebView;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.ItemViewModel;

import static com.readrops.app.utils.ReadropsKeys.ACTION_BAR_COLOR;
import static com.readrops.app.utils.ReadropsKeys.IMAGE_URL;
import static com.readrops.app.utils.ReadropsKeys.ITEM_ID;
import static com.readrops.app.utils.ReadropsKeys.WEB_URL;

public class ItemActivity extends AppCompatActivity {

    private static final String TAG = ItemActivity.class.getSimpleName();
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;

    private ItemViewModel viewModel;
    private TextView date;
    private TextView title;
    private TextView author;
    private TextView readTime;

    private RelativeLayout readTimeLayout;
    RelativeLayout dateLayout;

    private CollapsingToolbarLayout toolbarLayout;
    private Toolbar toolbar;
    private FloatingActionButton actionButton;
    private ReadropsWebView webView;

    private ItemWithFeed itemWithFeed;

    private boolean appBarCollapsed;

    private CoordinatorLayout rootLayout;
    private String urlToDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra(ITEM_ID, 0);
        String imageUrl = intent.getStringExtra(IMAGE_URL);

        toolbar = findViewById(R.id.collapsing_layout_toolbar);
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
        dateLayout = findViewById(R.id.activity_item_date_layout);
        rootLayout = findViewById(R.id.item_root);

        registerForContextMenu(webView);

        if (imageUrl == null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbarLayout.setTitleEnabled(false);
            scrim.setVisibility(View.GONE);
        } else {
            appBarLayout.setExpanded(true);
            toolbarLayout.setTitleEnabled(true);

            GlideApp.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView);
        }

        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        appBarLayout.addOnOffsetChangedListener(((appBarLayout1, i) -> {

            if (Math.abs(i) >= (appBarLayout.getTotalScrollRange() - actionBarSize - ((8 * appBarLayout.getTotalScrollRange()) / 100))) {
                appBarCollapsed = true;
                invalidateOptionsMenu();
            } else {
                appBarCollapsed = false;
                invalidateOptionsMenu();
            }
        }));

        viewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, this::bindUI);
        actionButton.setOnClickListener(v -> openInNavigator());
    }

    private void bindUI(ItemWithFeed itemWithFeed) {
        this.itemWithFeed = itemWithFeed;
        Item item = itemWithFeed.getItem();

        date.setText(DateUtils.formattedDateTimeByLocal(item.getPubDate()));

        if (item.getImageLink() == null)
            toolbar.setTitle(itemWithFeed.getFeedName());
        else
            toolbarLayout.setTitle(itemWithFeed.getFeedName());

        if (itemWithFeed.getFolder() != null) {
            toolbar.setSubtitle(itemWithFeed.getFolder().getName());
        }

        title.setText(item.getTitle());

        if (itemWithFeed.getBgColor() != 0) {
            title.setTextColor(itemWithFeed.getBgColor());
            Utils.setDrawableColor(dateLayout.getBackground(), itemWithFeed.getBgColor());
        } else if (itemWithFeed.getColor() != 0) {
            title.setTextColor(itemWithFeed.getColor());
            Utils.setDrawableColor(dateLayout.getBackground(), itemWithFeed.getColor());
        }

        if (item.getAuthor() != null && !item.getAuthor().isEmpty()) {
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

            getWindow().setStatusBarColor(itemWithFeed.getBgColor());

            actionButton.setBackgroundTintList(ColorStateList.valueOf(itemWithFeed.getBgColor()));
        } else if (itemWithFeed.getColor() != 0) {
            toolbarLayout.setBackgroundColor(itemWithFeed.getColor());
            toolbarLayout.setContentScrimColor(itemWithFeed.getColor());
            toolbarLayout.setStatusBarScrimColor(itemWithFeed.getColor());

            getWindow().setStatusBarColor(itemWithFeed.getColor());

            actionButton.setBackgroundTintList(ColorStateList.valueOf(itemWithFeed.getColor()));
        }

        webView.setItem(itemWithFeed);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_open);
        item.setVisible(appBarCollapsed);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.item_share:
                shareArticle();
                return true;
            case R.id.item_open:
                int value = Integer.valueOf(SharedPreferencesManager.readString(this,
                        SharedPreferencesManager.SharedPrefKey.OPEN_ITEMS_IN));
                if (value == 0)
                    openInNavigator();
                else
                    openInWebView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void openInNavigator() {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemWithFeed.getItem().getLink()));
        startActivity(urlIntent);
    }

    private void openInWebView() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WEB_URL, itemWithFeed.getItem().getLink());
        intent.putExtra(ACTION_BAR_COLOR, itemWithFeed.getColor() != 0 ? itemWithFeed.getColor() : itemWithFeed.getBgColor());

        startActivity(intent);
    }

    private void shareArticle() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, itemWithFeed.getItem().getTitle() + " - " + itemWithFeed.getItem().getLink());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_article)));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();

        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            new MaterialDialog.Builder(this)
                    .title(R.string.image_options)
                    .items(R.array.image_options)
                    .itemsCallback((dialog, itemView, position, text) -> {
                        if (position == 0)
                            shareImage(hitTestResult.getExtra());
                        else {
                            if (PermissionManager.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                                downloadImage(hitTestResult.getExtra());
                            else {
                                urlToDownload = hitTestResult.getExtra();
                                PermissionManager.requestPermissions(this, WRITE_EXTERNAL_STORAGE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                        }

                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage(urlToDownload);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    Utils.showSnackBarWithAction(rootLayout, getString(R.string.download_image_permission),
                            getString(R.string.try_again),
                            v -> PermissionManager.requestPermissions(this, WRITE_EXTERNAL_STORAGE_REQUEST,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE));
                } else {
                    Utils.showSnackBarWithAction(rootLayout, getString(R.string.download_image_permission),
                            getString(R.string.permissions), v -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            });
                }

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void downloadImage(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.download_image))
                .setMimeType("image/png")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.png");

        request.allowScanningByMediaScanner();

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void shareImage(String url) {
        GlideApp.with(this)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            Uri uri = viewModel.saveImageInCache(resource);
                            Intent intent = ShareCompat.IntentBuilder.from(ItemActivity.this)
                                    .setType("image/png")
                                    .setStream(uri)
                                    .setChooserTitle(R.string.share_image)
                                    .createChooserIntent()
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // not useful
                    }
                });

    }
}
