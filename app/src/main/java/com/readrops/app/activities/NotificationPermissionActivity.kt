package com.readrops.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.readrops.app.R
import com.readrops.app.adapters.NotificationPermissionListAdapter
import com.readrops.app.databinding.ActivityNotificationPermissionBinding
import com.readrops.app.utils.ReadropsKeys
import com.readrops.app.utils.ReadropsKeys.ACCOUNT_ID
import com.readrops.app.utils.SharedPreferencesManager
import com.readrops.app.utils.Utils
import com.readrops.app.viewmodels.NotificationPermissionViewModel
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NotificationPermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationPermissionBinding
    private val viewModel by viewModels<NotificationPermissionViewModel>()
    private var adapter: NotificationPermissionListAdapter? = null

    private var isFirstCheck = true
    private var feedStateChanged = false
    private var feeds = listOf<Feed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.notifications)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val accountId = intent.getIntExtra(ACCOUNT_ID, 0)

        viewModel.getAccount(accountId).observe(this, Observer { account ->
            viewModel.account = account

            if (adapter == null) {
                // execute the method only once
                setupUI(account)
            }
        })
    }

    private fun setupUI(account: Account) {
        binding.notifPermissionAccountSwitch.isChecked = account.isNotificationsEnabled
        binding.notifPermissionAccountSwitch.setOnCheckedChangeListener { _, isChecked ->
            account.isNotificationsEnabled = isChecked
            binding.notifPermissionFeedsSwitch.isEnabled = isChecked

            adapter?.enableAll = isChecked
            adapter?.notifyDataSetChanged()

            viewModel.setAccountNotificationsState(isChecked)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { Utils.showSnackbar(binding.root, it.message) }
                    .subscribe()

            if (isChecked) displayAutoSynchroPopup()
        }

        binding.notifPermissionFeedsSwitch.isEnabled = account.isNotificationsEnabled
        binding.notifPermissionFeedsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (canUpdateAllFeedsPermissions(isChecked)) {
                viewModel.setAllFeedsNotificationState(isChecked)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { Utils.showSnackbar(binding.root, it.message) }
                        .subscribe()
            }

            if (isFirstCheck) isFirstCheck = false
            if (feedStateChanged) feedStateChanged = false
        }

        adapter = NotificationPermissionListAdapter(account.isNotificationsEnabled) { feed ->
            feedStateChanged = true

            viewModel.setFeedNotificationState(feed)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { Utils.showSnackbar(binding.root, it.message) }
                    .subscribe()
        }

        binding.notifPermissionAccountList.layoutManager = LinearLayoutManager(this)
        binding.notifPermissionAccountList.adapter = adapter

        viewModel.getFeedsWithNotifPermission().observe(this, Observer { newFeeds ->
            feeds = newFeeds

            binding.notifPermissionFeedsSwitch.isChecked = newFeeds.all { it.isNotificationEnabled }
            adapter?.submitList(newFeeds)
        })
    }

    /**
     * Inform if is possible to update all feeds notifications permissions in the same time.
     * The method takes into account the following states :
     * - first check : when opening the activity with all feeds permissions enabled,
     * the enable all feeds permissions switch will be checked but the request mustn't be executed
     * - feed state : if all feeds permissions are enabled and a feed permission is disabled,
     * the enable all feeds permissions switch will be unchecked but the request mustn't be executed as only one feed permission is disabled
     * - all feeds permissions switch checked : if the setOnCheckedChangeListener method is triggered because all feeds permissions were enabled,
     * do not execute the request as it would be pointless
     */
    private fun canUpdateAllFeedsPermissions(isChecked: Boolean): Boolean {
        return !isFirstCheck && (!feedStateChanged || (isChecked && !feeds.all { it.isNotificationEnabled }))
    }

    private fun displayAutoSynchroPopup() {
        val autoSynchroValue = SharedPreferencesManager.readString(this, SharedPreferencesManager.SharedPrefKey.AUTO_SYNCHRO)

        if (autoSynchroValue.toFloat() <= 0) {
            MaterialDialog.Builder(this)
                    .title(R.string.auto_synchro_disabled)
                    .content(R.string.enable_auto_synchro_text)
                    .positiveText(R.string.open)
                    .neutralText(R.string.cancel)
                    .onPositive { _, _ ->
                        val intent = Intent(this, SettingsActivity::class.java).apply {
                            putExtra(ReadropsKeys.SETTINGS, SettingsActivity.SettingsKey.SETTINGS.ordinal)
                        }

                        startActivity(intent)
                    }
                    .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
