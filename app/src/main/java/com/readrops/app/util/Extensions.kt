package com.readrops.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun TextStyle.toDp(): Dp = fontSize.value.dp

fun Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))