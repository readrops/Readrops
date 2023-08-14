package com.readrops.app.compose.utils.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val veryShortSpacing: Dp = 4.dp,
    val shortSpacing: Dp = 8.dp,
    val mediumSpacing: Dp = 16.dp,
    val largeSpacing: Dp = 24.dp,
    val veryLargeSpacing: Dp = 48.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

@Composable
fun VeryShortSpacer() = Spacer(Modifier.size(MaterialTheme.spacing.veryShortSpacing))

@Composable
fun ShortSpacer() = Spacer(Modifier.size(MaterialTheme.spacing.shortSpacing))

@Composable
fun MediumSpacer() = Spacer(Modifier.size(MaterialTheme.spacing.mediumSpacing))

@Composable
fun LargeSpacer() = Spacer(Modifier.size(MaterialTheme.spacing.largeSpacing))

@Composable
fun VeryLargeSpacer() = Spacer(Modifier.size(MaterialTheme.spacing.veryLargeSpacing))