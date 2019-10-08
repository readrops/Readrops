package com.readrops.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.utils.DiffCallback;
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil;
import com.readrops.app.R;
import com.readrops.app.adapters.AccountArrayAdapter;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.ReadropsItemTouchCallback;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.AddFeedsViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AddFeedActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText feedInput;
    private Button load;
    private ProgressBar progressBar;
    private Button validate;
    private RecyclerView parseResultsRecyclerView;
    private TextView resultsTextView;

    private NestedScrollView rootLayout;

    private ProgressBar feedInsertionProgressBar;
    private RecyclerView insertionResultsRecyclerView;

    private Spinner accountSpinner;
    private AccountArrayAdapter arrayAdapter;

    private ItemAdapter<ParsingResult> parseItemsAdapter;
    private ItemAdapter<FeedInsertionResult> insertionResultsAdapter;
    FastAdapter<ParsingResult> fastAdapter;

    private AddFeedsViewModel viewModel;
    private ArrayList<Feed> feedsToUpdate;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        feedInput = findViewById(R.id.add_feed_text_input);
        load = findViewById(R.id.add_feed_load);
        validate = findViewById(R.id.add_feed_ok);
        progressBar = findViewById(R.id.add_feed_loading);
        parseResultsRecyclerView = findViewById(R.id.add_feed_results);
        resultsTextView = findViewById(R.id.add_feed_results_text_view);
        feedInsertionProgressBar = findViewById(R.id.add_feed_insert_progressbar);
        insertionResultsRecyclerView = findViewById(R.id.add_feed_inserted_results_recyclerview);
        accountSpinner = findViewById(R.id.add_feed_account_spinner);

        rootLayout = findViewById(R.id.add_feed_root);

        load.setOnClickListener(this);
        validate.setOnClickListener(this);
        validate.setEnabled(false);

        feedInput.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            int drawablePos = (feedInput.getRight() - feedInput.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width());
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= drawablePos) {
                feedInput.setText("");
                return true;
            }

            return false;
        });

        viewModel = ViewModelProviders.of(this).get(AddFeedsViewModel.class);

        parseItemsAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(parseItemsAdapter);
        fastAdapter.withSelectable(true);
        fastAdapter.withOnClickListener((v, adapter, item, position) -> {
            if (item.isChecked()) {
                item.setChecked(false);
                fastAdapter.notifyAdapterItemChanged(position);
            } else {
                item.setChecked(true);
                fastAdapter.notifyAdapterItemChanged(position);
            }

            validate.setEnabled(recyclerViewHasCheckedItems());

            return true;
        });

        parseResultsRecyclerView.setAdapter(fastAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        parseResultsRecyclerView.setLayoutManager(layoutManager);

        new ItemTouchHelper(new ReadropsItemTouchCallback(this,
                new ReadropsItemTouchCallback.Config.Builder()
                        .swipeDirs(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                        .swipeCallback((viewHolder, direction) -> {
                            parseItemsAdapter.remove(viewHolder.getAdapterPosition());

                            if (parseItemsAdapter.getAdapterItemCount() == 0) {
                                resultsTextView.setVisibility(View.GONE);
                                parseResultsRecyclerView.setVisibility(View.GONE);
                            }
                        })
                        .build()))
                .attachToRecyclerView(parseResultsRecyclerView);

        insertionResultsAdapter = new ItemAdapter<>();
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);
        insertionResultsRecyclerView.setAdapter(FastAdapter.with(insertionResultsAdapter));
        insertionResultsRecyclerView.setLayoutManager(layoutManager1);

        viewModel.getAccounts().observe(this, accounts -> {
            arrayAdapter = new AccountArrayAdapter(this, accounts);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            accountSpinner.setAdapter(arrayAdapter);
        });

        feedsToUpdate = new ArrayList<>();
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
                insertionResultsAdapter.clear();
                insertFeeds();
                break;
        }
    }

    private boolean isValidUrl() {
        String url = feedInput.getText().toString().trim();

        if (url.isEmpty()) {
            feedInput.setError(getString(R.string.empty_field));
            return false;
        } else if (!Patterns.WEB_URL.matcher(url).matches()) {
            feedInput.setError(getString(R.string.wrong_url));
            return false;
        } else
            return true;
    }

    private boolean recyclerViewHasCheckedItems() {
        for (ParsingResult result : parseItemsAdapter.getAdapterItems()) {
            if (result.isChecked())
                return true;
        }

        return false;
    }

    private void disableParsingResult(ParsingResult parsingResult) {
        for (ParsingResult result : parseItemsAdapter.getAdapterItems()) {
            if (result.getUrl().equals(parsingResult.getUrl())) {
                result.setChecked(false);
                fastAdapter.notifyAdapterItemChanged(parseItemsAdapter.getAdapterPosition(result));
            }
        }
    }

    private void loadFeed() {
        String url = feedInput.getText().toString().trim();

        final String finalUrl;
        if (!(url.contains(Utils.HTTP_PREFIX) || url.contains(Utils.HTTPS_PREFIX)))
            finalUrl = Utils.HTTPS_PREFIX + url;
        else
            finalUrl = url;

        viewModel.parseUrl(finalUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<ParsingResult>>() {
                    @Override
                    public void onSuccess(List<ParsingResult> parsingResultList) {
                        displayParseResults(parsingResultList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.showSnackbar(rootLayout, e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void displayParseResults(List<ParsingResult> parsingResultList) {
        if (!parsingResultList.isEmpty()) {
            parseResultsRecyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            resultsTextView.setVisibility(View.VISIBLE);

            DiffUtil.DiffResult diffResult = FastAdapterDiffUtil.calculateDiff(parseItemsAdapter, parsingResultList, new DiffCallback<ParsingResult>() {
                @Override
                public boolean areItemsTheSame(ParsingResult oldItem, ParsingResult newItem) {
                    return oldItem.getUrl().equals(newItem.getUrl());
                }

                @Override
                public boolean areContentsTheSame(ParsingResult oldItem, ParsingResult newItem) {
                    return oldItem.getUrl().equals(newItem.getUrl()) &&
                            oldItem.isChecked() == newItem.isChecked();
                }

                @Nullable
                @Override
                public Object getChangePayload(ParsingResult oldItem, int oldItemPosition, ParsingResult newItem, int newItemPosition) {
                    newItem.setChecked(oldItem.isChecked());
                    return newItem;
                }
            }, false);

            FastAdapterDiffUtil.set(parseItemsAdapter, diffResult);
            validate.setEnabled(recyclerViewHasCheckedItems());
        } else
            progressBar.setVisibility(View.GONE);
    }

    private void insertFeeds() {
        feedInsertionProgressBar.setVisibility(View.VISIBLE);
        validate.setEnabled(false);

        List<ParsingResult> feedsToInsert = new ArrayList<>();
        for (ParsingResult result : parseItemsAdapter.getAdapterItems()) {
            if (result.isChecked())
                feedsToInsert.add(result);
        }

        Account account = (Account) accountSpinner.getSelectedItem();

        account.setLogin(SharedPreferencesManager.readString(this, account.getLoginKey()));
        account.setPassword(SharedPreferencesManager.readString(this, account.getPasswordKey()));

        viewModel.addFeeds(feedsToInsert, account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<FeedInsertionResult>>() {
                    @Override
                    public void onSuccess(List<FeedInsertionResult> feedInsertionResults) {
                        displayInsertionResults(feedInsertionResults);
                    }

                    @Override
                    public void onError(Throwable e) {
                        feedInsertionProgressBar.setVisibility(View.GONE);
                        validate.setEnabled(true);
                        Utils.showSnackbar(rootLayout, e.getMessage());
                    }
                });
    }

    private void displayInsertionResults(List<FeedInsertionResult> feedInsertionResults) {
        feedInsertionProgressBar.setVisibility(View.GONE);
        insertionResultsRecyclerView.setVisibility(View.VISIBLE);

        for (FeedInsertionResult feedInsertionResult : feedInsertionResults) {
            if (feedInsertionResult.getFeed() != null)
                feedsToUpdate.add(feedInsertionResult.getFeed());

            disableParsingResult(feedInsertionResult.getParsingResult());
        }

        insertionResultsAdapter.add(feedInsertionResults);
        validate.setEnabled(recyclerViewHasCheckedItems());
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

    @Override
    public void finish() {
        if (!feedsToUpdate.isEmpty()) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("feedIds", feedsToUpdate);

            setResult(RESULT_OK, intent);
        }

        super.finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                onClick(load);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
