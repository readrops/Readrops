package com.readrops.api.services.nextcloudnews

data class NextcloudNewsSyncData(
    val lastModified: Long = 0,
    val readIds: List<Int> = listOf(),
    val unreadIds: List<Int> = listOf(),
    val starredIds: List<Int> = listOf(),
    val unstarredIds: List<Int> = listOf(),
)