package io.github.frankgrobbelaar.kendai.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.ui.kit.EmptyState
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar

/** The library. Until it can find books, it says what will be here. */
@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        KendaiTopBar(title = stringResource(R.string.nav_library))
        EmptyState(
            icon = KendaiIcons.Library,
            title = stringResource(R.string.library_empty_title),
            body = stringResource(R.string.library_empty_body),
        )
    }
}
