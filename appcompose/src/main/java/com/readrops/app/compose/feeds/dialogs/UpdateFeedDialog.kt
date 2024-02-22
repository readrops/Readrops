package com.readrops.app.compose.feeds.dialogs

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.compose.R
import com.readrops.app.compose.feeds.FeedViewModel
import com.readrops.app.compose.util.components.BaseDialog
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.MediumSpacer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFeedDialog(
    viewModel: FeedViewModel,
    onDismissRequest: () -> Unit
) {
    val state by viewModel.updateFeedDialogState.collectAsStateWithLifecycle()

    BaseDialog(
        title = stringResource(R.string.edit_feed),
        icon = painterResource(id = R.drawable.ic_rss_feed_grey),
        onDismiss = onDismissRequest
    ) {
        OutlinedTextField(
            value = state.feedName,
            onValueChange = { viewModel.setUpdateFeedDialogStateFeedName(it) },
            label = { Text(text = stringResource(R.string.feed_name)) },
            singleLine = true,
            isError = state.isFeedNameError,
            supportingText = {
                if (state.isFeedNameError) {
                    Text(
                        text = state.feedNameError?.errorText().orEmpty()
                    )
                }
            }
        )

        MediumSpacer()

        OutlinedTextField(
            value = state.feedUrl,
            onValueChange = { viewModel.setUpdateFeedDialogFeedUrl(it) },
            label = { Text(text = stringResource(R.string.feed_url)) },
            singleLine = true,
            readOnly = state.isFeedUrlReadOnly,
            isError = state.isFeedUrlError,
            supportingText = {
                if (state.isFeedUrlError) {
                    Text(
                        text = state.feedUrlError?.errorText().orEmpty()
                    )
                }
            }
        )

        MediumSpacer()

        ExposedDropdownMenuBox(
            expanded = state.isAccountDropDownExpanded && state.hasFolders,
            onExpandedChange = { viewModel.setAccountDropDownState(state.isAccountDropDownExpanded.not()) }
        ) {
            ExposedDropdownMenu(
                expanded = state.isAccountDropDownExpanded && state.hasFolders,
                onDismissRequest = { viewModel.setAccountDropDownState(false) }
            ) {
                for (folder in state.folders) {
                    DropdownMenuItem(
                        text = { Text(text = folder.name!!) },
                        onClick = {
                            viewModel.setSelectedFolder(folder)
                            viewModel.setAccountDropDownState(false)
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.ic_folder_grey),
                                contentDescription = null,
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.selectedFolder?.name.orEmpty(),
                readOnly = true,
                enabled = state.hasFolders,
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isAccountDropDownExpanded)
                },
                leadingIcon = {
                    if (state.selectedFolder != null) {
                        Icon(
                            painterResource(id = R.drawable.ic_folder_grey),
                            contentDescription = null,
                        )
                    }
                },
                modifier = Modifier.menuAnchor()
            )
        }

        LargeSpacer()

        TextButton(
            onClick = { viewModel.updateFeedDialogValidate() },
        ) {
            Text(text = stringResource(R.string.validate))
        }
    }
}
