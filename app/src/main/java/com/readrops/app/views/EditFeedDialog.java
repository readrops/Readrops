package com.readrops.app.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Spinner;

import com.readrops.app.R;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.viewmodels.ManageFeedsViewModel;

public class EditFeedDialog extends DialogFragment {

    private TextInputEditText feedName;
    private TextInputEditText feedUrl;
    private Spinner folder;

    private FeedWithFolder feedWithFolder;
    private ManageFeedsViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(getActivity()).get(ManageFeedsViewModel.class);
        feedWithFolder = getArguments().getParcelable("feedWithFolder");

        View v = getActivity().getLayoutInflater().inflate(R.layout.edit_feed_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.edit_feed))
                .setPositiveButton(getString(R.string.validate), (dialog, which) -> {
                    Feed feed = feedWithFolder.getFeed();
                    feed.setName(feedName.getText().toString().trim());
                    feed.setUrl(feedUrl.getText().toString().trim());

                    viewModel.updateFeedWithFolder(feedWithFolder);
                });

        builder.setView(v);
        fillData(v);

        return builder.create();
    }

    private void fillData(View v) {
        feedName = v.findViewById(R.id.edit_feed_name_edit_text);
        feedUrl = v.findViewById(R.id.edit_feed_url_edit_text);
        folder = v.findViewById(R.id.edit_feed_folder_spinner);

        feedName.setText(feedWithFolder.getFeed().getName());
        feedUrl.setText(feedWithFolder.getFeed().getUrl());
    }


}
