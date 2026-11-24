package io.github.frankgrobbelaar.kendai.ui

import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import io.github.frankgrobbelaar.kendai.MainDispatcherRule
import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import io.github.frankgrobbelaar.kendai.library.BookCount
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.library.Covers
import io.github.frankgrobbelaar.kendai.library.FolderSuggestions
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.library.LibraryDatabase
import io.github.frankgrobbelaar.kendai.library.Samples
import io.github.frankgrobbelaar.kendai.ui.firstrun.FirstRunStep
import io.github.frankgrobbelaar.kendai.ui.firstrun.FirstRunViewModel
import io.github.frankgrobbelaar.kendai.ui.firstrun.FolderChoice
import java.io.File
import kotlin.io.path.invariantSeparatorsPathString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FirstRunViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    @get:Rule val folder = TemporaryFolder()

    /** Somewhere else to pick a folder from, such as an SD card. */
    @get:Rule val card = TemporaryFolder()

    private lateinit var database: LibraryDatabase
    private var granted = false
    private lateinit var library: Library

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
        Samples.epub(file("Books/Kokoro.epub"), title = "Kokoro", author = "Natsume Sōseki")
        Samples.text(file("Documents/Taxes.pdf"), "not really a PDF")
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun file(path: String) = File(folder.root, path).apply { parentFile?.mkdirs() }

    private fun TestScope.firstRun(
        saved: SavedStateHandle = SavedStateHandle()
    ): FirstRunViewModel {
        library =
            Library(
                dao = database.library(),
                files = DeviceFileSystem(),
                covers = Covers(File(folder.root, ".covers")),
                scope = backgroundScope,
                storageRoot = root,
            )
        val firstRun =
            FirstRunViewModel(
                library = library,
                suggestions = FolderSuggestions(DeviceFileSystem(), root),
                storageAccess = { granted },
                saved = saved,
            )
        // Collected from the start, so its value keeps up with each step.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            firstRun.state.collect {}
        }
        return firstRun
    }

    @Test
    fun `asks for access, then goes on to the folders once it's given`() = runTest {
        val firstRun = firstRun()
        assertEquals(FirstRunStep.Welcome, firstRun.state.value.step)

        firstRun.start()
        firstRun.recheckAccess()
        assertEquals(FirstRunStep.Access, firstRun.state.value.step)

        granted = true
        firstRun.recheckAccess()

        assertEquals(FirstRunStep.Choose, firstRun.state.value.step)
    }

    @Test
    fun `suggests the folders with books, leaving folders of documents unticked`() = runTest {
        granted = true
        val firstRun = firstRun()

        firstRun.start()

        val choices = firstRun.state.first { it.choices != null }.choices
        assertEquals(
            listOf(
                FolderChoice("$root/Books", "Books", BookCount(2, BookKind.Book), ticked = true),
                FolderChoice(
                    "$root/Documents",
                    "Documents",
                    BookCount(1, BookKind.Document),
                    ticked = false,
                ),
            ),
            choices,
        )
    }

    @Test
    fun `adds the ticked folders, then finds their books`() = runTest {
        granted = true
        val firstRun = firstRun()
        firstRun.start()
        firstRun.state.first { it.choices != null }

        firstRun.add()

        val progress = firstRun.state.first { it.progress.done }.progress
        assertEquals(FirstRunStep.Finding, firstRun.state.value.step)
        assertEquals(2, progress.found)
        assertEquals(1f, progress.fraction)
        assertEquals(listOf("Books"), progress.folders)
        assertEquals(setOf("Moby-Dick", "Kokoro"), progress.latest.map { it.book.title }.toSet())
        assertEquals(listOf("$root/Books"), library.folders.first())
    }

    @Test
    fun `a folder picked elsewhere joins the choices, ticked`() = runTest {
        granted = true
        val comics = File(card.root, "Comics").apply { mkdirs() }
        Samples.text(File(comics, "Nemo.cbz"), "not really a comic")
        val picked = comics.toPath().invariantSeparatorsPathString
        val firstRun = firstRun()
        firstRun.start()
        firstRun.state.first { it.choices != null }

        firstRun.pick(null)
        assertTrue(firstRun.state.value.notOnDevice)
        firstRun.pick(picked)

        val state = firstRun.state.first { it.ticked == 2 }
        assertEquals(
            FolderChoice(picked, "Comics", BookCount(1, BookKind.Comic), ticked = true),
            state.choices?.last(),
        )
        assertEquals(false, state.notOnDevice)
    }

    @Test
    fun `going back and on again while folders are looked for lists each one once`() = runTest {
        granted = true
        val firstRun = firstRun()

        firstRun.start()
        firstRun.back()
        firstRun.start()

        val choices = firstRun.state.first { it.choices != null }.choices.orEmpty()
        assertEquals(listOf("$root/Books", "$root/Documents"), choices.map { it.path })
    }

    @Test
    fun `back goes to the welcome, past access that's been given`() = runTest {
        granted = true
        val saved = SavedStateHandle()
        val firstRun = firstRun(saved)
        firstRun.start()

        firstRun.back()

        assertEquals(FirstRunStep.Welcome, firstRun.state.value.step)
        assertEquals("Welcome", saved.get<String>("step"))
    }
}
