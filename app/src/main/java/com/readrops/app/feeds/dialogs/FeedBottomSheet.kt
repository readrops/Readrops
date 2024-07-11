package com.readrops.app.feeds.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedModalBottomSheet(
    feed: Feed,
    onDismissRequest: () -> Unit,
    onOpen: () -> Unit,
    onUpdate: () -> Unit,
    onUpdateColor: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() }
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.largeSpacing
                )
            ) {
                AsyncImage(
                    model = feed.iconUrl,
                    contentDescription = feed.name!!,
                    placeholder = painterResource(id = R.drawable.ic_rss_feed_grey),
                    error = painterResource(id = R.drawable.ic_rss_feed_grey),
                    modifier = Modifier.size(MaterialTheme.spacing.veryLargeSpacing)
                )

                MediumSpacer()

                Column {
                    Text(
                        text = feed.name!!,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (feed.description != null) {
                        VeryShortSpacer()

                        Text(
                            text = feed.description!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            MediumSpacer()

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
            )

            MediumSpacer()

            BottomSheetOption(
                text = stringResource(R.string.open),
                icon = ImageVector.vectorResource(id = R.drawable.ic_open_in_browser),
                onClick = onOpen
            )

            BottomSheetOption(
                text = stringResource(id = R.string.update),
                icon = Icons.Default.Create,
                onClick = onUpdate
            )

            BottomSheetOption(
                text = "Update color",
                icon = ImageVector.vectorResource(R.drawable.ic_color),
                onClick = onUpdateColor
            )

            BottomSheetOption(
                text = stringResource(R.string.delete),
                icon = Icons.Default.Delete,
                onClick = onDelete
            )
        }

        LargeSpacer()
    }
}

@Composable
fun BottomSheetOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.mediumSpacing,
                    vertical = MaterialTheme.spacing.shortSpacing
                )

        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )

            MediumSpacer()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}