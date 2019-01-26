package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import com.readrops.app.database.entities.Item;


import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements SimpleCallback, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private MainItemListAdapter adapter;
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

        itemsMap = new TreeMap<>(LocalDateTime::compareTo);
        itemList = new ArrayList<>();

        viewModel.getItems().observe(this, (List<Item> items) -> {
            for (Item item : items) {
                itemsMap.put(item.getFormatedDate(), item);
            }

            adapter.submitList(items);
        });

        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(this);

        initRecyclerView();

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
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.items_recycler_view);

        adapter = new MainItemListAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration decoration = new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(decoration);

        recyclerView.setAdapter(adapter);
    }

    private void updateList() {

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
