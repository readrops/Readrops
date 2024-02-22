package com.readrops.app.compose.account.selection

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.BaseDialog
import com.readrops.app.compose.util.components.SelectableImageText
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.account.AccountType

@Composable
fun AccountSelectionDialog(
    onDismiss: () -> Unit,
    onValidate: (AccountType) -> Unit,
) {
    BaseDialog(
        title = stringResource(R.string.new_account),
        icon = painterResource(id = R.drawable.ic_add_account),
        onDismiss = onDismiss
    ) {
        AccountType.values().forEach { type ->
            SelectableImageText(
                image = painterResource(
                    id = if (type != AccountType.LOCAL)
                        type.iconRes
                    else
                        R.drawable.ic_rss_feed_grey
                ),
                text = stringResource(id = type.typeName),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.mediumSpacing,
                padding = MaterialTheme.spacing.shortSpacing,
                imageSize = 36.dp,
                onClick = { onValidate(type) }
            )
        }
    }
}