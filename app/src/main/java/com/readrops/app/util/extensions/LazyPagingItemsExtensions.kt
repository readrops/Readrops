package com.readrops.app.util.extensions

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

fun <T : Any> LazyPagingItems<T>.isLoading(): Boolean {
    return loadState.refresh is LoadState.Loading && itemCount == 0
}

fun <T : Any> LazyPagingItems<T>.isError(): Boolean {
    return loadState.append is LoadState.Error //|| loadState.refresh is LoadState.Error
}

fun <T : Any> LazyPagingItems<T>.isNotEmpty(): Boolean {
    return itemCount > 0
}