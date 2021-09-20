package com.readrops.app.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.api.opml.OPMLHelper;
import com.readrops.app.R;
import com.readrops.app.databinding.ActivityAccountTypeListBinding;
import com.readrops.app.itemslist.MainActivity;
import com.readrops.app.utils.Utils;
import com.readrops.db.entities.account.Account;
import com.readrops.db.entities.account.AccountType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.readrops.api.opml.OPMLHelper.OPEN_OPML_FILE_REQUEST;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT_TYPE;
import static com.readrops.app.utils.ReadropsKeys.FROM_MAIN_ACTIVITY;

import org.koin.android.compat.ViewModelCompat;

public class AccountTypeListActivity extends AppCompatActivity {

    private static final String TAG = AccountTypeListActivity.class.getSimpleName();

    private ActivityAccountTypeListBinding binding;
    private AccountTypeListAdapter adapter;
    private AccountViewModel viewModel;

    private boolean fromMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccountTypeListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = ViewModelCompat.getViewModel(this, AccountViewModel.class);

        setTitle(R.string.new_account);

        binding.accountTypeRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.accountTypeRecyclerview.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        fromMainActivity = getIntent().getBooleanExtra(FROM_MAIN_ACTIVITY, false);

        if (fromMainActivity)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new AccountTypeListAdapter(accountType -> {
            if (accountType != AccountType.LOCAL) {
                Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);

                if (fromMainActivity)
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                intent.putExtra(ACCOUNT_TYPE, (Parcelable) accountType);

                startActivity(intent);
                finish();
            } else {
                Account account = new Account(null, getString(AccountType.LOCAL.getTypeName()), AccountType.LOCAL);
                account.setCurrentAccount(true);

                viewModel.insert(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<Long>() {
                            @Override
                            public void onSuccess(Long id) {
                                account.setId(id.intValue());
                                goToNextActivity(account);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, e.getMessage());
                                Utils.showSnackbar(binding.accountTypeListRoot, e.getMessage());
                            }
                        });
            }

        });

        binding.accountTypeRecyclerview.setAdapter(adapter);
        adapter.setAccountTypes(getData());
    }

    private List<AccountType> getData() {
        List<AccountType> accountTypes = new ArrayList<>();

        accountTypes.add(AccountType.LOCAL);
        accountTypes.add(AccountType.NEXTCLOUD_NEWS);
        accountTypes.add(AccountType.FRESHRSS);

        return accountTypes;
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

    public void openOPMLFile(View view) {
        OPMLHelper.openFileIntent(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_OPML_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .title(R.string.opml_processing)
                    .content(R.string.operation_takes_time)
                    .progress(true, 100)
                    .cancelable(false)
                    .show();

            parseOPMLFile(uri, dialog);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void parseOPMLFile(Uri uri, MaterialDialog dialog) {
        Account account = new Account(null, getString(AccountType.LOCAL.getTypeName()), AccountType.LOCAL);
        account.setCurrentAccount(true);

        viewModel.insert(account)
                .flatMapCompletable(id -> {
                    account.setId(id.intValue());
                    viewModel.setAccount(account);

                    return viewModel.parseOPMLFile(uri, this);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                        goToNextActivity(account);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());

                        dialog.dismiss();
                        Utils.showSnackbar(binding.accountTypeListRoot, e.getMessage());
                    }
                });
    }

    private void goToNextActivity(Account account) {
        if (fromMainActivity) {
            Intent intent = new Intent();
            intent.putExtra(ACCOUNT, account);
            setResult(RESULT_OK, intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(ACCOUNT, account);

            startActivity(intent);
        }

        finish();
    }
}
