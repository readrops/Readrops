package com.readrops.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.readrops.app.utils.Utils;
import com.readrops.app.utils.HtmlParser;
import com.readrops.readropslibrary.ParsingResult;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddFeedDialog extends Dialog implements View.OnClickListener {

    private int layoutId;
    private Button button;
    private TextInputEditText textInputEditText;
    private ProgressBar progressBar;
    private TextView errorTextView;

    private RecyclerView recyclerView;
    private AddFeedListAdapter adapter;

    public AddFeedDialog(@NonNull Context context, int layoutId) {
        super(context);
        if (context instanceof Activity)
            setOwnerActivity((Activity) context);
        this.layoutId = layoutId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        button = findViewById(R.id.add_feed_validate);
        button.setOnClickListener(this);
        textInputEditText = findViewById(R.id.add_feed_edit_text);
        progressBar = findViewById(R.id.add_feed_progressbar);
        errorTextView = findViewById(R.id.add_feed_error);
    }


    @Override
    public void onClick(View view) {
        if (isValidUrl()) {
            if (recyclerView != null && recyclerView.getVisibility() == View.VISIBLE)
                recyclerView.setVisibility(View.GONE);
            if (errorTextView.getVisibility() == View.VISIBLE)
                errorTextView.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);
            parseUrl();
        }
    }

    private boolean isValidUrl() {
        String url = textInputEditText.getText().toString().trim();

        if (url.isEmpty()) {
            textInputEditText.setError(getContext().getString(R.string.add_feed_empty_field));
            return false;
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            textInputEditText.setError(getContext().getString(R.string.add_feed_wrong_url));
            return false;
        } else
            return true;
    }

    private void parseUrl() {
        String url = textInputEditText.getText().toString().trim();

        final String finalUrl;
        if (!(url.contains(Utils.HTTP_PREFIX) || url.contains(Utils.HTTPS_PREFIX)))
            finalUrl = Utils.HTTPS_PREFIX + url;
        else
            finalUrl = url;

        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                RSSNetwork rssApi = new RSSNetwork();
                if (rssApi.isUrlFeedLink(finalUrl)) {
                    ParsingResult parsingResult = new ParsingResult(finalUrl, null);
                    handler.post(() -> ((MainActivity) getOwnerActivity()).insertNewFeed(parsingResult));
                    dismiss();
                } else {
                    List<ParsingResult> results = HtmlParser.getFeedLink(finalUrl);

                    if (results.size() > 0)
                        handler.post(() -> displayResults(results));
                    else
                        handler.post(() -> displayError(R.string.add_feed_no_result));
                }
            } catch (Exception e) {
                if (e instanceof UnknownHostException)
                    handler.post(() -> displayError(R.string.add_feed_unknownhost_error));
                else
                    handler.post(() -> displayError(R.string.add_feed_connexion_error));
            }


        });
    }

    private void displayResults(List<ParsingResult> results) {
        recyclerView = findViewById(R.id.add_feed_recyclerview);
        adapter = new AddFeedListAdapter(results);

        adapter.setOnItemClickListener((parsingResult -> {
            ((MainActivity) getOwnerActivity()).insertNewFeed(parsingResult);
            dismiss();
        }));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), ((LinearLayoutManager) layoutManager).getOrientation());
        recyclerView.addItemDecoration(decoration);

        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void displayError(@StringRes int stringId) {
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(stringId);
    }
}
