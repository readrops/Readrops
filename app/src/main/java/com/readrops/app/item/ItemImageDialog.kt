package com.readrops.app.item

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.components.SelectableIconText
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.spacing

enum class ItemImageChoice {
    SHARE,
    DOWNLOAD
}

@Composable
fun ItemImageDialog(
    onChoice: (ItemImageChoice) -> Unit,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(id = R.string.image_options),
        icon = painterResource(id = R.drawable.ic_image),
        onDismiss = onDismiss
    ) {
        Column {
            SelectableIconText(
                icon = rememberVectorPainter(image = Icons.Default.Share),
                text = stringResource(id = R.string.share_image),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.mediumSpacing,
                padding = MaterialTheme.spacing.shortSpacing,
                onClick = { onChoice(ItemImageChoice.SHARE) }
            )

            SelectableIconText(
                icon = painterResource(id = R.drawable.ic_download),
                text = stringResource(id = R.string.download_image),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.mediumSpacing,
                padding = MaterialTheme.spacing.shortSpacing,
                onClick = { onChoice(ItemImageChoice.DOWNLOAD) }
            )
        }
    }
}

@DefaultPreview
@Composable
private fun ItemImageDialogPreview() {
    ItemImageDialog(
        onChoice = {},
        onDismiss = {}
    )
}