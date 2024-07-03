package com.readrops.app.timelime.drawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.readrops.app.util.theme.DrawerSpacing

@Composable
fun DrawerFeedItem(
    label: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    badge: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavigationDrawerItemDefaults.colors()

    Surface(
        selected = selected,
        onClick = onClick,
        color = colors.containerColor(selected = selected).value,
        shape = CircleShape,
        modifier = modifier
            .height(36.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 24.dp)
        ) {
            val iconColor = colors.iconColor(selected).value
            CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)

            DrawerSpacing()

            Box(Modifier.weight(1f)) {
                val labelColor = colors.textColor(selected).value
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }

            DrawerSpacing()

            val badgeColor = colors.badgeColor(selected).value
            CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
        }
    }
}