package com.readrops.app.compose.item

import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import coil.compose.AsyncImage
import com.readrops.api.utils.DateUtils
import com.readrops.app.compose.R
import com.readrops.app.compose.util.Utils
import com.readrops.app.compose.util.components.AndroidScreen
import com.readrops.app.compose.util.components.CenteredProgressIndicator
import com.readrops.app.compose.util.components.IconText
import com.readrops.app.compose.util.theme.MediumSpacer
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class ItemScreen(
    private val itemId: Int,
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val screenModel =
            getScreenModel<ItemScreenModel>(parameters = { parametersOf(itemId) })
        val scrollState = rememberScrollState()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val primaryColor = MaterialTheme.colorScheme.primary
        val backgroundColor = MaterialTheme.colorScheme.background
        val onBackgroundColor = MaterialTheme.colorScheme.onBackground

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
                        accentColor = tintColor,
                        baseColor = MaterialTheme.colorScheme.onBackground,
                        bottomPadding = true
                    )
                }

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            webViewClient = WebViewClient()

                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setSupportZoom(false)

                            isVerticalScrollBarEnabled = false
                            setBackgroundColor(backgroundColor.toArgb())
                        }
                    },
                    update = { webView ->
                        val tintColor = if (itemWithFeed.bgColor != 0) {
                            Color(itemWithFeed.bgColor)
                        } else {
                            primaryColor
                        }

                        val string = context.getString(
                            R.string.webview_html_template,
                            Utils.getCssColor(tintColor.toArgb()),
                            Utils.getCssColor(onBackgroundColor.toArgb()),
                            Utils.getCssColor(backgroundColor.toArgb()),
                            screenModel.formatText()
                        )
                        val data =
                            Base64.encodeToString(string.encodeToByteArray(), Base64.NO_PADDING)

                        webView.loadData(data, "text/html; charset=utf-8", "base64")
                    }
                )

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
    val accentColor = if (itemWithFeed.bgColor != 0) {
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
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxSize()
        ) {
            SimpleTitle(
                itemWithFeed = itemWithFeed,
                titleColor = onScrimColor,
                accentColor = accentColor,
                baseColor = onScrimColor,
                bottomPadding = true
            )
        }
    }

    MediumSpacer()
}

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
        modifier = Modifier.padding(
            start = spacing,
            end = spacing,
            top = spacing,
            bottom = if (bottomPadding) spacing else 0.dp
        )
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

        ShortSpacer()

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
                tint = accentColor
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