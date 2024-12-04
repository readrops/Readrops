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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.util.components.SwitchText
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedModalBottomSheet(
    feed: Feed,
    accountNotificationsEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onOpen: () -> Unit,
    onUpdate: () -> Unit,
    //onUpdateColor: () -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onDelete: () -> Unit,
    canUpdateFeed: Boolean,
    canDeleteFeed: Boolean
) {
    ModalBottomSheet(
        dragHandle = null,
        onDismissRequest = { onDismissRequest() }
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (feed.imageUrl != null) {
                    AsyncImage(
                        model = feed.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.65f)
                                )
                            }
                            .blur(2.5.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = MaterialTheme.spacing.largeSpacing,
                        start = MaterialTheme.spacing.largeSpacing,
                        end = MaterialTheme.spacing.largeSpacing,
                        bottom = MaterialTheme.spacing.mediumSpacing
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
                            color = if (feed.imageUrl != null) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (feed.description != null) {
                            VeryShortSpacer()

                            Text(
                                text = feed.description!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (feed.imageUrl != null) {
                                    Color.White
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (feed.imageUrl == null) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                )
            }

            SwitchText(
                title = stringResource(R.string.enable_notifications),
                subtitle = if (!accountNotificationsEnabled) {
                    stringResource(R.string.account_notifications_disabled)
                } else null,
                isChecked = feed.isNotificationEnabled,
                onCheckedChange = onUpdateNotifications
            )

            BottomSheetOption(
                text = stringResource(R.string.open),
                icon = ImageVector.vectorResource(id = R.drawable.ic_open_in_browser),
                onClick = onOpen
            )

            if (canUpdateFeed) {
                BottomSheetOption(
                    text = stringResource(id = R.string.update),
                    icon = Icons.Default.Create,
                    onClick = onUpdate
                )
            }

            /*BottomSheetOption(
                text = stringResource(R.string.update_color),
                icon = ImageVector.vectorResource(R.drawable.ic_color),
                onClick = onUpdateColor
            )*/

            if (canDeleteFeed) {
                BottomSheetOption(
                    text = stringResource(R.string.delete),
                    icon = Icons.Default.Delete,
                    onClick = onDelete
                )
            }
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