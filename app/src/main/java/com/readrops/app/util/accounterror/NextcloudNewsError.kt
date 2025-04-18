package com.readrops.app.util.accounterror

import android.content.Context
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.app.R

class NextcloudNewsError(context: Context) : AccountError(context) {

    override fun newFeedMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                409 -> context.resources.getString(R.string.feed_already_exists)
                422 -> context.getString(R.string.invalid_feed)
                else -> httpMessage(exception)
            }
        }

        else -> genericMessage(exception)
    }

    override fun updateFeedMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                404 -> context.resources.getString(R.string.feed_doesnt_exist)
                else -> httpMessage(exception)
            }
        }

        else -> genericMessage(exception)
    }

    override fun deleteFeedMessage(exception: Exception): String {
        return updateFeedMessage(exception)
    }

    override fun newFolderMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                409 -> context.resources.getString(R.string.folder_already_exists)
                422 -> context.resources.getString(R.string.invalid_folder)
                else -> httpMessage(exception)
            }
        }

        else -> genericMessage(exception)
    }

    override fun updateFolderMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                404 -> context.resources.getString(R.string.folder_doesnt_exist)
                409 -> context.resources.getString(R.string.folder_already_exists)
                422 -> context.getString(R.string.invalid_folder)
                else -> httpMessage(exception)
            }
        }

        else -> genericMessage(exception)
    }

    override fun deleteFolderMessage(exception: Exception): String {
        return updateFolderMessage(exception)
    }
}