package com.readrops.app.util.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.readrops.app.util.theme.ShortSpacer

@Composable
fun TextHorizontalDivider(
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )

        ShortSpacer()

        HorizontalDivider()
    }
}