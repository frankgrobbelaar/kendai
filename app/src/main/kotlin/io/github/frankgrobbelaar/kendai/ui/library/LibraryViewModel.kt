package io.github.frankgrobbelaar.kendai.ui.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.frankgrobbelaar.kendai.KendaiApplication
import io.github.frankgrobbelaar.kendai.KendaiSettings
import io.github.frankgrobbelaar.kendai.Layout
import io.github.frankgrobbelaar.kendai.SortAndView
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.files.StorageAccess
import io.github.frankgrobbelaar.kendai.files.parentOf
import io.github.frankgrobbelaar.kendai.library.Finding
import io.github.frankgrobbelaar.kendai.library.Library
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The library's ways in, as its chips show them. Groups, Authors and Series come later. */
enum class LibraryView {
    Home,
    Folders,
}

@Immutable
data class LibraryUiState(
    val view: LibraryView = LibraryView.Home,
    /** The folder open in the folder view, or null at its top. */
    val folder: String? = null,
    /** Whether kendai can read the device's storage. */
    val access: Boolean = true,
    /** Whether the library holds any folders. */
    val hasFolders: Boolean = true,
    /** The books found most recently, or null until they're read. */
    val recent: List<BookItem>? = null,
    /** What the folder view shows, or null until it's worked out. */
    val shelf: Shelf? = null,
    /** The folder view's order and layout. */
    val folders: SortAndView = SortAndView(),
    /** The look for books under way, if there is one. */
    val finding: Finding? = null,
    /** Whether a look asked for by pulling the list down is under way. */
    val refreshing: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val library: Library,
    private val settings: KendaiSettings,
    private val storageAccess: StorageAccess,
    private val saved: SavedStateHandle,
    compute: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val view = saved.getStateFlow(VIEW, LibraryView.Home.name).map(LibraryView::valueOf)
    private val folder = saved.getStateFlow<String?>(FOLDER, null)
    private val access = MutableStateFlow(storageAccess.granted())
    private val refreshing = MutableStateFlow(false)
    private val sortAndView = settings.choices.map { it.folders }.distinctUntilChanged()
    private val roots = library.folders.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val recent =
        library.recentlyAdded
            .map { books ->
                books.map { itemOf(it, library::coverOf, where = folderNameOf(it.path)) }
            }
            .flowOn(compute)

    // Worked out only while the folder view is showing, as it goes through every book.
    private val shelf = view.flatMapLatest { view ->
        if (view != LibraryView.Folders) {
            flowOf(null)
        } else {
            combine(library.shelved, library.folders, folder, sortAndView) {
                    shelved,
                    roots,
                    folder,
                    choice ->
                    shelfOf(shelved, roots, folder, choice.sort, library::coverOf)
                }
                .flowOn(compute)
        }
    }

    private val around =
        combine(view, access, refreshing, library.finding, sortAndView) {
            view,
            access,
            refreshing,
            finding,
            choice ->
            LibraryUiState(
                view = view,
                access = access,
                refreshing = refreshing,
                finding = finding,
                folders = choice,
            )
        }

    val state: StateFlow<LibraryUiState> =
        combine(around, roots, recent, shelf, folder) { around, roots, recent, shelf, folder ->
                around.copy(
                    folder = folder,
                    hasFolders = roots.isNotEmpty(),
                    recent = recent,
                    shelf = shelf,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS),
                LibraryUiState(),
            )

    init {
        // Kendai looks for new, changed and gone books each time it opens.
        library.lookSoon()
    }

    fun show(view: LibraryView) {
        saved[VIEW] = view.name
    }

    /** Opens [folder] in the folder view, or the view's top when it's null. */
    fun open(folder: String?) {
        saved[FOLDER] = folder
    }

    /** Up a folder in the folder view; from a library folder, to the view's top. */
    fun up() {
        val current = folder.value ?: return
        val root =
            roots.value
                .filter { current == it || current.startsWith("$it/") }
                .maxByOrNull { it.length }
        open(if (root == null || current == root) null else parentOf(current))
    }

    /** Looks for books again, as a pull on the list asks. */
    fun refresh() {
        refreshing.value = true
        viewModelScope.launch {
            library.lookSoon().await()
            refreshing.value = false
        }
    }

    /** Whether kendai can still read the device, as Android's settings may have changed it. */
    fun recheckAccess() {
        val had = access.value
        access.value = storageAccess.granted()
        if (!had && access.value) library.lookSoon()
    }

    fun setSort(sort: SortBy) {
        viewModelScope.launch { settings.setFoldersSort(sort) }
    }

    fun setLayout(layout: Layout) {
        viewModelScope.launch { settings.setFoldersLayout(layout) }
    }

    companion object {
        private const val VIEW = "view"
        private const val FOLDER = "folder"
        private const val STOP_AFTER_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as KendaiApplication).container
                LibraryViewModel(
                    library = container.library,
                    settings = container.settings,
                    storageAccess = container.storageAccess,
                    saved = createSavedStateHandle(),
                )
            }
        }
    }
}
