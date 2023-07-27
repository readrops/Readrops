package com.readrops.app.compose.feeds

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.readrops.db.entities.Feed
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun FeedItem(feed: Feed) {
    val context = LocalContext.current

    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
    ) {
        CoilImage(imageRequest = {
            ImageRequest.Builder(context)
                    .data(feed.url)
                    .build()
        })

        Text(
                text = feed.name!!,
                style = MaterialTheme.typography.headlineSmall
        )
    }
}
