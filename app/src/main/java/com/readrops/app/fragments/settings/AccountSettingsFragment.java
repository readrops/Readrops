package com.readrops.app.fragments.settings;


import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.api.opml.OPMLHelper;
import com.readrops.api.opml.OPMLParser;
import com.readrops.app.R;
import com.readrops.app.ReadropsApp;
import com.readrops.app.activities.AddAccountActivity;
import com.readrops.app.activities.ManageFeedsFoldersActivity;
import com.readrops.app.activities.NotificationPermissionActivity;
import com.readrops.app.utils.FileUtils;
import com.readrops.app.utils.PermissionManager;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.AccountViewModel;
import com.readrops.db.entities.account.Account;
import com.readrops.db.entities.account.AccountType;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

import static android.app.Activity.RESULT_OK;
import static com.readrops.api.opml.OPMLHelper.OPEN_OPML_FILE_REQUEST;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;
import static com.readrops.app.utils.ReadropsKeys.ACCOUNT_ID;
import static com.readrops.app.utils.ReadropsKeys.EDIT_ACCOUNT;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = AccountSettingsFragment.class.getSimpleName();

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.acount_preferences);

        account = getArguments().getParcelable(ACCOUNT);

        Preference feedsFoldersPref = findPreference("feeds_folders_key");
        Preference credentialsPref = findPreference("credentials_key");
        Preference deleteAccountPref = findPreference("delete_account_key");
        Preference opmlPref = findPreference("opml_import_export");
        Preference notificationPref = findPreference("notifications");

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
                    .itemsCallback(((dialog, itemView, position, text) -> openOPMLMode(position)))
                    .show();
            return true;
        });

        notificationPref.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), NotificationPermissionActivity.class);
            intent.putExtra(ACCOUNT_ID, account.getId());

            startActivity(intent);
            return true;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        viewModel.setAccount(account);
    }

    private void deleteAccount() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.delete_account_question)
                .positiveText(R.string.validate)
                .negativeText(R.string.cancel)
                .onPositive(((dialog, which) -> {
                    SharedPreferencesManager.remove(getContext(), account.getLoginKey());
                    SharedPreferencesManager.remove(getContext(), account.getPasswordKey());

                    viewModel.delete(account)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    getActivity().finish();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Utils.showSnackbar(getView(), e.getMessage());
                                }
                            });
                }))
                .show();
    }

    private void openOPMLMode(int position) {
        if (position == 0) {
            OPMLHelper.openFileIntent(this);
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (PermissionManager.isPermissionGranted(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    exportAsOPMLFile();
                } else {
                    requestExternalStoragePermission();
                }
            } else {
                exportAsOPMLFile();
            }
        }
    }

    // region opml import

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

    private void parseOPMLFile(Uri uri, MaterialDialog dialog) {
        viewModel.parseOPMLFile(uri)
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

                        displayErrorMessage();
                    }
                });
    }

    private void displayErrorMessage() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.processing_file_failed)
                .neutralText(R.string.cancel)
                .iconRes(R.drawable.ic_error)
                .show();
    }

    //endregion

    //region opml export

    private void exportAsOPMLFile() {
        String fileName = "subscriptions.opml";

        try {
            String path = FileUtils.writeDownloadFile(getContext(), fileName, "text/xml", outputStream -> {
                viewModel.getFoldersWithFeeds()
                        .flatMapCompletable(folderListMap -> OPMLParser.write(folderListMap, outputStream))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(e -> Utils.showSnackbar(getView(), e.getMessage()))
                        .subscribe();

                return Unit.INSTANCE;
            });

            displayNotification(fileName, path);
        } catch (Exception e) {
            displayErrorMessage();
        }

    }

    private void displayNotification(String name, String absolutePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(absolutePath), "text/plain");

        Notification notification = new NotificationCompat.Builder(getContext(), ReadropsApp.OPML_EXPORT_CHANNEL_ID)
                .setContentTitle(getString(R.string.opml_export))
                .setContentText(name)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentIntent(PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
        manager.notify(2, notification);
    }

    private void requestExternalStoragePermission() {
        PermissionManager.requestPermissions(this, WRITE_EXTERNAL_STORAGE_REQUEST,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    Utils.showSnackBarWithAction(getView(), getString(R.string.external_storage_opml_export),
                            getString(R.string.try_again), v -> requestExternalStoragePermission());
                } else {
                    Utils.showSnackBarWithAction(getView(), getString(R.string.external_storage_opml_export),
                            getString(R.string.permissions), v -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                                getContext().startActivity(intent);
                            });
                }
            } else {
                exportAsOPMLFile();
            }
        }
    }

    //endregion
}
