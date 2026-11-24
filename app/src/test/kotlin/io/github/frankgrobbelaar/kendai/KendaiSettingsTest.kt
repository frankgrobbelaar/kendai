package io.github.frankgrobbelaar.kendai

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KendaiSettingsTest {
    @Test
    fun `starts as the device is, in Washi's colours, with e-ink mode automatic`() = runTest {
        assertEquals(Choices(), KendaiSettings(MemoryStore()).choices.first())
        assertEquals(Theme.System, Choices().theme)
        assertEquals(EinkMode.Automatic, Choices().einkMode)
    }

    @Test
    fun `keeps each choice`() = runTest {
        val settings = KendaiSettings(MemoryStore())

        settings.setTheme(Theme.Dark)
        settings.setDynamicColor(true)
        settings.setEinkMode(EinkMode.On)

        assertEquals(
            Choices(Theme.Dark, dynamicColor = true, EinkMode.On),
            settings.choices.first(),
        )
    }
}
