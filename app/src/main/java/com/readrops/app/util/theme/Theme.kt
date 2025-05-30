package com.readrops.app.util.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

private val BlackWhiteLightScheme = lightColorScheme(
    primary = primaryBlackWhiteLight,
    onPrimary = onPrimaryBlackWhiteLight,
    primaryContainer = primaryContainerBlackWhiteLight,
    onPrimaryContainer = onPrimaryContainerBlackWhiteLight,
    secondary = secondaryBlackWhiteLight,
    onSecondary = onSecondaryBlackWhiteLight,
    secondaryContainer = secondaryContainerBlackWhiteLight,
    onSecondaryContainer = onSecondaryContainerBlackWhiteLight,
    tertiary = tertiaryBlackWhiteLight,
    onTertiary = onTertiaryBlackWhiteLight,
    tertiaryContainer = tertiaryContainerBlackWhiteLight,
    onTertiaryContainer = onTertiaryContainerBlackWhiteLight,
    error = errorBlackWhiteLight,
    onError = onErrorBlackWhiteLight,
    errorContainer = errorContainerBlackWhiteLight,
    onErrorContainer = onErrorContainerBlackWhiteLight,
    background = backgroundBlackWhiteLight,
    onBackground = onBackgroundBlackWhiteLight,
    surface = surfaceBlackWhiteLight,
    onSurface = onSurfaceBlackWhiteLight,
    surfaceVariant = surfaceVariantBlackWhiteLight,
    onSurfaceVariant = onSurfaceVariantBlackWhiteLight,
    outline = outlineBlackWhiteLight,
    outlineVariant = outlineVariantBlackWhiteLight,
    scrim = scrimBlackWhiteLight,
    inverseSurface = inverseSurfaceBlackWhiteLight,
    inverseOnSurface = inverseOnSurfaceBlackWhiteLight,
    inversePrimary = inversePrimaryBlackWhiteLight,
    surfaceDim = surfaceDimBlackWhiteLight,
    surfaceBright = surfaceBrightBlackWhiteLight,
    surfaceContainerLowest = surfaceContainerLowestBlackWhiteLight,
    surfaceContainerLow = surfaceContainerLowBlackWhiteLight,
    surfaceContainer = surfaceContainerBlackWhiteLight,
    surfaceContainerHigh = surfaceContainerHighBlackWhiteLight,
    surfaceContainerHighest = surfaceContainerHighestBlackWhiteLight,
)

private val BlackWhiteDarkScheme = lightColorScheme(
    primary = primaryBlackWhiteDark,
    onPrimary = onPrimaryBlackWhiteDark,
    primaryContainer = primaryContainerBlackWhiteDark,
    onPrimaryContainer = onPrimaryContainerBlackWhiteDark,
    secondary = secondaryBlackWhiteDark,
    onSecondary = onSecondaryBlackWhiteDark,
    secondaryContainer = secondaryContainerBlackWhiteDark,
    onSecondaryContainer = onSecondaryContainerBlackWhiteDark,
    tertiary = tertiaryBlackWhiteDark,
    onTertiary = onTertiaryBlackWhiteDark,
    tertiaryContainer = tertiaryContainerBlackWhiteDark,
    onTertiaryContainer = onTertiaryContainerBlackWhiteDark,
    error = errorBlackWhiteDark,
    onError = onErrorBlackWhiteDark,
    errorContainer = errorContainerBlackWhiteDark,
    onErrorContainer = onErrorContainerBlackWhiteDark,
    background = backgroundBlackWhiteDark,
    onBackground = onBackgroundBlackWhiteDark,
    surface = surfaceBlackWhiteDark,
    onSurface = onSurfaceBlackWhiteDark,
    surfaceVariant = surfaceVariantBlackWhiteDark,
    onSurfaceVariant = onSurfaceVariantBlackWhiteDark,
    outline = outlineBlackWhiteDark,
    outlineVariant = outlineVariantBlackWhiteDark,
    scrim = scrimBlackWhiteDark,
    inverseSurface = inverseSurfaceBlackWhiteDark,
    inverseOnSurface = inverseOnSurfaceBlackWhiteDark,
    inversePrimary = inversePrimaryBlackWhiteDark,
    surfaceDim = surfaceDimBlackWhiteDark,
    surfaceBright = surfaceBrightBlackWhiteDark,
    surfaceContainerLowest = surfaceContainerLowestBlackWhiteDark,
    surfaceContainerLow = surfaceContainerLowBlackWhiteDark,
    surfaceContainer = surfaceContainerBlackWhiteDark,
    surfaceContainerHigh = surfaceContainerHighBlackWhiteDark,
    surfaceContainerHighest = surfaceContainerHighestBlackWhiteDark,
)

@Composable
fun ReadropsTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    themeColorScheme: String = "readrops",
    content: @Composable () -> Unit
) {


    val colors = when(themeColorScheme) {
        "blackwhite" -> {
            if (!useDarkTheme) {
                BlackWhiteLightScheme
            } else {
                BlackWhiteDarkScheme
            }
        }
	"material3" -> {
	    if (!useDarkTheme) {
		dynamicLightColorScheme(LocalContext.current)
	    } else {
		dynamicDarkColorScheme(LocalContext.current)
	    }
	} else -> {
            if (!useDarkTheme) {
                lightScheme
            } else {
                darkScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
