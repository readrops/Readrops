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
fun AddFolderDialog(
    viewModel: FeedViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.addFolderState.collectAsStateWithLifecycle()

    BaseDialog(
        title = "Add Folder",
        icon = painterResource(id = R.drawable.ic_new_folder),
        onDismiss = { onDismiss() },
        onValidate = { viewModel.addFolderValidate() }
    ) {
        OutlinedTextField(
            value = state.name,
            label = {
                Text(text = "URL")
            },
            onValueChange = { viewModel.setFolderName(it) },
            singleLine = true,
            trailingIcon = {
                if (state.name.isNotEmpty()) {
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
            isError = state.isEmpty,
            supportingText = {
                if (state.isEmpty) {
                    Text(text = state.errorText)
                }
            }
        )
    }
}