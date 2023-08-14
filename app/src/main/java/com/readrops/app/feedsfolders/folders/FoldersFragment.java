package com.readrops.app.feedsfolders.folders;


import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.api.utils.exceptions.ConflictException;
import com.readrops.api.utils.exceptions.UnknownFormatException;
import com.readrops.app.R;
import com.readrops.app.databinding.FragmentFoldersBinding;
import com.readrops.app.feedsfolders.ManageFeedsFoldersViewModel;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;

import org.koin.android.compat.SharedViewModelCompat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class FoldersFragment extends Fragment {

    private FoldersAdapter adapter;
    private FragmentFoldersBinding binding;
    private ManageFeedsFoldersViewModel viewModel;

    private Account account;

    public FoldersFragment() {
        // Required empty public constructor
    }

    public static FoldersFragment newInstance(Account account) {
        FoldersFragment fragment = new FoldersFragment();

        Bundle args = new Bundle();
        args.putParcelable(ACCOUNT, account);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = getArguments().getParcelable(ACCOUNT);

        if (account.getLogin() == null)
            account.setLogin(SharedPreferencesManager.readString(account.getLoginKey()));
        if (account.getPassword() == null)
            account.setPassword(SharedPreferencesManager.readString(account.getPasswordKey()));

        adapter = new FoldersAdapter(this::openFolderOptionsDialog);
        viewModel = SharedViewModelCompat.sharedViewModel(this, ManageFeedsFoldersViewModel.class).getValue();

        viewModel.setAccount(account);
        viewModel.getFeedCountByAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(Integer feedCount) {
                        adapter.setTotalFeedCount(feedCount);
                        getFoldersWithFeedCount();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Utils.showSnackbar(binding.foldersRoot, e.getMessage());
                    }
                });
    }

    private void getFoldersWithFeedCount() {
        viewModel.getFoldersWithFeedCount().observe(this, folders -> {
            adapter.submitList(folders);

            if (!folders.isEmpty()) {
                binding.foldersEmptyList.setVisibility(View.GONE);
            } else {
                binding.foldersEmptyList.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFoldersBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.foldersList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.foldersList.setAdapter(adapter);
    }

    public void editFolder(Folder folder) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.edit_folder)
                .positiveText(R.string.validate)
                .input(getString(R.string.folder), folder.getName(), false, (dialog, input) -> {
                    folder.setName(input.toString());

                    viewModel.updateFolder(folder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> {
                                String message;
                                if (throwable instanceof ConflictException)
                                    message = getString(R.string.folder_already_exists);
                                else if (throwable instanceof UnknownFormatException)
                                    message = getString(R.string.folder_bad_format);
                                else if (throwable instanceof Resources.NotFoundException)
                                    message = getString(R.string.folder_doesnt_exist);
                                else
                                    message = getString(R.string.error_occured);

                                Utils.showSnackbar(binding.foldersRoot, message);
                            })
                            .subscribe();
                })
                .show();
    }

    public void deleteFolder(Folder folder) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_folder)
                .negativeText(R.string.cancel)
                .positiveText(R.string.validate)
                .onPositive((dialog, which) -> viewModel.deleteFolder(folder)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> {
                            String message;
                            if (throwable instanceof Resources.NotFoundException)
                                message = getString(R.string.folder_doesnt_exist);
                            else
                                message = throwable.getMessage();

                            Utils.showSnackbar(binding.foldersRoot, message);
                        })
                        .subscribe())
                .show();
    }

    private void openFolderOptionsDialog(Folder folder) {
        FolderOptionsDialogFragment fragment = FolderOptionsDialogFragment.Companion.newInstance(folder);

        getChildFragmentManager()
                .beginTransaction()
                .add(fragment, "")
                .commit();
    }
}

