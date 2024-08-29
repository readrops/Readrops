package com.readrops.app.more.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.readrops.app.R
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceBaseDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = AlertDialogDefaults.shape,
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = modifier
                    .padding(MaterialTheme.spacing.largeSpacing)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AlertDialogDefaults.titleContentColor
                )

                MediumSpacer()

                content()
            }
        }
    }
}

data class  ToggleableInfo<T>(
    val key: T,
    val text: String,
    val isSelected: Boolean
)

@Composable
fun <T> RadioButtonPreferenceDialog(
    title: String,
    entries: List<ToggleableInfo<T>>,
    onCheckChange: (T) -> Unit,
    onDismiss: () -> Unit
) {
    PreferenceBaseDialog(
        title = title,
        onDismiss = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            entries.forEach { entry ->
                RadioButtonItem(
                    text = entry.text,
                    isSelected = entry.isSelected,
                    onClick = { onCheckChange(entry.key) }
                )
            }

            MediumSpacer()

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(id = R.string.back))
            }
        }
    }
}

@Composable
fun RadioButtonItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.shortSpacing,
                    vertical = MaterialTheme.spacing.veryShortSpacing
                )
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            LargeSpacer()

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
