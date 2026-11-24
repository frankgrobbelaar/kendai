package io.github.frankgrobbelaar.kendai.library

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

// Android's real picture decoding, which tells a picture from anything else.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class CoversTest {
    @get:Rule val folder = TemporaryFolder()

    private fun png(width: Int, height: Int): ByteArray =
        ByteArrayOutputStream()
            .also {
                ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", it)
            }
            .toByteArray()

    @Test
    fun `keeps a picture as a small cover, named by the book's fingerprint`() {
        val covers = Covers(File(folder.root, "covers"))

        assertTrue(covers.keep("moby", png(600, 900)))
        assertTrue(covers.fileOf("moby").isFile)
    }

    @Test
    fun `keeps a picture far taller than it is wide`() {
        val covers = Covers(File(folder.root, "covers"))

        assertTrue(covers.keep("strip", png(1, 800)))
    }

    @Test
    fun `turns down what isn't a picture`() {
        val covers = Covers(File(folder.root, "covers"))

        assertFalse(covers.keep("text", "not a picture".toByteArray()))
        assertFalse(covers.fileOf("text").exists())
    }
}
