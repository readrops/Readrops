package com.readrops.app.itemslist;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.readrops.app.R;
import com.readrops.app.account.AccountTypeListActivity;
import com.readrops.app.addfeed.AddFeedActivity;
import com.readrops.app.databinding.ActivityMainBinding;
import com.readrops.app.item.ItemActivity;
import com.readrops.app.settings.SettingsActivity;
import com.readrops.app.utils.GlideRequests;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.utils.customviews.ReadropsItemTouchCallback;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.account.Account;
import com.readrops.db.filters.FilterType;
import com.readrops.db.filters.ListSortType;
import com.readrops.db.pojo.ItemWithFeed;

import org.jetbrains.annotations.NotNull;
import org.koin.androidx.viewmodel.compat.ViewModelCompat;
import org.koin.java.KoinJavaComponent;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT_ID;
import static com.readrops.app.utils.ReadropsKeys.FEEDS;
import static com.readrops.app.utils.ReadropsKeys.FROM_MAIN_ACTIVITY;
import static com.readrops.app.utils.ReadropsKeys.IMAGE_URL;
import static com.readrops.app.utils.ReadropsKeys.ITEM_ID;
import static com.readrops.app.utils.ReadropsKeys.SETTINGS;
import static com.readrops.app.utils.ReadropsKeys.STARRED_ITEM;
import static com.readrops.app.utils.ReadropsKeys.SYNCING;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        ReadropsItemTouchCallback.SwipeCallback, ActionMode.Callback {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int ADD_FEED_REQUEST = 1;
    public static final int MANAGE_ACCOUNT_REQUEST = 2;
    public static final int ITEM_REQUEST = 3;
    public static final int ADD_ACCOUNT_REQUEST = 4;

    private ActivityMainBinding binding;
    private MainItemListAdapter adapter;

    private Drawer drawer;

    private PagedList<ItemWithFeed> allItems;

    private MainViewModel viewModel;
    private DrawerManager drawerManager;

    private int feedCount;
    private int feedNb;
    private boolean scrollToTop;
    private boolean allItemsSelected;
    private boolean updating;

    private ActionMode actionMode;
    private Disposable syncDisposable;

    private ItemWithFeed selectedItemWithFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMain);

        binding.swipeRefreshLayout.setOnRefreshListener(this);

        feedCount = 0;
        initRecyclerView();

        viewModel = ViewModelCompat.getViewModel(this, MainViewModel.class);

        viewModel.getItemsWithFeed().observe(this, itemWithFeeds -> {
            allItems = itemWithFeeds;

            if (!itemWithFeeds.isEmpty())
                binding.emptyListLayout.setVisibility(View.GONE);
            else
                binding.emptyListLayout.setVisibility(View.VISIBLE);

            if (!binding.swipeRefreshLayout.isRefreshing())
                adapter.submitList(itemWithFeeds);
        });

        drawerManager = new DrawerManager(this, binding.toolbarMain, (view, position, drawerItem) -> {
            handleDrawerClick(drawerItem);

            return true;
        });

        drawerManager.setHeaderListener((view, profile, current) -> {
            if (!current) {
                int id = (int) profile.getIdentifier();

                switch (id) {
                    case DrawerManager.ADD_ACCOUNT_ID:
                        Intent intent = new Intent(this, AccountTypeListActivity.class);
                        intent.putExtra(FROM_MAIN_ACTIVITY, true);
                        startActivityForResult(intent, ADD_ACCOUNT_REQUEST);
                        break;
                    case DrawerManager.ACCOUNT_SETTINGS_ID:
                        Intent intent1 = new Intent(this, SettingsActivity.class);
                        intent1.putExtra(SETTINGS,
                                SettingsActivity.SettingsKey.ACCOUNT_SETTINGS.ordinal());
                        intent1.putExtra(ACCOUNT, viewModel.getCurrentAccount());
                        startActivity(intent1);
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
                intent.putExtra(SETTINGS,
                        SettingsActivity.SettingsKey.ACCOUNT_SETTINGS.ordinal());
                intent.putExtra(ACCOUNT, viewModel.getCurrentAccount());
                startActivityForResult(intent, MANAGE_ACCOUNT_REQUEST);
            }

            return true;
        });

        Account currentAccount = getIntent().getParcelableExtra(ACCOUNT);
        WeakReference<Account> accountWeakReference = new WeakReference<>(currentAccount);

        viewModel.getAllAccounts().observe(this, accounts -> {
            getAccountCredentials(accounts);
            viewModel.setAccounts(accounts);

            // the activity was just opened
            if (drawer == null) {
                int currentAccountId = 0;
                if (getIntent().hasExtra(ACCOUNT_ID)) { // coming from a notification
                    currentAccountId = getIntent().getIntExtra(ACCOUNT_ID, 1);
                    viewModel.setCurrentAccount(currentAccountId);
                }

                drawer = drawerManager.buildDrawer(accounts, currentAccountId);
                drawer.setSelection(DrawerManager.ARTICLES_ITEM_ID);
                updateDrawerFeeds();

                openItemActivity(getIntent());
            } else if (accounts.size() < drawerManager.getNumberOfProfiles() && !accounts.isEmpty()) {
                drawerManager.updateHeader(accounts);
                updateDrawerFeeds();
            } else if (accounts.isEmpty()) {
                Intent intent = new Intent(this, AccountTypeListActivity.class);
                startActivity(intent);
                finish();
            }

            if (accountWeakReference.get() != null && !accountWeakReference.get().isLocal()) {
                binding.swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                accountWeakReference.clear();
            } else if (currentAccount == null && savedInstanceState != null && savedInstanceState.getBoolean(SYNCING)) {
                binding.swipeRefreshLayout.setRefreshing(true);
                onRefresh();
                savedInstanceState.clear();
            }


        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        openItemActivity(intent);
    }

    private void openItemActivity(Intent intent) {
        if (intent.hasExtra(ITEM_ID) && intent.hasExtra(IMAGE_URL)) {
            Intent itemIntent = new Intent(this, ItemActivity.class);
            itemIntent.putExtras(intent);

            startActivity(itemIntent);

            Item item = new Item();
            item.setId(intent.getIntExtra(ITEM_ID, 0));
            item.setRead(true);

            viewModel.setItemReadState(item)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
                    .subscribe();
        }
    }

    private void handleDrawerClick(IDrawerItem drawerItem) {
        if (drawerItem instanceof PrimaryDrawerItem) {
            drawer.closeDrawer();
            int id = (int) drawerItem.getIdentifier();

            switch (id) {
                case DrawerManager.ARTICLES_ITEM_ID:
                    viewModel.setFilterType(FilterType.NO_FILTER);
                    scrollToTop = true;
                    viewModel.invalidate();
                    break;
                case DrawerManager.READ_LATER_ID:
                    viewModel.setFilterType(FilterType.READ_IT_LATER_FILTER);
                    viewModel.invalidate();
                    break;
                case DrawerManager.STARS_ID:
                    viewModel.setFilterType(FilterType.STARS_FILTER);
                    viewModel.invalidate();
                    break;
                case DrawerManager.ABOUT_ID:
                    startAboutActivity();
                    break;
                case DrawerManager.SETTINGS_ID:
                    Intent intent = new Intent(getApplication(), SettingsActivity.class);
                    intent.putExtra(SETTINGS,
                            SettingsActivity.SettingsKey.SETTINGS.ordinal());
                    startActivity(intent);
                    break;
            }
        } else if (drawerItem instanceof SecondaryDrawerItem) {
            drawer.closeDrawer();

            viewModel.setFilterFeedId((int) drawerItem.getIdentifier());
            viewModel.setFilterType(FilterType.FEED_FILTER);
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
                        Utils.showSnackbar(binding.mainRoot, e.getMessage());
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
        ViewPreloadSizeProvider preloadSizeProvider = new ViewPreloadSizeProvider();
        adapter = new MainItemListAdapter(KoinJavaComponent.get(GlideRequests.class), preloadSizeProvider);
        adapter.setOnItemClickListener(new MainItemListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemWithFeed itemWithFeed, int position) {
                if (actionMode == null) {
                    Intent intent = new Intent(getApplicationContext(), ItemActivity.class);

                    intent.putExtra(ITEM_ID, itemWithFeed.getItem().getId());
                    intent.putExtra(IMAGE_URL, itemWithFeed.getItem().getImageLink());
                    intent.putExtra(STARRED_ITEM, drawerManager.getCurrentSelection() == DrawerManager.STARS_ID &&
                            viewModel.getCurrentAccount().getConfig().useStarredItems());
                    startActivityForResult(intent, ITEM_REQUEST);

                    itemWithFeed.getItem().setRead(true);
                    viewModel.setItemReadState(itemWithFeed)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
                            .subscribe();

                    adapter.notifyItemChanged(position, itemWithFeed);
                    updateDrawerFeeds();
                } else {
                    adapter.toggleSelection(position);
                    int selectionSize = adapter.getSelection().size();

                    if (selectionSize > 0)
                        actionMode.setTitle(String.valueOf(selectionSize));
                    else
                        actionMode.finish();
                }
            }

            @Override
            public void onItemLongClick(ItemWithFeed itemWithFeed, int position) {
                if (actionMode != null || binding.swipeRefreshLayout.isRefreshing())
                    return;

                selectedItemWithFeed = itemWithFeed;
                adapter.toggleSelection(position);

                actionMode = startActionMode(MainActivity.this);
                actionMode.setTitle(String.valueOf(adapter.getSelection().size()));
            }
        });

        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<String>(Glide.with(this), adapter, preloadSizeProvider, 10);
        binding.itemsRecyclerView.addOnScrollListener(preloader);

        binding.itemsRecyclerView.setRecyclerListener(viewHolder -> {
            MainItemListAdapter.ItemViewHolder vh = (MainItemListAdapter.ItemViewHolder) viewHolder;
            KoinJavaComponent.get(GlideRequests.class).clear(vh.getItemImage());
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.itemsRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        binding.itemsRecyclerView.addItemDecoration(decoration);

        binding.itemsRecyclerView.setAdapter(adapter);


        Drawable readLater = ContextCompat.getDrawable(this, R.drawable.ic_read_later).mutate();
        DrawableCompat.setTint(readLater, ContextCompat.getColor(this, android.R.color.white));

        new ItemTouchHelper(new ReadropsItemTouchCallback(this,
                new ReadropsItemTouchCallback.Config.Builder()
                        .swipeDirs(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                        .swipeCallback(this)
                        .leftDraw(ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_read_later, readLater)
                        .rightDraw(ContextCompat.getColor(this, R.color.colorAccent), R.drawable.ic_read, null)
                        .build()))
                .attachToRecyclerView(binding.itemsRecyclerView);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (scrollToTop) {
                    binding.itemsRecyclerView.scrollToPosition(0);
                    scrollToTop = false;
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                if (scrollToTop) {
                    binding.itemsRecyclerView.scrollToPosition(0);
                    scrollToTop = false;
                } else
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        });

        binding.itemsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.addFeedFab.hide();
                } else {
                    binding.addFeedFab.show();
                }
            }
        });
    }

    @Override
    public void onSwipe(@NotNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT) { // set item read state
            ItemWithFeed itemWithFeed = adapter.getItemWithFeed(viewHolder.getAdapterPosition());

            itemWithFeed.getItem().setRead(!itemWithFeed.getItem().isRead());
            viewModel.setItemReadState(itemWithFeed)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
                    .subscribe();

            adapter.notifyItemChanged(viewHolder.getAdapterPosition());
        } else { // add item to read it later section
            viewModel.setItemReadItLater((int) adapter.getItemId(viewHolder.getAdapterPosition()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
                    .subscribe();

            if (viewModel.getFilterType() == FilterType.READ_IT_LATER_FILTER)
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        binding.swipeRefreshLayout.setEnabled(false);

        actionMode.getMenuInflater().inflate(R.menu.item_list_contextual_menu, menu);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        menu.findItem(R.id.item_mark_read).setVisible(!selectedItemWithFeed.getItem().isRead());
        menu.findItem(R.id.item_mark_unread).setVisible(selectedItemWithFeed.getItem().isRead());

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
        binding.swipeRefreshLayout.setEnabled(true);

        adapter.clearSelection();
    }

    private void setReadState(boolean read) {
        if (allItemsSelected) {
            viewModel.setAllItemsReadState(read)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
                    .subscribe();

            allItemsSelected = false;
        } else {
            viewModel.setItemsReadState(adapter.getSelectedItems(), read)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(throwable -> Utils.showSnackbar(binding.mainRoot, throwable.getMessage()))
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
                            Utils.showSnackbar(binding.mainRoot, e.getMessage());
                        }
                    });
        } else {
            sync(null);
        }
    }

    public void openAddFeedActivity(View view) {
        Intent intent = new Intent(this, AddFeedActivity.class);
        intent.putExtra(ACCOUNT_ID, viewModel.getCurrentAccount().getId());
        startActivityForResult(intent, ADD_FEED_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ADD_FEED_REQUEST && resultCode == RESULT_OK && data != null) {
            List<Feed> feeds = data.getParcelableArrayListExtra(FEEDS);

            if (feeds != null && !feeds.isEmpty() && viewModel.isAccountLocal()) {
                binding.swipeRefreshLayout.setRefreshing(true);
                feedNb = feeds.size();
                sync(feeds);
            }

        } else if (requestCode == MANAGE_ACCOUNT_REQUEST) {
            updateDrawerFeeds();

        } else if (requestCode == ADD_ACCOUNT_REQUEST && resultCode == RESULT_OK && data != null) {
            Account newAccount = data.getParcelableExtra(ACCOUNT);

            if (newAccount != null) {
                // get credentials before creating the repository
                if (!newAccount.isLocal()) {
                    getAccountCredentials(Collections.singletonList(newAccount));
                }

                viewModel.addAccount(newAccount);
                adapter.clearData();

                // start syncing only if the account is not local
                if (!viewModel.isAccountLocal()) {
                    binding.swipeRefreshLayout.setRefreshing(true);
                    onRefresh();
                }

                drawerManager.resetItems();
                drawerManager.addAccount(newAccount, true);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sync(@Nullable List<Feed> feeds) {
        viewModel.sync(feeds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feed>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        syncDisposable = d;

                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            binding.syncProgressLayout.setVisibility(View.VISIBLE);
                            binding.syncProgressBar.setProgress(0);
                        }
                    }

                    @Override
                    public void onNext(Feed feed) {
                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            binding.syncProgressTextView.setText(getString(R.string.updating_feed, feed.getName()));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.syncProgressBar.setProgress((feedCount * 100) / feedNb, true);
                            } else
                                binding.syncProgressBar.setProgress((feedCount * 100) / feedNb);
                        }

                        feedCount++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.syncProgressLayout.setVisibility(View.GONE);

                        Utils.showSnackbar(binding.mainRoot, e.getMessage());
                        drawerManager.enableAccountSelection();
                        updating = false;
                    }

                    @Override
                    public void onComplete() {
                        viewModel.invalidate();

                        if (viewModel.isAccountLocal() && feedNb > 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                binding.syncProgressBar.setProgress(100, true);
                            else
                                binding.syncProgressBar.setProgress(100);

                            binding.syncProgressLayout.setVisibility(View.GONE);
                        }

                        binding.swipeRefreshLayout.setRefreshing(false);

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
                    SharedPreferencesManager.writeValue(
                            SharedPreferencesManager.SharedPrefKey.SHOW_READ_ARTICLES, false);
                } else {
                    item.setChecked(true);
                    viewModel.setShowReadItems(true);
                    SharedPreferencesManager.writeValue(
                            SharedPreferencesManager.SharedPrefKey.SHOW_READ_ARTICLES, true);
                }

                viewModel.invalidate();
                return true;
            case R.id.item_sort:
                displayFilterDialog();
                return true;
            case R.id.start_sync:
                if (!viewModel.isAccountLocal()) {
                    binding.swipeRefreshLayout.setRefreshing(true);
                }
                onRefresh();
                break;
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

    private void getAccountCredentials(List<Account> accounts) {
        for (Account account : accounts) {
            if (account.getLogin() == null)
                account.setLogin(SharedPreferencesManager.readString(account.getLoginKey()));

            if (account.getPassword() == null)
                account.setPassword(SharedPreferencesManager.readString(account.getPasswordKey()));
        }
    }

    private void startAboutActivity() {
        Libs.ActivityStyle activityStyle;
        if (Boolean.valueOf(SharedPreferencesManager.readString(SharedPreferencesManager.SharedPrefKey.DARK_THEME)))
            activityStyle = Libs.ActivityStyle.DARK;
        else
            activityStyle = Libs.ActivityStyle.LIGHT_DARK_TOOLBAR;

        new LibsBuilder()
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutAppName(getString(R.string.app_name))
                .withAboutDescription(getString(R.string.app_description, getString(R.string.app_licence), getString(R.string.app_url)))
                .withLicenseShown(true)
                .withLicenseDialog(false)
                .withActivityTitle(getString(R.string.about))
                .withActivityStyle(activityStyle)
                .withFields(R.string.class.getFields())
                .start(this);
    }

    @Override
    protected void onDestroy() {
        if (syncDisposable != null && !syncDisposable.isDisposed())
            syncDisposable.dispose();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (binding.swipeRefreshLayout.isRefreshing())
            outState.putBoolean(SYNCING, true);

        super.onSaveInstanceState(outState);
    }
}
