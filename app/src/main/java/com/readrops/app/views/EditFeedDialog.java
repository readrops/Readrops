package com.readrops.app.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.readrops.app.R;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.viewmodels.ManageFeedsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EditFeedDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private TextInputEditText feedName;
    private TextInputEditText feedUrl;
    private Spinner folder;

    Map<String, Integer> values;

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

        viewModel.getFolders().observe(this, folders -> {
            values = new HashMap<>();
            for (Folder folder : folders) {
                if (folder.getId() != 1)
                    values.put(folder.getName(), folder.getId());
                else
                    values.put(getString(R.string.no_folder), 1);
            }

            ArrayAdapter<String> spinnerData = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(values.keySet()));
            spinnerData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            folder.setAdapter(spinnerData);
            folder.setOnItemSelectedListener(this);
        });

        return builder.create();
    }

    private void fillData(View v) {
        feedName = v.findViewById(R.id.edit_feed_name_edit_text);
        feedUrl = v.findViewById(R.id.edit_feed_url_edit_text);
        folder = v.findViewById(R.id.edit_feed_folder_spinner);

        feedName.setText(feedWithFolder.getFeed().getName());
        feedUrl.setText(feedWithFolder.getFeed().getUrl());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String folderName = (String)parent.getAdapter().getItem(position);
        feedWithFolder.getFeed().setFolderId(values.get(folderName));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
