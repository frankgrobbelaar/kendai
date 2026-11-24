package io.github.frankgrobbelaar.kendai.ui

import io.github.frankgrobbelaar.kendai.EinkMode
import io.github.frankgrobbelaar.kendai.KendaiSettings
import io.github.frankgrobbelaar.kendai.MainDispatcherRule
import io.github.frankgrobbelaar.kendai.MemoryStore
import io.github.frankgrobbelaar.kendai.Theme
import io.github.frankgrobbelaar.kendai.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private fun viewModel(einkScreen: Boolean) =
        SettingsViewModel(
            settings = KendaiSettings(MemoryStore()),
            einkScreen = einkScreen,
            dynamicColorAvailable = true,
            version = "0.1.0",
        )

    @Test
    fun `e-ink mode is on by itself on an e-ink reader, and can be turned off`() = runTest {
        val settings = viewModel(einkScreen = true)
        backgroundScope.launch { settings.state.collect {} }

        assertTrue(settings.state.value.einkOn)
        assertTrue(settings.state.value.einkScreen)

        settings.setEinkMode(EinkMode.Off)
        assertFalse(settings.state.first { it.choices.einkMode == EinkMode.Off }.einkOn)
    }

    @Test
    fun `e-ink mode can be turned on anywhere, and the theme is kept`() = runTest {
        val settings = viewModel(einkScreen = false)
        backgroundScope.launch { settings.state.collect {} }
        assertFalse(settings.state.value.einkOn)

        settings.setEinkMode(EinkMode.On)
        settings.setTheme(Theme.Dark)

        val state = settings.state.first { it.choices.theme == Theme.Dark }
        assertTrue(state.einkOn)
        assertEquals("0.1.0", state.version)
    }
}
