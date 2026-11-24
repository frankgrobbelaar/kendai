package io.github.frankgrobbelaar.kendai

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import io.github.frankgrobbelaar.kendai.files.DeviceFileSystem
import io.github.frankgrobbelaar.kendai.files.DeviceStorageAccess
import io.github.frankgrobbelaar.kendai.files.StorageAccess
import io.github.frankgrobbelaar.kendai.files.deviceStorageRoot
import io.github.frankgrobbelaar.kendai.library.Covers
import io.github.frankgrobbelaar.kendai.library.FolderSuggestions
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.library.LibraryDatabase
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Kendai's long-lived parts, made once for the process and handed to what needs them. */
class AppContainer(
    val settings: KendaiSettings,
    /** Whether the screen is e-ink, which turns e-ink mode on unless Settings says otherwise. */
    val einkScreen: Boolean,
    val storageAccess: StorageAccess,
    val library: Library,
    val suggestions: FolderSuggestions,
)

/** Kendai's parts on a real device. */
fun deviceContainer(context: Context): AppContainer {
    val files = DeviceFileSystem()
    val root = deviceStorageRoot()
    val database = Room.databaseBuilder(context, LibraryDatabase::class.java, "library").build()
    return AppContainer(
        settings =
            KendaiSettings(
                PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }
            ),
        einkScreen = EinkScreen.detect(),
        storageAccess = DeviceStorageAccess,
        library =
            Library(
                dao = database.library(),
                files = files,
                covers = Covers(File(context.filesDir, "covers")),
                // Looking for books outlives any one screen: it's the app's.
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
                storageRoot = root,
            ),
        suggestions = FolderSuggestions(files, root),
    )
}

/** The container, from anywhere in the UI. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as KendaiApplication).container
