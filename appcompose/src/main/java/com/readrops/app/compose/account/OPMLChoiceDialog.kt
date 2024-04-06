package com.readrops.app.compose.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.BaseDialog
import com.readrops.app.compose.util.theme.spacing

enum class OPML {
    IMPORT,
    EXPORT
}

@Composable
fun OPMLChoiceDialog(
    onChoice: (OPML) -> Unit,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(id = R.string.opml_import_export),
        icon = painterResource(id = R.drawable.ic_import_export),
        onDismiss = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(OPML.IMPORT) }
        ) {
            Text(
                text = stringResource(id = R.string.opml_import),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(MaterialTheme.spacing.shortSpacing)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChoice(OPML.EXPORT) }
        ) {
            Text(
                text = stringResource(id = R.string.opml_export),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(MaterialTheme.spacing.shortSpacing)
            )
        }
    }
}