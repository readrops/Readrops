package com.readrops.app.compose.account.credentials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.compose.home.HomeScreen
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType

class AccountCredentialsScreen(
        private val accountType: AccountType,
        private val account: Account? = null,
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column {
            Text(
                    text = "AccountCredentialsScreen"
            )

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = { navigator.replaceAll(HomeScreen()) }) {
                Text(
                        text = "skip"
                )
            }
        }
    }
}