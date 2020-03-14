package com.readrops.app.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.readrops.app.R
import com.readrops.app.adapters.NotificationPermissionAdapter
import com.readrops.app.databinding.ActivityNotificationPermissionBinding

class NotificationPermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_permission)
        
        binding.notifPermissionAccountList.layoutManager = LinearLayoutManager(this)
    }
}
