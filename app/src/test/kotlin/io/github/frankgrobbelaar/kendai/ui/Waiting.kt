package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule

/**
 * Waits for work Compose doesn't wait for, such as a Room query or looking through folders, to show
 * [text] on the screen.
 */
fun ComposeContentTestRule.waitForText(text: String) {
    waitUntil(timeoutMillis = 10_000) {
        onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
}
