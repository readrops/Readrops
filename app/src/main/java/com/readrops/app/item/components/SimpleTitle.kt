package com.readrops.app.item.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.timelime.components.itemWithFeed
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.components.IconText
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.util.DateUtils
import kotlin.math.roundToInt

@Composable
fun SimpleTitle(
    itemWithFeed: ItemWithFeed,
    titleColor: Color,
    accentColor: Color,
    baseColor: Color,
    bottomPadding: Boolean,
) {
    val item = itemWithFeed.item
    val spacing = MaterialTheme.spacing.mediumSpacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = spacing,
                end = spacing,
                top = spacing,
                bottom = if (bottomPadding) spacing else 0.dp
            )
    ) {
        FeedIcon(
            iconUrl = itemWithFeed.feedIconUrl,
            name = itemWithFeed.feedName,
            size = 48.dp,
            modifier = Modifier.clip(CircleShape)
        )

        ShortSpacer()

        Text(
            text = itemWithFeed.feedName,
            style = MaterialTheme.typography.labelLarge,
            color = baseColor,
            textAlign = TextAlign.Center
        )

        ShortSpacer()

        Text(
            text = item.title!!,
            style = MaterialTheme.typography.headlineMedium,
            color = titleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (item.author != null) {
            ShortSpacer()

            IconText(
                icon = painterResource(id = R.drawable.ic_person),
                text = itemWithFeed.item.author!!,
                style = MaterialTheme.typography.labelMedium,
                color = baseColor,
                tint = accentColor
            )
        }

        ShortSpacer()

        val readTime = if (item.readTime > 1) {
            stringResource(id = R.string.read_time, item.readTime.roundToInt())
        } else {
            stringResource(id = R.string.read_time_lower_than_1)
        }
        Text(
            text = "${DateUtils.formattedDate(item.pubDate!!)} ${stringResource(id = R.string.interpoint)} $readTime",
            style = MaterialTheme.typography.labelMedium,
            color = baseColor
        )
    }
}

@DefaultPreview
@Composable
private fun SimpleTitlePreview() {
    ReadropsTheme {
        SimpleTitle(
            itemWithFeed = itemWithFeed,
            titleColor = MaterialTheme.colorScheme.primary,
            accentColor = MaterialTheme.colorScheme.primary,
            baseColor = MaterialTheme.colorScheme.onBackground,
            bottomPadding = true
        )
    }
}