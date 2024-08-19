package com.readrops.app.more

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.readrops.app.R
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing

@Composable
fun DonationDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    
    BaseDialog(
        title = stringResource(id = R.string.make_donation),
        icon = painterResource(id = R.drawable.ic_donation),
        onDismiss = onDismiss
    ) {
        Column {
            Text(
                text = stringResource(R.string.donation_text)
            )

            MediumSpacer()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        uriHandler.openUri(context.getString(R.string.paypal_url))
                        onDismiss()
                    }
            ) {
                Text(
                    text = stringResource(R.string.paypal),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.mediumSpacing,
                        vertical = MaterialTheme.spacing.shortSpacing
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(context.getString(R.string.bitcoin_address)))
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.bitcoin_address),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                        onDismiss()
                    }
            ) {
                Text(
                    text = stringResource(R.string.bitcoin_copy_address),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.mediumSpacing,
                        vertical = MaterialTheme.spacing.shortSpacing
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(context.getString(R.string.litecoin_address)))
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.litecoin_address),
                                Toast.LENGTH_SHORT
                            )
                            .show()
                        onDismiss()
                    }
            ) {
                Text(
                    text = stringResource(R.string.litecoin_copy_address),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.mediumSpacing,
                        vertical = MaterialTheme.spacing.shortSpacing
                    )
                )
            }
        }
    }
}