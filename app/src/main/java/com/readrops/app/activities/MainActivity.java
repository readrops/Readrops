package com.readrops.app.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.utils.DrawerManager;
import com.readrops.app.views.MainItemListAdapter;
import com.readrops.app.viewmodels.MainViewModel;
import com.readrops.app.R;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.GlideApp;


import org.apache.commons.collections4.Predicate;
import org.joda.time.LocalDateTime;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int ADD_FEED_REQUEST = 1;
    public static final int MANAGE_FEEDS_REQUEST = 2;
    public static final int ITEM_REQUEST = 3;

    private RecyclerView recyclerView;
    private MainItemListAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    private Drawer drawer;
    private FloatingActionMenu actionMenu;

    private List<ItemWithFeed> allItems;
    private TreeMap<LocalDateTime, Item> itemsMap;

    private MainViewModel viewModel;
    private DrawerManager drawerManager;

    private RelativeLayout syncProgressLayout;
    private TextView syncProgress;
    private ProgressBar syncProgressBar;

    private int feedCount;
    private int feedNb;
    private int itemToUpdatePos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        actionMenu = findViewById(R.id.fab_menu);
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        itemsMap = new TreeMap<>(LocalDateTime::compareTo);
        allItems = new ArrayList<>();

        viewModel.getItemsWithFeed().observe(this, (itemWithFeeds -> {
            allItems = itemWithFeeds;

            if (!refreshLayout.isRefreshing())
                adapter.submitList(allItems);
        }));

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        syncProgressLayout = findViewById(R.id.sync_progress_layout);
        syncProgress = findViewById(R.id.sync_progress_text_view);
        syncProgressBar = findViewById(R.id.sync_progress_bar);

        feedCount = 0;
        initRecyclerView();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withShowDrawerOnFirstLaunch(true)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    handleDrawerClick(drawerItem);
                    return true;
                })
                .build();

        drawerManager = new DrawerManager(drawer);

        updateDrawerFeeds();
    }

    private void handleDrawerClick(IDrawerItem drawerItem) {
        if (drawerItem instanceof PrimaryDrawerItem) {
            drawer.closeDrawer();
            int id = (int)drawerItem.getIdentifier();

            switch (id) {
                case DrawerManager.ARTICLES_ITEM_ID:
                    adapter.submitList(allItems);
                    break;
                case DrawerManager.READ_LATER_ID:
                    break;

            }
        } else if (drawerItem instanceof SecondaryDrawerItem) {
            drawer.closeDrawer();
            filterItems((int)drawerItem.getIdentifier());
        }
    }

    private void filterItems(int id) {
        List<ItemWithFeed> filteredItems = new ArrayList<>(allItems);
        CollectionUtils.filter(filteredItems, object -> object.getFeedId() == id);

        adapter.submitList(filteredItems);
    }

    private void updateDrawerFeeds() {
        viewModel.getFoldersWithFeeds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Map<Folder, List<Feed>>>() {
                    @Override
                    public void onSuccess(Map<Folder, List<Feed>> folderListHashMap) {
                        drawerManager.updateDrawer(getApplicationContext(), folderListHashMap);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen())
            drawer.closeDrawer();
        else
            super.onBackPressed();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.items_recycler_view);

        ViewPreloadSizeProvider preloadSizeProvider = new ViewPreloadSizeProvider();
        adapter = new MainItemListAdapter(GlideApp.with(this), preloadSizeProvider);
        adapter.setOnItemClickListener((itemWithFeed, position) -> {
            Intent intent = new Intent(this, ItemActivity.class);

            intent.putExtra(ItemActivity.ITEM_ID, itemWithFeed.getItem().getId());
            intent.putExtra(ItemActivity.IMAGE_URL, itemWithFeed.getItem().getImageLink());
            startActivityForResult(intent, ITEM_REQUEST);

            viewModel.setItemRead(itemWithFeed.getItem().getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

            itemWithFeed.getItem().setRead(true);
            adapter.notifyItemChanged(position, itemWithFeed);
            updateDrawerFeeds();
        });

        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<String>(Glide.with(this), adapter, preloadSizeProvider, 10);
        recyclerView.addOnScrollListener(preloader);

        recyclerView.setRecyclerListener(viewHolder -> {
            MainItemListAdapter.ItemViewHolder vh = (MainItemListAdapter.ItemViewHolder) viewHolder;
            GlideApp.with(this).clear(vh.getItemImage());
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(decoration);

        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (i == ItemTouchHelper.LEFT)
                    adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                else {
                    Log.d("", "");
                }
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        }).attachToRecyclerView(recyclerView);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(0);
            }
        });
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "syncing started");

        viewModel.getFeedCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        feedNb = integer;
                        sync(null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "error on getting feeds number", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void openAddFeedActivity(View view) {
        actionMenu.close(true);

        Intent intent = new Intent(this, AddFeedActivity.class);
        startActivityForResult(intent, ADD_FEED_REQUEST);
    }

    public void addFolder(View view) {
        actionMenu.close(true);

        Intent intent = new Intent(this, ManageFeedsActivity.class);
        startActivityForResult(intent, MANAGE_FEEDS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ADD_FEED_REQUEST && resultCode ==  RESULT_OK) {
            ArrayList<Feed> feeds = data.getParcelableArrayListExtra("feedIds");

            if (feeds != null && feeds.size() > 0) {
                refreshLayout.setRefreshing(true);
                feedNb = feeds.size();
                sync(feeds);
            }
        } else if (requestCode == MANAGE_FEEDS_REQUEST) {
            updateDrawerFeeds();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sync(List<Feed> feeds) {
        viewModel.sync(feeds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        syncProgressLayout.setVisibility(View.VISIBLE);
                        syncProgressBar.setProgress(0);
                    }

                    @Override
                    public void onNext(Feed feed) {
                        syncProgress.setText(getString(R.string.updating_feed, feed.getName()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            syncProgressBar.setProgress((feedCount * 100) / feedNb, true);
                        } else
                            syncProgressBar.setProgress((feedCount * 100) / feedNb);

                        feedCount++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            syncProgressBar.setProgress(100, true);
                        else
                            syncProgressBar.setProgress(100);

                        syncProgressLayout.setVisibility(View.GONE);
                        refreshLayout.setRefreshing(false);

                        adapter.submitList(allItems);
                        updateDrawerFeeds(); // update drawer after syncing feeds
                    }
                });
    }
}
