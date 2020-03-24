package com.readrops.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.readrops.app.R
import com.readrops.app.databinding.NotificationLayoutBinding
import com.readrops.app.utils.GlideApp
import com.readrops.readropsdb.entities.Feed

class NotificationPermissionAdapter(var enableAll: Boolean, val listener: (feed: Feed) -> Unit) :
        ListAdapter<Feed, NotificationPermissionAdapter.NotificationPermissionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationPermissionViewHolder {
        val binding = DataBindingUtil.inflate<NotificationLayoutBinding>(LayoutInflater.from(parent.context),
                R.layout.notification_layout, parent, false)

        return NotificationPermissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationPermissionViewHolder, position: Int) {
        val feed = getItem(position)

        holder.binding.notificationFeedName.text = feed.name
        holder.binding.notificationSwitch.isChecked = feed.isNotificationEnabled

        holder.binding.notificationSwitch.isEnabled = enableAll
        holder.binding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != feed.isNotificationEnabled) listener(feed)
        }

        holder.itemView.setOnClickListener { if (enableAll) listener(feed) }

        GlideApp.with(holder.itemView.context)
                .load(feed.iconUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_rss_feed_grey)
                .into(holder.binding.notificationFeedIcon)
    }

    override fun onBindViewHolder(holder: NotificationPermissionViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val feed = payloads.first() as Feed
            holder.binding.notificationSwitch.isChecked = feed.isNotificationEnabled
        } else onBindViewHolder(holder, position)
    }

    inner class NotificationPermissionViewHolder(val binding: NotificationLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Feed>() {
            override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
                return oldItem.isNotificationEnabled == newItem.isNotificationEnabled
            }
        }
    }
}