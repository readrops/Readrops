package com.readrops.app.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.readrops.app.R;
import com.readrops.db.entities.account.Account;

import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.SETTINGS;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Account account = getIntent().getParcelableExtra(ACCOUNT);

        SettingsKey settingsKey = SettingsKey.values()[getIntent().getIntExtra(SETTINGS, -1)];
        Fragment fragment = null;

        switch (settingsKey) {
            case ACCOUNT_SETTINGS:
                fragment = AccountSettingsFragment.newInstance(account);
                setTitle(account.getAccountName());
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                setTitle(R.string.settings);
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_activity_fragment, fragment)
                .commit();
    }

    public enum SettingsKey {
        ACCOUNT_SETTINGS,
        SETTINGS
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
