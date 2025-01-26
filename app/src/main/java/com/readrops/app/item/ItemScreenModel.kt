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
import androidx.paging.cachedIn
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
import com.readrops.db.filters.QueryFilters
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemSelectionQueryBuilder
import com.readrops.db.queries.ItemsQueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    //TODO Is this really the best solution?
    lateinit var account: Account
    lateinit var repository: BaseRepository


    private var useCustomShareIntentTpl = preferences.useCustomShareIntentTpl.flow.stateIn(
        screenModelScope, SharingStarted.Eagerly, false
    )
    private var customShareIntentTpl = preferences.customShareIntentTpl.flow.stateIn(
        screenModelScope, SharingStarted.Eagerly, ""
    )

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
                        mutableState.update { it.copy(itemState = buildPager()) }
                    } else {
                        val query = ItemSelectionQueryBuilder.buildQuery(
                            itemId = itemId,
                            separateState = account.config.useSeparateState
                        )

                        database.itemDao().selectItemById(query)
                            .distinctUntilChanged()
                            .collect { itemWithFeed ->
                                mutableState.update {
                                    it.copy(itemState = flowOf(PagingData.from(listOf(itemWithFeed))))
                                }
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

    private fun buildPager(): Flow<PagingData<ItemWithFeed>> {
        val query = ItemsQueryBuilder.buildItemsQuery(
            queryFilters = queryFilters,
            separateState = account.config.useSeparateState
        )

        val pageNb = (((itemIndex + PAGING_PAGE_SIZE - 1) / PAGING_PAGE_SIZE) + 1)
            .coerceAtLeast(1)

        return Pager(
            config = PagingConfig(
                initialLoadSize = PAGING_PAGE_SIZE * pageNb,
                pageSize = PAGING_PAGE_SIZE,
                prefetchDistance = PAGING_PREFETCH_DISTANCE
            ),
            pagingSourceFactory = { database.itemDao().selectAll(query) }
        )
            .flow
            .cachedIn(screenModelScope)
    }

    fun shareItem(item: Item, context: Context) = Utils.shareItem(
        item, context, useCustomShareIntentTpl.value, customShareIntentTpl.value
    )

    fun setItemReadState(item: Item) {
        screenModelScope.launch(dispatcher) {
            repository.setItemReadState(item)
        }
    }

    fun setItemStarState(item: Item) {
        screenModelScope.launch(dispatcher) {
            repository.setItemStarState(item)
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
}

@Stable
data class ItemState(
    val itemState: Flow<PagingData<ItemWithFeed>> = emptyFlow(),
    val imageDialogUrl: String? = null,
    val fileDownloadedEvent: Boolean = false,
    val openInExternalBrowser: Boolean = false,
    val theme: String? = "",
    val error: String? = null,
)
