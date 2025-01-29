package com.readrops.app.util.extensions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.readrops.db.entities.Feed

@Composable
fun Feed.getColorOrDefault(): Color {
    return if (color != 0) {
        Color(color)
    } else {
        MaterialTheme.colorScheme.primary
    }
}

fun Feed.getColorOrNull(): Color? = Color(color).takeIf { color != 0 }