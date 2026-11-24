package io.github.frankgrobbelaar.kendai.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Kendai's look: Washi on Material 3. With [dynamicColor] the wallpaper's colours replace Washi's
 * (Android 12 and later); type, shapes and motion stay. In [eink] mode everything is black and
 * white, rules are 2 dp, and nothing ripples.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KendaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    eink: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val dynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colors =
        when {
            eink -> WashiEink
            dynamic && darkTheme -> dynamicDarkColorScheme(context)
            dynamic -> dynamicLightColorScheme(context)
            darkTheme -> WashiDark
            else -> WashiLight
        }
    MaterialTheme(colorScheme = colors, typography = WashiTypography, shapes = WashiShapes) {
        // Text and icons with no colour of their own read on Washi's paper, light or dark. One
        // provider either way, so turning e-ink mode on or off keeps every screen's state.
        CompositionLocalProvider(
            LocalContentColor provides colors.onBackground,
            LocalEink provides eink,
            // A ripple is motion, and smears on e-ink.
            LocalRippleConfiguration provides if (eink) null else RippleConfiguration(),
            content = content,
        )
    }
}

/** What Material's theme doesn't carry: whether e-ink mode is on, and the rules it draws. */
object Washi {
    val eink: Boolean
        @Composable @ReadOnlyComposable get() = LocalEink.current

    /** Hairlines in light and dark; 2 dp in e-ink, where a hairline barely shows. */
    val ruleWidth: Dp
        @Composable @ReadOnlyComposable get() = if (LocalEink.current) 2.dp else 1.dp
}

internal val LocalEink = staticCompositionLocalOf { false }
