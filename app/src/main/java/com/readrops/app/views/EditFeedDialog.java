package com.readrops.app.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.readrops.app.R;
import com.readrops.app.activities.ManageFeedsFoldersActivity;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.viewmodels.ManageFeedsFoldersViewModel;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditFeedDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {

    private TextInputEditText feedName;
    private TextInputEditText feedUrl;
    private Spinner folder;

    private Map<String, Integer> values;

    private FeedWithFolder feedWithFolder;
    private Account account;
    private ManageFeedsFoldersViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(getActivity()).get(ManageFeedsFoldersViewModel.class);

        feedWithFolder = getArguments().getParcelable("feedWithFolder");
        account = getArguments().getParcelable(ManageFeedsFoldersActivity.ACCOUNT);

        viewModel.setAccount(account);

        View v = getActivity().getLayoutInflater().inflate(R.layout.edit_feed_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.edit_feed)
                .setPositiveButton(R.string.validate, (dialog, which) -> {
                    Feed feed = feedWithFolder.getFeed();
                    feed.setName(feedName.getText().toString().trim());
                    feed.setUrl(feedUrl.getText().toString().trim());

                    viewModel.updateFeedWithFolder(feed)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                });

        builder.setView(v);
        fillData(v);

        viewModel.getFolders().observe(this, folders -> {
            values = new TreeMap<>(String::compareTo);
            values.put(getString(R.string.no_folder), 0);

            for (Folder folder : folders) {
                values.put(folder.getName(), folder.getId());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(values.keySet()));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            folder.setAdapter(adapter);
            folder.setOnItemSelectedListener(this);

            if (feedWithFolder.getFolder() != null)
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
        String folderName = (String) parent.getAdapter().getItem(position);
        int folderId = values.get(folderName);

        feedWithFolder.getFeed().setFolderId(folderId == 0 ? null : folderId);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
