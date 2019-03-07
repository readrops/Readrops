package com.readrops.app.activities;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.mikepenz.fastadapter.commons.utils.DiffCallback;
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.views.EditFeedDialog;
import com.readrops.app.views.FeedWithFolderItem;
import com.readrops.app.viewmodels.ManageFeedsViewModel;
import com.readrops.app.R;
import com.readrops.app.database.pojo.FeedWithFolder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class ManageFeedsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ModelAdapter<FeedWithFolder, FeedWithFolderItem> itemAdapter;
    private FastAdapter fastAdapter;
    private ManageFeedsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_feeds);

        recyclerView = findViewById(R.id.feeds_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        itemAdapter = new ModelAdapter<>(feedWithFolder -> {
            FeedWithFolderItem folderItem = new FeedWithFolderItem(feedWithFolder);
            folderItem.setListener(new FeedWithFolderItem.ManageFeedsListener() {
                @Override
                public void onEdit(FeedWithFolder feedWithFolder) {
                    openEditFeedDialog(feedWithFolder);

                }

                @Override
                public void onDelete(FeedWithFolder feedWithFolder) {
                    viewModel.deleteFeed(feedWithFolder.getFeed().getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Toast.makeText(getApplication(), "feed deleted", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getApplication(), "error on feed deletion", Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });

            return folderItem;
        });

        fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ManageFeedsViewModel.class);
        viewModel.getFeedsWithFolder().observe(this, feedWithFolders -> {
            List<FeedWithFolderItem> items = new ArrayList<>();
            for (FeedWithFolder feedWithFolder :  feedWithFolders) {

                FeedWithFolderItem folderItem = new FeedWithFolderItem(feedWithFolder);
                folderItem.setListener(new FeedWithFolderItem.ManageFeedsListener() {
                    @Override
                    public void onEdit(FeedWithFolder feedWithFolder) {
                        openEditFeedDialog(feedWithFolder);
                    }

                    @Override
                    public void onDelete(FeedWithFolder feedWithFolder) {
                        viewModel.deleteFeed(feedWithFolder.getFeed().getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        Toast.makeText(getApplication(), "feed deleted", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(getApplication(), "error on feed deletion", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });

                items.add(folderItem);
            }

            if (items.size() > 0) {
                DiffUtil.DiffResult diffResult = FastAdapterDiffUtil.calculateDiff(itemAdapter, items, new DiffCallback<FeedWithFolderItem>(){
                    @Override
                    public boolean areItemsTheSame(FeedWithFolderItem oldItem, FeedWithFolderItem newItem) {
                        return oldItem.getModel().getFeed().getId() == newItem.getModel().getFeed().getId();
                    }

                    @Override
                    public boolean areContentsTheSame(FeedWithFolderItem oldItem, FeedWithFolderItem newItem) {
                        FeedWithFolder feedWithFolder1 = oldItem.getModel();
                        FeedWithFolder feedWithFolder2 = newItem.getModel();

                        return feedWithFolder1.getFolder().getName().equals(feedWithFolder2.getFolder().getName()) &&
                                feedWithFolder1.getFeed().getName().equals(feedWithFolder2.getFeed().getName()) &&
                                feedWithFolder1.getFeed().getUrl().equals(feedWithFolder2.getFeed().getUrl());
                    }

                    @Nullable
                    @Override
                    public Object getChangePayload(FeedWithFolderItem oldItem, int oldItemPosition, FeedWithFolderItem newItem, int newItemPosition) {
                        Bundle bundle = new Bundle();

                        if (!oldItem.getModel().getFeed().getName().equals(newItem.getModel().getFeed().getName()))
                            bundle.putString(FeedWithFolderItem.FEED_NAME_KEY, newItem.getModel().getFeed().getName());

                        if (!oldItem.getModel().getFolder().getName().equals(newItem.getModel().getFolder().getName()))
                            bundle.putString(FeedWithFolderItem.FOLDER_NAME_KEY, newItem.getModel().getFolder().getName());

                        if (bundle.size() > 0)
                            return bundle;
                        else
                            return null;
                    }
                });

                FastAdapterDiffUtil.set(itemAdapter, diffResult);
            }
        });
    }

    private void openEditFeedDialog(FeedWithFolder feedWithFolder) {
        EditFeedDialog editFeedDialog = new EditFeedDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feedWithFolder", feedWithFolder);
        editFeedDialog.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(editFeedDialog, "").commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_folder:
                addFolder();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFolder() {
        new MaterialDialog.Builder(this)
                .title(R.string.add_folder)
                .positiveText(R.string.validate)
                .input(R.string.folder, 0, (dialog, input) -> {
                    Folder folder = new Folder(input.toString());
                    viewModel.addFolder(folder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Toast.makeText(getApplication(), "folder inserted", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getApplication(), "error on folder insertion", Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
