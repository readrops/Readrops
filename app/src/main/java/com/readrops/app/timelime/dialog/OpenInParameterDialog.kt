package com.readrops.app.timelime.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.more.preferences.components.RadioButtonItem
import com.readrops.app.util.components.dialog.BaseDialog
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.db.entities.OpenIn

@Composable
fun OpenInParameterDialog(
    openIn: OpenIn,
    onValidate: (openIn: OpenIn, openInAsk: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var currentOpenIn by remember(openIn) { mutableStateOf(OpenIn.LOCAL_VIEW) }
    var isChecked by remember { mutableStateOf(false) }

    BaseDialog(
        title = stringResource(R.string.open_feed_in),
        icon = painterResource(R.drawable.ic_open_in_browser),
        onDismiss = onDismiss
    ) {
        RadioButtonItem(
            text = stringResource(R.string.local_view),
            isSelected = currentOpenIn == OpenIn.LOCAL_VIEW,
            onClick = { currentOpenIn = OpenIn.LOCAL_VIEW }
        )

        RadioButtonItem(
            text = stringResource(R.string.external_view),
            isSelected = currentOpenIn == OpenIn.EXTERNAL_VIEW,
            onClick = { currentOpenIn = OpenIn.EXTERNAL_VIEW }
        )

        ShortSpacer()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isChecked = !isChecked }
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it }
            )

            Text(
                text = stringResource(R.string.do_not_ask_again_next_feeds),
                style = MaterialTheme.typography.bodySmall
            )
        }

        ShortSpacer()

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(R.string.cancel))
            }

            TextButton(
                onClick = { onValidate(currentOpenIn, !isChecked) }
            ) {
                Text(text = stringResource(R.string.validate))
            }
        }

    }
}