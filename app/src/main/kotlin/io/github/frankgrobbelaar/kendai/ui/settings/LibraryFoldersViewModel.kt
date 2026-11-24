package io.github.frankgrobbelaar.kendai.ui.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.frankgrobbelaar.kendai.KendaiApplication
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.ui.library.FolderItem
import io.github.frankgrobbelaar.kendai.ui.library.shelfOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class LibraryFoldersUiState(
    /** The library's folders, or null until they're read. */
    val folders: List<FolderItem>? = null,
    /** Whether the folder just picked turned out not to be on the device. */
    val notOnDevice: Boolean = false,
)

class LibraryFoldersViewModel(
    private val library: Library,
    compute: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val notOnDevice = MutableStateFlow(false)

    private val folders =
        combine(library.shelved, library.folders) { shelved, roots ->
                shelfOf(shelved, roots, folder = null, SortBy.Title, library::coverOf).folders
            }
            .flowOn(compute)

    val state: StateFlow<LibraryFoldersUiState> =
        combine(folders, notOnDevice, ::LibraryFoldersUiState)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS),
                LibraryFoldersUiState(),
            )

    /** Adds a folder picked with Android's picker; null for one that isn't on the device. */
    fun add(path: String?) {
        notOnDevice.value = path == null
        if (path == null) return
        viewModelScope.launch {
            library.addFolders(listOf(path))
            library.lookSoon()
        }
    }

    /** Takes [path] out of the library; its files stay where they are. */
    fun remove(path: String) {
        notOnDevice.update { false }
        viewModelScope.launch { library.removeFolder(path) }
    }

    companion object {
        private const val STOP_AFTER_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                LibraryFoldersViewModel(
                    (this[APPLICATION_KEY] as KendaiApplication).container.library
                )
            }
        }
    }
}
