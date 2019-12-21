package com.readrops.app.fragments.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.readrops.app.R;
import com.readrops.app.database.Database;
import com.readrops.app.utils.feedscolors.FeedsColorsIntentService;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.readrops.app.utils.ReadropsKeys.FEEDS;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        Preference feedsColorsPreference = findPreference("reload_feeds_colors");
        Preference themePreference = findPreference("dark_theme");

        AtomicBoolean serviceStarted = new AtomicBoolean(false);
        feedsColorsPreference.setOnPreferenceClickListener(preference -> {
            Database database = Database.getInstance(getContext());

            database.feedDao().getAllFeeds().observe(getActivity(), feeds -> {
                if (!serviceStarted.get()) {
                    Intent intent = new Intent(getContext(), FeedsColorsIntentService.class);
                    intent.putParcelableArrayListExtra(FEEDS, new ArrayList<>(feeds));

                    getContext().startService(intent);
                    serviceStarted.set(true);
                }
            });

            return true;
        });

        themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean darkTheme = Boolean.parseBoolean(newValue.toString());

            if (darkTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            return true;
        });
    }

}
