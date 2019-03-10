package com.readrops.app.activities;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.readrops.app.R;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.Utils;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddFeedActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText feedInput;
    private Button load;
    private ProgressBar progressBar;
    private Button validate;
    private RecyclerView recyclerView;

    private ItemAdapter<ParsingResult> itemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        feedInput = findViewById(R.id.add_feed_text_input);
        load = findViewById(R.id.add_feed_load);
        validate = findViewById(R.id.add_feed_ok);
        progressBar = findViewById(R.id.fadd_feed_loading);
        recyclerView = findViewById(R.id.add_feed_results);

        load.setOnClickListener(this);
        validate.setOnClickListener(this);

        itemAdapter = new ItemAdapter<>();
        FastAdapter<ParsingResult> fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.withSelectable(true);
        fastAdapter.withOnClickListener((v, adapter, item, position) -> {
            return true;
        });

        recyclerView.setAdapter(fastAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_feed_load:
                if (isValidUrl()) {
                    progressBar.setVisibility(View.VISIBLE);
                    loadFeed();
                }
                break;
            case R.id.add_feed_ok:
                exit();
                break;
        }
    }

    private boolean isValidUrl() {
        String url = feedInput.getText().toString().trim();

        if (url.isEmpty()) {
            feedInput.setError(getString(R.string.add_feed_empty_field));
            return false;
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            feedInput.setError(getString(R.string.add_feed_wrong_url));
            return false;
        } else
            return true;
    }

    private void exit() {

    }

    private void loadFeed() {
        String url = feedInput.getText().toString().trim();

        final String finalUrl;
        if (!(url.contains(Utils.HTTP_PREFIX) || url.contains(Utils.HTTPS_PREFIX)))
            finalUrl = Utils.HTTPS_PREFIX + url;
        else
            finalUrl = url;

        Single.create((SingleOnSubscribe<List<ParsingResult>>) emitter -> {
            RSSNetwork rssApi = new RSSNetwork();
            List<ParsingResult> results = new ArrayList<>();

            if (rssApi.isUrlFeedLink(finalUrl)) {
                ParsingResult parsingResult = new ParsingResult(finalUrl, null);
                results.add(parsingResult);

            } else
                results.addAll(HtmlParser.getFeedLink(finalUrl));

            emitter.onSuccess(results);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<ParsingResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<ParsingResult> parsingResultList) {
                        displayResults(parsingResultList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void displayResults(List<ParsingResult> parsingResultList) {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        itemAdapter.add(parsingResultList);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
}
