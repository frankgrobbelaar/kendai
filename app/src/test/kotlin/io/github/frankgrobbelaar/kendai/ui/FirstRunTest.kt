package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.frankgrobbelaar.kendai.TestKendaiApplication
import io.github.frankgrobbelaar.kendai.library.Samples
import io.github.frankgrobbelaar.kendai.ui.theme.KendaiTheme
import java.io.File
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h780dp")
class FirstRunTest {
    @get:Rule val compose = createComposeRule()

    private val app
        get() = RuntimeEnvironment.getApplication() as TestKendaiApplication

    private fun file(path: String) = File(app.root, path).apply { parentFile?.mkdirs() }

    @After
    fun giveAccessBack() {
        TestKendaiApplication.granted = true
    }

    @Test
    fun `the first run adds the folders ticked, then opens on the books found`() {
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.text(file("Documents/Taxes.pdf"), "not really a PDF")
        TestKendaiApplication.granted = true
        compose.setContent { KendaiTheme { KendaiRoot() } }

        compose.waitForText("Get started")
        compose.onNodeWithText("Get started").performClick()
        compose.waitForText("Books")
        compose.onNodeWithText("Books").assertIsOn()
        compose.onNodeWithText("Documents").assertIsOff()
        compose.onNodeWithText("Add 1 folder").performClick()

        compose.waitForText("Found 1 book")
        compose.onNodeWithText("Open the library").performClick()

        compose.waitForText("Moby-Dick")
        compose.onNodeWithText("Recently added").assertExists()
        compose.onNodeWithText("Moby-Dick").assertExists()
        compose.onNodeWithText("Herman Melville · EPUB · Books").assertExists()
    }

    @Test
    fun `without access, the first run explains why it's needed before Android asks`() {
        TestKendaiApplication.granted = false
        compose.setContent { KendaiTheme { KendaiRoot() } }

        compose.waitForText("Get started")
        compose.onNodeWithText("Get started").performClick()

        compose.onNodeWithText("Kendai reads your books where they are").assertExists()
        compose.onNodeWithText("Allow access").assertExists()
    }
}
