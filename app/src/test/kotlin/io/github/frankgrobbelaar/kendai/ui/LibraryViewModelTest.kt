package io.github.frankgrobbelaar.kendai.ui

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import io.github.frankgrobbelaar.kendai.KendaiSettings
import io.github.frankgrobbelaar.kendai.Layout
import io.github.frankgrobbelaar.kendai.MainDispatcherRule
import io.github.frankgrobbelaar.kendai.MemoryStore
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import io.github.frankgrobbelaar.kendai.library.Covers
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.library.LibraryDatabase
import io.github.frankgrobbelaar.kendai.library.Samples
import io.github.frankgrobbelaar.kendai.ui.library.LibraryView
import io.github.frankgrobbelaar.kendai.ui.library.LibraryViewModel
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LibraryViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val folder = TemporaryFolder()

    private lateinit var database: LibraryDatabase
    private var granted = true

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
        Samples.epub(file("Books/Moby-Dick.epub"))
        Samples.epub(
            file("Books/Classics/Kokoro.epub"),
            title = "Kokoro",
            author = "Natsume Sōseki",
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun file(path: String) = File(folder.root, path).apply { parentFile?.mkdirs() }

    private suspend fun TestScope.libraryScreen(): LibraryViewModel {
        val library =
            Library(
                dao = database.library(),
                files = DeviceFileSystem(),
                covers = Covers(File(folder.root, ".covers")),
                scope = backgroundScope,
                storageRoot = root,
            )
        library.addFolders(listOf("$root/Books"))
        val screen =
            LibraryViewModel(
                library = library,
                settings = KendaiSettings(MemoryStore()),
                storageAccess = { granted },
                saved = SavedStateHandle(),
            )
        backgroundScope.launch { screen.state.collect {} }
        return screen
    }

    @Test
    fun `home shows what was added recently, found as the library opens`() = runTest {
        val screen = libraryScreen()

        val recent = screen.state.first { it.recent?.size == 2 }.recent.orEmpty()

        val moby = recent.single { it.title == "Moby-Dick" }
        assertEquals("Herman Melville · EPUB · Books", moby.meta)
        assertEquals(LibraryView.Home, screen.state.value.view)
    }

    @Test
    fun `the folder view goes down into folders and back up`() = runTest {
        val screen = libraryScreen()
        screen.state.first { it.recent?.size == 2 }

        screen.show(LibraryView.Folders)
        val top = screen.state.first { it.shelf != null }.shelf
        assertEquals(listOf("Books"), top?.folders?.map { it.name })

        screen.open("$root/Books")
        val books = screen.state.first { it.shelf?.folder == "$root/Books" }.shelf
        assertEquals(listOf("Classics"), books?.folders?.map { it.name })
        assertEquals(listOf("Moby-Dick"), books?.books?.map { it.title })

        screen.open("$root/Books/Classics")
        screen.state.first { it.shelf?.folder == "$root/Books/Classics" }
        screen.up()
        screen.state.first { it.shelf?.folder == "$root/Books" }
        screen.up()
        assertNull(
            screen.state.first { it.shelf?.folder == null && it.shelf != null }.shelf?.folder
        )
    }

    @Test
    fun `the folder view keeps its own sort and view`() = runTest {
        val screen = libraryScreen()

        screen.setSort(SortBy.Author)
        screen.setLayout(Layout.Covers)

        val state = screen.state.first { it.folders.layout == Layout.Covers }
        assertEquals(SortBy.Author, state.folders.sort)
    }

    @Test
    fun `says so when kendai can no longer read the device`() = runTest {
        val screen = libraryScreen()
        screen.state.first { it.recent != null }

        granted = false
        screen.recheckAccess()

        assertFalse(screen.state.first { !it.access }.access)
    }
}
