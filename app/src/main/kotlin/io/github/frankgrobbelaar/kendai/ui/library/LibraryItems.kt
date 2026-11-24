package io.github.frankgrobbelaar.kendai.ui.library

import androidx.compose.runtime.Immutable
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.files.nameOf
import io.github.frankgrobbelaar.kendai.files.parentOf
import io.github.frankgrobbelaar.kendai.library.BookCount
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.library.NaturalOrder
import io.github.frankgrobbelaar.kendai.library.ShelvedBook
import java.io.File
import java.util.Locale
import kotlin.math.abs

/** A book as the library shows it. */
@Immutable
data class BookItem(
    val fingerprint: String,
    /** Its file; a book with two files appears once for each. */
    val path: String,
    val title: String,
    val author: String,
    /** The line under its title: "Jane Austen · EPUB · 520 KB". */
    val meta: String,
    /** Its cover picture, or null to draw one. */
    val cover: File?,
    val kind: BookKind,
    /** A plain-text guide's first lines, for its drawn cover. */
    val excerpt: String,
    val addedAt: Long,
)

/** A folder in the folder view, and the books in it and in the folders inside it. */
@Immutable data class FolderItem(val path: String, val name: String, val count: BookCount)

/** One step of the way down to the folder view's folder. */
@Immutable data class Crumb(val name: String, val folder: String)

/**
 * What the folder view shows: at the top ([folder] null) the library's folders, otherwise the
 * folders inside [folder] that hold books, then its own books.
 */
@Immutable
data class Shelf(
    val folder: String? = null,
    /** The way down from the library's folder, "Books" then "Classics", to [folder]. */
    val crumbs: List<Crumb> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val books: List<BookItem> = emptyList(),
    /** Every book here, in [folder] and the folders inside it. */
    val count: BookCount = BookCount(0),
)

/**
 * [folder] as the folder view shows it, from the library's [shelved] books in its folders [roots],
 * with its books in [sort] order. A folder that's no longer in the library shows the top.
 */
internal fun shelfOf(
    shelved: List<ShelvedBook>,
    roots: List<String>,
    folder: String?,
    sort: SortBy,
    coverOf: (String) -> File,
): Shelf {
    val root = folder?.let { inside ->
        roots.filter { inside == it || inside.startsWith("$it/") }.maxByOrNull { it.length }
    }
    if (folder == null || root == null) return topOf(shelved, roots)
    val prefix = "$folder/"
    val inside = shelved.filter { it.path.startsWith(prefix) }
    val (here, deeper) = inside.partition { parentOf(it.path) == folder }
    val folders =
        deeper
            .groupBy { it.path.removePrefix(prefix).substringBefore('/') }
            .map { (name, books) -> FolderItem("$folder/$name", name, countOf(books)) }
            .sortedWith(compareBy(NaturalOrder) { it.name })
    val books = here.map { itemOf(it, coverOf, where = null) }.sortedWith(orderOf(sort))
    val names = folder.removePrefix(root).split('/').filter { it.isNotEmpty() }
    val crumbs =
        names.runningFold(Crumb(nameOf(root), root)) { above, name ->
            Crumb(name, "${above.folder}/$name")
        }
    return Shelf(folder, crumbs, folders, books, countOf(inside))
}

/** The folder view's top: the library's folders, each with the books in it. */
private fun topOf(shelved: List<ShelvedBook>, roots: List<String>): Shelf {
    val byRoot = roots.associateWith { root -> shelved.filter { it.path.startsWith("$root/") } }
    return Shelf(
        folders = roots.map { FolderItem(it, nameOf(it), countOf(byRoot.getValue(it))) },
        count = countOf(byRoot.values.flatten().distinctBy { it.path }),
    )
}

/**
 * [shelved] as a book in a list. Its line ends with [where] it is, or its size when that's null; a
 * book with no author has its size in the author's place.
 */
internal fun itemOf(shelved: ShelvedBook, coverOf: (String) -> File, where: String?): BookItem {
    val book = shelved.book
    val size = sizeLabel(book.size)
    val meta =
        when {
            book.author.isBlank() -> listOfNotNull(book.format.label, size, where)
            else -> listOf(book.author, book.format.label, where ?: size)
        }
    return BookItem(
        fingerprint = book.fingerprint,
        path = shelved.path,
        title = book.title,
        author = book.author,
        meta = meta.joinToString(" · "),
        cover = if (book.hasCover) coverOf(book.fingerprint) else null,
        kind = book.kind,
        excerpt = book.excerpt,
        addedAt = book.addedAt,
    )
}

/** The name of the folder a book's file is in, which recently added shows. */
internal fun folderNameOf(path: String): String = nameOf(parentOf(path))

private fun countOf(books: List<ShelvedBook>) =
    BookCount(books.size, books.map { it.book.kind }.distinct().singleOrNull())

private fun orderOf(sort: SortBy): Comparator<BookItem> =
    when (sort) {
        SortBy.Title -> compareBy(NaturalOrder) { it.title }
        // Books without an author go last.
        SortBy.Author ->
            compareBy<BookItem> { it.author.isBlank() }
                .thenBy(NaturalOrder) { it.author }
                .thenBy(NaturalOrder) { it.title }
        SortBy.Added ->
            compareByDescending<BookItem> { it.addedAt }.thenBy(NaturalOrder) { it.title }
    }

/** A size in Android's units (1 KB is 1,000 bytes), one decimal below 10: "520 KB", "4.2 MB". */
internal fun sizeLabel(bytes: Long, locale: Locale = Locale.getDefault()): String {
    var value = bytes.toDouble()
    var unit = 0
    while (abs(value) >= STEP && unit < UNITS.lastIndex) {
        value /= STEP
        unit++
    }
    val number =
        if (unit == 0 || value >= ONE_DECIMAL_BELOW) "%.0f".format(locale, value)
        else "%.1f".format(locale, value)
    return "$number ${UNITS[unit]}"
}

private val UNITS = listOf("B", "KB", "MB", "GB", "TB")
private const val STEP = 1000.0
private const val ONE_DECIMAL_BELOW = 10
