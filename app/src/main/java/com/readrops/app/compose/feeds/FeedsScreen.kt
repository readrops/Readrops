package com.readrops.app.compose.feeds

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen

class FeedsScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Column {
            Text(
                text = "Feeds"
            )
        }
    }
}