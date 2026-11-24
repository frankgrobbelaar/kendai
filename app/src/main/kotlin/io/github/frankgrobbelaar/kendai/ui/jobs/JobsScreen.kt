package io.github.frankgrobbelaar.kendai.ui.jobs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.ui.kit.EmptyState
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiIcons
import io.github.frankgrobbelaar.kendai.ui.kit.KendaiTopBar

/** Jobs: downloads, file changes and clean-ups, with their progress. None run yet. */
@Composable
fun JobsScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        KendaiTopBar(title = stringResource(R.string.nav_jobs))
        EmptyState(
            icon = KendaiIcons.Jobs,
            title = stringResource(R.string.jobs_empty_title),
            body = stringResource(R.string.jobs_empty_body),
        )
    }
}
