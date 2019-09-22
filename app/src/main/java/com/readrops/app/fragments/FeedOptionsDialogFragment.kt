package com.readrops.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.readrops.app.R
import com.readrops.app.activities.ManageFeedsFoldersActivity.ACCOUNT
import com.readrops.app.database.entities.account.Account
import com.readrops.app.database.pojo.FeedWithFolder
import com.readrops.app.databinding.FeedOptionsLayoutBinding

class FeedOptionsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var feedWithFolder: FeedWithFolder
    private lateinit var account: Account
    private lateinit var binding: FeedOptionsLayoutBinding

    companion object {
        val FEED_KEY = "FEED_KEY"

        fun newInstance(feedWithFolder: FeedWithFolder, account: Account): FeedOptionsDialogFragment {
            val bundle = Bundle()
            bundle.putParcelable(FEED_KEY, feedWithFolder)
            bundle.putParcelable(ACCOUNT, account)

            val feedsOptionsDialogFragment = FeedOptionsDialogFragment()
            feedsOptionsDialogFragment.arguments = bundle

            return feedsOptionsDialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        feedWithFolder = arguments?.getParcelable(FEED_KEY)!!
        account = arguments?.getParcelable(ACCOUNT)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.feed_options_layout, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.feedOptionsTitle.text = feedWithFolder.feed.name

        binding.feedOptionsEditLayout.setOnClickListener { openEditFeedDialog() }
        binding.feedOptionsOpenRootLayout.setOnClickListener { openFeedRootUrl() }
        binding.feedOptionsDeleteLayout.setOnClickListener { deleteFeed() }
    }

    private fun openEditFeedDialog() {
        dismiss()
        val editFeedDialogFragment = EditFeedDialogFragment.newInstance(feedWithFolder, account)

        activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.add(editFeedDialogFragment, "")
                ?.commit()
    }

    private fun openFeedRootUrl() {
        dismiss()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(feedWithFolder.feed.siteUrl)))
    }

    private fun deleteFeed() {
        dismiss()
        (parentFragment as FeedsFragment).deleteFeed(feedWithFolder.feed)
    }

}
