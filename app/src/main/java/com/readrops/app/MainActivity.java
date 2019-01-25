package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.readrops.app.database.entities.Item;


import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements SimpleCallback, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private MainAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    private List<Item> itemList;

    private TreeMap<LocalDateTime, Item> itemsMap;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(MainViewModel.class);
        viewModel.setSimpleCallback(this);

        viewModel.getItems().observe(this, (List<Item> items) -> {
            itemList = items;
            initRecyclerView();
        });

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        itemsMap = new TreeMap<>(LocalDateTime::compareTo);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.items_recycler_view);
        adapter = new MainAdapter(this, itemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(decoration);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSuccess() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onFailure(Exception ex) {

    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "syncing started");
        viewModel.sync();
    }
}
