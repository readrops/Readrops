package com.readrops.app.feeds.dialogs

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.R
import com.readrops.app.feeds.FeedScreenModel
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.LoadingTextButton
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFeedDialog(
    viewModel: FeedScreenModel,
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
            enabled = !state.isFeedUrlReadOnly,
            isError = state.isFeedUrlError,
            supportingText = {
                if (state.isFeedUrlError) {
                    Text(
                        text = state.feedUrlError?.errorText().orEmpty()
                    )
                } else if (state.isFeedUrlReadOnly) {
                    Text(
                        text = stringResource(id = R.string.feed_url_read_only)
                    )
                }
            }
        )

        MediumSpacer()

        ExposedDropdownMenuBox(
            expanded = state.isFolderDropDownExpanded && state.hasFolders,
            onExpandedChange = { viewModel.setFolderDropDownState(state.isFolderDropDownExpanded.not()) }
        ) {
            ExposedDropdownMenu(
                expanded = state.isFolderDropDownExpanded && state.hasFolders,
                onDismissRequest = { viewModel.setFolderDropDownState(false) }
            ) {
                for (folder in state.folders) {
                    DropdownMenuItem(
                        text = { Text(text = folder.name!!) },
                        onClick = {
                            viewModel.setSelectedFolder(folder)
                            viewModel.setFolderDropDownState(false)
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
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.isFolderDropDownExpanded)
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

        if (state.exception != null) {
            MediumSpacer()

            Text(
                text = ErrorMessage.get(state.exception!!, LocalContext.current),
                color = MaterialTheme.colorScheme.error
            )
        }

        LargeSpacer()

        LoadingTextButton(
            text = stringResource(R.string.validate),
            isLoading = state.isLoading,
            onClick = { viewModel.updateFeedDialogValidate() },
        )
    }
}
