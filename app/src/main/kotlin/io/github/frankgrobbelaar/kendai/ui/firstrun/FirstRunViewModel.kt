package io.github.frankgrobbelaar.kendai.ui.firstrun

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.frankgrobbelaar.kendai.KendaiApplication
import io.github.frankgrobbelaar.kendai.files.FileSystemException
import io.github.frankgrobbelaar.kendai.files.StorageAccess
import io.github.frankgrobbelaar.kendai.files.nameOf
import io.github.frankgrobbelaar.kendai.library.BookCount
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.library.FolderSuggestions
import io.github.frankgrobbelaar.kendai.library.Library
import io.github.frankgrobbelaar.kendai.library.ShelvedBook
import io.github.frankgrobbelaar.kendai.ui.library.BookItem
import io.github.frankgrobbelaar.kendai.ui.library.folderNameOf
import io.github.frankgrobbelaar.kendai.ui.library.itemOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The first run's screens, in order. Access is skipped when kendai has it already. */
enum class FirstRunStep {
    Welcome,
    Access,
    Choose,
    Finding,
}

/** A folder to add to the library, ticked or not. */
@Immutable
data class FolderChoice(
    val path: String,
    val name: String,
    val count: BookCount,
    val ticked: Boolean,
)

/** A book just found, and the name of the folder it's in. */
@Immutable data class FoundBook(val book: BookItem, val folder: String)

/** How the look for books in the new folders is going. */
@Immutable
data class Progress(
    /** Books found so far, or all of them once [done]. */
    val found: Int = 0,
    /** How far along, from 0 to 1. */
    val fraction: Float = 0f,
    val done: Boolean = false,
    /** The library's folders, by name. */
    val folders: List<String> = emptyList(),
    /** The latest books found, the newest first. */
    val latest: List<FoundBook> = emptyList(),
)

@Immutable
data class FirstRunUiState(
    val step: FirstRunStep = FirstRunStep.Welcome,
    /** The folders to choose from, or null while kendai looks for them. */
    val choices: List<FolderChoice>? = null,
    /** Whether the folder just picked turned out not to be on the device. */
    val notOnDevice: Boolean = false,
    val progress: Progress = Progress(),
) {
    val ticked: Int
        get() = choices.orEmpty().count { it.ticked }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FirstRunViewModel(
    private val library: Library,
    private val suggestions: FolderSuggestions,
    private val storageAccess: StorageAccess,
    private val saved: SavedStateHandle,
) : ViewModel() {

    private val local =
        MutableStateFlow(
            FirstRunUiState(
                step = saved.get<String>(STEP)?.let(FirstRunStep::valueOf) ?: FirstRunStep.Welcome
            )
        )

    /** The books the new folders should hold, as their counts said, to measure progress by. */
    private var expected = 0

    private val looked = MutableStateFlow(false)

    /** Looking for folders with books in, at most once at a time. */
    private var search: Job? = null

    // Books found so far, kept through the moment between the look ending and its count arriving.
    private val foundSoFar = library.finding.scan(0) { last, finding -> finding?.found ?: last }

    // The library is watched only while its books are being found.
    val state: StateFlow<FirstRunUiState> =
        local
            .flatMapLatest { local ->
                if (local.step != FirstRunStep.Finding) {
                    flowOf(local)
                } else {
                    combine(foundSoFar, library.recentlyAdded, library.folders, looked) {
                        soFar,
                        recent,
                        folders,
                        looked ->
                        local.copy(
                            progress = progressOf(local.progress, soFar, recent, folders, looked)
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS), local.value)

    private fun progressOf(
        progress: Progress,
        soFar: Int,
        recent: List<ShelvedBook>,
        folders: List<String>,
        looked: Boolean,
    ): Progress {
        val found = if (looked) progress.found else soFar
        return progress.copy(
            found = found,
            fraction = if (looked) 1f else fractionOf(found),
            done = looked,
            folders = folders.map(::nameOf),
            latest =
                recent.take(LATEST).map {
                    FoundBook(itemOf(it, library::coverOf, where = null), folderNameOf(it.path))
                },
        )
    }

    init {
        when (local.value.step) {
            FirstRunStep.Choose -> findChoices()
            // Brought back after Android closed kendai mid-look: look again to know when it's done.
            FirstRunStep.Finding -> waitForLook()
            else -> Unit
        }
    }

    /** Past the welcome: to the access screen, or straight to the folders if kendai has access. */
    fun start() {
        goTo(if (storageAccess.granted()) FirstRunStep.Choose else FirstRunStep.Access)
    }

    /** Coming back from Android's settings: on to the folders if access was given. */
    fun recheckAccess() {
        if (local.value.step == FirstRunStep.Access && storageAccess.granted())
            goTo(FirstRunStep.Choose)
    }

    /** One screen back, as far as the welcome. */
    fun back() {
        when (local.value.step) {
            FirstRunStep.Access -> goTo(FirstRunStep.Welcome)
            FirstRunStep.Choose ->
                goTo(if (storageAccess.granted()) FirstRunStep.Welcome else FirstRunStep.Access)
            else -> Unit
        }
    }

    fun toggle(path: String) {
        local.update { state ->
            state.copy(
                choices =
                    state.choices?.map { if (it.path == path) it.copy(ticked = !it.ticked) else it }
            )
        }
    }

    /** A folder picked with Android's picker, ticked; null for one that isn't on the device. */
    fun pick(path: String?) {
        if (path == null) {
            local.update { it.copy(notOnDevice = true) }
            return
        }
        local.update { it.copy(notOnDevice = false) }
        if (local.value.choices.orEmpty().any { it.path == path }) {
            local.update { state ->
                state.copy(
                    choices =
                        state.choices?.map { if (it.path == path) it.copy(ticked = true) else it }
                )
            }
            return
        }
        viewModelScope.launch {
            val choice = FolderChoice(path, nameOf(path), suggestions.count(path), ticked = true)
            // Picked twice while it was being counted: it joins once.
            local.update { state ->
                if (state.choices.orEmpty().any { it.path == path }) state
                else state.copy(choices = state.choices.orEmpty() + choice)
            }
        }
    }

    /** Adds the ticked folders to the library and starts looking for their books. */
    fun add() {
        val ticked = local.value.choices.orEmpty().filter { it.ticked }
        if (ticked.isEmpty()) return
        expected = ticked.sumOf { it.count.books }
        viewModelScope.launch {
            library.addFolders(ticked.map { it.path })
            goTo(FirstRunStep.Finding)
        }
    }

    private fun goTo(step: FirstRunStep) {
        saved[STEP] = step.name
        local.update { it.copy(step = step) }
        when (step) {
            FirstRunStep.Choose -> if (local.value.choices == null) findChoices()
            FirstRunStep.Finding -> waitForLook()
            else -> Unit
        }
    }

    private fun findChoices() {
        if (search?.isActive == true) return
        search = viewModelScope.launch {
            val found =
                try {
                    suggestions.find()
                } catch (_: FileSystemException) {
                    // The top of the device can't be read: say nothing was found, and offer a pick.
                    emptyList()
                }
            // Folders of documents alone are often bank statements and manuals: not ticked.
            val suggested = found.map {
                FolderChoice(
                    it.path,
                    it.name,
                    it.count,
                    ticked = it.count.kind != BookKind.Document,
                )
            }
            // Folders picked while kendai looked keep their place and their tick.
            local.update { state ->
                val picked = state.choices.orEmpty()
                state.copy(
                    choices = suggested.filter { s -> picked.none { it.path == s.path } } + picked
                )
            }
        }
    }

    private fun waitForLook() {
        viewModelScope.launch {
            library.lookSoon().await()
            val folders = library.folders.first()
            val found =
                library.shelved.first().count { book ->
                    folders.any { book.path.startsWith("$it/") }
                }
            local.update { it.copy(progress = it.progress.copy(found = found)) }
            looked.value = true
        }
    }

    /**
     * How far through the look is: never quite all the way until it's done, as counts stop at 999.
     */
    private fun fractionOf(found: Int): Float =
        if (expected == 0) 0f else (found.toFloat() / expected).coerceAtMost(ALMOST_ALL)

    companion object {
        private const val STEP = "step"
        private const val LATEST = 5
        private const val ALMOST_ALL = 0.95f
        private const val STOP_AFTER_MILLIS = 5_000L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as KendaiApplication).container
                FirstRunViewModel(
                    library = container.library,
                    suggestions = container.suggestions,
                    storageAccess = container.storageAccess,
                    saved = createSavedStateHandle(),
                )
            }
        }
    }
}
