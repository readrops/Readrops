package com.readrops.app.compose.account.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.compose.R
import com.readrops.app.compose.account.credentials.AccountCredentialsScreen
import com.readrops.app.compose.home.HomeScreen
import com.readrops.app.compose.util.components.AndroidScreen
import com.readrops.db.entities.account.AccountType
import org.koin.androidx.compose.getViewModel

class AccountSelectionScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<AccountSelectionViewModel>()
        val navState by viewModel.navState.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Choose an account")

            Spacer(modifier = Modifier.size(8.dp))

            AccountType.values().forEach { accountType ->
                Row(
                        modifier = Modifier.clickable { viewModel.createAccount(accountType) }
                ) {
                    Icon(
                            painter = painterResource(id = R.drawable.ic_freshrss),
                            contentDescription = accountType.name,
                            modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.size(4.dp))

                    Text(text = accountType.name)
                }

                Spacer(modifier = Modifier.size(8.dp))
            }
        }

        when (navState) {
            is AccountSelectionViewModel.NavState.GoToHomeScreen -> {
                // using replace makes the app crash due to a screen key conflict
                navigator.replaceAll(HomeScreen())
            }

            is AccountSelectionViewModel.NavState.GoToAccountCredentialsScreen -> {
                val accountType = (navState as AccountSelectionViewModel.NavState.GoToAccountCredentialsScreen).accountType

                navigator.push(AccountCredentialsScreen(accountType))
                viewModel.resetNavState()
            }

            else -> {}
        }
    }
}