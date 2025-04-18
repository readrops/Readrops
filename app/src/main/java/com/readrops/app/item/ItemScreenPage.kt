package com.readrops.app.item

import android.widget.RelativeLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import com.readrops.app.item.components.BackgroundTitle
import com.readrops.app.item.components.BottomBarState
import com.readrops.app.item.components.ItemScreenBottomBar
import com.readrops.app.item.components.SimpleTitle
import com.readrops.app.item.components.rememberBottomBarNestedScrollConnection
import com.readrops.app.item.view.ItemNestedScrollView
import com.readrops.app.item.view.ItemWebView
import com.readrops.app.util.extensions.displayColor
import com.readrops.db.pojo.ItemWithFeed

@Composable
fun ItemScreenPage(
    itemWithFeed: ItemWithFeed,
    snackbarHostState: SnackbarHostState,
    onOpenUrl: (String) -> Unit,
    onShareItem: () -> Unit,
    onSetReadState: (Boolean) -> Unit,
    onSetStarState: (Boolean) -> Unit,
    onOpenImageDialog: (String) -> Unit,
    onPop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = itemWithFeed.item

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val accentColor = itemWithFeed.displayColor(MaterialTheme.colorScheme.background.toArgb())

    val nestedScrollConnection = rememberBottomBarNestedScrollConnection()
    var refreshAndroidView by remember { mutableStateOf(true) }
    var isScrollable by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.nestedScroll(nestedScrollConnection)
            .navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ItemScreenBottomBar(
                state = BottomBarState(
                    isRead = itemWithFeed.isRead,
                    isStarred = itemWithFeed.isStarred,
                    isOpenUrlVisible = !item.link.isNullOrEmpty()
                ),
                accentColor = accentColor,
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(nestedScrollConnection.bottomBarHeight)
                    .offset {
                        if (isScrollable) {
                            IntOffset(
                                x = 0,
                                y = -nestedScrollConnection.bottomBarOffset
                            )
                        } else {
                            IntOffset(0, 0)
                        }
                    },
                onShare = onShareItem,
                onOpenUrl = { onOpenUrl(item.link!!) },
                onChangeReadState = onSetReadState,
                onChangeStarState = onSetStarState
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
                        useBackgroundTitle = item.imageLink != null,
                        onGlobalLayoutListener = { viewHeight, contentHeight ->
                            isScrollable = viewHeight - contentHeight < 0
                        },
                        onUrlClick = { url -> onOpenUrl(url) },
                        onImageLongPress = { url -> onOpenImageDialog(url) }
                    ) {
                        if (item.imageLink != null) {
                            BackgroundTitle(
                                itemWithFeed = itemWithFeed,
                                onClickBack = onPop
                            )
                        } else {
                            Box {
                                IconButton(
                                    onClick = onPop,
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .align(Alignment.TopStart)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = null,
                                    )
                                }

                                SimpleTitle(
                                    itemWithFeed = itemWithFeed,
                                    titleColor = accentColor,
                                    onBackgroundColor = MaterialTheme.colorScheme.onBackground,
                                    bottomPadding = true
                                )
                            }
                        }
                    }
                },
                update = { nestedScrollView ->
                    if (refreshAndroidView) {
                        val relativeLayout =
                            (nestedScrollView.children.toList().first() as RelativeLayout)
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
}