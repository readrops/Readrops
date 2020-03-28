package com.readrops.app.activities

import android.content.Intent
import android.drm.DrmInfoRequest.ACCOUNT_ID
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.readrops.app.R
import com.readrops.app.adapters.NotificationPermissionListAdapter
import com.readrops.app.databinding.ActivityNotificationPermissionBinding
import com.readrops.app.utils.ReadropsKeys
import com.readrops.app.utils.SharedPreferencesManager
import com.readrops.app.utils.Utils
import com.readrops.app.viewmodels.NotificationPermissionViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class NotificationPermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_permission)

        setTitle(R.string.notifications)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val accountId = intent.getIntExtra(ACCOUNT_ID, 0)
        val viewModel by viewModels<NotificationPermissionViewModel>()
        var adapter: NotificationPermissionListAdapter? = null
        var feedStateChanged = false

        viewModel.getAccount(accountId).observe(this, Observer { account ->
            viewModel.account = account

            if (adapter == null) {
                // execute the following lines only once
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
                    if (!feedStateChanged) {
                        viewModel.setAllFeedsNotificationState(isChecked)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError { Utils.showSnackbar(binding.root, it.message) }
                                .subscribe()
                    }

                    feedStateChanged = false
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

                viewModel.getFeedsWithNotifPermission().observe(this, Observer { feeds ->
                    binding.notifPermissionFeedsSwitch.isChecked = feeds.all { it.isNotificationEnabled }
                    adapter?.submitList(feeds)
                })
            }

        })


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
