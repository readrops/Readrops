package com.readrops.app.compose.timelime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.api.utils.DateUtils
import com.readrops.app.compose.R
import com.readrops.app.compose.utils.theme.ShortSpacer
import com.readrops.app.compose.utils.theme.VeryShortSpacer
import com.readrops.app.compose.utils.theme.spacing
import com.readrops.db.pojo.ItemWithFeed

@Composable
fun TimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onReadLater: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(
                PaddingValues(
                    horizontal = MaterialTheme.spacing.shortSpacing,
                    vertical = MaterialTheme.spacing.veryShortSpacing
                )
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.shortSpacing,
                        end = MaterialTheme.spacing.shortSpacing,
                        top = MaterialTheme.spacing.shortSpacing,
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rss_feed_grey),
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.typography.labelLarge.fontSize.value.dp)
                    )

                    VeryShortSpacer()

                    Column {
                        Text(
                            text = itemWithFeed.feedName,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        VeryShortSpacer()

                        if (itemWithFeed.folder != null) {
                            Text(
                                text = itemWithFeed.folder!!.name!!,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Text(
                    text = DateUtils.formattedDateByLocal(itemWithFeed.item.pubDate!!),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            ShortSpacer()

            Text(
                text = itemWithFeed.item.title!!,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.shortSpacing)
            )

            ShortSpacer()

            if (itemWithFeed.item.imageLink != null) {
                AsyncImage(
                    model = itemWithFeed.item.imageLink,
                    contentDescription = itemWithFeed.item.title!!,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .aspectRatio(3f / 2f)
                        .fillMaxWidth()
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.shortSpacing)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.clickable { onFavorite() }
                )

                Icon(
                    imageVector = Icons.Outlined.Add, // placeholder icon
                    contentDescription = null,
                    modifier = Modifier.clickable { onReadLater() }
                )
                
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.clickable { onShare() }
                )
            }
        }
    }
}