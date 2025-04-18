package com.readrops.app.feeds.color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.components.IconText
import com.readrops.app.util.extensions.canDisplayOnBackground
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed

@Composable
fun ColorPreview(
    feed: Feed,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(CardDefaults.cardColors().containerColor)
            .padding(MaterialTheme.spacing.mediumSpacing)
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
        ) {
            FeedIcon(
                iconUrl = feed.iconUrl,
                name = feed.name.orEmpty(),
            )

            ShortSpacer()

            Text(
                text = feed.name!!,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Companion.Ellipsis,
            )
        }

        if (!color.toArgb()
                .canDisplayOnBackground(CardDefaults.cardColors().containerColor.toArgb())
        ) {
            ShortSpacer()

            IconText(
                icon = painterResource(R.drawable.ic_warning),
                tint = MaterialTheme.colorScheme.error,
                color = MaterialTheme.colorScheme.error,
                text = stringResource(R.string.color_contrast_too_low),
                maxLines = Int.MAX_VALUE,
                style = MaterialTheme.typography.bodySmall,
                spacing = MaterialTheme.spacing.shortSpacing,
                iconSize = 16.dp
            )
        }
    }
}