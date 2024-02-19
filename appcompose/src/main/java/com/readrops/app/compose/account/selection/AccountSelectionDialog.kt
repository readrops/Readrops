package com.readrops.app.compose.account.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.SelectableImageText
import com.readrops.app.compose.util.theme.MediumSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.account.AccountType

@Composable
fun AccountSelectionDialog(
    onDismiss: () -> Unit,
    onValidate: (AccountType) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(MaterialTheme.spacing.largeSpacing)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_account),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.largeSpacing)
                )

                MediumSpacer()

                Text(
                    text = stringResource(R.string.new_account),
                    style = MaterialTheme.typography.headlineSmall
                )

                MediumSpacer()

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
    }
}