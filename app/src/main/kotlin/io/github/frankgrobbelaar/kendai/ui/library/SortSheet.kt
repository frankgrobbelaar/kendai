package io.github.frankgrobbelaar.kendai.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.frankgrobbelaar.kendai.Layout
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.SortAndView
import io.github.frankgrobbelaar.kendai.SortBy
import io.github.frankgrobbelaar.kendai.ui.kit.Overline
import io.github.frankgrobbelaar.kendai.ui.kit.Segmented
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

/**
 * The folder view's Sort and view sheet, as on the design's library board: what to order books by,
 * and covers or a list. A choice applies at once.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(
    choice: SortAndView,
    onSort: (SortBy) -> Unit,
    onLayout: (Layout) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val sorts = SortBy.entries
    val sortLabels =
        listOf(
            stringResource(R.string.sort_title),
            stringResource(R.string.sort_author),
            stringResource(R.string.sort_added),
        )
    val layouts = listOf(Layout.Covers, Layout.List)
    val layoutLabels =
        listOf(stringResource(R.string.view_covers), stringResource(R.string.view_list))
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = WashiCorners.Sheet,
        containerColor = colors.surfaceContainer,
        dragHandle = {
            Box(
                Modifier.padding(top = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(colors.outlineVariant, RoundedCornerShape(2.dp))
            )
        },
    ) {
        Column(
            Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.sort_and_view),
                style = MaterialTheme.typography.titleLarge,
            )
            Column(Modifier.selectableGroup()) {
                Overline(stringResource(R.string.sort_by), Modifier.padding(bottom = 4.dp))
                sorts.forEachIndexed { index, sort ->
                    SortChoice(
                        label = sortLabels[index],
                        selected = sort == choice.sort,
                        onClick = { onSort(sort) },
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Overline(stringResource(R.string.view_label))
                Segmented(
                    count = layouts.size,
                    label = { layoutLabels[it] },
                    selected = layouts.indexOf(choice.layout),
                    onSelect = { onLayout(layouts[it]) },
                )
            }
        }
    }
}

/** One order to sort by: the chosen one in the seal's colour, with its radio button on. */
@Composable
private fun SortChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else null,
            color = if (selected) colors.primary else colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        RadioButton(
            selected = selected,
            onClick = null,
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = colors.primary,
                    unselectedColor = colors.onSurfaceVariant,
                ),
        )
    }
}
