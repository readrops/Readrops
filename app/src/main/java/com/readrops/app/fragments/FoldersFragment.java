package com.readrops.app.fragments;


import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.activities.ManageFeedsFoldersActivity;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.databinding.FragmentFoldersBinding;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.ManageFeedsFoldersViewModel;
import com.readrops.app.adapters.FoldersAdapter;
import com.readrops.readropslibrary.utils.ConflictException;
import com.readrops.readropslibrary.utils.UnknownFormatException;

import io.reactivex.android.schedulers.AndroidSchedulers;
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
        args.putParcelable(ManageFeedsFoldersActivity.ACCOUNT, account);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = getArguments().getParcelable(ManageFeedsFoldersActivity.ACCOUNT);

        if (account.getLogin() == null)
            account.setLogin(SharedPreferencesManager.readString(getContext(), account.getLoginKey()));
        if (account.getPassword() == null)
            account.setPassword(SharedPreferencesManager.readString(getContext(), account.getPasswordKey()));

        adapter = new FoldersAdapter(this::editFolder);
        viewModel = ViewModelProviders.of(this).get(ManageFeedsFoldersViewModel.class);

        viewModel.setAccount(account);
        viewModel.getFolders().observe(this, folders -> adapter.submitList(folders));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_folders, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.foldersList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.foldersList.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteFolder(adapter.getFolder(viewHolder.getAdapterPosition()), viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.foldersList);
    }

    private void editFolder(Folder folder) {
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

    private void deleteFolder(Folder folder, int position) {
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
                .onNegative((dialog, which) -> adapter.notifyItemChanged(position))
                .show();
    }
}

