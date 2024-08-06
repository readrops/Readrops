package com.readrops.app.item

import android.content.Intent
import android.net.Uri
import android.widget.RelativeLayout
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import coil.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.item.view.ItemNestedScrollView
import com.readrops.app.item.view.ItemWebView
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.CenteredProgressIndicator
import com.readrops.app.util.components.IconText
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.util.DateUtils
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class ItemScreen(
    private val itemId: Int
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val density = LocalDensity.current

        val screenModel =
            getScreenModel<ItemScreenModel>(parameters = { parametersOf(itemId) })
        val state by screenModel.state.collectAsStateWithLifecycle()

        val primaryColor = MaterialTheme.colorScheme.primary
        val backgroundColor = MaterialTheme.colorScheme.background
        val onBackgroundColor = MaterialTheme.colorScheme.onBackground

        val snackbarHostState = remember { SnackbarHostState() }
        var isScrollable by remember { mutableStateOf(true) }
        var refreshAndroidView by remember { mutableStateOf(true) }

        // https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll#parent-compose-child-view
        val bottomBarHeight = 64.dp
        val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx().toFloat() }
        val bottomBarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset = bottomBarOffsetHeightPx.floatValue + delta
                    bottomBarOffsetHeightPx.floatValue = newOffset.coerceIn(-bottomBarHeightPx, 0f)

                    return Offset.Zero
                }
            }
        }

        if (state.imageDialogUrl != null) {
            ItemImageDialog(
                onChoice = {
                    if (it == ItemImageChoice.SHARE) {
                        screenModel.shareImage(state.imageDialogUrl!!, context)
                    } else {
                        screenModel.downloadImage(state.imageDialogUrl!!, context)

                    }

                    screenModel.closeImageDialog()
                },
                onDismiss = { screenModel.closeImageDialog() }
            )
        }

        LaunchedEffect(state.fileDownloadedEvent) {
            if (state.fileDownloadedEvent) {
                snackbarHostState.showSnackbar("Downloaded file!")
            }
        }

        if (state.itemWithFeed != null) {
            val itemWithFeed = state.itemWithFeed!!
            val item = itemWithFeed.item

            val accentColor = if (itemWithFeed.color != 0) {
                Color(itemWithFeed.color)
            } else {
                primaryColor
            }

            fun openUrl(url: String) {
                if (state.openInExternalBrowser) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } else {
                    CustomTabsIntent.Builder()
                        .setDefaultColorSchemeParams(
                            CustomTabColorSchemeParams
                                .Builder()
                                .setToolbarColor(accentColor.toArgb())
                                .build()
                        )
                        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                        .setUrlBarHidingEnabled(true)
                        .build()
                        .launchUrl(context, url.toUri())
                }
            }

            Scaffold(
                modifier = Modifier
                    .nestedScroll(nestedScrollConnection),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    ItemScreenBottomBar(
                        state = state.bottomBarState,
                        accentColor = accentColor,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(bottomBarHeight)
                            .offset {
                                if (isScrollable) {
                                    IntOffset(
                                        x = 0,
                                        y = -bottomBarOffsetHeightPx.floatValue.roundToInt()
                                    )
                                } else {
                                    IntOffset(0, 0)
                                }
                            },
                        onShare = { screenModel.shareItem(item, context) },
                        onOpenUrl = { openUrl(item.link!!) },
                        onChangeReadState = {
                            screenModel.setItemReadState(item.apply { isRead = it })
                        },
                        onChangeStarState = {
                            screenModel.setItemStarState(item.apply { isStarred = it })
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.padding(paddingValues)
                ) {
                    AndroidView(
                        factory = { context ->
                            ItemNestedScrollView(
                                context = context,
                                onGlobalLayoutListener = { viewHeight, contentHeight ->
                                    isScrollable = viewHeight - contentHeight < 0
                                },
                                onUrlClick = { url -> openUrl(url) },
                                onImageLongPress = { url -> screenModel.openImageDialog(url) }
                            ) {
                                if (item.imageLink != null) {
                                    BackgroundTitle(itemWithFeed = itemWithFeed)
                                } else {
                                    val tintColor = if (itemWithFeed.color != 0) {
                                        Color(itemWithFeed.color)
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
                            }
                        },
                        update = { nestedScrollView ->
                            if (refreshAndroidView) {
                                val relativeLayout =
                                    (nestedScrollView.children.toList()[0] as RelativeLayout)
                                val webView = relativeLayout.children.toList()[1] as ItemWebView

                                webView.loadText(
                                    itemWithFeed = itemWithFeed,
                                    accentColor = accentColor,
                                    backgroundColor = backgroundColor,
                                    onBackgroundColor = onBackgroundColor
                                )

                                refreshAndroidView = false
                            }
                        }
                    )
                }
            }
        } else {
            CenteredProgressIndicator()
        }
    }
}

@Composable
fun BackgroundTitle(
    itemWithFeed: ItemWithFeed,
) {
    val onScrimColor = Color.White.copy(alpha = 0.85f)
    val accentColor = if (itemWithFeed.color != 0) {
        Color(itemWithFeed.color)
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
            text = "${DateUtils.formattedDate(item.pubDate!!)} · $readTime",
            style = MaterialTheme.typography.labelMedium,
            color = baseColor
        )
    }
}