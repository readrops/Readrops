package com.readrops.app;

import android.arch.lifecycle.ViewModelProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.PageParser;
import com.readrops.readropslibrary.QueryCallback;
import com.readrops.readropslibrary.Utils.Utils;
import com.readrops.readropslibrary.localfeed.AItem;
import com.readrops.readropslibrary.localfeed.RSSNetwork;
import com.readrops.readropslibrary.localfeed.atom.ATOMEntry;
import com.readrops.readropslibrary.localfeed.json.JSONFeed;
import com.readrops.readropslibrary.localfeed.json.JSONItem;
import com.readrops.readropslibrary.localfeed.rss.RSSFeed;
import com.readrops.readropslibrary.localfeed.rss.RSSItem;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String url = "https://framablog.org/";

    private RecyclerView recyclerView;
    private MainAdapter adapter;

    private List<Item> itemList;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Thread thread = new Thread(() -> {
            String imageUrl = PageParser.getOGImageLink("https://usbeketrica.com/galerie/dennis-osadebe-portrait-of-a-bright-generation");
            Log.d("", "");

            runOnUiThread(() -> {
                getItems();
            });
        });

        thread.start();*/

        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(MainViewModel.class);

        viewModel.getItems().observe(this, (List<Item> items) -> {
            this.itemList = items;
            initRecyclerView();
        });

        viewModel.sync();
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
}
