package com.readrops.app.compose.util.components

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
import androidx.compose.ui.graphics.painter.Painter
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.toDp


@Composable
fun CenteredColumn(
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}

@Composable
fun Placeholder(
    text: String,
    painter: Painter,
) {
    CenteredColumn {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.typography.displayMedium.toDp() * 1.5f)
        )

        ShortSpacer()

        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall
        )
    }
}