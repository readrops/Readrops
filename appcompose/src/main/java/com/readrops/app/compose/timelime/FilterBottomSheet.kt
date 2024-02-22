package com.readrops.app.compose.timelime

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
import com.readrops.app.compose.R
import com.readrops.app.compose.util.theme.LargeSpacer
import com.readrops.app.compose.util.theme.ShortSpacer
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.filters.ListSortType
import com.readrops.db.queries.QueryFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    viewModel: TimelineViewModel,
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
                    .clickable { viewModel.setShowReadItemsState(!filters.showReadItems) }
            ) {
                Checkbox(
                    checked = filters.showReadItems,
                    onCheckedChange = { viewModel.setShowReadItemsState(!filters.showReadItems) }
                )

                ShortSpacer()

                Text(
                    text = stringResource(R.string.show_read_articles)
                )
            }

            ShortSpacer()

            fun setSortTypeState() {
                viewModel.setSortTypeState(
                    if (filters.sortType == ListSortType.NEWEST_TO_OLDEST)
                        ListSortType.OLDEST_TO_NEWEST
                    else
                        ListSortType.NEWEST_TO_OLDEST
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { setSortTypeState() }
            ) {
                Checkbox(
                    checked = filters.sortType == ListSortType.OLDEST_TO_NEWEST,
                    onCheckedChange = { setSortTypeState() }
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