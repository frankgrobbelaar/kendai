package io.github.frankgrobbelaar.kendai

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Light or dark: as the device is, or always one of them. */
enum class Theme {
    System,
    Light,
    Dark,
}

/** E-ink mode: on by itself on an e-ink reader, or always on, or always off. */
enum class EinkMode {
    Automatic,
    On,
    Off,
}

/** How a library view lays out its books: rows, or a grid of covers. */
enum class Layout {
    List,
    Covers,
}

/** What a library view orders its books by. */
enum class SortBy {
    Title,
    Author,
    Added,
}

/** A library view's order and layout, as its Sort and view sheet sets them. */
data class SortAndView(val sort: SortBy = SortBy.Title, val layout: Layout = Layout.List)

/** What Settings lets you choose. */
data class Choices(
    val theme: Theme = Theme.System,
    /** The wallpaper's colours instead of Washi's. */
    val dynamicColor: Boolean = false,
    val einkMode: EinkMode = EinkMode.Automatic,
    /** The folder view's; each view keeps its own. */
    val folders: SortAndView = SortAndView(),
)

/** Kendai's own choices, kept on the device. */
class KendaiSettings(private val store: DataStore<Preferences>) {

    val choices: Flow<Choices> =
        store.data
            .map { prefs ->
                Choices(
                    theme = Theme.entries.find { it.name == prefs[THEME] } ?: Theme.System,
                    dynamicColor = prefs[DYNAMIC_COLOR] ?: false,
                    einkMode =
                        EinkMode.entries.find { it.name == prefs[EINK_MODE] } ?: EinkMode.Automatic,
                    folders =
                        SortAndView(
                            sort =
                                SortBy.entries.find { it.name == prefs[FOLDERS_SORT] }
                                    ?: SortBy.Title,
                            layout =
                                Layout.entries.find { it.name == prefs[FOLDERS_LAYOUT] }
                                    ?: Layout.List,
                        ),
                )
            }
            .distinctUntilChanged()

    suspend fun setTheme(theme: Theme) {
        store.edit { it[THEME] = theme.name }
    }

    suspend fun setDynamicColor(on: Boolean) {
        store.edit { it[DYNAMIC_COLOR] = on }
    }

    suspend fun setEinkMode(mode: EinkMode) {
        store.edit { it[EINK_MODE] = mode.name }
    }

    suspend fun setFoldersSort(sort: SortBy) {
        store.edit { it[FOLDERS_SORT] = sort.name }
    }

    suspend fun setFoldersLayout(layout: Layout) {
        store.edit { it[FOLDERS_LAYOUT] = layout.name }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val EINK_MODE = stringPreferencesKey("eink_mode")
        val FOLDERS_SORT = stringPreferencesKey("folders_sort")
        val FOLDERS_LAYOUT = stringPreferencesKey("folders_layout")
    }
}
