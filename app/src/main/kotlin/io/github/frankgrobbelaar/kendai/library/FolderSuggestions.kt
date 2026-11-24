package io.github.frankgrobbelaar.kendai.library

import io.github.frankgrobbelaar.kendai.files.FileSystem
import io.github.frankgrobbelaar.kendai.files.FileSystemException

/** How many books there are somewhere, and their kind when they're all of one kind. */
data class BookCount(val books: Int, val kind: BookKind? = null)

/** A folder at the top of the device's storage, and the books in it. */
data class Suggestion(val path: String, val name: String, val count: BookCount)

/**
 * The folders on the device that hold books, for the first run to suggest. Counting stops at [CAP]
 * books and [DEPTH] folders down, so a huge folder doesn't hold the first run up.
 */
class FolderSuggestions(private val files: FileSystem, private val root: String) {
    /** The folders at the top of the device's storage with books in, the fullest first. */
    suspend fun find(): List<Suggestion> =
        files
            .list(root)
            .filter { it.isFolder && !it.name.startsWith(".") && it.name != "Android" }
            .map { Suggestion(it.path, it.name, count(it.path)) }
            .filter { it.count.books > 0 }
            .sortedWith(compareByDescending<Suggestion> { it.count.books }.thenBy { it.name })

    /** The books in [path] and the folders inside it. */
    suspend fun count(path: String): BookCount {
        val tally = Tally()
        tally.add(path, depth = 0)
        return BookCount(tally.books, tally.kinds.singleOrNull())
    }

    private inner class Tally {
        var books = 0
        val kinds = mutableSetOf<BookKind>()

        suspend fun add(path: String, depth: Int) {
            val entries =
                try {
                    files.list(path)
                } catch (_: FileSystemException) {
                    // A folder that can't be read has no books to suggest.
                    return
                }
            for (entry in entries) {
                if (books >= CAP) return
                when {
                    entry.name.startsWith(".") -> Unit
                    entry.isFolder -> if (depth < DEPTH) add(entry.path, depth + 1)
                    else ->
                        BookFormat.of(entry.name)?.let {
                            books++
                            kinds += it.kind
                        }
                }
            }
        }
    }

    private companion object {
        const val CAP = 999
        const val DEPTH = 6
    }
}
