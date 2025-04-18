package com.readrops.app.util.components.dialog

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.R

@Composable
fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(id = R.string.error_occured),
        icon = painterResource(id = R.drawable.ic_error),
        onDismiss = onDismiss
    ) {
        Text(
            text = error,
            color = AlertDialogDefaults.textContentColor
        )
    }
}

