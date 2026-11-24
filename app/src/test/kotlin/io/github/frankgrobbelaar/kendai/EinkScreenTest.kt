package io.github.frankgrobbelaar.kendai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EinkScreenTest {
    @Test
    fun `e-ink readers are known by their maker, however it's written`() {
        assertTrue(EinkScreen.detect(manufacturer = "ONYX", brand = "Onyx"))
        assertTrue(EinkScreen.detect(manufacturer = "Onyx International", brand = "BOOX"))
        assertTrue(EinkScreen.detect(manufacturer = "Bigme", brand = "bigme"))
        assertTrue(EinkScreen.detect(manufacturer = "unknown", brand = "Meebook"))
    }

    @Test
    fun `phones and tablets aren't`() {
        assertFalse(EinkScreen.detect(manufacturer = "samsung", brand = "samsung"))
        assertFalse(EinkScreen.detect(manufacturer = "Google", brand = "google"))
    }

    @Test
    fun `automatic follows the screen, and on and off override it`() {
        assertEquals(listOf(true, false), listOf(true, false).map(EinkMode.Automatic::isOn))
        assertEquals(listOf(true, true), listOf(true, false).map(EinkMode.On::isOn))
        assertEquals(listOf(false, false), listOf(true, false).map(EinkMode.Off::isOn))
    }
}
