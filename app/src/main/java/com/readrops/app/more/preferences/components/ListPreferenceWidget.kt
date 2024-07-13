package com.readrops.app.more.preferences.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.readrops.app.util.Preference
import kotlinx.coroutines.launch

@Composable
fun <T> ListPreferenceWidget(
    preference: Preference<T>,
    entries: Map<T, String>,
    title: String,
    modifier: Modifier = Modifier,
    onValueChange: (T) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val selectedKey by preference.flow.collectAsStateWithLifecycle(initialValue = preference.default)

    if (showDialog) {
        val values = remember {
            entries.map { entry ->
                ToggleableInfo(
                    key = entry.key,
                    text = entry.value,
                    isSelected = selectedKey == entry.key
                )
            }.toMutableStateList()
        }

        RadioButtonPreferenceDialog(
            title = title,
            entries = values,
            onCheckChange = { newKey ->
                onValueChange(newKey)

                values.replaceAll {
                    it.copy(isSelected = it.key == newKey)
                }

                coroutineScope.launch {
                    preference.write(newKey)
                }
            },
            onDismiss = { showDialog = false }
        )
    }

    BasePreference(
        title = title,
        subtitle = entries[selectedKey],
        onClick = { showDialog = true },
        modifier = modifier
    )
}