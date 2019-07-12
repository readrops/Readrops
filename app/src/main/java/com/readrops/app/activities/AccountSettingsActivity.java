package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityAccountSettingsBinding;

public class AccountSettingsActivity extends AppCompatActivity {

    public static final String ACCOUNT = "ACCOUNT";

    private ActivityAccountSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_settings);
        Account account = getIntent().getParcelableExtra(ACCOUNT);

        setTitle(account.getAccountName());

        binding.accountSettingsFeeds.setOnClickListener(v -> {
            Intent intent = new Intent(getApplication(), ManageFeedsActivity.class);
            intent.putExtra(ManageFeedsActivity.ACCOUNT, account);
            startActivity(intent);
        });

        binding.accountSettingsAccount.setOnClickListener(v -> {
            if (account.getAccountType() != Account.AccountType.LOCAL) {
                Intent intent = new Intent(getApplication(), AddAccountActivity.class);
                intent.putExtra(AddAccountActivity.EDIT_ACCOUNT, account);
                startActivity(intent);
            }

        });
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
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
