package com.readrops.app.views;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.DialogFragment;

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
import java.util.Map;
import java.util.TreeMap;

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
            values = new TreeMap<>(String::compareTo);
            for (Folder folder : folders) {
                if (folder.getId() != 1)
                    values.put(folder.getName(), folder.getId());
                else
                    values.put(getString(R.string.no_folder), 1);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(values.keySet()));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            folder.setAdapter(adapter);
            folder.setOnItemSelectedListener(this);

            if (!feedWithFolder.getFolder().getName().equals("reserved"))
                folder.setSelection(adapter.getPosition(feedWithFolder.getFolder().getName()));
            else
                folder.setSelection(adapter.getPosition(getString(R.string.no_folder)));
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
