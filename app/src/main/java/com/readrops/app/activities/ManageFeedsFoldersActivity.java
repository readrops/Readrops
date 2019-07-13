package com.readrops.app.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.readrops.app.R;
import com.readrops.app.database.entities.Account;
import com.readrops.app.databinding.ActivityManageFeedsFoldersBinding;
import com.readrops.app.fragments.FeedsFragment;
import com.readrops.app.fragments.FoldersFragment;

public class ManageFeedsFoldersActivity extends AppCompatActivity {

    public static final String ACCOUNT = "ACCOUNT";

    private ActivityManageFeedsFoldersBinding binding;
    private FeedsFoldersPageAdapter pageAdapter;

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_feeds_folders);
        setSupportActionBar(binding.manageFeedsFoldersToolbar);

        account = getIntent().getParcelableExtra(ACCOUNT);

        pageAdapter = new FeedsFoldersPageAdapter(getSupportFragmentManager());

        binding.manageFeedsFoldersViewpager.setAdapter(pageAdapter);
        binding.manageFeedsFoldersTablayout.setupWithViewPager(binding.manageFeedsFoldersViewpager);
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
                //addFolder();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
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
                    return getApplicationContext().getString(R.string.feeds_and_folders);
                case 1:
                    return getApplicationContext().getString(R.string.folder);
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
