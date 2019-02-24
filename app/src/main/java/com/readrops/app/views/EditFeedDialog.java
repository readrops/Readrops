package com.readrops.app.views;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.FragmentActivity;
import android.widget.Button;
import android.widget.Spinner;

import com.readrops.app.R;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.viewmodels.ManageFeedsViewModel;

public class EditFeedDialog extends Dialog {

    private TextInputEditText feedName;
    private TextInputEditText feedUrl;
    private Spinner folder;
    private Button validate;

    private FeedWithFolder feedWithFolder;
    private ManageFeedsViewModel viewModel;

    public EditFeedDialog(@NonNull Context context, FeedWithFolder feedWithFolder) {
        super(context);
        if (context instanceof Activity)
            setOwnerActivity((Activity) context);

        this.feedWithFolder = feedWithFolder;
        viewModel = ViewModelProviders.of((FragmentActivity) getOwnerActivity()).get(ManageFeedsViewModel.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_feed_layout);

        feedName =  findViewById(R.id.edit_feed_name_edit_text);
        feedUrl = findViewById(R.id.edit_feed_url_edit_text);
        folder = findViewById(R.id.edit_feed_folder_spinner);
        validate = findViewById(R.id.edit_feed_validate);
    }
}
