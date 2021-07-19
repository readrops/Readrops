package com.readrops.app.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionManager {

    @JvmStatic
    fun isPermissionGranted(context: Context, permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    @JvmStatic
    fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String) =
            ActivityCompat.requestPermissions(activity, permissions, requestCode)

    @JvmStatic
    fun requestPermissions(fragment: Fragment, requestCode: Int, vararg permissions: String) =
            fragment.requestPermissions(permissions, requestCode)

}