package com.readrops.db.queries

import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder

object ItemSelectionQueryBuilder {

    private val COLUMNS = arrayOf(
        "Item.id",
        "Item.remote_id",
        "title",
        "Item.description",
        "content",
        "link",
        "pub_date",
        "image_link",
        "author",
        "icon_url",
        "color",
        "read_time",
        "Feed.name",
        "Feed.open_in",
        "Feed.id as feedId",
        "siteUrl",
        "Folder.id as folder_id",
        "Folder.name as folder_name"
    )

    private val SEPARATE_STATE_COLUMNS = arrayOf(
        "case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End is_read",
        "case When ItemState.remote_id is NULL Or ItemState.read = 1 Then 1 else 0 End read",
        "case When ItemState.starred = 1 Then 1 else 0 End is_starred",
        "case When ItemState.starred = 1 Then 1 else 0 End starred"
    )

    private const val JOIN =
        "Item Inner Join Feed On Item.feed_id = Feed.id Left Join Folder on Folder.id = Feed.folder_id"

    private const val SEPARATE_STATE_JOIN =
        " Left Join ItemState On ItemState.remote_id = Item.remote_id"

    /**
     * @param separateState Indicates if item state must be retrieved from ItemState table
     */
    @JvmStatic
    fun buildQuery(itemId: Int, separateState: Boolean): SupportSQLiteQuery {
        val tables = buildString {
            append(JOIN)
            if (separateState) append(SEPARATE_STATE_JOIN)
        }

        val columns = if (separateState) {
            COLUMNS + SEPARATE_STATE_COLUMNS
        } else {
            COLUMNS + arrayOf("read AS is_read", "read", "starred AS is_starred", "starred")
        }

        return SupportSQLiteQueryBuilder.builder(tables).run {
            columns(columns)
            selection("Item.id = $itemId", null)

            create()
        }
    }
}