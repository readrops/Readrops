package com.readrops.app.compose.account

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.androidx.AndroidScreen

class AccountSelectionScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Column {
            Text(text = "account selection")
        }
    }
}