package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ModelAdapter;
import com.readrops.app.database.pojo.FeedWithFolder;

public class ManageFeedsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ModelAdapter<FeedWithFolder, FeedWithFolderItem> itemAdapter;
    private ManageFeedsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_feeds);

        recyclerView = findViewById(R.id.feeds_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        itemAdapter = new ModelAdapter<>(FeedWithFolderItem::new);

        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(ManageFeedsViewModel.class);
        viewModel.getFeedsWithFolder().observe(this, feedWithFolders -> itemAdapter.add(feedWithFolders));
    }
}
