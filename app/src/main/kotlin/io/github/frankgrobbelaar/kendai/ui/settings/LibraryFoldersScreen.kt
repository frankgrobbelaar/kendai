package io.github.frankgrobbelaar.kendai.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.files.pathOfPicked
import io.github.frankgrobbelaar.kendai.ui.kit.FolderRow
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar
import io.github.frankgrobbelaar.kendai.ui.kit.LinkButton
import io.github.frankgrobbelaar.kendai.ui.kit.Overline
import io.github.frankgrobbelaar.kendai.ui.kit.bookCountLabel

@Composable
fun LibraryFoldersRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryFoldersViewModel = viewModel(factory = LibraryFoldersViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { tree ->
            if (tree != null) viewModel.add(pathOfPicked(tree))
        }
    LibraryFoldersScreen(
        state = state,
        onBack = onBack,
        onAdd = { picker.launch(null) },
        onRemove = viewModel::remove,
        modifier = modifier,
    )
}

/**
 * Settings › Library folders, as on the design's settings board: the folders the library holds,
 * each with Remove, and Add a folder. Removing one never deletes a file.
 */
@Composable
fun LibraryFoldersScreen(
    state: LibraryFoldersUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val folders = state.folders
    Column(modifier.fillMaxSize()) {
        KendaiTopBar(
            title = stringResource(R.string.folders_title),
            subtitle = stringResource(R.string.nav_settings),
            onBack = onBack,
        )
        Column(
            Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
        ) {
            Text(
                text =
                    stringResource(
                        if (folders?.isEmpty() == true) R.string.folders_none
                        else R.string.folders_note
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            if (!folders.isNullOrEmpty()) {
                Overline(
                    stringResource(R.string.library_device),
                    Modifier.padding(top = 12.dp, bottom = 2.dp),
                )
                folders.forEach { folder ->
                    val remove = stringResource(R.string.folders_remove_described, folder.name)
                    FolderRow(name = folder.name, detail = bookCountLabel(folder.count)) {
                        LinkButton(
                            text = stringResource(R.string.folders_remove),
                            onClick = { onRemove(folder.path) },
                            description = remove,
                        )
                    }
                }
            }
            if (folders != null) {
                LinkButton(
                    text = stringResource(R.string.folders_add),
                    onClick = onAdd,
                    icon = KendaiIcons.Add,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (state.notOnDevice) {
                Text(
                    text = stringResource(R.string.choose_not_on_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                )
            }
        }
    }
}
