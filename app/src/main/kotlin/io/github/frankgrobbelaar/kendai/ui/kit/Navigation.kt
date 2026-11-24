package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.frankgrobbelaar.kendai.ui.theme.Washi

/** The bar along the bottom on phones, below a hairline, as on the design's Kit · Nav. */
@Composable
fun KendaiNavigationBar(current: Place, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val rule = MaterialTheme.colorScheme.outlineVariant
    val width = Washi.ruleWidth
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(rule, Offset.Zero, Offset(size.width, 0f), width.toPx())
                }
                .navigationBarsPadding()
                .height(76.dp)
                .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Place.entries.forEach { place ->
            PlaceItem(place, selected = place == current, onClick = { onSelect(place) })
        }
    }
}

/** The rail down the side on tablets and in wide windows, beside a hairline. */
@Composable
fun KendaiNavigationRail(current: Place, onSelect: (Place) -> Unit, modifier: Modifier = Modifier) {
    val rule = MaterialTheme.colorScheme.outlineVariant
    val width = Washi.ruleWidth
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(96.dp)
                .drawBehind {
                    drawLine(
                        rule,
                        Offset(size.width, 0f),
                        Offset(size.width, size.height),
                        width.toPx(),
                    )
                }
                .statusBarsPadding()
                .padding(top = 24.dp)
                .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Place.entries.forEach { place ->
            PlaceItem(place, selected = place == current, onClick = { onSelect(place) })
        }
    }
}

/** One place: its icon and label, the current one in ink with a 2 dp seal rule under its label. */
@Composable
private fun PlaceItem(
    place: Place,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val color = if (selected || Washi.eink) colors.onSurface else colors.onSurfaceVariant
    val seal = colors.primary
    Column(
        modifier =
            modifier
                .widthIn(min = 88.dp)
                .selectable(selected = selected, role = Role.Tab, onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(place.icon, contentDescription = null, tint = color)
        Text(
            text = stringResource(place.label),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                ),
            color = color,
            modifier =
                Modifier.drawBehind {
                    if (selected) {
                        val y = size.height + 1.dp.toPx()
                        drawLine(seal, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
                    }
                },
        )
    }
}
