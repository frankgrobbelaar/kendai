package io.github.frankgrobbelaar.kendai.library

import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FolderSuggestionsTest {
    @get:Rule val folder = TemporaryFolder()

    private val root
        get() = folder.root.toPath().invariantSeparatorsPathString

    private fun touch(path: String) {
        File(folder.root, path).apply { parentFile?.mkdirs() }.writeText("x")
    }

    @Test
    fun `suggests the folders at the top that hold books, the fullest first`() = runTest {
        touch("Books/Moby-Dick.epub")
        touch("Books/Classics/Kokoro.EPUB")
        touch("Comics/Nemo.cbz")
        touch("Documents/Router manual.pdf")
        touch("Documents/Taxes.xlsx")
        touch("Guides/Starfall.txt")
        touch("Guides/Hollow Crown.epub")
        touch("Music/Song.mp3")
        touch(".Hidden/Secret.epub")
        touch("Android/data/some.app/Cached.epub")
        File(folder.root, "Empty").mkdirs()

        val found = FolderSuggestions(DeviceFileSystem(), root).find()

        assertEquals(
            listOf(
                Suggestion("$root/Books", "Books", BookCount(2, BookKind.Book)),
                Suggestion("$root/Guides", "Guides", BookCount(2)),
                Suggestion("$root/Comics", "Comics", BookCount(1, BookKind.Comic)),
                Suggestion("$root/Documents", "Documents", BookCount(1, BookKind.Document)),
            ),
            found,
        )
    }

    @Test
    fun `counts a folder picked anywhere, skipping hidden ones`() = runTest {
        touch("Download/Guides/a.txt")
        touch("Download/Guides/b.txt")
        touch("Download/Guides/.old/c.txt")

        val count = FolderSuggestions(DeviceFileSystem(), root).count("$root/Download/Guides")

        assertEquals(BookCount(2, BookKind.Guide), count)
    }
}
