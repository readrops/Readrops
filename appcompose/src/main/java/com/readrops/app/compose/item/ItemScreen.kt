package com.readrops.app.compose.item

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.readrops.app.compose.util.components.AndroidScreen

class ItemScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Text(text ="item screen")
    }
}