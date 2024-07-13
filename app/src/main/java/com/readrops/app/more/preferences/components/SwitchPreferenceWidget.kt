package com.readrops.app.more.preferences.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.readrops.app.util.Preference
import kotlinx.coroutines.launch

@Composable
fun SwitchPreferenceWidget(
    preference: Preference<Boolean>,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val isChecked by preference.flow.collectAsState(initial = preference.default)
    val coroutineScope = rememberCoroutineScope()

    BasePreference(
        title = title,
        subtitle = subtitle,
        onClick = {
            coroutineScope.launch {
                preference.write(!isChecked)
            }
        },
        rightComponent = {
            Switch(
                checked = isChecked,
                onCheckedChange = {
                    coroutineScope.launch {
                        preference.write(!isChecked)
                    }
                }
            )
        },
        modifier = modifier
    )
}