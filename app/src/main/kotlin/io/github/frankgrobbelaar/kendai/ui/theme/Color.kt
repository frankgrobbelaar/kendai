package io.github.frankgrobbelaar.kendai.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Washi in light: paper and ink with a vermilion seal. Paper is the surface, sheet the surface
 * container, rule the outline variant, ink 2 the on-surface variant, and seal soft the primary
 * container. See docs/design.md.
 */
internal val WashiLight =
    lightColorScheme(
        primary = Color(0xFFA9402B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF2D9CF),
        onPrimaryContainer = Color(0xFF5A1D11),
        inversePrimary = Color(0xFFE58A6E),
        secondary = Color(0xFF625649),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEDE4D6),
        onSecondaryContainer = Color(0xFF26211C),
        tertiary = Color(0xFF5E6B4E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFDDE3D0),
        onTertiaryContainer = Color(0xFF263020),
        background = Color(0xFFF5EFE4),
        onBackground = Color(0xFF26211C),
        surface = Color(0xFFF5EFE4),
        onSurface = Color(0xFF26211C),
        surfaceVariant = Color(0xFFEDE4D6),
        onSurfaceVariant = Color(0xFF625649),
        surfaceTint = Color(0xFFA9402B),
        inverseSurface = Color(0xFF26211C),
        inverseOnSurface = Color(0xFFF5EFE4),
        error = Color(0xFFA9402B),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF2D9CF),
        onErrorContainer = Color(0xFF5A1D11),
        outline = Color(0xFF8C7F70),
        outlineVariant = Color(0xFFDCD2C2),
        scrim = Color(0xFF26211C),
        surfaceBright = Color(0xFFFBF7F0),
        surfaceDim = Color(0xFFE9E1D3),
        surfaceContainerLowest = Color(0xFFFFFDF9),
        surfaceContainerLow = Color(0xFFF8F3EA),
        surfaceContainer = Color(0xFFFBF7F0),
        surfaceContainerHigh = Color(0xFFEFE7DA),
        surfaceContainerHighest = Color(0xFFE9E1D3),
    )

/** Washi in dark: a warm charcoal, never black, with the seal lightened so it still reads. */
internal val WashiDark =
    darkColorScheme(
        primary = Color(0xFFE58A6E),
        onPrimary = Color(0xFF1D1A16),
        primaryContainer = Color(0xFF4A2A20),
        onPrimaryContainer = Color(0xFFF8D5C9),
        inversePrimary = Color(0xFFA9402B),
        secondary = Color(0xFFB5A898),
        onSecondary = Color(0xFF1D1A16),
        secondaryContainer = Color(0xFF332D27),
        onSecondaryContainer = Color(0xFFEEE6DA),
        tertiary = Color(0xFFB9C7A4),
        onTertiary = Color(0xFF1D1A16),
        tertiaryContainer = Color(0xFF3B4630),
        onTertiaryContainer = Color(0xFFDDE3D0),
        background = Color(0xFF1D1A16),
        onBackground = Color(0xFFEEE6DA),
        surface = Color(0xFF1D1A16),
        onSurface = Color(0xFFEEE6DA),
        surfaceVariant = Color(0xFF332D27),
        onSurfaceVariant = Color(0xFFB5A898),
        surfaceTint = Color(0xFFE58A6E),
        inverseSurface = Color(0xFFEEE6DA),
        inverseOnSurface = Color(0xFF1D1A16),
        error = Color(0xFFE58A6E),
        onError = Color(0xFF1D1A16),
        errorContainer = Color(0xFF4A2A20),
        onErrorContainer = Color(0xFFF8D5C9),
        outline = Color(0xFF8E8273),
        outlineVariant = Color(0xFF3A342C),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF332D27),
        surfaceDim = Color(0xFF1D1A16),
        surfaceContainerLowest = Color(0xFF171411),
        surfaceContainerLow = Color(0xFF221E1A),
        surfaceContainer = Color(0xFF27231E),
        surfaceContainerHigh = Color(0xFF2F2A24),
        surfaceContainerHighest = Color(0xFF39332C),
    )

private val Ink = Color(0xFF000000)
private val Page = Color(0xFFFFFFFF)

/**
 * E-ink mode: black and white only, with no greys that ghost and no colour, even on colour e-ink.
 * Every role is one or the other.
 */
internal val WashiEink =
    lightColorScheme(
        primary = Ink,
        onPrimary = Page,
        primaryContainer = Page,
        onPrimaryContainer = Ink,
        inversePrimary = Page,
        secondary = Ink,
        onSecondary = Page,
        secondaryContainer = Page,
        onSecondaryContainer = Ink,
        tertiary = Ink,
        onTertiary = Page,
        tertiaryContainer = Page,
        onTertiaryContainer = Ink,
        background = Page,
        onBackground = Ink,
        surface = Page,
        onSurface = Ink,
        surfaceVariant = Page,
        onSurfaceVariant = Ink,
        surfaceTint = Page,
        inverseSurface = Ink,
        inverseOnSurface = Page,
        error = Ink,
        onError = Page,
        errorContainer = Page,
        onErrorContainer = Ink,
        outline = Ink,
        outlineVariant = Ink,
        scrim = Ink,
        surfaceBright = Page,
        surfaceDim = Page,
        surfaceContainerLowest = Page,
        surfaceContainerLow = Page,
        surfaceContainer = Page,
        surfaceContainerHigh = Page,
        surfaceContainerHighest = Page,
    )
