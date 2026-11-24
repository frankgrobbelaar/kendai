package io.github.frankgrobbelaar.kendai.library

import androidx.room.Room
import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import io.github.frankgrobbelaar.kendai.files.Entry
import io.github.frankgrobbelaar.kendai.files.FileSystem
import io.github.frankgrobbelaar.kendai.files.FileSystemException
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LibraryTest {
    @get:Rule val folder = TemporaryFolder()

    private lateinit var database: LibraryDatabase
    private val files = Gate(DeviceFileSystem())

    private val root
        get() = folder.root.toPath().invariantSeparatorsPathString

    @Before
    fun openDatabase() {
        database =
            Room.inMemoryDatabaseBuilder(
                    RuntimeEnvironment.getApplication(),
                    LibraryDatabase::class.java,
                )
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun TestScope.library() =
        Library(
            dao = database.library(),
            files = files,
            covers = Covers(File(folder.root, ".covers")),
            scope = backgroundScope,
            storageRoot = root,
        )

    private fun file(path: String) = File(folder.root, path).apply { parentFile?.mkdirs() }

    private suspend fun Library.shelvedPaths() =
        shelved.first().map { it.path.removePrefix("$root/") }.toSet()

    @Test
    fun `finds the books in its folders and nothing else`() = runTest {
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.text(file("Books/Guides/Starfall.txt"), "STARFALL TACTICS")
        Samples.text(file("Books/Notes.docx"), "not a book kendai reads")
        Samples.epub(file("Books/.trash/Old.epub"))
        Samples.epub(file("Download/Elsewhere.epub"))
        val library = library()

        library.addFolders(listOf("$root/Books"))
        library.lookSoon().await()

        assertEquals(
            setOf("Books/Moby-Dick.epub", "Books/Guides/Starfall.txt"),
            library.shelvedPaths(),
        )
        val moby = library.shelved.first().single { it.path.endsWith("Moby-Dick.epub") }.book
        assertEquals("Herman Melville", moby.author)
        assertEquals(BookKind.Book, moby.kind)
    }

    @Test
    fun `a changed file is read again, and one that's gone is dropped`() = runTest {
        val moby = Samples.epub(file("Books/Moby-Dick.epub"))
        val guide = Samples.text(file("Books/Starfall.txt"), "version 1")
        val library = library()
        library.addFolders(listOf("$root/Books"))
        library.lookSoon().await()
        val before = library.shelved.first().single { it.path.endsWith(".txt") }.book.fingerprint

        guide.writeText("version 2, much longer")
        moby.delete()
        library.lookSoon().await()

        assertEquals(setOf("Books/Starfall.txt"), library.shelvedPaths())
        val after = library.shelved.first().single().book.fingerprint
        assertNotEquals(before, after)
    }

    @Test
    fun `a folder that can't be read keeps what was found in it`() = runTest {
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.text(file("Books/Guides/Starfall.txt"), "STARFALL")
        val library = library()
        library.addFolders(listOf("$root/Books"))
        library.lookSoon().await()

        files.blocked = "$root/Books/Guides"
        file("Books/Moby-Dick.epub").delete()
        library.lookSoon().await()

        assertEquals(setOf("Books/Guides/Starfall.txt"), library.shelvedPaths())
    }

    @Test
    fun `the Android folder at the top of the storage holds apps' data, not books`() = runTest {
        Samples.epub(file("Android/data/some.app/Cached.epub"))
        Samples.epub(file("Books/Moby-Dick.epub"))
        val library = library()

        library.addFolders(listOf(root))
        library.lookSoon().await()

        assertEquals(setOf("Books/Moby-Dick.epub"), library.shelvedPaths())
    }

    @Test
    fun `a folder inside one the library holds isn't added, and a new outer one takes over`() =
        runTest {
            val library = library()

            library.addFolders(listOf("$root/Books/Classics", "$root/Download"))
            library.addFolders(listOf("$root/Books"))
            library.addFolders(listOf("$root/Books/Classics/Russian"))

            assertEquals(listOf("$root/Books", "$root/Download"), library.folders.first())
        }

    @Test
    fun `a book found again through an outer folder belongs to it`() = runTest {
        Samples.epub(file("Books/Classics/Kokoro.epub"))
        val library = library()
        library.addFolders(listOf("$root/Books/Classics"))
        library.lookSoon().await()

        library.addFolders(listOf("$root/Books"))
        library.lookSoon().await()
        library.removeFolder("$root/Books")

        assertEquals(emptySet<String>(), library.shelvedPaths())
    }

    @Test
    fun `removing a folder takes its books out of the library but not off the device`() = runTest {
        val moby = Samples.epub(file("Books/Moby-Dick.epub"))
        val library = library()
        library.addFolders(listOf("$root/Books"))
        library.lookSoon().await()

        library.removeFolder("$root/Books")

        assertEquals(emptySet<String>(), library.shelvedPaths())
        assertEquals(emptyList<String>(), library.folders.first())
        assertTrue(moby.exists())
    }

    @Test
    fun `asking during a look looks again afterwards, for a folder added meanwhile`() = runTest {
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.epub(file("Comics/Nemo.epub"), title = "Little Nemo")
        val library = library()
        library.addFolders(listOf("$root/Books"))
        val first = files.holding { library.lookSoon() }

        library.addFolders(listOf("$root/Comics"))
        val second = library.lookSoon()
        files.letGo()
        first.await()
        second.await()

        assertEquals(setOf("Books/Moby-Dick.epub", "Comics/Nemo.epub"), library.shelvedPaths())
    }

    @Test
    fun `a folder removed during a look doesn't come back with what the look found`() = runTest {
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.epub(file("Download/Router manual.epub"), title = "Router manual")
        val library = library()
        library.addFolders(listOf("$root/Books", "$root/Download"))
        val look = files.holding { library.lookSoon() }

        library.removeFolder("$root/Download")
        val after = library.lookSoon()
        files.letGo()
        look.await()

        assertEquals(
            setOf("Books/Moby-Dick.epub"),
            library.recentlyAdded
                .first()
                .map {
                    it.path.removePrefix("$root/")
                }
                .toSet(),
        )
        after.await()
        assertEquals(setOf("Books/Moby-Dick.epub"), library.shelvedPaths())
    }

    /**
     * The device, with a folder that can't be read, and a look that can be held still part-way, at
     * its first listing, until it's let go.
     */
    private class Gate(private val inner: FileSystem) : FileSystem {
        @Volatile var blocked: String? = null
        @Volatile private var hold: CompletableDeferred<Unit>? = null
        @Volatile private var entered: CompletableDeferred<Unit>? = null

        override suspend fun list(path: String): List<Entry> {
            hold?.let {
                entered?.complete(Unit)
                it.await()
            }
            if (path == blocked) throw FileSystemException.AccessDenied(path)
            return inner.list(path)
        }

        /** Starts a look with [start] and returns once it's held at its first listing. */
        suspend fun <T> holding(start: () -> T): T {
            hold = CompletableDeferred()
            val inside = CompletableDeferred<Unit>().also { entered = it }
            return start().also { inside.await() }
        }

        fun letGo() {
            val held = hold
            hold = null
            held?.complete(Unit)
        }
    }
}
