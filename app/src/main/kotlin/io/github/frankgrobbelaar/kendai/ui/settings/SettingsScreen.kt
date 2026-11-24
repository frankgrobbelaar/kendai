package io.github.frankgrobbelaar.kendai.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.frankgrobbelaar.kendai.EinkMode
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.Theme
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar
import io.github.frankgrobbelaar.kendai.ui.kit.Overline
import io.github.frankgrobbelaar.kendai.ui.kit.Segmented

@Composable
fun SettingsRoute(
    onLibraryFolders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onTheme = viewModel::setTheme,
        onDynamicColor = viewModel::setDynamicColor,
        onEinkMode = viewModel::setEinkMode,
        onLibraryFolders = onLibraryFolders,
        modifier = modifier,
    )
}

/**
 * Settings: how kendai looks, the library's folders, and its version. More sections join as their
 * features land.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onTheme: (Theme) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onEinkMode: (EinkMode) -> Unit,
    onLibraryFolders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        KendaiTopBar(title = stringResource(R.string.nav_settings))
        Overline(stringResource(R.string.settings_appearance), SECTION)
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
        Overline(stringResource(R.string.settings_library), SECTION)
        LinkSetting(
            label = stringResource(R.string.settings_folders),
            value = state.folders.toString(),
            onClick = onLibraryFolders,
        )
        Overline(stringResource(R.string.settings_about), SECTION)
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

/** A setting with a page of its own: its name, what it's set to, and a chevron. */
@Composable
private fun LinkSetting(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 56.dp)
                .padding(start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = colors.onSurfaceVariant,
        )
        Icon(KendaiIcons.Chevron, contentDescription = null, tint = colors.onSurfaceVariant)
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

/** Room around a section's name. */
private val SECTION = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
