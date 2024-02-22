package com.readrops.app.compose.util.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CenteredProgressIndicator(
    modifier: Modifier = Modifier
) {
    CenteredColumn {
        CircularProgressIndicator(modifier)
    }
}