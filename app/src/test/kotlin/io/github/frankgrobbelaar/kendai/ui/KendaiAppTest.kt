package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.frankgrobbelaar.kendai.ui.theme.KendaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class KendaiAppTest {
    @get:Rule val compose = createComposeRule()

    /** A place in the navigation bar or rail, rather than its screen's title. */
    private fun place(label: String): SemanticsNodeInteraction =
        compose.onNode(hasText(label) and isSelectable())

    @Test
    @Config(qualifiers = "w360dp-h780dp")
    fun `a phone has the bar along the bottom, and each place opens from it`() {
        compose.setContent { KendaiTheme { KendaiApp() } }

        compose.onNodeWithTag(NAVIGATION_BAR).assertExists()
        compose.onNodeWithTag(NAVIGATION_RAIL).assertDoesNotExist()
        compose.waitForText("Your library is empty")

        place("Jobs").performClick()
        place("Jobs").assertIsSelected()
        compose.onNodeWithText("Nothing running").assertExists()

        place("Settings").performClick()
        compose.onNodeWithText("E-ink mode").assertExists()
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp")
    fun `turning e-ink mode on keeps the place you're in`() {
        var eink by mutableStateOf(false)
        compose.setContent { KendaiTheme(eink = eink) { KendaiApp() } }
        place("Jobs").performClick()

        eink = true
        compose.waitForIdle()

        place("Jobs").assertIsSelected()
        compose.onNodeWithText("Nothing running").assertExists()
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp")
    fun `an empty library leads to its folders in Settings, which have the screen to themselves`() {
        compose.setContent { KendaiTheme { KendaiApp() } }

        compose.waitForText("Choose folders")
        compose.onNodeWithText("Choose folders").performClick()

        compose.onNodeWithText("Library folders").assertExists()
        compose.onNodeWithTag(NAVIGATION_BAR).assertDoesNotExist()

        compose.onNodeWithContentDescription("Back").performClick()

        compose.onNodeWithText("E-ink mode").assertExists()
        place("Settings").assertIsSelected()
    }

    @Test
    @Config(qualifiers = "w1040dp-h650dp")
    fun `a tablet has the rail down the side`() {
        compose.setContent { KendaiTheme { KendaiApp() } }

        compose.onNodeWithTag(NAVIGATION_RAIL).assertExists()
        compose.onNodeWithTag(NAVIGATION_BAR).assertDoesNotExist()
    }
}
