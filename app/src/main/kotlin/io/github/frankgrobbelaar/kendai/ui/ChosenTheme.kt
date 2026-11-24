package io.github.frankgrobbelaar.kendai.ui

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.frankgrobbelaar.kendai.Theme
import io.github.frankgrobbelaar.kendai.appContainer
import io.github.frankgrobbelaar.kendai.isOn
import io.github.frankgrobbelaar.kendai.ui.theme.KendaiTheme

/**
 * Kendai's look as chosen in Settings: light, dark or as the device is, in Washi's colours or the
 * wallpaper's, and e-ink mode on an e-ink reader. Nothing is drawn until the choice is read, so the
 * wrong theme never flashes.
 */
@Composable
fun ChosenTheme(content: @Composable () -> Unit) {
    val container = appContainer()
    val choices by container.settings.choices.collectAsStateWithLifecycle(initialValue = null)
    val chosen = choices ?: return
    val eink = chosen.einkMode.isOn(container.einkScreen)
    val dark =
        !eink &&
            when (chosen.theme) {
                Theme.System -> isSystemInDarkTheme()
                Theme.Light -> false
                Theme.Dark -> true
            }
    // The status and navigation bars' icons follow kendai's theme, not the device's.
    val activity = LocalActivity.current as? ComponentActivity
    DisposableEffect(activity, dark) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
            navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark },
        )
        onDispose {}
    }
    KendaiTheme(
        darkTheme = dark,
        dynamicColor = chosen.dynamicColor && !eink,
        eink = eink,
        content = content,
    )
}

// The scrims Android's own edge-to-edge puts behind three-button navigation.
private val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
