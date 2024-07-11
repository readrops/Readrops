package com.readrops.app.util.components.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.ErrorMessage
import com.readrops.app.util.components.LoadingTextButton
import com.readrops.app.util.components.TextFieldError
import com.readrops.app.util.theme.LargeSpacer

data class TextFieldDialogState(
    val value: String = "",
    val textFieldError: TextFieldError? = null,
    val exception: Exception? = null,
    val isLoading: Boolean = false
) {
    val isTextFieldError
        get() = textFieldError != null
}

@Composable
fun TextFieldDialog(
    title: String,
    icon: Painter,
    label: String,
    state: TextFieldDialogState,
    onValueChange: (String) -> Unit,
    onValidate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseDialog(
        title = title,
        icon = icon,
        onDismiss = {  if (!state.isLoading) onDismiss() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = state.value,
            label = { Text(text = label) },
            onValueChange = onValueChange,
            singleLine = true,
            trailingIcon = {
                if (state.value.isNotEmpty()) {
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
            isError = state.isTextFieldError,
            supportingText = { Text(text = state.textFieldError?.errorText().orEmpty()) }
        )

        if (state.exception != null) {
            Text(
                text = ErrorMessage.get(state.exception, LocalContext.current),
                color = MaterialTheme.colorScheme.error
            )
        }

        LargeSpacer()

        LoadingTextButton(
            text = stringResource(R.string.validate),
            isLoading = state.isLoading,
            onClick = { onValidate() },
        )
    }
}