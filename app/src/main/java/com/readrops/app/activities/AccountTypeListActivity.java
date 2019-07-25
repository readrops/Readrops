package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityAccountTypeListBinding;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.AccountViewModel;
import com.readrops.app.adapters.AccountTypeListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AccountTypeListActivity extends AppCompatActivity {

    private ActivityAccountTypeListBinding binding;
    private AccountTypeListAdapter adapter;
    private AccountViewModel viewModel;

    private boolean fromMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_type_list);
        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        binding.accountTypeRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.accountTypeRecyclerview.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        fromMainActivity = getIntent().getBooleanExtra("fromMainActivity", false);

        adapter = new AccountTypeListAdapter(accountType -> {
            if (!(accountType == Account.AccountType.LOCAL)) {
                Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);

                if (fromMainActivity)
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                intent.putExtra("accountType", (Parcelable) accountType);

                startActivity(intent);
                finish();
            } else
                createNewLocalAccount(accountType);

        });

        binding.accountTypeRecyclerview.setAdapter(adapter);
        adapter.setAccountTypes(getData());
    }

    private List<Account.AccountType> getData() {
        List<Account.AccountType> accountTypes = new ArrayList<>();

        accountTypes.add(Account.AccountType.LOCAL);
        accountTypes.add(Account.AccountType.NEXTCLOUD_NEWS);

        return accountTypes;
    }

    private void createNewLocalAccount(Account.AccountType accountType) {
        Account account = new Account(null, getString(accountType.getName()), accountType);
        account.setCurrentAccount(true);

        viewModel.insert(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Long>() {
                    @Override
                    public void onSuccess(Long id) {
                        account.setId(id.intValue());

                        if (fromMainActivity) {
                            Intent intent = new Intent();
                            intent.putExtra(MainActivity.ACCOUNT_KEY, account);
                            setResult(RESULT_OK, intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(MainActivity.ACCOUNT_KEY, account);

                            startActivity(intent);
                        }

                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.showSnackbar(binding.accountTypeListRoot, e.getMessage());
                    }
                });
    }
}
