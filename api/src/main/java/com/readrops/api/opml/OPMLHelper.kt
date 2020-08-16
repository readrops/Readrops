package com.readrops.api.opml

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

object OPMLHelper {

    const val OPEN_OPML_FILE_REQUEST = 1

    @JvmStatic
    fun openFileIntent(activity: Activity) =
            activity.startActivityForResult(createIntent(), OPEN_OPML_FILE_REQUEST)

    @JvmStatic
    fun openFileIntent(fragment: Fragment) =
            fragment.startActivityForResult(createIntent(), OPEN_OPML_FILE_REQUEST)


    private fun createIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/*", "text/*"))
        }
    }
}