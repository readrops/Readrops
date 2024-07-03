package com.readrops.app.util.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.readrops.app.util.theme.spacing
import com.readrops.app.util.toDp

@Composable
fun BaseText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    spacing: Dp = MaterialTheme.spacing.veryShortSpacing,
    onClick: (() -> Unit)? = null,
    leftContent: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
    ) {
        leftContent()

        Spacer(Modifier.width(spacing))

        Text(
            text = text,
            style = style,
            color = color,
        )
    }
}

@Composable
fun IconText(
    icon: Painter,
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    tint: Color = LocalContentColor.current,
    spacing: Dp = MaterialTheme.spacing.veryShortSpacing,
    onClick: (() -> Unit)? = null,
) {
    BaseText(
        text = text,
        style = style,
        color = color,
        spacing = spacing,
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = icon,
            tint = tint,
            contentDescription = null,
            modifier = Modifier.size(style.toDp()),
        )
    }
}

@Composable
fun ImageText(
    image: Painter,
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    spacing: Dp = MaterialTheme.spacing.veryShortSpacing,
    imageSize: Dp = style.toDp(),
    onClick: (() -> Unit)? = null
) {
    BaseText(
        text = text,
        style = style,
        color = color,
        spacing = spacing,
        modifier = modifier,
        onClick = onClick
    ) {
        Image(
            painter = image,
            contentDescription = null,
            modifier = Modifier.size(imageSize),
        )
    }
}



@Composable
fun SelectableIconText(
    icon: Painter,
    text: String,
    style: TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    tint: Color = LocalContentColor.current,
    spacing: Dp = MaterialTheme.spacing.veryShortSpacing,
    padding: Dp = MaterialTheme.spacing.shortSpacing
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(padding)
    ) {
        BaseText(
            text = text,
            style = style,
            color = color,
            spacing = spacing
        ) {
            Icon(
                painter = icon,
                tint = tint,
                contentDescription = null,
                modifier = Modifier.size(style.toDp()),
            )
        }
    }
}

@Composable
fun SelectableImageText(
    image: Painter,
    text: String,
    style: TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    spacing: Dp = MaterialTheme.spacing.veryShortSpacing,
    padding: Dp = MaterialTheme.spacing.shortSpacing,
    imageSize: Dp = style.toDp()
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(padding)
    ) {
        BaseText(
            text = text,
            style = style,
            color = color,
            spacing = spacing
        ) {
            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier.size(imageSize),
            )
        }
    }
}