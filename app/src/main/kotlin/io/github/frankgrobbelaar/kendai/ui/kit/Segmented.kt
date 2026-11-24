package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

/**
 * One choice of a few, side by side, as on the design's sort sheet: the chosen one in ink, the rest
 * on what's behind, inside an ink border.
 */
@Composable
fun Segmented(
    count: Int,
    label: (Int) -> String,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(WashiCorners.Button)
                .border(Washi.ruleWidth, colors.onSurface, WashiCorners.Button)
                .selectableGroup()
    ) {
        repeat(count) { index ->
            val on = index == selected
            Box(
                modifier =
                    Modifier.weight(1f)
                        .heightIn(min = 44.dp)
                        .background(if (on) colors.onSurface else Color.Transparent)
                        .selectable(
                            selected = on,
                            role = Role.RadioButton,
                            onClick = { onSelect(index) },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(index),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) colors.surface else colors.onSurface,
                )
            }
        }
    }
}
