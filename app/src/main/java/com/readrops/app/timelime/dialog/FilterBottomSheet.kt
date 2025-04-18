package com.readrops.app.timelime.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.filters.OrderField
import com.readrops.db.filters.OrderType
import com.readrops.db.filters.QueryFilters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    filters: QueryFilters,
    onSetShowReadItems: () -> Unit,
    onSetOrderField: () -> Unit,
    onSetOrderType: () -> Unit,
    onDismiss: () -> Unit
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        //sheetState = rememberStandardBottomSheetState()
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
        ) {
            Text(
                text = stringResource(R.string.filters),
                style = MaterialTheme.typography.titleMedium
            )

            ShortSpacer()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSetShowReadItems)
            ) {
                Checkbox(
                    checked = filters.showReadItems,
                    onCheckedChange = { onSetShowReadItems() }
                )

                ShortSpacer()

                Text(
                    text = stringResource(R.string.show_read_articles)
                )
            }

            ShortSpacer()

            Column(
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.order_by))

                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = {
                            RichTooltip(
                                title = { Text(text = stringResource(id = R.string.order_by)) }
                            ) {
                                Text(
                                    text = stringResource(R.string.order_field_tooltip),
                                )
                            }
                        },
                        state = tooltipState
                    ) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    tooltipState.show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null
                            )
                        }
                    }
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = filters.orderField == OrderField.ID,
                        onClick = onSetOrderField,
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(text = stringResource(R.string.identifier))
                    }

                    SegmentedButton(
                        selected = filters.orderField == OrderField.DATE,
                        onClick = onSetOrderField,
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(text = stringResource(R.string.date))
                    }
                }

                MediumSpacer()

                Text(text = stringResource(R.string.with_direction))

                ShortSpacer()

                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = filters.orderType == OrderType.ASC,
                        onClick = onSetOrderType,
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(text = stringResource(R.string.ascending))
                    }

                    SegmentedButton(
                        selected = filters.orderType == OrderType.DESC,
                        onClick = onSetOrderType,
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(text = stringResource(R.string.descending))
                    }
                }
            }

            LargeSpacer()
        }
    }
}

@DefaultPreview
@Composable
private fun FilterBottomSheetPreview() {
    ReadropsTheme {
        FilterBottomSheet(
            onSetShowReadItems = {},
            onSetOrderType = {},
            onSetOrderField = {},
            filters = QueryFilters(),
            onDismiss = {}
        )
    }
}