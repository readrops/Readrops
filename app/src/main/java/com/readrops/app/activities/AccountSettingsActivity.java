package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityAccountSettingsBinding;
import com.readrops.app.viewmodels.AccountViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class AccountSettingsActivity extends AppCompatActivity {

    public static final String ACCOUNT = "ACCOUNT";

    private ActivityAccountSettingsBinding binding;
    private AccountViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_settings);
        Account account = getIntent().getParcelableExtra(ACCOUNT);

        setTitle(account.getAccountName());

        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        binding.accountSettingsFeeds.setOnClickListener(v -> {
            Intent intent = new Intent(getApplication(), ManageFeedsFoldersActivity.class);
            intent.putExtra(ManageFeedsFoldersActivity.ACCOUNT, account);
            startActivity(intent);
        });

        binding.accountSettingsAccount.setOnClickListener(v -> {
            if (account.getAccountType() != Account.AccountType.LOCAL) {
                Intent intent = new Intent(getApplication(), AddAccountActivity.class);
                intent.putExtra(AddAccountActivity.EDIT_ACCOUNT, account);
                startActivity(intent);
            }

        });

        binding.accountSettingsDeleteAccount.setOnClickListener(v -> deleteAccount(account));
    }


    private void deleteAccount(Account account) {
        new MaterialDialog.Builder(this)
                .title(R.string.delete_account_question)
                .positiveText(R.string.validate)
                .negativeText(R.string.cancel)
                .onPositive(((dialog, which) -> viewModel.delete(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(AccountSettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })))
                .show();
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
