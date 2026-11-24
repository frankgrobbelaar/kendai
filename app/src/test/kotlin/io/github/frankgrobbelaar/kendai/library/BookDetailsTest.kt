package io.github.frankgrobbelaar.kendai.library

import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BookDetailsTest {
    @get:Rule val folder = TemporaryFolder()

    private fun file(name: String) = File(folder.root, name)

    @Test
    fun `an EPUB gives its title, author, series and cover`() {
        val found = BookDetails.read(Samples.epub(file("moby.epub")), BookFormat.Epub)

        assertEquals(
            Details("Moby-Dick", "Herman Melville", "Sea stories", 2.0),
            found.details,
        )
        assertArrayEquals(Samples.PNG, found.cover)
    }

    @Test
    fun `an EPUB without a title or cover is named after its file`() {
        val found =
            BookDetails.read(
                Samples.epub(file("the_white_whale.epub"), title = "", cover = null),
                BookFormat.Epub,
            )

        assertEquals("the white whale", found.details.title)
        assertNull(found.cover)
    }

    @Test
    fun `an FB2 gives its details and the cover from its Base64`() {
        val found = BookDetails.read(Samples.fb2(file("valley.fb2")), BookFormat.Fb2)

        assertEquals(
            Details("The Valley of Fear", "Arthur Conan Doyle", "Sherlock Holmes", 4.0),
            found.details,
        )
        assertArrayEquals(Samples.PNG, found.cover)
    }

    @Test
    fun `a comic's first page is its cover, in page order, and ComicInfo names it`() {
        val found = BookDetails.read(Samples.cbz(file("nemo.cbz")), BookFormat.Cbz)

        assertEquals(Details("Little Nemo 1", "Winsor McCay", "Little Nemo", 1.0), found.details)
        assertArrayEquals(Samples.PNG, found.cover)
    }

    @Test
    fun `a text guide's first lines become its cover, cut to fit`() {
        val guide =
            Samples.text(
                file("starfall.txt"),
                "STARFALL TACTICS WALKTHROUGH, VERSION 1.2\n\n\n[WLK01] The beginning\n",
            )

        val found = BookDetails.read(guide, BookFormat.Txt)

        assertEquals("starfall", found.details.title)
        assertEquals("STARFALL TACTICS WALKTHROUGH\n[WLK01] The beginning", found.details.excerpt)
    }

    @Test
    fun `Markdown and HTML take their titles from their headings`() {
        val notes = Samples.text(file("notes.md"), "Some words first\n# Speedrun notes\n## Route")
        val manual =
            Samples.text(
                file("manual.html"),
                "<html><head><title>Router &amp; modem manual</title></head></html>",
            )

        assertEquals("Speedrun notes", BookDetails.read(notes, BookFormat.Markdown).details.title)
        assertEquals(
            "Router & modem manual",
            BookDetails.read(manual, BookFormat.Html).details.title,
        )
    }

    @Test
    fun `a damaged book still joins the library, named after its file`() {
        val broken = Samples.text(file("Broken Book.epub"), "this is not a zip")

        val found = BookDetails.read(broken, BookFormat.Epub)

        assertEquals(Details("Broken Book"), found.details)
        assertNull(found.cover)
    }

    @Test
    fun `a title from a file's name loses its extension and underscores`() {
        assertEquals("The Tale of Genji", BookDetails.titleFromName("The_Tale__of Genji.epub"))
        assertEquals(".epub", BookDetails.titleFromName(".epub"))
    }
}
