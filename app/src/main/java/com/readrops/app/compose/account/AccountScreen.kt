package com.readrops.app.compose.account

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen

class AccountScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Column {
            Text(text = "Account")
        }
    }

}