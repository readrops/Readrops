package com.readrops.app.util.accounterror

import android.content.Context
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.app.R

class GReaderError(context: Context) : AccountError(context) {

    override fun newFeedMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                400 -> context.getString(R.string.feed_already_exists)
                else -> httpMessage(exception)
            }
        }
        else -> genericMessage(exception)
    }

    override fun updateFeedMessage(exception: Exception): String {
        return newFeedMessage(exception)
    }

    override fun deleteFeedMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                400 -> context.resources.getString(R.string.feed_doesnt_exist)
                else -> httpMessage(exception)
            }
        }
        else -> genericMessage(exception)
    }

    override fun newFolderMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                400 -> context.resources.getString(R.string.folder_already_exists)
                else -> httpMessage(exception)
            }
        }
        else -> genericMessage(exception)
    }

    override fun updateFolderMessage(exception: Exception): String {
        return newFolderMessage(exception)
    }

    override fun deleteFolderMessage(exception: Exception): String = when (exception) {
        is HttpException -> {
            when (exception.code) {
                400 -> context.resources.getString(R.string.folder_doesnt_exist)
                else -> httpMessage(exception)
            }
        }
        else -> genericMessage(exception)
    }

}