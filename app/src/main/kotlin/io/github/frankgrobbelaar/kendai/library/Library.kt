package io.github.frankgrobbelaar.kendai.library

import io.github.frankgrobbelaar.kendai.files.Entry
import io.github.frankgrobbelaar.kendai.files.FileSystem
import io.github.frankgrobbelaar.kendai.files.FileSystemException
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How far a look for books has got: files found so far, and how many of them are new books. */
data class Finding(val found: Int, val new: Int)

/**
 * The library: the folders it holds, the books found in them, and looking for them. Each book is
 * read once, by its fingerprint; a file that hasn't changed isn't read again.
 */
class Library(
    private val dao: LibraryDao,
    private val files: FileSystem,
    private val covers: Covers,
    /** Where looks run: the app's, since a look outlives any one screen. */
    scope: CoroutineScope,
    /** The top of the device's storage, whose Android folder holds apps' data, not books. */
    private val storageRoot: String,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    val folders: Flow<List<String>> = dao.folders()

    val recentlyAdded: Flow<List<ShelvedBook>> = dao.recentlyAdded(RECENT)

    val shelved: Flow<List<ShelvedBook>> = dao.shelved()

    private val looking = MutableStateFlow<Finding?>(null)

    /** The look for books under way, or null when there's none. */
    val finding: StateFlow<Finding?> = looking.asStateFlow()

    // Asking while a look is under way queues one more, however often it's asked: it may have
    // started before a folder was added.
    private val asked = Channel<Unit>(Channel.CONFLATED)
    private val waiting = mutableListOf<CompletableDeferred<Unit>>()

    init {
        scope.launch {
            while (true) {
                asked.receive()
                val answered = synchronized(waiting) { waiting.toList().also { waiting.clear() } }
                look()
                answered.forEach { it.complete(Unit) }
            }
        }
    }

    fun coverOf(fingerprint: String): File = covers.fileOf(fingerprint)

    /**
     * Adds [paths] to the library's folders. One inside a folder the library holds already is left
     * out, since its books are found anyway; a folder the library holds inside a new one gives way.
     */
    suspend fun addFolders(paths: List<String>) =
        withContext(io) {
            val held = dao.folderList()
            val all = (held + paths).distinct()
            val kept = all.filterNot { path -> all.any { path.startsWith("$it/") } }
            dao.changeFolders(
                gone = held - kept,
                added = (kept - held).map { FolderEntity(it, clock()) },
            )
        }

    /**
     * Takes [path] out of the library. Its files stay where they are. A look under way may still
     * find books there, so another follows to clear them out.
     */
    suspend fun removeFolder(path: String) {
        withContext(io) { dao.removeFolder(path) }
        lookSoon()
    }

    /**
     * Looks through every folder for books without waiting: new files are read, changed ones read
     * again, and ones that have gone are dropped. Completes when a look that started after this
     * call has finished.
     */
    fun lookSoon(): Deferred<Unit> {
        val answer = CompletableDeferred<Unit>()
        synchronized(waiting) { waiting += answer }
        asked.trySend(Unit)
        return answer
    }

    private suspend fun look() =
        withContext(io) {
            val known = dao.copies().associateBy { it.path }
            val look = Look(known, dao.fingerprints().toMutableSet())
            looking.value = Finding(0, 0)
            try {
                val folders = dao.folderList()
                for (folder in folders) walk(folder, folder, look)
                // A folder that couldn't be read keeps what was found in it before.
                val gone =
                    known.values.filter { copy ->
                        copy.path !in look.seen &&
                            (copy.folder !in folders ||
                                look.unread.none { copy.path.startsWith(it) })
                    }
                // In batches, within the most values SQLite takes in one statement.
                gone.map { it.path }.chunked(BATCH).forEach { dao.removeCopies(it) }
            } finally {
                looking.value = null
            }
        }

    private suspend fun walk(path: String, folder: String, look: Look) {
        val entries =
            try {
                files.list(path)
            } catch (_: FileSystemException) {
                // Out of reach for now, such as an SD card taken out: keep what it held.
                look.unread += "$path/"
                return
            }
        for (entry in entries) {
            when {
                entry.name.startsWith(".") -> Unit
                entry.isFolder ->
                    if (entry.path != "$storageRoot/Android") walk(entry.path, folder, look)
                else -> BookFormat.of(entry.name)?.let { take(entry, it, folder, look) }
            }
        }
    }

    private suspend fun take(entry: Entry, format: BookFormat, folder: String, look: Look) {
        look.seen += entry.path
        val copy = look.known[entry.path]
        when {
            copy == null || copy.size != entry.size || copy.modified != entry.modified ->
                read(entry, format, folder, look)
            // Found through a folder that took the place of the one it was found in before.
            copy.folder != folder -> dao.putCopy(copy.copy(folder = folder))
        }
        looking.value = Finding(look.seen.size, look.new)
    }

    /** Reads a new or changed file: its fingerprint, and the book's details if it's a new book. */
    private suspend fun read(entry: Entry, format: BookFormat, folder: String, look: Look) {
        val file = File(entry.path)
        try {
            val fingerprint = Fingerprint.of(file)
            if (fingerprint !in look.fingerprints) {
                val found = BookDetails.read(file, format)
                val hasCover = found.cover?.let { covers.keep(fingerprint, it) } == true
                dao.addBook(bookOf(fingerprint, found.details, format, entry.size, hasCover))
                look.fingerprints += fingerprint
                look.new++
            }
            dao.putCopy(CopyEntity(entry.path, fingerprint, folder, entry.size, entry.modified))
        } catch (_: IOException) {
            // Gone or locked since it was listed. It's read again next time, and until then it
            // keeps what was known of it.
        }
    }

    private fun bookOf(
        fingerprint: String,
        details: Details,
        format: BookFormat,
        size: Long,
        hasCover: Boolean,
    ) =
        BookEntity(
            fingerprint = fingerprint,
            title = details.title,
            author = details.author,
            series = details.series,
            seriesIndex = details.seriesIndex,
            format = format,
            kind = format.kind,
            size = size,
            addedAt = clock(),
            hasCover = hasCover,
            excerpt = details.excerpt,
        )

    /** One look's working notes. */
    private class Look(val known: Map<String, CopyEntity>, val fingerprints: MutableSet<String>) {
        val seen = mutableSetOf<String>()
        val unread = mutableSetOf<String>()
        var new = 0
    }

    private companion object {
        const val RECENT = 20
        const val BATCH = 500
    }
}
