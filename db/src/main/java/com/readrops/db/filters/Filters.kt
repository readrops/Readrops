package com.readrops.db.filters

enum class MainFilter {
    STARS,
    NEW,
    ALL
}

enum class SubFilter {
    FEED,
    FOLDER,
    ALL
}

enum class OrderType {
    DESC,
    ASC
}

data class QueryFilters(
    val showReadItems: Boolean = true,
    val feedId: Int = 0,
    val folderId: Int = 0,
    val accountId: Int = 0,
    val mainFilter: MainFilter = MainFilter.ALL,
    val subFilter: SubFilter = SubFilter.ALL,
    val orderType: OrderType = OrderType.DESC,
)