package com.readrops.app.fragments.settings;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.activities.AddAccountActivity;
import com.readrops.app.activities.ManageFeedsFoldersActivity;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.database.entities.account.AccountType;
import com.readrops.app.viewmodels.AccountViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.EDIT_ACCOUNT;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private Account account;
    private AccountViewModel viewModel;

    public AccountSettingsFragment() {

    }

    public static AccountSettingsFragment newInstance(Account account) {
        AccountSettingsFragment fragment = new AccountSettingsFragment();
        Bundle args = new Bundle();

        args.putParcelable(ACCOUNT, account);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.acount_preferences);

        account = getArguments().getParcelable(ACCOUNT);

        Preference feedsFoldersPref = findPreference("feeds_folders_key");
        Preference credentialsPref = findPreference("credentials_key");
        Preference deleteAccountPref = findPreference("delete_account_key");

        if (account.is(AccountType.LOCAL))
            credentialsPref.setVisible(false);

        feedsFoldersPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), ManageFeedsFoldersActivity.class);
            intent.putExtra(ACCOUNT, account);
            startActivity(intent);

            return true;
        });

        credentialsPref.setOnPreferenceClickListener(preference -> {
            if (!account.isLocal()) {
                Intent intent = new Intent(getContext(), AddAccountActivity.class);
                intent.putExtra(EDIT_ACCOUNT, account);
                startActivity(intent);
            }

            return true;
        });

        deleteAccountPref.setOnPreferenceClickListener(preference -> {
            deleteAccount();
            return true;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
    }

    private void deleteAccount() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.delete_account_question)
                .positiveText(R.string.validate)
                .negativeText(R.string.cancel)
                .onPositive(((dialog, which) -> viewModel.delete(account)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                getActivity().finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })))
                .show();
    }
}
