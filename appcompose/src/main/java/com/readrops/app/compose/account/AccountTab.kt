package com.readrops.app.compose.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.readrops.app.compose.R
import com.readrops.app.compose.account.selection.AccountSelectionScreen
import com.readrops.app.compose.util.components.SelectableIconText
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.MediumSpacer
import com.readrops.app.compose.util.theme.spacing
import org.koin.androidx.compose.getViewModel

object AccountTab : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = stringResource(R.string.account)
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getViewModel<AccountViewModel>()

        val closeHome by viewModel.closeHome.collectAsStateWithLifecycle()
        val state by viewModel.accountState.collectAsStateWithLifecycle()

        if (closeHome) {
            navigator.replaceAll(AccountSelectionScreen())
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.account)) },
                    actions = {
                        IconButton(
                            onClick = { }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {}
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_account),
                        contentDescription = null
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_freshrss),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    MediumSpacer()

                    Text(
                        text = state.account.accountName!!,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                LargeSpacer()

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_add_account),
                    text = stringResource(R.string.credentials),
                    style = MaterialTheme.typography.titleMedium,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    onClick = { }
                )

                SelectableIconText(
                    icon = painterResource(id = R.drawable.ic_notifications),
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    onClick = { }
                )

                SelectableIconText(
                    icon = rememberVectorPainter(image = Icons.Default.AccountCircle),
                    text = stringResource(R.string.delete_account),
                    style = MaterialTheme.typography.titleMedium,
                    padding = MaterialTheme.spacing.mediumSpacing,
                    color = MaterialTheme.colorScheme.error,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = { /*viewModel.deleteAccount()*/ }
                )
            }
        }
    }
}