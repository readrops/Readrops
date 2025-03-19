package com.readrops.app.item

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.Stable
import androidx.core.content.FileProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.readrops.app.R
import com.readrops.app.repositories.BaseRepository
import com.readrops.app.util.PAGING_PAGE_SIZE
import com.readrops.app.util.PAGING_PREFETCH_DISTANCE
import com.readrops.app.util.Preferences
import com.readrops.app.util.Utils
import com.readrops.db.Database
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.QueryFilters
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemSelectionQueryBuilder
import com.readrops.db.queries.ItemsQueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File
import java.net.URI

class ItemScreenModel(
    private val itemId: Int,
    private val itemIndex: Int,
    private val queryFilters: QueryFilters,
    private val database: Database,
    private val preferences: Preferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<ItemState>(ItemState()), KoinComponent {

    //TODO Is <lateinit var> really the best solution?
    private lateinit var account: Account
    private lateinit var repository: BaseRepository
    private lateinit var pagingSource: PagingSource<Int, ItemWithFeed>

    private val useCustomShareIntentTpl = preferences.useCustomShareIntentTpl.flow.stateIn(
        screenModelScope, SharingStarted.Eagerly, false
    )
    private val customShareIntentTpl = preferences.customShareIntentTpl.flow.stateIn(
        screenModelScope, SharingStarted.Eagerly, ""
    )

    private val useStateChanges = itemIndex > -1 && (queryFilters.mainFilter != MainFilter.ALL
            || !queryFilters.showReadItems)

    private val _itemState: MutableStateFlow<PagingData<ItemWithFeed>> =
        MutableStateFlow(PagingData.empty())
    var itemState: StateFlow<PagingData<ItemWithFeed>> = _itemState.asStateFlow()

    init {
        screenModelScope.launch(dispatcher) {
            database.accountDao().selectCurrentAccount()
                .collect { account ->
                    this@ItemScreenModel.account = account!!

                    // With Fever, we notify directly the server about state changes
                    // so we need account credentials
                    if (account.type == AccountType.FEVER) {
                        get<SharedPreferences>().apply {
                            account.login = getString(account.loginKey, null)
                            account.password = getString(account.passwordKey, null)
                        }
                    }

                    repository = get { parametersOf(account) }

                    if (itemIndex > -1) {
                        itemState = buildPager()
                    } else {
                        val query = ItemSelectionQueryBuilder.buildQuery(
                            itemId = itemId,
                            separateState = account.config.useSeparateState
                        )

                        database.itemDao().selectItemById(query)
                            .collect { itemWithFeed ->
                                _itemState.update { PagingData.from(listOf(itemWithFeed)) }
                            }
                    }
                }
        }

        screenModelScope.launch(dispatcher) {
            combine(
                preferences.openLinksWith.flow,
                preferences.theme.flow
            ) { openLinksWith, theme ->
                openLinksWith to theme
            }.collect { (openLinksWith, theme) ->
                mutableState.update {
                    it.copy(
                        openInExternalBrowser = when (openLinksWith) {
                            "external_navigator" -> true
                            else -> false
                        },
                        theme = theme
                    )
                }
            }
        }
    }

    private fun createPagingSource(): PagingSource<Int, ItemWithFeed> {
        val query = ItemsQueryBuilder.buildItemsQuery(
            queryFilters = queryFilters,
            separateState = account.config.useSeparateState
        )

        return database.itemDao().selectAll(query).apply {
            pagingSource = this
        }
    }

    private suspend fun buildPager(): StateFlow<PagingData<ItemWithFeed>> {
        val pageNb = (((itemIndex + PAGING_PAGE_SIZE - 1) / PAGING_PAGE_SIZE) + 1)
            .coerceAtLeast(1)

        return Pager(
            config = PagingConfig(
                initialLoadSize = PAGING_PAGE_SIZE * pageNb,
                pageSize = PAGING_PAGE_SIZE,
                prefetchDistance = PAGING_PREFETCH_DISTANCE
            ),
            pagingSourceFactory = { createPagingSource() }
        )
            .flow
            .map {
                it.map { itemWithFeed ->
                    val stateChange = state.value.stateChanges
                        .firstOrNull { stateChange -> stateChange.itemId == itemWithFeed.item.id }

                    if (stateChange != null) {
                        itemWithFeed.copy(
                            isRead = if (stateChange.readChange) {
                                !itemWithFeed.isRead
                            } else {
                                itemWithFeed.isRead
                            },
                            isStarred = if (stateChange.starChange) {
                                !itemWithFeed.isStarred
                            } else {
                                itemWithFeed.isStarred
                            }
                        )
                    } else {
                        itemWithFeed
                    }
                }
            }
            .cachedIn(screenModelScope)
            .stateIn(screenModelScope)
    }

    // TODO this must be tested one way or another
    private fun updateStateChange(item: Item, readChange: Boolean) {
        val stateChange = state.value.stateChanges.firstOrNull { it.itemId == item.id }

        if (stateChange != null) {
            val newStateChange = if (readChange) {
                stateChange.copy(readChange = !stateChange.readChange)
            } else {
                stateChange.copy(starChange = !stateChange.starChange)
            }

            if (!newStateChange.readChange && !newStateChange.starChange) {
                mutableState.update {
                    it.copy(stateChanges = it.stateChanges.filterNot { stateChange -> stateChange.itemId == item.id })
                }
            } else {
                mutableState.update {
                    it.copy(stateChanges = it.stateChanges.map { mapStateChange ->
                        if (mapStateChange.itemId == item.id) {
                            newStateChange
                        } else {
                            mapStateChange
                        }
                    })
                }
            }
        } else {
            mutableState.update {
                it.copy(
                    stateChanges = it.stateChanges + if (readChange) {
                        StateChange(
                            item = item,
                            readChange = true
                        )
                    } else {
                        StateChange(
                            item = item,
                            starChange = true
                        )
                    }
                )
            }
        }
    }

    fun setItemRead(itemWithFeed: ItemWithFeed) {
        val item = itemWithFeed.item

        if (!itemWithFeed.isRead && !state.value.stateChanges.any { it.item.id == item.id }) {
            setItemReadState(item)
        }
    }

    fun setItemReadState(item: Item) {
        if (useStateChanges) {
            updateStateChange(item, readChange = true)
            pagingSource.invalidate()
        } else {
            screenModelScope.launch(dispatcher) {
                repository.setItemReadState(item.apply { isRead = !isRead })
            }
        }
    }

    fun setItemStarState(item: Item) {
        if (useStateChanges) {
            updateStateChange(item, readChange = false)
            pagingSource.invalidate()
        } else {
            screenModelScope.launch(dispatcher) {
                repository.setItemStarState(item.apply { isStarred = !isStarred })
            }
        }
    }

    fun openImageDialog(url: String) = mutableState.update { it.copy(imageDialogUrl = url) }

    fun closeImageDialog() = mutableState.update { it.copy(imageDialogUrl = null) }

    fun downloadImage(url: String, context: Context) {
        screenModelScope.launch(dispatcher) {
            val bitmap = getImage(url, context)

            if (bitmap == null) {
                mutableState.update { it.copy(error = context.getString(R.string.error_image_download)) }
                return@launch
            }

            val target = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                url.substringAfterLast('/')
            ).apply {
                outputStream().apply {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, this)
                    flush()
                    close()
                }
            }

            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), null, null)
            mutableState.update { it.copy(fileDownloadedEvent = true) }
        }
    }

    fun shareImage(url: String, context: Context) {
        screenModelScope.launch(dispatcher) {
            val bitmap = getImage(url, context)
            if (bitmap == null) {
                mutableState.update { it.copy(error = context.getString(R.string.error_image_download)) }
                return@launch
            }

            val uri = saveImageInCache(bitmap, url, context)

            Intent().apply {
                action = Intent.ACTION_SEND

                clipData = ClipData.newRawUri(null, uri)
                putExtra(Intent.EXTRA_STREAM, uri)

                type = "image/*"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }.also {
                context.startActivity(Intent.createChooser(it, null))
            }
        }
    }

    private suspend fun getImage(url: String, context: Context): Bitmap? {
        val downloader = context.imageLoader

        val image = downloader.execute(
            ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
        ).image

        return image?.toBitmap()
    }

    private fun saveImageInCache(bitmap: Bitmap, url: String, context: Context): Uri {
        val imagesFolder = File(context.cacheDir.absolutePath, "images")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val name = URI.create(url).path.substringAfterLast('/')
        val image = File(imagesFolder, name).apply {
            outputStream().apply {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, this)
                flush()
                close()
            }
        }

        return FileProvider.getUriForFile(context, context.packageName, image)
    }

    fun shareItem(item: Item, context: Context) = Utils.shareItem(
        item, context, useCustomShareIntentTpl.value, customShareIntentTpl.value
    )

    override fun onDispose() {
        screenModelScope.launch(dispatcher) {
            withContext(NonCancellable) {
                repository.setItemsRead(
                    items = state.value.stateChanges
                        .filter { it.readChange }
                        .map { it.item }
                )

                state.value.stateChanges
                    .filter { it.starChange }
                    .forEach {
                        repository.setItemStarState(it.item.apply {
                            isStarred = !isStarred
                        })
                    }
            }
        }
    }
}

@Stable
data class ItemState(
    val imageDialogUrl: String? = null,
    val fileDownloadedEvent: Boolean = false,
    val openInExternalBrowser: Boolean = false,
    val theme: String? = "",
    val error: String? = null,
    val stateChanges: List<StateChange> = listOf()
)

@Stable
data class StateChange(
    val item: Item,
    val starChange: Boolean = false,
    val readChange: Boolean = false
) {

    val itemId: Int
        get() = item.id
}
