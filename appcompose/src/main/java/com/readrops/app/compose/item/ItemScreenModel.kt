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

}

@Stable
data class ItemState(
    val itemWithFeed: ItemWithFeed? = null
)