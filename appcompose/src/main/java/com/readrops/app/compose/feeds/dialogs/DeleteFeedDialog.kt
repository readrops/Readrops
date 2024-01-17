package com.readrops.app.compose.feeds.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.readrops.db.entities.Feed

@Composable
fun DeleteFeedDialog(
    feed: Feed,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
            )
        },
        title = {
            Text(text = "Delete feed")
        },
        text = {
            Text(text = "Do you want to delete feed ${feed.name}?")
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        },
    )
}