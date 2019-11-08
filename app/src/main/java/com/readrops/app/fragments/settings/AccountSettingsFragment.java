package com.readrops.app.fragments.settings;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.readrops.app.utils.OPMLMatcher;
import com.readrops.app.viewmodels.AccountViewModel;
import com.readrops.readropslibrary.opml.OPMLParser;
import com.readrops.readropslibrary.opml.model.OPML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.EDIT_ACCOUNT;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = AccountSettingsFragment.class.getSimpleName();

    private static final int OPEN_OPML_FILE_REQUEST = 1;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;

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
        Preference opmlPref = findPreference("opml_import_export");

        if (account.is(AccountType.LOCAL))
            credentialsPref.setVisible(false);

        if (!account.is(AccountType.LOCAL))
            opmlPref.setVisible(false);

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

        opmlPref.setOnPreferenceClickListener(preference -> {
            new MaterialDialog.Builder(getActivity())
                    .items(R.array.opml_import_export)
                    .itemsCallback(((dialog, itemView, position, text) -> {
                        if (position == 0) {
                            openOPMLFile();
                        } else {
                            if (isExternalStoragePermissionGranted())
                                exportAsOPMLFile();
                            else
                                requestExternalStoragePermission();
                        }
                    }))
                    .show();
            return true;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        viewModel.setAccount(account);
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

    private void openOPMLFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");

        startActivityForResult(intent, OPEN_OPML_FILE_REQUEST);
    }

    private void exportAsOPMLFile() {
        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            File file = new File(filePath, "subscriptions.opml");

            final OutputStream outputStream = new FileOutputStream(file);

            viewModel.getFoldersWithFeeds()
                    .flatMapCompletable(folderListMap -> {
                        OPML opml = OPMLMatcher.INSTANCE.foldersAndFeedsToOPML(folderListMap, getContext());

                        return OPMLParser.write(opml, outputStream);
                    }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate(() -> {
                        try {
                            outputStream.flush();
                            outputStream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: ");
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private boolean isExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == OPEN_OPML_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.opml_processing)
                    .content(R.string.operation_takes_time)
                    .progress(true, 100)
                    .cancelable(false)
                    .show();

            parseOPMLFile(uri, dialog);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //if (shouldShowRequestPermissionRationale(permissions[0]))

            } else {
                exportAsOPMLFile();
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void parseOPMLFile(Uri uri, MaterialDialog dialog) {
        OPMLParser.parse(uri, getContext())
                .flatMapCompletable(opml -> viewModel.insertOPMLFoldersAndFeeds(opml))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();

                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.processing_file_failed)
                                .neutralText(R.string.cancel)
                                .iconRes(R.drawable.ic_error)
                                .show();
                    }
                });
    }
}
