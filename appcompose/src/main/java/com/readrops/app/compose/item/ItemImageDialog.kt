package com.readrops.app.compose.item

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.BaseDialog
import com.readrops.app.compose.util.components.SelectableImageText
import com.readrops.app.compose.util.theme.spacing

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
            SelectableImageText(
                image = rememberVectorPainter(image = Icons.Default.Share),
                text = stringResource(id = R.string.share_image),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.shortSpacing,
                padding = MaterialTheme.spacing.shortSpacing,
                imageSize = 16.dp,
                onClick = { onChoice(ItemImageChoice.SHARE) }
            )

            SelectableImageText(
                image = painterResource(id = R.drawable.ic_download),
                text = stringResource(id = R.string.download_image),
                style = MaterialTheme.typography.titleMedium,
                spacing = MaterialTheme.spacing.shortSpacing,
                padding = MaterialTheme.spacing.shortSpacing,
                imageSize = 16.dp,
                onClick = { onChoice(ItemImageChoice.DOWNLOAD) }
            )
        }
    }
}