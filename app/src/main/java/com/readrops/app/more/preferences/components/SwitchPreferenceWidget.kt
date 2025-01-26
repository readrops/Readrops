package com.readrops.app.more.preferences.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.readrops.app.util.Preference
import kotlinx.coroutines.launch

@Composable
fun SwitchPreferenceWidget(
    preference: Preference<Boolean>,
    isChecked: Boolean,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onValueChanged: ((Boolean) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    suspend fun changeValue() {
        val newValue = !isChecked
        preference.write(newValue)
        onValueChanged?.let { it(newValue) }
    }

    BasePreference(
        title = title,
        subtitle = subtitle,
        onClick = {
            coroutineScope.launch {
                changeValue()
            }
        },
        rightComponent = {
            Switch(
                checked = isChecked,
                onCheckedChange = {
                    coroutineScope.launch {
                        changeValue()
                    }
                }
            )
        },
        modifier = modifier
    )
}