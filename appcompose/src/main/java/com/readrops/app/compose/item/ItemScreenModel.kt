package com.readrops.app.compose.item

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.db.Database
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.queries.ItemSelectionQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class ItemScreenModel(
    private val database: Database,
    private val itemId: Int
) : StateScreenModel<ItemState>(ItemState()) {

    init {
        screenModelScope.launch(Dispatchers.IO) {
            mutableState.update {
                val query = ItemSelectionQueryBuilder.buildQuery(itemId, false)

                it.copy(
                    itemWithFeed = database.newItemDao().selectItemById(query)
                )
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
}

@Stable
data class ItemState(
    val itemWithFeed: ItemWithFeed? = null
)