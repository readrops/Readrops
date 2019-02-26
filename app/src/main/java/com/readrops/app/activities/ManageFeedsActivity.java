package com.readrops.app.activities;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IInterceptor;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.readrops.app.views.EditFeedDialog;
import com.readrops.app.views.FeedWithFolderItem;
import com.readrops.app.viewmodels.ManageFeedsViewModel;
import com.readrops.app.R;
import com.readrops.app.database.pojo.FeedWithFolder;

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

                }
            });

            return folderItem;
        });

        fastAdapter = FastAdapter.with(itemAdapter);

        recyclerView.setAdapter(fastAdapter);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ManageFeedsViewModel.class);
        viewModel.getFeedsWithFolder().observe(this, feedWithFolders -> {
            itemAdapter.add(feedWithFolders);
            fastAdapter.notifyAdapterDataSetChanged();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
