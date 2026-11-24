package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.ui.graphics.Color
import io.github.frankgrobbelaar.kendai.ui.theme.WashiDark
import io.github.frankgrobbelaar.kendai.ui.theme.WashiEink
import io.github.frankgrobbelaar.kendai.ui.theme.WashiLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WashiThemeTest {
    @Test
    fun `light is paper and ink with the seal`() {
        assertEquals(Color(0xFFF5EFE4), WashiLight.background)
        assertEquals(Color(0xFF26211C), WashiLight.onBackground)
        assertEquals(Color(0xFFA9402B), WashiLight.primary)
    }

    @Test
    fun `dark is a warm charcoal, never black`() {
        assertNotEquals(Color.Black, WashiDark.background)
        assertEquals(Color(0xFF1D1A16), WashiDark.background)
    }

    @Test
    fun `e-ink is black and white only`() {
        val roles =
            listOf(
                WashiEink.primary,
                WashiEink.onPrimary,
                WashiEink.primaryContainer,
                WashiEink.background,
                WashiEink.onBackground,
                WashiEink.surface,
                WashiEink.onSurface,
                WashiEink.onSurfaceVariant,
                WashiEink.surfaceContainer,
                WashiEink.outline,
                WashiEink.outlineVariant,
            )
        assertTrue(roles.all { it == Color.Black || it == Color.White })
    }
}
