package io.github.frankgrobbelaar.kendai

import androidx.room.Room
import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import io.github.frankgrobbelaar.kendai.library.Covers
import io.github.frankgrobbelaar.kendai.library.FolderSuggestions
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.library.LibraryDatabase
import java.io.File
import java.nio.file.Files
import kotlin.io.path.invariantSeparatorsPathString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Kendai for Robolectric: settings and the library in memory, a device that's an empty temporary
 * folder at [root], All files access as [granted] says, and a screen that isn't e-ink.
 */
class TestKendaiApplication : KendaiApplication() {
    /** The top of the device's storage: a new temporary folder for each test. */
    val root: String by lazy {
        Files.createTempDirectory("kendai-device").invariantSeparatorsPathString
    }

    override val container: AppContainer by lazy {
        val database =
            Room.inMemoryDatabaseBuilder(this, LibraryDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        AppContainer(
            settings = KendaiSettings(MemoryStore()),
            einkScreen = false,
            storageAccess = { granted },
            library =
                Library(
                    dao = database.library(),
                    files = DeviceFileSystem(),
                    covers = Covers(File(root, ".covers")),
                    scope = CoroutineScope(SupervisorJob()),
                    storageRoot = root,
                ),
            suggestions = FolderSuggestions(DeviceFileSystem(), root),
        )
    }

    companion object {
        @Volatile var granted = true
    }
}
