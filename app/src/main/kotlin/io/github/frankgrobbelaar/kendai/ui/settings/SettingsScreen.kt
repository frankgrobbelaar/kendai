package io.github.frankgrobbelaar.kendai.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.frankgrobbelaar.kendai.EinkMode
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.Theme
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onTheme = viewModel::setTheme,
        onDynamicColor = viewModel::setDynamicColor,
        onEinkMode = viewModel::setEinkMode,
        modifier = modifier,
    )
}

/** Settings: how kendai looks, and its version. More sections join as their features land. */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onTheme: (Theme) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onEinkMode: (EinkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        KendaiTopBar(title = stringResource(R.string.nav_settings))
        Overline(stringResource(R.string.settings_appearance))
        ThemeSetting(theme = state.choices.theme, onTheme = onTheme)
        // E-ink mode is black and white, so the wallpaper's colours have nothing to change there.
        if (state.dynamicColorAvailable && !state.einkOn) {
            SwitchSetting(
                label = stringResource(R.string.settings_dynamic_color),
                body = stringResource(R.string.settings_dynamic_color_body),
                checked = state.choices.dynamicColor,
                onChange = onDynamicColor,
            )
        }
        EinkSetting(
            mode = state.choices.einkMode,
            einkScreen = state.einkScreen,
            onEinkMode = onEinkMode,
        )
        Overline(stringResource(R.string.settings_about))
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = state.version,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Light, dark, or as the device is. */
@Composable
private fun ThemeSetting(theme: Theme, onTheme: (Theme) -> Unit, modifier: Modifier = Modifier) {
    val themes = Theme.entries
    val labels =
        listOf(
            stringResource(R.string.settings_theme_system),
            stringResource(R.string.settings_theme_light),
            stringResource(R.string.settings_theme_dark),
        )
    Setting(label = stringResource(R.string.settings_theme), modifier = modifier) {
        Segmented(
            count = themes.size,
            label = { labels[it] },
            selected = themes.indexOf(theme),
            onSelect = { onTheme(themes[it]) },
        )
    }
}

/** E-ink mode, saying whether this device's screen is e-ink, which Automatic follows. */
@Composable
private fun EinkSetting(
    mode: EinkMode,
    einkScreen: Boolean,
    onEinkMode: (EinkMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = EinkMode.entries
    val labels =
        listOf(
            stringResource(R.string.settings_eink_automatic),
            stringResource(R.string.settings_eink_on),
            stringResource(R.string.settings_eink_off),
        )
    Setting(
        label = stringResource(R.string.settings_eink),
        body =
            stringResource(
                if (einkScreen) R.string.settings_eink_found else R.string.settings_eink_not_found
            ),
        modifier = modifier,
    ) {
        Segmented(
            count = modes.size,
            label = { labels[it] },
            selected = modes.indexOf(mode),
            onSelect = { onEinkMode(modes[it]) },
        )
    }
}

/** A section's name: small, tracked capitals in ink 2. */
@Composable
private fun Overline(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp).semantics {
                heading()
            },
    )
}

/** A setting: its name, a line on what it does if it needs one, then its control. */
@Composable
private fun Setting(
    label: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    control: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        control()
        if (body != null) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A setting that's on or off: the whole row toggles it. */
@Composable
private fun SwitchSetting(
    label: String,
    body: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors =
                SwitchDefaults.colors(
                    checkedTrackColor = colors.primary,
                    checkedThumbColor = colors.onPrimary,
                    uncheckedTrackColor = colors.surfaceContainerHigh,
                    uncheckedThumbColor = colors.outline,
                    uncheckedBorderColor = colors.outline,
                ),
        )
    }
}

/**
 * One choice of a few, side by side, as on the design's sort sheet: the chosen one in ink, the rest
 * on paper, inside an ink border.
 */
@Composable
private fun Segmented(
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
                        .background(if (on) colors.onSurface else colors.surface)
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
