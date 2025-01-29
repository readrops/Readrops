package com.readrops.app.account.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.db.entities.account.AccountType

@Composable
fun AccountWarningDialog(
    type: AccountType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    TwoChoicesDialog(
        title = stringResource(R.string.warning),
        icon = painterResource(R.drawable.ic_warning),
        text = when (type) {
            AccountType.GREADER -> stringResource(R.string.greader_warning)
            AccountType.FEVER -> stringResource(R.string.fever_warning)
            else -> throw IllegalArgumentException("Account type not supported")
        },
        confirmText = stringResource(R.string.understand),
        dismissText = stringResource(R.string.back),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@DefaultPreview
@Composable
private fun AccountWarningDialogPreview() {
    ReadropsTheme {
        AccountWarningDialog(
            type = AccountType.FEVER,
            onConfirm = {},
            onDismiss = {}
        )
    }
}