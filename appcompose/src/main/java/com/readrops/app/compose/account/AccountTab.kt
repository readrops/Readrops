package com.readrops.app.compose.account

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object AccountTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
                index = 3u,
                title = "Account"
        )

    @Composable
    override fun Content() {
        Column {
            Text(text = "Account")
        }
    }

}