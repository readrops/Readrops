package com.readrops.app.compose.feeds.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.compose.R
import com.readrops.app.compose.account.selection.adaptiveIconPainterResource
import com.readrops.app.compose.feeds.FeedScreenModel
import com.readrops.app.compose.util.components.BaseDialog
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.ShortSpacer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedDialog(
    viewModel: FeedScreenModel,
    onDismiss: () -> Unit,
) {
    val state by viewModel.addFeedDialogState.collectAsStateWithLifecycle()

    var isExpanded by remember { mutableStateOf(false) }

    BaseDialog(
        title = stringResource(R.string.add_feed_item),
        icon = painterResource(id = R.drawable.ic_rss_feed_grey),
        onDismiss = onDismiss
    ) {
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
                            Image(
                                painter = adaptiveIconPainterResource(
                                    id = state.selectedAccount.accountType!!.iconRes
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
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
                    Image(
                        painter = adaptiveIconPainterResource(
                            id = state.selectedAccount.accountType!!.iconRes
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
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
