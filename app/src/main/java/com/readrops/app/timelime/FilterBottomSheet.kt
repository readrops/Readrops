package com.readrops.app.timelime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.filters.ListSortType
import com.readrops.db.queries.QueryFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onSetShowReadItemsState: () -> Unit,
    onSetSortTypeState: () -> Unit,
    filters: QueryFilters,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
        ) {
            Text(
                text = stringResource(R.string.filters)
            )

            ShortSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSetShowReadItemsState)
            ) {
                Checkbox(
                    checked = filters.showReadItems,
                    onCheckedChange = { onSetShowReadItemsState() }
                )

                ShortSpacer()

                Text(
                    text = stringResource(R.string.show_read_articles)
                )
            }

            ShortSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSetSortTypeState)
            ) {
                Checkbox(
                    checked = filters.sortType == ListSortType.OLDEST_TO_NEWEST,
                    onCheckedChange = { onSetSortTypeState() }
                )

                ShortSpacer()

                Text(
                    text = "Show oldest items first"
                )
            }

            LargeSpacer()
        }
    }
}