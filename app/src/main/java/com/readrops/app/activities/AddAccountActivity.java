package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.readrops.app.R;
import com.readrops.readropsdb.entities.account.Account;
import com.readrops.readropsdb.entities.account.AccountType;
import com.readrops.app.databinding.ActivityAddAccountBinding;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.AccountViewModel;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT_TYPE;
import static com.readrops.app.utils.ReadropsKeys.EDIT_ACCOUNT;

public class AddAccountActivity extends AppCompatActivity {

    private ActivityAddAccountBinding binding;
    private AccountViewModel viewModel;

    private AccountType accountType;
    private boolean forwardResult, editAccount;

    private Account accountToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_account);
        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        accountType = getIntent().getParcelableExtra(ACCOUNT_TYPE);

        int flag = getIntent().getFlags();
        forwardResult = flag == Intent.FLAG_ACTIVITY_FORWARD_RESULT;

        accountToEdit = getIntent().getParcelableExtra(EDIT_ACCOUNT);

        if (forwardResult || accountToEdit != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            if (accountToEdit != null) {
                viewModel.setAccountType(accountToEdit.getAccountType());
                editAccount = true;
                fillFields();
            } else {
                viewModel.setAccountType(accountType);

                binding.providerImage.setImageResource(accountType.getIconRes());
                binding.providerName.setText(accountType.getName());
                binding.addAccountName.setText(accountType.getName());
                if (accountType.getName() == R.string.freshrss) {
                    binding.addAccountPasswordLayout.setHelperText(getString(R.string.password_helper));
                }
            }
        } catch (Exception e) {
            // TODO : see how to handle this exception
            e.printStackTrace();
        }

    }

    public void createAccount(View view) {
        if (fieldsAreValid()) {
            String url = binding.addAccountUrl.getText().toString().trim();
            String name = binding.addAccountName.getText().toString().trim();
            String login = binding.addAccountLogin.getText().toString().trim();
            String password = binding.addAccountPassword.getText().toString().trim();


            if (editAccount) {
                accountToEdit.setUrl(url);
                accountToEdit.setAccountName(name);
                accountToEdit.setLogin(login);
                accountToEdit.setPassword(password);

                updateAccount();
            } else {
                Account account = new Account(url, name, accountType);
                account.setLogin(login);
                account.setPassword(password);

                viewModel.login(account, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Boolean>() {

                            @Override
                            public void onSubscribe(Disposable d) {
                                binding.addAccountLoading.setVisibility(View.VISIBLE);
                                binding.addAccountValidate.setEnabled(false);
                            }

                            @Override
                            public void onSuccess(Boolean success) {
                                binding.addAccountLoading.setVisibility(View.GONE);

                                if (success) {
                                    saveLoginPassword(account);

                                    if (forwardResult) {
                                        Intent intent = new Intent();
                                        intent.putExtra(ACCOUNT, account);
                                        setResult(RESULT_OK, intent);
                                        finish();

                                    } else {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.putExtra(ACCOUNT, account);
                                        startActivity(intent);
                                    }

                                    finish();
                                } else {
                                    binding.addAccountValidate.setEnabled(true);
                                    Utils.showSnackbar(binding.addAccountRoot, getString(R.string.login_failed));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                binding.addAccountLoading.setVisibility(View.GONE);
                                binding.addAccountValidate.setEnabled(true);

                                Utils.showSnackbar(binding.addAccountRoot, e.getMessage());
                            }
                        });
            }

        }
    }

    private boolean fieldsAreValid() {
        boolean valid = true;

        if (binding.addAccountUrl.getText().toString().trim().isEmpty()) {
            binding.addAccountUrl.setError(getString(R.string.empty_field));
            valid = false;
        } else if (!Patterns.WEB_URL.matcher(binding.addAccountUrl.getText().toString().trim()).matches()) {
            binding.addAccountUrl.setError(getString(R.string.wrong_url));
            valid = false;
        }

        if (binding.addAccountName.getText().toString().trim().isEmpty()) {
            binding.addAccountName.setError(getString(R.string.empty_field));
            valid = false;
        }

        if (binding.addAccountLogin.getText().toString().trim().isEmpty()) {
            binding.addAccountLogin.setError(getString(R.string.empty_field));
            valid = false;
        }

        if (binding.addAccountPassword.getText().toString().trim().isEmpty()) {
            binding.addAccountPassword.setError(getString(R.string.empty_field));
            valid = false;
        }

        return valid;
    }

    private void saveLoginPassword(Account account) {
        SharedPreferencesManager.writeValue(this, account.getLoginKey(), account.getLogin());
        SharedPreferencesManager.writeValue(this, account.getPasswordKey(), account.getPassword());

        account.setLogin(null);
        account.setPassword(null);
    }

    private void fillFields() {
        binding.providerImage.setImageResource(accountToEdit.getAccountType().getIconRes());
        binding.providerName.setText(accountToEdit.getAccountType().getName());

        binding.addAccountUrl.setText(accountToEdit.getUrl());
        binding.addAccountName.setText(accountToEdit.getAccountName());
        binding.addAccountLogin.setText(SharedPreferencesManager.readString(this, accountToEdit.getLoginKey()));
        binding.addAccountPassword.setText(SharedPreferencesManager.readString(this, accountToEdit.getPasswordKey()));
    }

    private void updateAccount() {
        viewModel.login(accountToEdit, false)
                .doOnError(throwable -> Utils.showSnackbar(binding.addAccountRoot, throwable.getMessage()))
                .flatMapCompletable(b -> {
                    if (b) {
                        saveLoginPassword(accountToEdit);
                        return viewModel.update(accountToEdit);
                    } else {
                        runOnUiThread(() -> {
                            binding.addAccountLoading.setVisibility(View.GONE);
                            binding.addAccountValidate.setEnabled(true);
                            Utils.showSnackbar(binding.addAccountRoot, getString(R.string.login_failed));
                        });

                        return Completable.never();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        binding.addAccountLoading.setVisibility(View.VISIBLE);
                        binding.addAccountValidate.setEnabled(false);
                    }

                    @Override
                    public void onComplete() {
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.addAccountLoading.setVisibility(View.GONE);
                        binding.addAccountValidate.setEnabled(true);

                        Utils.showSnackbar(binding.addAccountRoot, e.getMessage());
                    }
                });
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                createAccount(null);
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }
}
