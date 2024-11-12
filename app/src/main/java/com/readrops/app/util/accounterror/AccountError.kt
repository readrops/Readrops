package com.readrops.app.util.accounterror

import android.content.Context
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.LoginFailedException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import com.readrops.app.R
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import java.io.IOException
import java.net.UnknownHostException

abstract class AccountError(protected val context: Context) {

    open fun newFeedMessage(exception: Exception): String = genericMessage(exception)

    open fun updateFeedMessage(exception: Exception): String = genericMessage(exception)

    open fun deleteFeedMessage(exception: Exception): String = genericMessage(exception)

    open fun newFolderMessage(exception: Exception): String = genericMessage(exception)

    open fun updateFolderMessage(exception: Exception): String = genericMessage(exception)

    open fun deleteFolderMessage(exception: Exception): String = genericMessage(exception)

    protected fun genericMessage(exception: Exception) = when (exception) {
        is HttpException -> httpMessage(exception)
        is UnknownHostException -> context.resources.getString(R.string.unreachable_url)
        is NoSuchFileException -> context.resources.getString(R.string.unable_open_file)
        is IOException -> context.resources.getString(
            R.string.network_failure,
            exception.message.orEmpty()
        )

        is ParseException, is UnknownFormatException -> context.resources.getString(R.string.processing_feed_error)
        is LoginFailedException -> context.getString(R.string.login_failed)
        else -> "${exception.javaClass.simpleName}: ${exception.message}"
    }

    protected fun httpMessage(exception: HttpException): String {
        return when (exception.code) {
            in 400..499 -> {
                when (exception.code) {
                    400 -> context.resources.getString(R.string.http_error_400)
                    401 -> context.resources.getString(R.string.http_error_401)
                    403 -> context.resources.getString(R.string.http_error_403)
                    404 -> context.resources.getString(R.string.http_error_404)
                    else -> context.resources.getString(R.string.http_error_4XX, exception.code)
                }
            }

            in 500..599 -> {
                context.resources.getString(R.string.http_error_5XX, exception.code)
            }

            else -> context.resources.getString(R.string.http_error, exception.code)
        }
    }

    companion object {

        fun from(account: Account, context: Context): AccountError = when (account.type) {
            AccountType.FRESHRSS -> FreshRSSError(context)
            AccountType.NEXTCLOUD_NEWS -> NextcloudNewsError(context)
            else -> DefaultAccountError(context)
        }

        class DefaultAccountError(context: Context) : AccountError(context)
    }
}

