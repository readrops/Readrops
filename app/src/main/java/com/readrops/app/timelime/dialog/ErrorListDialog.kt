package com.readrops.app.timelime.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.repositories.ErrorResult
import com.readrops.app.util.accounterror.AccountError
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer

@Composable
fun ErrorListDialog(
    errorResult: ErrorResult,
    onDismiss: () -> Unit,
) {
    val scrollableState = rememberScrollState()
    val accountError = AccountError.Companion.DefaultAccountError(LocalContext.current)

    BaseDialog(
        title = stringResource(R.string.synchronization_errors),
        icon = painterResource(id = R.drawable.ic_error),
        onDismiss = onDismiss,
        modifier = Modifier.heightIn(max = 500.dp)
    ) {
        Text(
            text = pluralStringResource(
                id = R.plurals.error_occurred_feed,
                count = errorResult.size
            )
        )

        MediumSpacer()

        Column(
            modifier = Modifier.verticalScroll(scrollableState)
        ) {
            for (error in errorResult.entries) {
                Text(text = "${error.key.name}: ${accountError.genericMessage(error.value)}")

                ShortSpacer()
            }
        }
    }
}