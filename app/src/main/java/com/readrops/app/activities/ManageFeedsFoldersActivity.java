package com.readrops.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.databinding.ActivityManageFeedsFoldersBinding;
import com.readrops.app.fragments.FeedsFragment;
import com.readrops.app.fragments.FoldersFragment;
import com.readrops.app.viewmodels.ManageFeedsFoldersViewModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class ManageFeedsFoldersActivity extends AppCompatActivity {

    public static final String ACCOUNT = "ACCOUNT";

    private ActivityManageFeedsFoldersBinding binding;
    private FeedsFoldersPageAdapter pageAdapter;
    private ManageFeedsFoldersViewModel viewModel;

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds_folders);
        setSupportActionBar(binding.manageFeedsFoldersToolbar);

        binding.manageFeedsFoldersToolbar.setTitleTextColor(Color.WHITE);
        binding.manageFeedsFoldersTablayout.setTabTextColors(Color.WHITE, Color.WHITE);

        account = getIntent().getParcelableExtra(ACCOUNT);

        pageAdapter = new FeedsFoldersPageAdapter(getSupportFragmentManager());

        binding.manageFeedsFoldersViewpager.setAdapter(pageAdapter);
        binding.manageFeedsFoldersTablayout.setupWithViewPager(binding.manageFeedsFoldersViewpager);

        viewModel = ViewModelProviders.of(this).get(ManageFeedsFoldersViewModel.class);
        viewModel.setAccount(account);

        binding.manageFeedsFoldersViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.manageFeedsFoldersTablayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        }
        return super.onOptionsItemSelected(item);
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
                            .subscribe(new DisposableCompletableObserver() {
                                @Override
                                public void onComplete() {
                                    Toast.makeText(getApplicationContext(), "folder inserted", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .show();
    }

    public class FeedsFoldersPageAdapter extends FragmentPagerAdapter {

        private FeedsFoldersPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
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
