package com.readrops.app.fragments.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.readrops.app.R;
import com.readrops.app.database.Database;
import com.readrops.app.utils.feedscolors.FeedsColorsIntentService;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        AtomicBoolean serviceStarted = new AtomicBoolean(false);
        Preference feedsColorsPreference = findPreference("reload_feeds_colors");

        feedsColorsPreference.setOnPreferenceClickListener(preference -> {
            Database database = Database.getInstance(getContext());

            database.feedDao().getAllFeeds().observe(getActivity(), feeds -> {
                if (!serviceStarted.get()) {
                    Intent intent = new Intent(getContext(), FeedsColorsIntentService.class);
                    intent.putParcelableArrayListExtra(FeedsColorsIntentService.FEEDS, new ArrayList<>(feeds));

                    getContext().startService(intent);
                    serviceStarted.set(true);
                }
            });

            return true;
        });
    }
}
