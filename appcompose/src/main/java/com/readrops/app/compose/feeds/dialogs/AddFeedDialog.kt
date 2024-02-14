package com.readrops.app.compose.feeds.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.compose.R
import com.readrops.app.compose.feeds.FeedViewModel
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.MediumSpacer
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedDialog(
    viewModel: FeedViewModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.addFeedDialogState.collectAsStateWithLifecycle()

    var isExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(MaterialTheme.spacing.largeSpacing)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rss_feed_grey),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.largeSpacing)
                )

                MediumSpacer()

                Text(
                    text = stringResource(R.string.add_feed_item),
                    style = MaterialTheme.typography.headlineSmall
                )

                MediumSpacer()

                OutlinedTextField(
                    value = state.url,
                    label = {
                        Text(text = "URL")
                    },
                    onValueChange = { viewModel.setAddFeedDialogURL(it) },
                    singleLine = true,
                    trailingIcon = {
                        if (state.url.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setAddFeedDialogURL("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    isError = state.isError,
                    supportingText = { Text(state.error?.errorText().orEmpty()) }
                )

                ShortSpacer()

                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = isExpanded.not() }
                ) {
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        for (account in state.accounts) {
                            DropdownMenuItem(
                                text = { Text(text = account.accountName!!) },
                                onClick = {
                                    isExpanded = false
                                    viewModel.setAddFeedDialogSelectedAccount(account)
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (state.selectedAccount.isLocal){
                                                R.drawable.ic_rss_feed_grey}
                                            else
                                                state.selectedAccount.accountType!!.iconRes
                                        ),
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.selectedAccount.accountName!!,
                        readOnly = true,
                        onValueChange = {},
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    id = if (state.selectedAccount.isLocal){
                                        R.drawable.ic_rss_feed_grey}
                                    else
                                        state.selectedAccount.accountType!!.iconRes
                                ),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.menuAnchor()
                    )
                }

                LargeSpacer()

                TextButton(
                    onClick = { viewModel.addFeedDialogValidate() },
                ) {
                    Text(text = stringResource(R.string.validate))
                }
            }
        }
    }
}
