package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.frankgrobbelaar.kendai.ui.jobs.JobsScreen
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiNavigationBar
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiNavigationRail
import io.github.frankgrobbelaar.kendai.ui.kit.Place
import io.github.frankgrobbelaar.kendai.ui.library.LibraryRoute
import io.github.frankgrobbelaar.kendai.ui.settings.LibraryFoldersRoute
import io.github.frankgrobbelaar.kendai.ui.settings.SettingsRoute
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import kotlinx.serialization.Serializable

@Serializable data object LibraryHome : NavKey

@Serializable data object JobsHome : NavKey

@Serializable data object SettingsHome : NavKey

/** Settings › Library folders: the folders the library holds. */
@Serializable data object LibraryFolders : NavKey

/**
 * Kendai's three places: Library, Jobs and Settings, each keeping its own history. Phones get a bar
 * along the bottom; tablets and wide windows, such as one side of a split screen on a tablet, get a
 * rail down the side. A screen opened from a place has the screen to itself.
 */
@Composable
fun KendaiApp(modifier: Modifier = Modifier) {
    var place by rememberSaveable { mutableStateOf(Place.Library) }
    val histories =
        Histories(
            library = rememberNavBackStack(LibraryHome),
            jobs = rememberNavBackStack(JobsHome),
            settings = rememberNavBackStack(SettingsHome),
        )
    val history = histories.of(place)
    val entries =
        rememberEntries(
            place,
            histories,
            onChooseFolders = {
                place = Place.Settings
                histories.settings.retainAll(listOf(SettingsHome))
                histories.settings.add(LibraryFolders)
            },
        )
    val eink = Washi.eink
    Frame(
        place = place,
        placesShown = history.size == 1,
        onSelect = { place = it },
        modifier = modifier,
    ) { screensModifier ->
        NavDisplay(
            entries = entries,
            modifier = screensModifier,
            onBack = {
                if (history.size > 1) history.removeLastOrNull() else place = Place.Library
            },
            transitionSpec = { screenChange(eink) },
            popTransitionSpec = { screenChange(eink) },
            predictivePopTransitionSpec = { screenChange(eink) },
        )
    }
}

/**
 * The places' bar along the bottom, or rail down the side when the window is wide, while
 * [placesShown], and the [screens] beside them, kept clear of Android's bars.
 */
@Composable
private fun Frame(
    place: Place,
    placesShown: Boolean,
    onSelect: (Place) -> Unit,
    modifier: Modifier = Modifier,
    screens: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        val wide = maxWidth >= RAIL_FROM
        // Without the bar along the bottom, the screens keep clear of Android's own.
        val clear =
            Modifier.statusBarsPadding()
                .then(if (wide || !placesShown) Modifier.navigationBarsPadding() else Modifier)
        // One place for the screens either way, so they keep their state when the window changes.
        Row(Modifier.fillMaxSize()) {
            if (wide && placesShown) {
                KendaiNavigationRail(
                    current = place,
                    onSelect = onSelect,
                    modifier = Modifier.testTag(NAVIGATION_RAIL),
                )
            }
            Column(Modifier.weight(1f)) {
                screens(Modifier.weight(1f).then(clear))
                if (!wide && placesShown) {
                    KendaiNavigationBar(
                        current = place,
                        onSelect = onSelect,
                        modifier = Modifier.testTag(NAVIGATION_BAR),
                    )
                }
            }
        }
    }
}

/** Each place's history of screens. */
private class Histories(
    val library: NavBackStack<NavKey>,
    val jobs: NavBackStack<NavKey>,
    val settings: NavBackStack<NavKey>,
) {
    fun of(place: Place) =
        when (place) {
            Place.Library -> library
            Place.Jobs -> jobs
            Place.Settings -> settings
        }
}

/**
 * The screens to show in [place]. Every history is decorated all the time, so a place out of sight
 * keeps its screens' state, and the library sits under the other places, so back from their first
 * screen goes to the library.
 */
@Composable
private fun rememberEntries(
    place: Place,
    histories: Histories,
    onChooseFolders: () -> Unit,
): List<NavEntry<NavKey>> {
    val decorators: List<NavEntryDecorator<NavKey>> =
        listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    val library =
        rememberDecoratedNavEntries(
            histories.library,
            decorators,
            screensFor(Place.Library, histories.library, onChooseFolders),
        )
    val jobs =
        rememberDecoratedNavEntries(
            histories.jobs,
            decorators,
            screensFor(Place.Jobs, histories.jobs, onChooseFolders),
        )
    val settings =
        rememberDecoratedNavEntries(
            histories.settings,
            decorators,
            screensFor(Place.Settings, histories.settings, onChooseFolders),
        )
    return when (place) {
        Place.Library -> library
        Place.Jobs -> library + jobs
        Place.Settings -> library + settings
    }
}

/** What each key shows in [place]. What's opened from a place goes on top of its [history]. */
private fun screensFor(place: Place, history: NavBackStack<NavKey>, onChooseFolders: () -> Unit) =
    entryProvider {
        // Each place keeps its own state for a screen, should two places ever open the same one.
        val keyIn: (NavKey) -> Any = { "${place.name}/$it" }
        entry<LibraryHome>(keyIn) { LibraryRoute(onChooseFolders = onChooseFolders) }
        entry<JobsHome>(keyIn) { JobsScreen() }
        entry<SettingsHome>(keyIn) {
            SettingsRoute(onLibraryFolders = { history.add(LibraryFolders) })
        }
        entry<LibraryFolders>(keyIn) {
            LibraryFoldersRoute(onBack = { history.removeLastOrNull() })
        }
    }

/** Screens change with a short, calm crossfade; in e-ink, at once. */
private fun screenChange(eink: Boolean): ContentTransform =
    if (eink) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        fadeIn(tween(SCREEN_MILLIS)) togetherWith fadeOut(tween(SCREEN_MILLIS))
    }

private const val SCREEN_MILLIS = 250

/** Material's medium width class starts here: a tablet, or a phone on its side. */
private val RAIL_FROM = 600.dp

internal const val NAVIGATION_BAR = "navigation-bar"
internal const val NAVIGATION_RAIL = "navigation-rail"
