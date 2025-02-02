package com.readrops.app.item

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.CenteredProgressIndicator
import com.readrops.app.util.components.Placeholder
import com.readrops.app.util.extensions.isError
import com.readrops.app.util.extensions.isLoading
import com.readrops.app.util.extensions.openInCustomTab
import com.readrops.app.util.extensions.openUrl
import com.readrops.db.filters.QueryFilters
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.parameter.parametersOf

class ItemScreen(
    private val itemId: Int,
    private val itemIndex: Int,
    private val queryFilters: QueryFilters
) : AndroidScreen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        val screenModel =
            koinScreenModel<ItemScreenModel>(parameters = { parametersOf(itemId, itemIndex, queryFilters) })
        val state by screenModel.state.collectAsStateWithLifecycle()
        val items = state.itemState.collectAsLazyPagingItems()

        val snackbarHostState = remember { SnackbarHostState() }

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
                snackbarHostState.showSnackbar(context.getString(R.string.downloaded_file))
            }
        }

        LaunchedEffect(state.error) {
            if (state.error != null) {
                snackbarHostState.showSnackbar(state.error!!)
            }
        }

        when {
            items.isLoading() -> {
                CenteredProgressIndicator()
            }

            items.isError() -> {
                Placeholder(
                    text = stringResource(R.string.error_occured),
                    painter = painterResource(id = R.drawable.ic_error)
                )
            }

            else -> {
                val pagerState = rememberPagerState(
                    initialPage = if (itemIndex > -1) itemIndex else 0,
                    pageCount = { items.itemCount }
                )

                LaunchedEffect(pagerState.currentPage) {
                    snapshotFlow { pagerState.currentPage }
                        .distinctUntilChanged()
                        .collect { pageIndex ->
                            items[pageIndex]?.let {
                                if (!it.isRead) {
                                    screenModel.setItemReadState(it.item.apply { isRead = true })
                                }
                            }
                        }
                }

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 2,
                    key = items.itemKey { it.item.id }
                ) { page ->
                    val itemWithFeed = items[page]

                    if (itemWithFeed != null) {
                        val accentColor = if (itemWithFeed.color != 0) {
                            Color(itemWithFeed.color)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }

                        val item = itemWithFeed.item

                        ItemScreenPage(
                            itemWithFeed = itemWithFeed,
                            snackbarHostState = snackbarHostState,
                            onOpenUrl = { url ->
                                if (state.openInExternalBrowser) {
                                    context.openUrl(url)
                                } else {
                                    context.openInCustomTab(url, state.theme, accentColor)
                                }
                            },
                            onShareItem = { screenModel.shareItem(item, context) },
                            onSetReadState = {
                                screenModel.setItemReadState(item.apply { isRead = it })
                            },
                            onSetStarState = {
                                screenModel.setItemStarState(item.apply { isStarred = it })
                            },
                            onOpenImageDialog = { screenModel.openImageDialog(it) },
                            onPop = { navigator.pop() },
                        )
                    }
                }
            }
        }
    }
}