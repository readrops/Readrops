package com.readrops.app.util.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.readrops.app.R

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

private val BlackWhiteLightColors = lightColorScheme(
    primary = bw_theme_light_primary,
    onPrimary = bw_theme_light_onPrimary,
    primaryContainer = bw_theme_light_primaryContainer,
    onPrimaryContainer = bw_theme_light_onPrimaryContainer,
    secondary = bw_theme_light_secondary,
    onSecondary = bw_theme_light_onSecondary,
    secondaryContainer = bw_theme_light_secondaryContainer,
    onSecondaryContainer = bw_theme_light_onSecondaryContainer,
    tertiary = bw_theme_light_tertiary,
    onTertiary = bw_theme_light_onTertiary,
    tertiaryContainer = bw_theme_light_tertiaryContainer,
    onTertiaryContainer = bw_theme_light_onTertiaryContainer,
    error = bw_theme_light_error,
    errorContainer = bw_theme_light_errorContainer,
    onError = bw_theme_light_onError,
    onErrorContainer = bw_theme_light_onErrorContainer,
    background = bw_theme_light_background,
    onBackground = bw_theme_light_onBackground,
    surface = bw_theme_light_surface,
    onSurface = bw_theme_light_onSurface,
    surfaceVariant = bw_theme_light_surfaceVariant,
    onSurfaceVariant = bw_theme_light_onSurfaceVariant,
    outline = bw_theme_light_outline,
    inverseOnSurface = bw_theme_light_inverseOnSurface,
    inverseSurface = bw_theme_light_inverseSurface,
    inversePrimary = bw_theme_light_inversePrimary,
    surfaceTint = bw_theme_light_surfaceTint,
    outlineVariant = bw_theme_light_outlineVariant,
    scrim = bw_theme_light_scrim,
)

private val BlackWhiteDarkColors = lightColorScheme(
    primary = bw_theme_dark_primary,
    onPrimary = bw_theme_dark_onPrimary,
    primaryContainer = bw_theme_dark_primaryContainer,
    onPrimaryContainer = bw_theme_dark_onPrimaryContainer,
    secondary = bw_theme_dark_secondary,
    onSecondary = bw_theme_dark_onSecondary,
    secondaryContainer = bw_theme_dark_secondaryContainer,
    onSecondaryContainer = bw_theme_dark_onSecondaryContainer,
    tertiary = bw_theme_dark_tertiary,
    onTertiary = bw_theme_dark_onTertiary,
    tertiaryContainer = bw_theme_dark_tertiaryContainer,
    onTertiaryContainer = bw_theme_dark_onTertiaryContainer,
    error = bw_theme_dark_error,
    errorContainer = bw_theme_dark_errorContainer,
    onError = bw_theme_dark_onError,
    onErrorContainer = bw_theme_dark_onErrorContainer,
    background = bw_theme_dark_background,
    onBackground = bw_theme_dark_onBackground,
    surface = bw_theme_dark_surface,
    onSurface = bw_theme_dark_onSurface,
    surfaceVariant = bw_theme_dark_surfaceVariant,
    onSurfaceVariant = bw_theme_dark_onSurfaceVariant,
    outline = bw_theme_dark_outline,
    inverseOnSurface = bw_theme_dark_inverseOnSurface,
    inverseSurface = bw_theme_dark_inverseSurface,
    inversePrimary = bw_theme_dark_inversePrimary,
    surfaceTint = bw_theme_dark_surfaceTint,
    outlineVariant = bw_theme_dark_outlineVariant,
    scrim = bw_theme_dark_scrim,
)

@Composable
fun ReadropsTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    themeColourScheme: String = "readrops",
    content: @Composable () -> Unit
) {


    val colors = when(themeColourScheme) {
        "blackwhite" -> {
            if (!useDarkTheme) {
                BlackWhiteLightColors
            } else {
                BlackWhiteDarkColors
            }
        } else -> {
            if (!useDarkTheme) {
                LightColors
            } else {
                DarkColors
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}