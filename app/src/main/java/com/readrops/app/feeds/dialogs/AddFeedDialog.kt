package com.readrops.app.feeds.dialogs

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.account.selection.adaptiveIconPainterResource
import com.readrops.app.feeds.AddFeedDialogState
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.LoadingTextButton
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.db.entities.account.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedDialog(
    state: AddFeedDialogState,
    onValueChange: (String) -> Unit,
    onExpandChange: (Boolean) -> Unit,
    onAccountClick: (Account) -> Unit,
    onValidate: () -> Unit,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(R.string.add_feed),
        icon = painterResource(id = R.drawable.ic_rss_feed_grey),
        onDismiss = { if (!state.isLoading) onDismiss() }
    ) {
        OutlinedTextField(
            value = state.url,
            label = { Text(text = stringResource(id = R.string.url)) },
            onValueChange = { onValueChange(it) },
            singleLine = true,
            trailingIcon = {
                if (state.url.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") }
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
            expanded = state.isAccountDropDownExpanded,
            onExpandedChange = { onExpandChange(!state.isAccountDropDownExpanded) }
        ) {
            ExposedDropdownMenu(
                expanded = state.isAccountDropDownExpanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                for (account in state.accounts) {
                    DropdownMenuItem(
                        text = { Text(text = account.name!!) },
                        onClick = {
                            onAccountClick(account)
                        },
                        leadingIcon = {
                            Image(
                                painter = adaptiveIconPainterResource(
                                    id = account.type!!.iconRes
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.selectedAccount.name!!,
                readOnly = true,
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isAccountDropDownExpanded)
                },
                leadingIcon = {
                    Image(
                        painter = adaptiveIconPainterResource(
                            id = state.selectedAccount.type!!.iconRes
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.menuAnchor()
            )
        }

        if (state.exception != null) {
            MediumSpacer()

            Text(
                text = ErrorMessage.get(state.exception, LocalContext.current),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        LargeSpacer()

        LoadingTextButton(
            text = stringResource(id = R.string.validate),
            isLoading = state.isLoading,
            onClick = onValidate,
        )
    }
}
