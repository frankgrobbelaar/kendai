package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.frankgrobbelaar.kendai.ui.jobs.JobsScreen
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiNavigationBar
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiNavigationRail
import io.github.frankgrobbelaar.kendai.ui.kit.Place
import io.github.frankgrobbelaar.kendai.ui.library.LibraryScreen
import io.github.frankgrobbelaar.kendai.ui.settings.SettingsRoute

/**
 * Kendai's three places: Library, Jobs and Settings. Phones get a bar along the bottom; tablets and
 * wide windows, such as one side of a split screen on a tablet, get a rail down the side.
 */
@Composable
fun KendaiApp(modifier: Modifier = Modifier) {
    var place by rememberSaveable { mutableStateOf(Place.Library) }
    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (maxWidth >= RAIL_FROM) {
            Row(Modifier.fillMaxSize()) {
                KendaiNavigationRail(
                    current = place,
                    onSelect = { place = it },
                    modifier = Modifier.testTag(NAVIGATION_RAIL),
                )
                PlaceScreen(place, Modifier.weight(1f).statusBarsPadding())
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                PlaceScreen(place, Modifier.weight(1f).statusBarsPadding())
                KendaiNavigationBar(
                    current = place,
                    onSelect = { place = it },
                    modifier = Modifier.testTag(NAVIGATION_BAR),
                )
            }
        }
    }
}

@Composable
private fun PlaceScreen(place: Place, modifier: Modifier = Modifier) {
    when (place) {
        Place.Library -> LibraryScreen(modifier)
        Place.Jobs -> JobsScreen(modifier)
        Place.Settings -> SettingsRoute(modifier)
    }
}

/** Material's medium width class starts here: a tablet, or a phone on its side. */
private val RAIL_FROM = 600.dp

internal const val NAVIGATION_BAR = "navigation-bar"
internal const val NAVIGATION_RAIL = "navigation-rail"
