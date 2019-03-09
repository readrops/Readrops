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
import com.readrops.app.views.FeedsAdapter;
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
    private FeedsAdapter adapter;

    private ManageFeedsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_feeds);

        recyclerView = findViewById(R.id.feeds_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        adapter = new FeedsAdapter(new FeedsAdapter.ManageFeedsListener() {
            @Override
            public void onEdit(FeedWithFolder feedWithFolder) {
                openEditFeedDialog(feedWithFolder);
            }

            @Override
            public void onDelete(FeedWithFolder feedWithFolder) {
                deleteFolder(feedWithFolder.getFeed().getId());
            }
        });

        recyclerView.setAdapter(adapter);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ManageFeedsViewModel.class);
        viewModel.getFeedsWithFolder().observe(this, feedWithFolders -> {
            adapter.submitList(feedWithFolders);
        });
    }

    private void deleteFolder(int feedId) {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.delete_feed))
                .positiveText(getString(R.string.validate))
                .negativeText(getString(R.string.cancel))
                .onPositive((dialog, which) -> viewModel.deleteFeed(feedId)
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
                        }))
                .show();
    }

    private void openEditFeedDialog(FeedWithFolder feedWithFolder) {
        EditFeedDialog editFeedDialog = new EditFeedDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feedWithFolder", feedWithFolder);
        editFeedDialog.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(editFeedDialog, "").commit();
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

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
