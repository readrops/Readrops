package com.readrops.app;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.github.clans.fab.FloatingActionMenu;
import com.readrops.app.database.ItemWithFeed;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.GlideApp;
import com.readrops.readropslibrary.ParsingResult;


import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements SimpleCallback, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private MainItemListAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private FloatingActionMenu actionMenu;

    private List<ItemWithFeed> newItems;
    private TreeMap<LocalDateTime, Item> itemsMap;

    private MainViewModel viewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        actionMenu = findViewById(R.id.fab_menu);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((menuItem) -> {
            menuItem.setChecked(true);
            drawerLayout.closeDrawers();

            switch (menuItem.getItemId()) {
                case R.id.to_read:
                    break;
                case R.id.non_read_articles:
                    break;
            }

            return true;
        });

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(MainViewModel.class);
        viewModel.setSimpleCallback(this);

        itemsMap = new TreeMap<>(LocalDateTime::compareTo);
        newItems = new ArrayList<>();

        viewModel.getItemsWithFeed().observe(this, (itemWithFeeds -> {
            newItems = itemWithFeeds;

            if (!refreshLayout.isRefreshing())
                adapter.submitList(newItems);
        }));

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        initRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.items_recycler_view);

        ViewPreloadSizeProvider preloadSizeProvider = new ViewPreloadSizeProvider();
        adapter = new MainItemListAdapter(GlideApp.with(this), preloadSizeProvider);
        adapter.setOnItemClickListener(itemWithFeed -> {
            Intent intent = new Intent(this, ItemActivity.class);
            intent.putExtra(ItemActivity.ITEM_ID, itemWithFeed.getItem().getId());
            intent.putExtra(ItemActivity.IMAGE_URL, itemWithFeed.getItem().getImageLink());

            startActivity(intent);
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

    private void updateList() {

    }

    @Override
    public void onSuccess() {
        refreshLayout.setRefreshing(false);
        adapter.submitList(newItems);
    }

    @Override
    public void onFailure(Exception e) {
        refreshLayout.setRefreshing(false);
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "syncing started");
        viewModel.sync();
    }

    public void displayAddFeedDialog(View view) {
        actionMenu.close(true);
        Dialog dialog = new AddFeedDialog(this, R.layout.add_feed_layout);
        dialog.show();
    }

    public void addFolder(View view) {
        actionMenu.close(true);
    }

    public void insertNewFeed(ParsingResult result) {
        refreshLayout.setRefreshing(true);
        viewModel.addFeed(result);
    }
}
