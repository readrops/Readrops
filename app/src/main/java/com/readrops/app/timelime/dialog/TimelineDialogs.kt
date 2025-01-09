package com.readrops.app.timelime.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.timelime.DialogState
import com.readrops.app.timelime.TimelineScreenModel
import com.readrops.app.timelime.TimelineState
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.db.entities.OpenIn
import com.readrops.db.filters.OrderField
import com.readrops.db.filters.OrderType
import com.readrops.db.pojo.ItemWithFeed

@Composable
fun TimelineDialogs(
    state: TimelineState,
    screenModel: TimelineScreenModel,
    onOpenItem: (ItemWithFeed, OpenIn) -> Unit
) {
    when (val dialog = state.dialog) {
        is DialogState.ConfirmDialog -> {
            TwoChoicesDialog(
                title = stringResource(R.string.mark_all_articles_read),
                text = stringResource(R.string.mark_all_articles_read_question),
                icon = painterResource(id = R.drawable.ic_rss_feed_grey),
                confirmText = stringResource(id = R.string.validate),
                dismissText = stringResource(id = R.string.cancel),
                onDismiss = { screenModel.closeDialog() },
                onConfirm = {
                    screenModel.closeDialog()
                    screenModel.setAllItemsRead()
                }
            )
        }

        is DialogState.FilterSheet -> {
            FilterBottomSheet(
                filters = state.filters,
                onSetShowReadItems = {
                    screenModel.setShowReadItemsState(!state.filters.showReadItems)
                },
                onSetOrderField = {
                    screenModel.setOrderFieldState(
                        if (state.filters.orderField == OrderField.ID) {
                            OrderField.DATE
                        } else {
                            OrderField.ID
                        }
                    )
                },
                onSetOrderType = {
                    screenModel.setOrderTypeState(
                        if (state.filters.orderType == OrderType.DESC) {
                            OrderType.ASC
                        } else {
                            OrderType.DESC
                        }
                    )
                },
                onDismiss = { screenModel.closeDialog() }
            )
        }

        is DialogState.ErrorList -> {
            ErrorListDialog(
                errorResult = dialog.errorResult,
                onDismiss = { screenModel.closeDialog(dialog) }
            )
        }

        is DialogState.OpenIn -> {
            val itemWithFeed = dialog.itemWithFeed

            OpenInParameterDialog(
                openIn = itemWithFeed.openIn!!,
                onValidate = { openIn, openInAsk ->
                    screenModel.updateOpenInParameter(
                        feedId = itemWithFeed.feedId,
                        openIn = openIn,
                        openInAsk = openInAsk
                    )

                    screenModel.closeDialog(dialog)

                    onOpenItem(itemWithFeed, openIn)
                    screenModel.setItemRead(itemWithFeed.item)
                },
                onDismiss = { screenModel.closeDialog(dialog) }
            )
        }

        else -> {}
    }
}