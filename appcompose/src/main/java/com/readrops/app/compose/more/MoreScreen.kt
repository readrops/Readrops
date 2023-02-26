package com.readrops.app.compose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen

class MoreScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Column {
            Text(
               text = "More"
            )
        }
    }
}