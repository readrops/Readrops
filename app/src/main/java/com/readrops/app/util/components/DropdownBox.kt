package com.readrops.app.util.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.readrops.app.util.theme.ShortSpacer

data class DropdownBoxValue(
    val id: Int,
    val text: String,
    val painter: Painter,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownBox(
    expanded: Boolean,
    text: String,
    label: String,
    painter: Painter?,
    values: List<DropdownBoxValue>,
    onExpandedChange: (Boolean) -> Unit,
    onValueClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        if (values.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
            ) {
                for (value in values) {
                    DropdownMenuItem(
                        text = { Text(text = value.text) },
                        onClick = { onValueClick(value.id) },
                        leadingIcon = {
                            Image(
                                painter = value.painter,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = text,
            label = { Text(text = label) },
            enabled = enabled,
            readOnly = true,
            onValueChange = {},
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            leadingIcon = {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactDropdownBox(
    expanded: Boolean,
    text: String,
    values: List<DropdownBoxValue>,
    onExpandedChange: (Boolean) -> Unit,
    onValueClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        if (values.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                matchTextFieldWidth = false
            ) {
                for (value in values) {
                    DropdownMenuItem(
                        text = { Text(text = value.text) },
                        onClick = { onValueClick(value.id) },
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .clickable { onExpandedChange(!expanded) }
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ShortSpacer()

            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
    }
}