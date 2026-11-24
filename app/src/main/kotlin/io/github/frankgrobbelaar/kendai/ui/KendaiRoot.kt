package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.frankgrobbelaar.kendai.appContainer
import io.github.frankgrobbelaar.kendai.ui.firstrun.FirstRunRoute

/**
 * The first run while the library has no folders, then the app. Once the first run has begun it
 * carries on to its end, though adding folders gives the library some on the way.
 */
@Composable
fun KendaiRoot(modifier: Modifier = Modifier) {
    val folders by appContainer().library.folders.collectAsStateWithLifecycle(initialValue = null)
    var firstRun by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(folders) { if (firstRun == null) folders?.let { firstRun = it.isEmpty() } }
    when (firstRun) {
        null -> Unit
        true -> FirstRunRoute(onDone = { firstRun = false }, modifier = modifier)
        false -> KendaiApp(modifier)
    }
}
