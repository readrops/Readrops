package com.readrops.app.feeds.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.readrops.app.R
import com.readrops.app.feeds.components.FeedBanner
import com.readrops.app.more.preferences.components.BasePreference
import com.readrops.app.util.components.SwitchText
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed
import com.readrops.db.entities.OpenIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedModalBottomSheet(
    feed: Feed,
    onDismissRequest: () -> Unit,
    onOpen: () -> Unit,
    onUpdate: () -> Unit,
    onUpdateColor: () -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onOpenInClick: () -> Unit,
    onDelete: () -> Unit,
    accountNotificationsEnabled: Boolean,
    canUpdateFeed: Boolean,
    canDeleteFeed: Boolean
) {
    ModalBottomSheet(
        dragHandle = null,
        onDismissRequest = { onDismissRequest() }
    ) {
        Column {
            FeedBanner(feed)

            SwitchText(
                title = stringResource(R.string.enable_notifications),
                subtitle = if (!accountNotificationsEnabled) {
                    stringResource(R.string.account_notifications_disabled)
                } else null,
                isChecked = feed.isNotificationEnabled,
                onCheckedChange = onUpdateNotifications
            )

            BasePreference(
                title = stringResource(R.string.open_feed_in),
                subtitle = if (feed.openIn == OpenIn.LOCAL_VIEW) {
                    stringResource(R.string.local_view)
                } else {
                    stringResource(R.string.external_view)
                },
                onClick = onOpenInClick,
                paddingValues = PaddingValues(
                    horizontal = MaterialTheme.spacing.mediumSpacing,
                    vertical = MaterialTheme.spacing.shortSpacing
                )
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

            BottomSheetOption(
                text = stringResource(R.string.update_color),
                icon = ImageVector.vectorResource(R.drawable.ic_color),
                onClick = onUpdateColor
            )

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