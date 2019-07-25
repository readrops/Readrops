package com.readrops.app.fragments;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.activities.AccountSettingsActivity;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.databinding.FragmentFeedsBinding;
import com.readrops.app.utils.SharedPreferencesManager;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.ManageFeedsFoldersViewModel;
import com.readrops.app.views.EditFeedDialog;
import com.readrops.app.views.FeedsAdapter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.readrops.app.activities.ManageFeedsFoldersActivity.ACCOUNT;


public class FeedsFragment extends Fragment {

    private FeedsAdapter adapter;
    private ManageFeedsFoldersViewModel viewModel;
    private Account account;

    private FragmentFeedsBinding binding;

    public FeedsFragment() {
        // Required empty public constructor
    }

    public static FeedsFragment newInstance(Account account) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();

        args.putParcelable(AccountSettingsActivity.ACCOUNT, account);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = getArguments().getParcelable(ACCOUNT);

        if (account.getLogin() == null)
            account.setLogin(SharedPreferencesManager.readString(getContext(), account.getLoginKey()));
        if (account.getPassword() == null)
            account.setPassword(SharedPreferencesManager.readString(getContext(), account.getPasswordKey()));

        viewModel = ViewModelProviders.of(this).get(ManageFeedsFoldersViewModel.class);
        viewModel.setAccount(account);

        viewModel.getFeedsWithFolder().observe(this, feedWithFolders -> adapter.submitList(feedWithFolders));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feeds, container, false);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.feedsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new FeedsAdapter(new FeedsAdapter.ManageFeedsListener() {
            @Override
            public void onEdit(FeedWithFolder feedWithFolder) {
                openEditFeedDialog(feedWithFolder);
            }

            @Override
            public void onOpenLink(FeedWithFolder feedWithFolder) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(50);

                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(feedWithFolder.getFeed().getSiteUrl()));
                startActivity(urlIntent);
            }
        });

        binding.feedsRecyclerview.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int swipeFlags = ItemTouchHelper.RIGHT;

                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                deleteFeed(adapter.getItemAt(viewHolder.getAdapterPosition()).getFeed(),
                        viewHolder.getAdapterPosition());

            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

        }).attachToRecyclerView(binding.feedsRecyclerview);
    }

    private void deleteFeed(Feed feed, int position) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.delete_feed)
                .positiveText(R.string.validate)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> viewModel.deleteFeed(feed)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Utils.showSnackbar(binding.feedsRoot,
                                        getString(R.string.feed_deleted, feed.getName()));
                            }

                            @Override
                            public void onError(Throwable e) {
                                adapter.notifyItemChanged(position);

                                String message;
                                if (e instanceof Resources.NotFoundException)
                                    message = getString(R.string.feed_doesnt_exist, feed.getName());
                                else
                                    message = getString(R.string.error_occured);

                                Utils.showSnackbar(binding.feedsRoot, message);
                            }
                        }))
                .onNegative(((dialog, which) -> adapter.notifyItemChanged(position)))
                .show();
    }

    private void openEditFeedDialog(FeedWithFolder feedWithFolder) {
        EditFeedDialog editFeedDialog = new EditFeedDialog();

        Bundle bundle = new Bundle();
        bundle.putParcelable("feedWithFolder", feedWithFolder);
        bundle.putParcelable(ACCOUNT, account);
        editFeedDialog.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(editFeedDialog, "").commit();
    }
}
