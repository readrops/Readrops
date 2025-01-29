package com.readrops.app.feeds.color

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.readrops.app.R
import com.readrops.app.util.FeedColors
import com.readrops.app.util.extensions.getColorOrNull
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedColorScreenModel(
    val feed: Feed,
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<FeedColorState>(FeedColorState(currentColor = feed.getColorOrNull())) {

    fun reloadColor(context: Context) {
        screenModelScope.launch(dispatcher) {
            val imageLoader = context.imageLoader
            val bitmap = imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(feed.iconUrl)
                    .allowHardware(false)
                    .build()
            ).image?.toBitmap()

            if (bitmap != null) {
                val newColor = FeedColors.getFeedColor(bitmap)
                    .run { Color(this) }
                mutableState.update { it.copy(newColor = newColor) }
            } else {
                mutableState.update { it.copy(error = context.getString(R.string.error_occurred_reloading_favicon_color)) }
            }
        }
    }

    fun showColorPickerDialog() = mutableState.update { it.copy(showColorPicker = true) }

    fun closeColorPickerDialog() = mutableState.update { it.copy(showColorPicker = false) }

    fun setNewColor(color: Color) = mutableState.update { it.copy(newColor = color) }

    fun resetColor() = mutableState.update { it.copy(newColor = null) }

    fun resetError() = mutableState.update { it.copy(error = null) }

    fun validate() {
        screenModelScope.launch {
            database.feedDao().updateFeedColor(feed.id, state.value.newColor!!.toArgb())
            mutableState.update { it.copy(canExit = true) }
        }
    }

}

data class FeedColorState(
    val currentColor: Color? = null,
    val newColor: Color? = null,
    val showColorPicker: Boolean = false,
    val error: String? = null,
    val canExit: Boolean = false
)

