package com.readrops.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.fragments.settings.AccountSettingsFragment;
import com.readrops.app.utils.DrawerManager;
import com.readrops.app.utils.GlideApp;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.MainViewModel;
import com.readrops.app.adapters.MainItemListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int ADD_FEED_REQUEST = 1;
    public static final int MANAGE_FEEDS_REQUEST = 2;
    public static final int ITEM_REQUEST = 3;
    public static final int ADD_ACCOUNT_REQUEST = 4;

    public static final String ACCOUNT_KEY = "account";

    private static final String SYNCING_KEY = "SYNCING";

    private RecyclerView recyclerView;
    private MainItemListAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private ConstraintLayout rootLayout;

    private Toolbar toolbar;
    private Drawer drawer;

    private PagedList<ItemWithFeed> allItems;

    private MainViewModel viewModel;
    private DrawerManager drawerManager;

    private RelativeLayout emptyListLayout;
    private RelativeLayout syncProgressLayout;
    private TextView syncProgress;
    private ProgressBar syncProgressBar;

    private int feedCount;
    private int feedNb;
    private boolean scrollToTop;
    private boolean allItemsSelected;
    private boolean updating;

    private ActionMode actionMode;
    private Disposable syncDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        emptyListLayout = findViewById(R.id.empty_list_layout);
        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        rootLayout = findViewById(R.id.main_root);

        syncProgressLayout = findViewById(R.id.sync_progress_layout);
        syncProgress = findViewById(R.id.sync_progress_text_view);
        syncProgressBar = findViewById(R.id.sync_progress_bar);
        syncProgressBar = findViewById(R.id.sync_progress_bar);

        feedCount = 0;
        initRecyclerView();

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.setShowReadItems(SharedPreferencesManager.readBoolean(this,
                SharedPreferencesManager.SharedPrefKey.SHOW_READ_ARTICLES));

        viewModel.getItemsWithFeed().observe(this, itemWithFeeds -> {
            allItems = itemWithFeeds;

            if (itemWithFeeds.size() > 0)
                emptyListLayout.setVisibility(View.GONE);
            else
                emptyListLayout.setVisibility(View.VISIBLE);

            if (!refreshLayout.isRefreshing())
                adapter.submitList(itemWithFeeds);
        });

        drawerManager = new DrawerManager(this, toolbar, (view, position, drawerItem) -> {
            handleDrawerClick(drawerItem);

            return true;
        });

        drawerManager.setHeaderListener((view, profile, current) -> {
            if (!current) {
                int id = (int) profile.getIdentifier();

                switch (id) {
                    case DrawerManager.ADD_ACCOUNT_ID:
                        Intent intent = new Intent(this, AccountTypeListActivity.class);
                        intent.putExtra("fromMainActivity", true);
                        startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                        break;
                    default:
                        if (!updating) {
                            viewModel.setCurrentAccount(id);
                            updateDrawerFeeds();
                        }
                        break;
                }
            } else {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.SETTINGS_KEY,
                        SettingsActivity.SettingsKey.ACCOUNT_SETTINGS.ordinal());
                intent.putExtra(AccountSettingsFragment.ACCOUNT, viewModel.getCurrentAccount());
                startActivity(intent);
            }

            return true;
        });

        Account currentAccount = getIntent().getParcelableExtra(MainActivity.ACCOUNT_KEY);
        WeakReference<Account> accountWeakReference = new WeakReference<>(currentAccount);

        viewModel.getAllAccounts().observe(this, accounts -> {
            viewModel.setAccounts(accounts);

            if (drawer == null) {
                drawer = drawerManager.buildDrawer(accounts);
                updateDrawerFeeds();
            } else if (accounts.size() < drawerManager.getNumberOfProfiles() && accounts.size() > 0) {
                drawerManager.updateHeader(accounts);
                updateDrawerFeeds();
            } else if (accounts.size() == 0) {
                Intent intent = new Intent(this, AccountTypeListActivity.class);
                startActivity(intent);
                finish();
            }

            if (accountWeakReference.get() != null && !(accountWeakReference.get().getAccountType() == Account.AccountType.LOCAL)) {
                refreshLayout.setRefreshing(true);
                onRefresh();
                accountWeakReference.clear();
            } else if (currentAccount == null && savedInstanceState != null && savedInstanceState.getBoolean(SYNCING_KEY)) {
                refreshLayout.setRefreshing(true);
                onRefresh();
                savedInstanceState.clear();
            }

        });
    }

    private void handleDrawerClick(IDrawerItem drawerItem) {
        if (drawerItem instanceof PrimaryDrawerItem) {
            drawer.closeDrawer();
            int id = (int) drawerItem.getIdentifier();

            switch (id) {
                case DrawerManager.ARTICLES_ITEM_ID:
                    viewModel.setFilterType(MainViewModel.FilterType.NO_FILTER);
                    scrollToTop = true;
                    viewModel.invalidate();
                    break;
                case DrawerManager.READ_LATER_ID:
                    viewModel.setFilterType(MainViewModel.FilterType.READ_IT_LATER_FILTER);
                    viewModel.invalidate();
                    break;

            }
        } else if (drawerItem instanceof SecondaryDrawerItem) {
            drawer.closeDrawer();

            viewModel.setFilterFeedId((int) drawerItem.getIdentifier());
            viewModel.setFilterType(MainViewModel.FilterType.FEED_FILTER);
            viewModel.invalidate();
        }
    }

    private void updateDrawerFeeds() {
        viewModel.getFoldersWithFeeds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Map<Folder, List<Feed>>>() {
                    @Override
                    public void onSuccess(Map<Folder, List<Feed>> folderListHashMap) {
                        drawerManager.updateDrawer(folderListHashMap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.showSnackbar(rootLayout, e.getMessage());
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
        adapter.setOnItemClickListener(new MainItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemWithFeed itemWithFeed, int position) {
                if (actionMode == null) {
                    Intent intent = new Intent(getApplicationContext(), ItemActivity.class);

                    intent.putExtra(ItemActivity.ITEM_ID, itemWithFeed.getItem().getId());
                    intent.putExtra(ItemActivity.IMAGE_URL, itemWithFeed.getItem().getImageLink());
                    startActivityForResult(intent, ITEM_REQUEST);

                    viewModel.setItemReadState(itemWithFeed.getItem().getId(), true,
                            !itemWithFeed.getItem().isReadChanged())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> Utils.showSnackbar(rootLayout, throwable.getMessage()))
                            .subscribe();

                    itemWithFeed.getItem().setRead(true);
                    adapter.notifyItemChanged(position, itemWithFeed);
                    updateDrawerFeeds();
                } else {
                    adapter.toggleSelection(position);

                    if (adapter.getSelection().isEmpty())
                        actionMode.finish();
                }

            }

            @Override
            public void onItemLongClick(ItemWithFeed itemWithFeed, int position) {
                if (actionMode != null)
                    return;

                adapter.toggleSelection(position);

                actionMode = startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        refreshLayout.setEnabled(false);
                        actionMode.getMenuInflater().inflate(R.menu.item_list_contextual_menu, menu);

                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        menu.findItem(R.id.item_mark_read).setVisible(!itemWithFeed.getItem().isRead());
                        menu.findItem(R.id.item_mark_unread).setVisible(itemWithFeed.getItem().isRead());

                        return true;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.item_mark_read:
                                setReadState(true);
                                break;
                            case R.id.item_mark_unread:
                                setReadState(false);
                                break;
                            case R.id.item_select_all:
                                if (allItemsSelected) {
                                    adapter.unselectAll();
                                    allItemsSelected = false;
                                    actionMode.finish();
                                } else {
                                    adapter.selectAll();
                                    allItemsSelected = true;
                                }
                                break;
                        }

                        return true;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        mode.finish();
                        actionMode = null;

                        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        refreshLayout.setEnabled(true);

                        adapter.clearSelection();
                    }
                });
            }
        });

        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<String>(Glide.with(this), adapter, preloadSizeProvider, 10);
        recyclerView.addOnScrollListener(preloader);

        recyclerView.setRecyclerListener(viewHolder -> {
            MainItemListAdapter.ItemViewHolder vh = (MainItemListAdapter.ItemViewHolder) viewHolder;
            GlideApp.with(this).clear(vh.getItemImage());
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
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
                if (i == ItemTouchHelper.LEFT) { // set item read state
                    ItemWithFeed itemWithFeed = adapter.getItemWithFeed(viewHolder.getAdapterPosition());

                    viewModel.setItemReadState(itemWithFeed.getItem().getId(), !itemWithFeed.getItem().isRead(),
                            !itemWithFeed.getItem().isReadChanged())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> Utils.showSnackbar(rootLayout, throwable.getMessage()))
                            .subscribe();

                    itemWithFeed.getItem().setRead(!itemWithFeed.getItem().isRead());

                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                } else { // add item to read it later section
                    viewModel.setItemReadItLater((int) adapter.getItemId(viewHolder.getAdapterPosition()))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> Utils.showSnackbar(rootLayout, throwable.getMessage()))
                            .subscribe();

                    if (viewModel.getFilterType() == MainViewModel.FilterType.READ_IT_LATER_FILTER)
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
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
                if (scrollToTop) {
                    recyclerView.scrollToPosition(0);
                    scrollToTop = false;
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (scrollToTop) {
                    recyclerView.scrollToPosition(0);
                    scrollToTop = false;
                } else
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        });
    }

    private void setReadState(boolean read) {
        if (allItemsSelected) {
            viewModel.setAllItemsReadState(read)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(rootLayout, throwable.getMessage()))
                    .subscribe();

            allItemsSelected = false;
        } else {
            viewModel.setItemsReadState(adapter.getSelectedItems(), read)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(rootLayout, throwable.getMessage()))
                    .subscribe();
        }

        adapter.updateSelection(read);
        updateDrawerFeeds();
        actionMode.finish();
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "syncing started");
        drawerManager.disableAccountSelection();
        updating = true;

        if (viewModel.isAccountLocal()) {
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
                            Utils.showSnackbar(rootLayout, e.getMessage());
                        }
                    });
        } else
            sync(null);
    }

    public void openAddFeedActivity(View view) {
        Intent intent = new Intent(this, AddFeedActivity.class);
        startActivityForResult(intent, ADD_FEED_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ADD_FEED_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                ArrayList<Feed> feeds = data.getParcelableArrayListExtra("feedIds");

                if (feeds != null && feeds.size() > 0) {
                    refreshLayout.setRefreshing(true);
                    feedNb = feeds.size();
                    sync(feeds);
                }
            }

        } else if (requestCode == MANAGE_FEEDS_REQUEST) {
            updateDrawerFeeds();

        } else if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Account newAccount = data.getParcelableExtra(ACCOUNT_KEY);

                if (newAccount != null) {
                    viewModel.addAccount(newAccount);

                    adapter.clearData();

                    if (!viewModel.isAccountLocal()) {
                        refreshLayout.setRefreshing(true);
                        onRefresh();
                    }

                    drawerManager.resetItems();
                    drawerManager.addAccount(newAccount, true);
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sync(@Nullable List<Feed> feeds) {
        Account account = viewModel.getCurrentAccount();
        if (account.getLogin() == null)
            account.setLogin(SharedPreferencesManager.readString(this, account.getLoginKey()));

        if (viewModel.getCurrentAccount().getPassword() == null)
            account.setPassword(SharedPreferencesManager.readString(this, account.getPasswordKey()));

        viewModel.sync(feeds, account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        syncDisposable = d;

                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            syncProgressLayout.setVisibility(View.VISIBLE);
                            syncProgressBar.setProgress(0);
                        }
                    }

                    @Override
                    public void onNext(Feed feed) {
                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            syncProgress.setText(getString(R.string.updating_feed, feed.getName()));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                syncProgressBar.setProgress((feedCount * 100) / feedNb, true);
                            } else
                                syncProgressBar.setProgress((feedCount * 100) / feedNb);
                        }

                        feedCount++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                        syncProgressLayout.setVisibility(View.GONE);

                        Utils.showSnackbar(rootLayout, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                syncProgressBar.setProgress(100, true);
                            else
                                syncProgressBar.setProgress(100);

                            syncProgressLayout.setVisibility(View.GONE);
                        }

                        refreshLayout.setRefreshing(false);

                        scrollToTop = true;
                        adapter.submitList(allItems);

                        drawerManager.enableAccountSelection();
                        updateDrawerFeeds(); // update drawer after syncing feeds
                        updating = false;
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_list_menu, menu);

        MenuItem articlesItem = menu.findItem(R.id.item_filter_read_items);
        articlesItem.setChecked(viewModel.showReadItems());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_filter_read_items:
                if (item.isChecked()) {
                    item.setChecked(false);
                    viewModel.setShowReadItems(false);
                    SharedPreferencesManager.writeValue(this,
                            SharedPreferencesManager.SharedPrefKey.SHOW_READ_ARTICLES, false);
                } else {
                    item.setChecked(true);
                    viewModel.setShowReadItems(true);
                    SharedPreferencesManager.writeValue(this,
                            SharedPreferencesManager.SharedPrefKey.SHOW_READ_ARTICLES, true);
                }

                viewModel.invalidate();
                return true;
            case R.id.item_sort:
                displayFilterDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayFilterDialog() {
        int index = viewModel.getSortType() == ListSortType.OLDEST_TO_NEWEST ? 1 : 0;

        new MaterialDialog.Builder(this)
                .title(R.string.filter)
                .items(R.array.filter_items)
                .itemsCallbackSingleChoice(index, (dialog, itemView, which, text) -> {
                    String[] items = getResources().getStringArray(R.array.filter_items);

                    if (text.toString().equals(items[0]))
                        viewModel.setSortType(ListSortType.NEWEST_TO_OLDEST);
                    else
                        viewModel.setSortType(ListSortType.OLDEST_TO_NEWEST);

                    scrollToTop = true;
                    viewModel.invalidate();
                    return true;
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if (syncDisposable != null && !syncDisposable.isDisposed())
            syncDisposable.dispose();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (refreshLayout.isRefreshing())
            outState.putBoolean(SYNCING_KEY, true);

        super.onSaveInstanceState(outState);
    }

    public enum ListSortType {
        NEWEST_TO_OLDEST,
        OLDEST_TO_NEWEST
    }
}
