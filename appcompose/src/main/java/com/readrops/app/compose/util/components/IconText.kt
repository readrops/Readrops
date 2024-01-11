package com.readrops.app.compose.util.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.readrops.app.compose.util.theme.spacing
import com.readrops.app.compose.util.toDp

@Composable
fun IconText(
    icon: Painter,
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    padding: Dp = MaterialTheme.spacing.veryShortSpacing,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
    ) {
        Icon(
            painter = icon,
            tint = tint,
            contentDescription = null,
            modifier = Modifier.size(style.toDp()),
        )

        Spacer(Modifier.width(padding))

        Text(
            text = text,
            style = style,
        )
    }
}