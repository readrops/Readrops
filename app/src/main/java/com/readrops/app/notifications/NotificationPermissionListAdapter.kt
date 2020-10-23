package com.readrops.app.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.readrops.app.R
import com.readrops.app.databinding.NotificationPermissionLayoutBinding
import com.readrops.app.utils.GlideRequests
import com.readrops.db.entities.Feed
import org.koin.core.KoinComponent
import org.koin.core.get

class NotificationPermissionListAdapter(var enableAll: Boolean, val listener: (feed: Feed) -> Unit) :
        ListAdapter<Feed, NotificationPermissionListAdapter.NotificationPermissionViewHolder>(DIFF_CALLBACK), KoinComponent {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationPermissionViewHolder {
        val binding = NotificationPermissionLayoutBinding.inflate(LayoutInflater.from(parent.context))

        return NotificationPermissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationPermissionViewHolder, position: Int) {
        val feed = getItem(position)

        holder.binding.notificationFeedName.text = feed.name
        holder.binding.notificationSwitch.isChecked = feed.isNotificationEnabled

        holder.binding.notificationSwitch.isEnabled = enableAll

        holder.itemView.setOnClickListener { if (enableAll) listener(getItem(position)) }

        get<GlideRequests>()
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

    inner class NotificationPermissionViewHolder(val binding: NotificationPermissionLayoutBinding) :
            RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Feed>() {
            override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
                return oldItem.isNotificationEnabled == newItem.isNotificationEnabled
            }

            override fun getChangePayload(oldItem: Feed, newItem: Feed): Any? {
                return newItem
            }
        }
    }
}