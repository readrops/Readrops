package com.readrops.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.work.Data
import java.io.Serializable

fun TextStyle.toDp(): Dp = fontSize.value.dp

fun Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

val Data.serializables by lazy {
    mutableMapOf<String, Serializable>()
}

fun Data.putSerializable(key: String, parcelable: Serializable): Data {
    serializables[key] = parcelable
    return this
}

fun Data.getSerializable(key: String): Serializable? = serializables[key]

fun Data.clearSerializables() {
    serializables.clear()
}
