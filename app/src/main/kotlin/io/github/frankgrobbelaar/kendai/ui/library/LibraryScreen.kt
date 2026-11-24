package io.github.frankgrobbelaar.kendai.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.frankgrobbelaar.kendai.Layout
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.files.openAllFilesAccess
import io.github.frankgrobbelaar.kendai.ui.kit.BookRow
import io.github.frankgrobbelaar.kendai.ui.kit.BookTile
import io.github.frankgrobbelaar.kendai.ui.kit.Chips
import io.github.frankgrobbelaar.kendai.ui.kit.EmptyState
import io.github.frankgrobbelaar.kendai.ui.kit.FolderRow
import io.github.frankgrobbelaar.kendai.ui.kit.InkButton
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar
import io.github.frankgrobbelaar.kendai.ui.kit.bookCountLabel
import io.github.frankgrobbelaar.kendai.ui.theme.Washi

@Composable
fun LibraryRoute(
    onChooseFolders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Android's settings are another app: look again whenever kendai comes back.
    LifecycleResumeEffect(viewModel) {
        viewModel.recheckAccess()
        onPauseOrDispose {}
    }
    // Back goes up a folder, then to the folder view's top, then out.
    BackHandler(enabled = state.view == LibraryView.Folders && state.folder != null) {
        viewModel.up()
    }
    LibraryScreen(
        state = state,
        onView = viewModel::show,
        onOpenFolder = viewModel::open,
        onRefresh = viewModel::refresh,
        onAllowAccess = { openAllFilesAccess(context) },
        onChooseFolders = onChooseFolders,
        onSort = viewModel::setSort,
        onLayout = viewModel::setLayout,
        modifier = modifier,
    )
}

/**
 * The library, as on the design's library board: Home with what was added recently, and Folders
 * with the real folders that hold books. Pulling the list down looks for books again.
 */
@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onView: (LibraryView) -> Unit,
    onOpenFolder: (String?) -> Unit,
    onRefresh: () -> Unit,
    onAllowAccess: () -> Unit,
    onChooseFolders: () -> Unit,
    onSort: (SortBy) -> Unit,
    onLayout: (Layout) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sorting by rememberSaveable { mutableStateOf(false) }
    val views = LibraryView.entries
    val labels =
        listOf(stringResource(R.string.library_home), stringResource(R.string.library_folders))
    Column(modifier.fillMaxSize()) {
        KendaiTopBar(title = stringResource(R.string.nav_library)) {
            if (state.view == LibraryView.Folders) {
                IconButton(onClick = { sorting = true }) {
                    Icon(
                        KendaiIcons.More,
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
            }
        }
        Chips(
            count = views.size,
            label = { labels[it] },
            selected = views.indexOf(state.view),
            onSelect = { onView(views[it]) },
        )
        when {
            !state.access ->
                EmptyState(
                    icon = KendaiIcons.Lock,
                    title = stringResource(R.string.access_lost_title),
                    body = stringResource(R.string.access_lost_body),
                ) {
                    InkButton(text = stringResource(R.string.access_allow), onClick = onAllowAccess)
                }
            !state.hasFolders ->
                EmptyState(
                    icon = KendaiIcons.Library,
                    title = stringResource(R.string.library_empty_title),
                    body = stringResource(R.string.library_empty_body),
                ) {
                    InkButton(
                        text = stringResource(R.string.library_empty_action),
                        onClick = onChooseFolders,
                    )
                }
            else -> Views(state, onRefresh, onOpenFolder)
        }
    }
    if (sorting) {
        SortSheet(
            choice = state.folders,
            onSort = onSort,
            onLayout = onLayout,
            onDismiss = { sorting = false },
        )
    }
}

/** The view chosen, which a pull looks for books again in. Each keeps its own scroll position. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Views(
    state: LibraryUiState,
    onRefresh: () -> Unit,
    onOpenFolder: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = rememberSaveableStateHolder()
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when (state.view) {
            LibraryView.Home -> saved.SaveableStateProvider("home") { Home(state) }
            // Each folder too, so going back up returns to where you were.
            LibraryView.Folders ->
                saved.SaveableStateProvider("folders/${state.shelf?.folder}") {
                    Folders(state, onOpenFolder)
                }
        }
    }
}

@Composable
private fun Home(state: LibraryUiState, modifier: Modifier = Modifier) {
    val recent = state.recent ?: return
    val finding = state.finding
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 16.dp),
    ) {
        when {
            recent.isNotEmpty() -> {
                item(key = "recent") { Section(stringResource(R.string.library_recent)) }
                items(recent, key = { it.path }) { BookRow(it) }
            }
            finding != null ->
                item(key = "finding") {
                    EmptyState(
                        icon = KendaiIcons.Library,
                        title = stringResource(R.string.finding_title),
                        body =
                            pluralStringResource(
                                R.plurals.finding_so_far,
                                finding.found,
                                finding.found,
                            ),
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
            else ->
                item(key = "none") {
                    EmptyState(
                        icon = KendaiIcons.Library,
                        title = stringResource(R.string.library_none_title),
                        body = stringResource(R.string.library_none_body),
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
        }
    }
}

/** A section's title, in Literata. */
@Composable
private fun Section(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp).semantics { heading() },
    )
}

@Composable
private fun Folders(
    state: LibraryUiState,
    onOpenFolder: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shelf = state.shelf ?: return
    Column(modifier.fillMaxSize()) {
        Places()
        Crumbs(shelf, onOpenFolder)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(
                shelf.folders,
                key = { "folder/${it.path}" },
                span = { GridItemSpan(maxLineSpan) },
            ) {
                FolderRow(
                    name = it.name,
                    detail = bookCountLabel(it.count),
                    modifier = Modifier.clickable { onOpenFolder(it.path) },
                ) {
                    Icon(
                        KendaiIcons.Chevron,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            books(shelf, covers = state.folders.layout == Layout.Covers)
        }
    }
}

/** The folder's own books: rows, or covers with room above and below. */
private fun LazyGridScope.books(shelf: Shelf, covers: Boolean) {
    when {
        shelf.folder != null && shelf.books.isEmpty() && shelf.folders.isEmpty() ->
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    icon = KendaiIcons.Folder,
                    title = stringResource(R.string.no_books_here),
                    body = stringResource(R.string.library_folder_empty),
                    modifier = Modifier.padding(top = 48.dp),
                )
            }
        covers -> {
            item(key = "gap", span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(14.dp))
            }
            items(shelf.books, key = { "book/${it.path}" }) {
                BookTile(it, Modifier.padding(bottom = 18.dp))
            }
        }
        else ->
            items(shelf.books, key = { "book/${it.path}" }, span = { GridItemSpan(maxLineSpan) }) {
                BookRow(it)
            }
    }
}

/**
 * Where books are: the device for now, servers later. The current one has a seal rule under it, and
 * they sit on a hairline.
 */
@Composable
private fun Places(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val rule = colors.outlineVariant
    val seal = colors.primary
    val width = Washi.ruleWidth
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .drawBehind {
                    val y = size.height - width.toPx() / 2
                    drawLine(rule, Offset(0f, y), Offset(size.width, y), width.toPx())
                }
                .padding(start = 20.dp, end = 20.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.library_device),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier =
                Modifier.drawBehind {
                        val y = size.height - 1.dp.toPx()
                        drawLine(seal, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
                    }
                    .padding(top = 8.dp, bottom = 10.dp),
        )
    }
}

/** "Device › Books › Classics", each step but the last a way back up, and what's in it. */
@Composable
private fun Crumbs(shelf: Shelf, onOpenFolder: (String?) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.bodySmall
    val steps =
        listOf(stringResource(R.string.library_device) to null) +
            shelf.crumbs.map { it.name to it.folder }
    val folders =
        pluralStringResource(R.plurals.count_folders, shelf.folders.size, shelf.folders.size)
    val books = bookCountLabel(shelf.count)
    Row(
        modifier =
            modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.weight(1f).horizontalScroll(rememberScrollState(), reverseScrolling = true),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            steps.forEachIndexed { index, (name, folder) ->
                val last = index == steps.lastIndex
                if (index > 0) Text(text = "›", style = style, color = colors.onSurfaceVariant)
                Text(
                    text = name,
                    style = style,
                    color = if (last) colors.onSurface else colors.onSurfaceVariant,
                    fontWeight = if (last) FontWeight.Bold else null,
                    modifier =
                        if (last) Modifier
                        else Modifier.clickable(role = Role.Button) { onOpenFolder(folder) },
                )
            }
        }
        Text(
            text = if (shelf.folders.isEmpty()) books else "$folders · $books",
            style = style,
            color = colors.onSurfaceVariant,
        )
    }
}
