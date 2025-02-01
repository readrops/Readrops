package com.readrops.app.item.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun rememberBottomBarNestedScrollConnection(density: Density = LocalDensity.current) =
    remember { BottomBarNestedScrollConnection(density) }

class BottomBarNestedScrollConnection(
    density: Density,
    val bottomBarHeight: Dp = 64.dp,
) : NestedScrollConnection {

    private val bottomBarHeightPx = with(density) { bottomBarHeight.roundToPx().toFloat() }

    var bottomBarOffset: Int by mutableIntStateOf(0)
        private set

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        val newOffset = bottomBarOffset.toFloat() + delta
        bottomBarOffset = newOffset.coerceIn(-bottomBarHeightPx, 0f).roundToInt()

        return Offset.Zero
    }
}