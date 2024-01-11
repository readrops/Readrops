package com.readrops.app.compose.more

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object MoreTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
                index = 4u,
                title = "More"
        )


    @Composable
    override fun Content() {
        Column {
            Text(
               text = "More"
            )
        }
    }
}