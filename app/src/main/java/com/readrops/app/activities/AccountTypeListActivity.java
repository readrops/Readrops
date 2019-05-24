package com.readrops.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityAccountTypeListBinding;
import com.readrops.app.viewmodels.AccountViewModel;
import com.readrops.app.views.AccountType;
import com.readrops.app.views.AccountTypeListAdapter;

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
            if (!(accountType.getAccountType() == Account.AccountType.LOCAL)) {
                Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);

                if (fromMainActivity)
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                intent.putExtra("accountType", accountType);

                startActivity(intent);
                finish();
            } else
                createNewLocalAccount(accountType);

        });

        binding.accountTypeRecyclerview.setAdapter(adapter);
        adapter.setAccountTypes(getData());
    }

    private List<AccountType> getData() {
        List<AccountType> accountTypes = new ArrayList<>();

        AccountType localAccount = new AccountType(getString(R.string.local_account),
                R.drawable.ic_readrops, Account.AccountType.LOCAL);
        AccountType nextNewsAccount = new AccountType(getString(R.string.nextcloud_news),
                R.drawable.ic_nextcloud_news, Account.AccountType.NEXTCLOUD_NEWS);

        accountTypes.add(localAccount);
        accountTypes.add(nextNewsAccount);

        return accountTypes;
    }

    private void createNewLocalAccount(AccountType accountType) {
        Account account = new Account(null, accountType.getName(), Account.AccountType.LOCAL);
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
                        Toast.makeText(AccountTypeListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
