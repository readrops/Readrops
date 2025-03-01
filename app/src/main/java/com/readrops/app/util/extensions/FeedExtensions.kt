package com.readrops.app.util.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.readrops.db.entities.Feed
import com.readrops.db.pojo.ItemWithFeed

fun Feed.getColorOrNull(): Color? = Color(color).takeIf { color != 0 }

@Composable
fun ItemWithFeed.displayColor(background: Int): Color {
    return if (color != 0 && color.canDisplayOnBackground(background)) {
        Color(color)
    } else {
        MaterialTheme.colorScheme.primary
    }
}