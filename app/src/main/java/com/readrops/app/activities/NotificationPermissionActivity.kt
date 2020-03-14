package com.readrops.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.readrops.app.R
import com.readrops.app.adapters.NotificationPermissionAdapter
import com.readrops.app.databinding.ActivityNotificationPermissionBinding
import com.readrops.app.utils.ReadropsKeys.ACCOUNT
import com.readrops.app.viewmodels.NotificationPermissionViewModel

class NotificationPermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_permission)

        setTitle(R.string.notifications)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val viewModel by viewModels<NotificationPermissionViewModel>()
        viewModel.account = intent.getParcelableExtra(ACCOUNT)

        val adapter = NotificationPermissionAdapter {

        }
        
        binding.notifPermissionAccountList.layoutManager = LinearLayoutManager(this)
        binding.notifPermissionAccountList.adapter = adapter

        viewModel.getFeedsWithNotifPermission().observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
