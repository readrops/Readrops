package com.readrops.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.databinding.ActivityManageFeedsFoldersBinding;
import com.readrops.app.fragments.FeedsFragment;
import com.readrops.app.fragments.FoldersFragment;
import com.readrops.app.utils.Utils;
import com.readrops.app.viewmodels.ManageFeedsFoldersViewModel;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.account.Account;
import com.readrops.api.utils.ConflictException;
import com.readrops.api.utils.UnknownFormatException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.readrops.app.utils.ReadropsKeys.ACCOUNT;

public class ManageFeedsFoldersActivity extends AppCompatActivity {

    private ActivityManageFeedsFoldersBinding binding;
    private ManageFeedsFoldersViewModel viewModel;

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManageFeedsFoldersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.manageFeedsFoldersToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        account = getIntent().getParcelableExtra(ACCOUNT);

        FeedsFoldersPageAdapter pageAdapter = new FeedsFoldersPageAdapter(getSupportFragmentManager());

        binding.manageFeedsFoldersViewpager.setAdapter(pageAdapter);
        binding.manageFeedsFoldersTablayout.setupWithViewPager(binding.manageFeedsFoldersViewpager);

        viewModel = new ViewModelProvider(this).get(ManageFeedsFoldersViewModel.class);
        viewModel.setAccount(account);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (account.getAccountType().getAccountConfig().isFolderCreation())
            getMenuInflater().inflate(R.menu.feeds_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_folder:
                addFolder();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void addFolder() {
        new MaterialDialog.Builder(ManageFeedsFoldersActivity.this)
                .title(R.string.add_folder)
                .positiveText(R.string.validate)
                .input(R.string.folder, 0, (dialog, input) -> {
                    Folder folder = new Folder(input.toString());
                    folder.setAccountId(account.getId());

                    viewModel.addFolder(folder)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(throwable -> {
                                String message;
                                if (throwable instanceof ConflictException)
                                    message = getString(R.string.folder_already_exists);
                                else if (throwable instanceof UnknownFormatException)
                                    message = getString(R.string.folder_bad_format);
                                else
                                    message = getString(R.string.error_occured);

                                Utils.showSnackbar(binding.manageFeedsFoldersRoot, message);
                            })
                            .subscribe();
                })
                .show();
    }

    public class FeedsFoldersPageAdapter extends FragmentPagerAdapter {

        private FeedsFoldersPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getApplicationContext().getString(R.string.feeds);
                case 1:
                    return getApplicationContext().getString(R.string.folders);
                default:
                    return null;
            }
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = FeedsFragment.newInstance(account);
                    break;
                case 1:
                    fragment = FoldersFragment.newInstance(account);
                    break;
            }

            return fragment;
        }
    }
}
