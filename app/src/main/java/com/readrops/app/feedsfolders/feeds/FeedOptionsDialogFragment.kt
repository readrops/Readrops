package com.readrops.app.feedsfolders.feeds

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.readrops.app.databinding.FeedOptionsLayoutBinding
import com.readrops.app.utils.ReadropsKeys.ACCOUNT
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.FeedWithFolder

class FeedOptionsDialogFragment : BottomSheetDialogFragment() {

    private lateinit var feedWithFolder: FeedWithFolder
    private lateinit var account: Account

    private var _binding: FeedOptionsLayoutBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val FEED_KEY = "FEED_KEY"

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
        _binding = FeedOptionsLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.feedOptionsTitle.text = feedWithFolder.feed.name

        binding.feedOptionsEditLayout.setOnClickListener { openEditFeedDialog() }
        binding.feedOptionsOpenRootLayout.setOnClickListener { openFeedRootUrl() }
        binding.feedOptionsDeleteLayout.setOnClickListener { deleteFeed() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
