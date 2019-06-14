package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityAddAccountBinding;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.viewmodels.AccountViewModel;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddAccountActivity extends AppCompatActivity {

    private ActivityAddAccountBinding binding;
    private AccountViewModel viewModel;

    private Account.AccountType accountType;
    private boolean forwardResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_account);
        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        accountType = getIntent().getParcelableExtra("accountType");

        int flag = getIntent().getFlags();
        forwardResult = flag == Intent.FLAG_ACTIVITY_FORWARD_RESULT;

        binding.providerImage.setImageResource(accountType.getIconRes());
        binding.providerName.setText(accountType.getName());

        binding.addAccountSkip.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            finish();
        });

        binding.addAccountName.setText(accountType.getName());
    }

    public void createAccount(View view) {
        if (fieldsAreValid()) {
            String url = binding.addAccountUrl.getText().toString().trim();
            String name = binding.addAccountName.getText().toString().trim();
            String login = binding.addAccountLogin.getText().toString().trim();
            String password = binding.addAccountPassword.getText().toString().trim();

            Account account = new Account(url, name, accountType);
            account.setLogin(login);
            account.setPassword(password);

            viewModel.login(account)
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
                                    intent.putExtra(MainActivity.ACCOUNT_KEY, account);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                } else {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra(MainActivity.ACCOUNT_KEY, account);
                                    startActivity(intent);
                                }

                                finish();
                            } else {
                                binding.addAccountValidate.setEnabled(true);
                                Toast.makeText(AddAccountActivity.this, "Impossible to login",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.addAccountLoading.setVisibility(View.GONE);
                            binding.addAccountValidate.setEnabled(true);
                            Toast.makeText(AddAccountActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
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
}
