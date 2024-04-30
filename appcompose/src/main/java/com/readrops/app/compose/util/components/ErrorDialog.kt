package com.readrops.app.compose.util.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.compose.R
import com.readrops.app.compose.util.ErrorMessage

@Composable
fun ErrorDialog(
    exception: Exception,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(id = R.string.error_occured),
        icon = painterResource(id = R.drawable.ic_error),
        onDismiss = onDismiss
    ) {
        Text(text = ErrorMessage.get(exception = exception))
    }
}

