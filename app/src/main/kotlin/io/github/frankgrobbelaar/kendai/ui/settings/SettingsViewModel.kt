package io.github.frankgrobbelaar.kendai.ui.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.frankgrobbelaar.kendai.BuildConfig
import io.github.frankgrobbelaar.kendai.Choices
import io.github.frankgrobbelaar.kendai.EinkMode
import io.github.frankgrobbelaar.kendai.KendaiApplication
import io.github.frankgrobbelaar.kendai.KendaiSettings
import io.github.frankgrobbelaar.kendai.Theme
import io.github.frankgrobbelaar.kendai.isOn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val choices: Choices = Choices(),
    /** Whether this device's screen is e-ink, which Automatic follows. */
    val einkScreen: Boolean = false,
    /** Whether e-ink mode is on now. */
    val einkOn: Boolean = false,
    /** Dynamic colour needs Android 12; before that, Settings doesn't offer it. */
    val dynamicColorAvailable: Boolean = false,
    val version: String = "",
)

class SettingsViewModel(
    private val settings: KendaiSettings,
    einkScreen: Boolean,
    dynamicColorAvailable: Boolean,
    version: String,
) : ViewModel() {

    // What the screen shows before the settings are read: the defaults, on this device.
    private val fixed =
        SettingsUiState(
            einkScreen = einkScreen,
            einkOn = Choices().einkMode.isOn(einkScreen),
            dynamicColorAvailable = dynamicColorAvailable,
            version = version,
        )

    val state: StateFlow<SettingsUiState> =
        settings.choices
            .map { fixed.copy(choices = it, einkOn = it.einkMode.isOn(einkScreen)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS), fixed)

    fun setTheme(theme: Theme) {
        viewModelScope.launch { settings.setTheme(theme) }
    }

    fun setDynamicColor(on: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(on) }
    }

    fun setEinkMode(mode: EinkMode) {
        viewModelScope.launch { settings.setEinkMode(mode) }
    }

    companion object {
        /** Kept alive this long after the screen goes, so turning the device doesn't restart it. */
        private const val STOP_AFTER_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as KendaiApplication).container
                SettingsViewModel(
                    settings = container.settings,
                    einkScreen = container.einkScreen,
                    dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                    version = BuildConfig.VERSION_NAME,
                )
            }
        }
    }
}
