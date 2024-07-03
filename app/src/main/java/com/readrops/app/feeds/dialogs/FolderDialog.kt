package com.readrops.app.feeds.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.R
import com.readrops.app.feeds.FeedScreenModel
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.BaseDialog
import com.readrops.app.util.theme.LargeSpacer

@Composable
fun FolderDialog(
    updateFolder: Boolean = false,
    viewModel: FeedScreenModel,
    onDismiss: () -> Unit,
    onValidate: () -> Unit
) {
    val state by viewModel.folderState.collectAsStateWithLifecycle()

    BaseDialog(
        title = stringResource(id = if (updateFolder) R.string.edit_folder else R.string.add_folder),
        icon = painterResource(id = if (updateFolder) R.drawable.ic_folder_grey else R.drawable.ic_new_folder),
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = state.name.orEmpty(),
            label = {
                Text(text = "URL")
            },
            onValueChange = { viewModel.setFolderName(it) },
            singleLine = true,
            trailingIcon = {
                if (!state.name.isNullOrEmpty()) {
                    IconButton(
                        onClick = { viewModel.setFolderName("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null
                        )
                    }
                }
            },
            isError = state.isTextFieldError,
            supportingText = { Text(text = state.nameError?.errorText().orEmpty()) }
        )

        if (state.exception != null) {
            Text(
                text = ErrorMessage.get(state.exception!!, LocalContext.current),
                color = MaterialTheme.colorScheme.error
            )
        }

        LargeSpacer()

        TextButton(
            onClick = { onValidate() },
        ) {
            Text(text = stringResource(R.string.validate))
        }
    }
}