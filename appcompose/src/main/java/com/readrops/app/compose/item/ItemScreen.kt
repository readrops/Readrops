package com.readrops.app.compose.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import coil.compose.AsyncImage
import com.readrops.api.utils.DateUtils
import com.readrops.app.compose.R
import com.readrops.app.compose.util.components.AndroidScreen
import com.readrops.app.compose.util.components.CenteredProgressIndicator
import com.readrops.app.compose.util.components.IconText
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.VeryShortSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class ItemScreen(
    private val itemId: Int,
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val screenModel =
            getScreenModel<ItemScreenModel>(parameters = { parametersOf(itemId) })
        val scrollState = rememberScrollState()

        val state by screenModel.state.collectAsStateWithLifecycle()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            if (state.itemWithFeed != null) {
                val itemWithFeed = state.itemWithFeed!!

                if (itemWithFeed.item.imageLink != null) {
                    BackgroundTitle(
                        itemWithFeed = itemWithFeed
                    )
                } else {
                    val tintColor = if (itemWithFeed.bgColor != 0) {
                        Color(itemWithFeed.bgColor)
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    }

                    SimpleTitle(
                        itemWithFeed = itemWithFeed,
                        titleColor = tintColor,
                        tintColor = tintColor,
                        baseColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                CenteredProgressIndicator()
            }
        }
    }
}

@Composable
fun BackgroundTitle(
    itemWithFeed: ItemWithFeed,
) {
    val onScrimColor = Color.White.copy(alpha = 0.85f)
    val tintColor = if (itemWithFeed.bgColor != 0) {
        Color(itemWithFeed.bgColor)
    } else {
        onScrimColor
    }

    Surface(
        shape = RoundedCornerShape(
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        ),
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {
        AsyncImage(
            model = itemWithFeed.item.imageLink,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_broken_image),
            modifier = Modifier
                .fillMaxSize()
        )

        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxSize()
        ) {
            SimpleTitle(
                itemWithFeed = itemWithFeed,
                titleColor = onScrimColor,
                tintColor = tintColor,
                baseColor = onScrimColor
            )
        }
    }
}

@Composable
fun SimpleTitle(
    itemWithFeed: ItemWithFeed,
    titleColor: Color,
    tintColor: Color,
    baseColor: Color
) {
    val item = itemWithFeed.item

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
    ) {
        AsyncImage(
            model = itemWithFeed.feedIconUrl,
            contentDescription = itemWithFeed.feedName,
            placeholder = painterResource(id = R.drawable.ic_rss_feed_grey),
            error = painterResource(id = R.drawable.ic_rss_feed_grey),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        VeryShortSpacer()

        Text(
            text = itemWithFeed.feedName,
            style = MaterialTheme.typography.labelLarge,
            color = baseColor
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
                tint = tintColor
            )
        }

        ShortSpacer()

        val readTime =
            if (item.readTime < 1) "< 1 min" else "${item.readTime.roundToInt()} mins"
        Text(
            text = "${DateUtils.formattedDate(item.pubDate!!)} · $readTime",
            style = MaterialTheme.typography.labelMedium,
            color = baseColor
        )
    }
}