package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

/**
 * A row of chips, one chosen, as on the design's Kit · Chips: the chosen one in ink, the rest
 * outlined. [label] names the chip at each index. They look small, but Android takes a tap within
 * 48 dp of one as a tap on it.
 */
@Composable
fun Chips(
    count: Int,
    label: @Composable (Int) -> String,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 10.dp)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val on = index == selected
            Text(
                text = label(index),
                style =
                    MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
                color = if (on) colors.surface else colors.onSurfaceVariant,
                modifier =
                    Modifier.clip(WashiCorners.Chip)
                        .background(if (on) colors.onSurface else colors.surface)
                        .border(
                            Washi.ruleWidth,
                            if (on) colors.onSurface else colors.outlineVariant,
                            WashiCorners.Chip,
                        )
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 11.dp, vertical = 5.dp),
            )
        }
    }
}
