package io.github.frankgrobbelaar.kendai.ui.library

import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.library.BookCount
import io.github.frankgrobbelaar.kendai.library.BookEntity
import io.github.frankgrobbelaar.kendai.library.BookFormat
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.library.ShelvedBook
import java.io.File
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryItemsTest {
    private val books = "/storage/emulated/0/Books"
    private val download = "/storage/emulated/0/Download"

    private fun shelved(
        path: String,
        title: String,
        author: String = "",
        format: BookFormat = BookFormat.Epub,
        size: Long = 520_000,
        addedAt: Long = 0,
    ) =
        ShelvedBook(
            BookEntity(
                fingerprint = path,
                title = title,
                author = author,
                series = "",
                seriesIndex = null,
                format = format,
                kind = format.kind,
                size = size,
                addedAt = addedAt,
                hasCover = false,
                excerpt = "",
            ),
            path,
        )

    private val library =
        listOf(
            shelved("$books/Book 10.epub", "Book 10", "Zola"),
            shelved("$books/Book 2.epub", "Book 2", addedAt = 5),
            shelved("$books/Book 1.epub", "Book 1", "Austen", addedAt = 9),
            shelved("$books/Classics/Kokoro.epub", "Kokoro", "Natsume Sōseki"),
            shelved("$books/Classics/Russian/War and Peace.epub", "War and Peace", "Tolstoy"),
            shelved("$books/Comics/Nemo.cbz", "Little Nemo", format = BookFormat.Cbz),
            shelved("$download/Router manual.pdf", "Router manual", format = BookFormat.Pdf),
        )

    private fun shelf(folder: String?, sort: SortBy = SortBy.Title) =
        shelfOf(library, listOf(books, download), folder, sort) { File(it) }

    @Test
    fun `the top shows the library's folders and everything in them`() {
        val top = shelf(folder = null)

        assertEquals(
            listOf(
                FolderItem(books, "Books", BookCount(6)),
                FolderItem(download, "Download", BookCount(1, BookKind.Document)),
            ),
            top.folders,
        )
        assertEquals(BookCount(7), top.count)
        assertEquals(emptyList<Crumb>(), top.crumbs)
    }

    @Test
    fun `a folder shows the folders inside it with books, then its own books in order`() {
        val inside = shelf(books)

        assertEquals(
            listOf(
                FolderItem("$books/Classics", "Classics", BookCount(2, BookKind.Book)),
                FolderItem("$books/Comics", "Comics", BookCount(1, BookKind.Comic)),
            ),
            inside.folders,
        )
        assertEquals(listOf("Book 1", "Book 2", "Book 10"), inside.books.map { it.title })
        assertEquals(listOf(Crumb("Books", books)), inside.crumbs)
        assertEquals(BookCount(6), inside.count)
    }

    @Test
    fun `the way down to a folder starts at its library folder`() {
        val deep = shelf("$books/Classics/Russian")

        assertEquals(
            listOf(
                Crumb("Books", books),
                Crumb("Classics", "$books/Classics"),
                Crumb("Russian", "$books/Classics/Russian"),
            ),
            deep.crumbs,
        )
        assertEquals(listOf("War and Peace"), deep.books.map { it.title })
    }

    @Test
    fun `a folder that's left the library shows the top`() {
        val gone = shelf("/storage/emulated/0/Old")

        assertNull(gone.folder)
        assertEquals(2, gone.folders.size)
    }

    @Test
    fun `books sort by author with the unknown last, or by when they were added`() {
        assertEquals(
            listOf("Book 1", "Book 10", "Book 2"),
            shelf(books, SortBy.Author).books.map { it.title },
        )
        assertEquals(
            listOf("Book 1", "Book 2", "Book 10"),
            shelf(books, SortBy.Added).books.map { it.title },
        )
    }

    @Test
    fun `a book's line says who wrote it, its format, and where it is or its size`() {
        val moby = shelved("$books/Moby-Dick.epub", "Moby-Dick", "Herman Melville")
        val guide =
            shelved("$download/Starfall.txt", "Starfall", format = BookFormat.Txt, size = 1_200_000)
        val before = Locale.getDefault()
        Locale.setDefault(Locale.UK)
        try {
            assertEquals("Herman Melville · EPUB · Books", itemOf(moby, ::File, "Books").meta)
            assertEquals("Herman Melville · EPUB · 520 KB", itemOf(moby, ::File, null).meta)
            assertEquals("TXT · 1.2 MB · Download", itemOf(guide, ::File, "Download").meta)
            assertEquals("TXT · 1.2 MB", itemOf(guide, ::File, null).meta)
        } finally {
            Locale.setDefault(before)
        }
        assertEquals("Download", folderNameOf(guide.path))
    }

    @Test
    fun `sizes are in Android's units, with a decimal below ten`() {
        val uk = Locale.UK
        assertEquals("999 B", sizeLabel(999, uk))
        assertEquals("520 KB", sizeLabel(520_000, uk))
        assertEquals("4.2 MB", sizeLabel(4_200_000, uk))
        assertEquals("12 MB", sizeLabel(12_000_000, uk))
        assertEquals("1.5 GB", sizeLabel(1_500_000_000, uk))
    }
}
