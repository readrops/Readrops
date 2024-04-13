package com.readrops.app.compose.item

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.db.Database
import com.readrops.db.entities.Item
import com.readrops.db.entities.account.Account
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemSelectionQueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class ItemScreenModel(
    private val database: Database,
    private val itemId: Int,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<ItemState>(ItemState()), KoinComponent {

    //TODO Is this really the best solution?
    lateinit var account: Account
    lateinit var repository: BaseRepository

    init {
        screenModelScope.launch(dispatcher) {
            database.newAccountDao().selectCurrentAccount()
                .collect { account ->
                    this@ItemScreenModel.account = account!!
                    repository = get { parametersOf(account) }

                    val query = ItemSelectionQueryBuilder.buildQuery(
                        itemId = itemId,
                        separateState = account.config.useSeparateState
                    )

                    database.newItemDao().selectItemById(query)
                        .collect { itemWithFeed ->
                            mutableState.update {
                                it.copy(itemWithFeed = itemWithFeed)
                            }
                        }
                }
        }
    }

    fun formatText(): String {
        val itemWithFeed = state.value.itemWithFeed!!

        val document = if (itemWithFeed.websiteUrl != null) Jsoup.parse(
            Parser.unescapeEntities(itemWithFeed.item.text, false), itemWithFeed.websiteUrl
        ) else Jsoup.parse(
            Parser.unescapeEntities(itemWithFeed.item.text, false)
        )

        document.select("div,span").forEach { it.clearAttributes() }
        return document.body().html()
    }

    fun shareItem(item: Item, context: Context) {
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, item.link)
        }.also {
            context.startActivity(Intent.createChooser(it, null))
        }
    }

    fun setItemReadState(item: Item) {
        //TODO support separateState
        screenModelScope.launch(dispatcher) {
            repository.setItemReadState(item)
        }
    }

    fun setItemStarState(item: Item) {
        //TODO support separateState
        screenModelScope.launch(dispatcher) {
            repository.setItemStarState(item)
        }
    }
}

@Stable
data class ItemState(
    val itemWithFeed: ItemWithFeed? = null
)