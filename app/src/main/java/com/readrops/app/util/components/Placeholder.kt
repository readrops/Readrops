package com.readrops.app.util.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.readrops.app.util.theme.ShortSpacer


@Composable
fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}

@Composable
fun Placeholder(
    text: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    iconSize: Dp = 48.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    CenteredColumn(
        modifier = modifier
    ) {
        Icon(
            painter = painter,
            tint = tint,
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )

        ShortSpacer()

        Text(
            text = text,
            style = textStyle
        )
    }
}