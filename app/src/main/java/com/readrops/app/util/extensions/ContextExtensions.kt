package com.readrops.app.util.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

fun Context.openUrl(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

fun Context.openInCustomTab(url: String, theme: String?, color: Color) {
    val colorScheme = when (theme) {
        "light" -> CustomTabsIntent.COLOR_SCHEME_LIGHT
        "dark" -> CustomTabsIntent.COLOR_SCHEME_DARK
        else -> CustomTabsIntent.COLOR_SCHEME_SYSTEM
    }

    CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams
                .Builder()
                .setToolbarColor(color.toArgb())
                .build()
        )
        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        .setUrlBarHidingEnabled(true)
        .setColorScheme(colorScheme)
        .build()
        .launchUrl(this, url.toUri())
}

// TODO arbitrary value, we might want to use windowClasses in the future
fun Context.isTabletUi(): Boolean {
    val configuration = resources.configuration
    return configuration.smallestScreenWidthDp >= 720
}

@Composable
@ReadOnlyComposable
fun isTabletUi(): Boolean = LocalContext.current.isTabletUi()