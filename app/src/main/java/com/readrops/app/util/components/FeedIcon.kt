package com.readrops.app.util.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.util.FeedKey

@Composable
fun FeedIcon(
    iconUrl: String?,
    name: String,
    size: Dp = 24.dp
) {
    AsyncImage(
        model = FeedKey(iconUrl),
        error = painterResource(id = R.drawable.ic_rss_feed_grey),
        placeholder = painterResource(R.drawable.ic_rss_feed_grey),
        fallback = painterResource(id = R.drawable.ic_rss_feed_grey),
        contentDescription = name,
        modifier = Modifier.size(size)
    )
}