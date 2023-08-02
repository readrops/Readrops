package com.readrops.app.compose.item

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen

class ItemScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Text(text ="item screen")
    }
}