package com.readrops.app.compose.feeds.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.compose.R
import com.readrops.app.compose.feeds.FeedViewModel
import com.readrops.app.compose.util.components.BaseDialog

@Composable
fun AddUpdateFolderDialog(
    updateFolder: Boolean = false,
    viewModel: FeedViewModel,
    onDismiss: () -> Unit,
    onValidate: () -> Unit
) {
    val state by viewModel.addFolderState.collectAsStateWithLifecycle()

    BaseDialog(
        title = if (updateFolder) "Update Folder" else "Add Folder",
        icon = painterResource(id = if (updateFolder) R.drawable.ic_folder_grey else R.drawable.ic_new_folder),
        onDismiss = onDismiss,
        onValidate = onValidate
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
            isError = state.isError,
            supportingText = { Text(text = state.nameError?.errorText().orEmpty()) }
        )
    }
}