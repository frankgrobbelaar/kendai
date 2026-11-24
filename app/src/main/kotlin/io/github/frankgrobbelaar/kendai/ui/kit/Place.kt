package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.frankgrobbelaar.kendai.R

/** The places at the top of kendai, as the navigation bar and rail show them. */
enum class Place(@param:StringRes val label: Int, val icon: ImageVector) {
    Library(R.string.nav_library, KendaiIcons.Library),
    Jobs(R.string.nav_jobs, KendaiIcons.Jobs),
    Settings(R.string.nav_settings, KendaiIcons.Settings),
}
