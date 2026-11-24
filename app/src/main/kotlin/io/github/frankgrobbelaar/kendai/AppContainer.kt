package io.github.frankgrobbelaar.kendai

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

/** Kendai's long-lived parts, made once for the process and handed to what needs them. */
class AppContainer(
    val settings: KendaiSettings,
    /** Whether the screen is e-ink, which turns e-ink mode on unless Settings says otherwise. */
    val einkScreen: Boolean,
) {
    constructor(
        context: Context
    ) : this(
        settings =
            KendaiSettings(
                PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("settings")
                }
            ),
        einkScreen = EinkScreen.detect(),
    )
}

/** The container, from anywhere in the UI. */
@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as KendaiApplication).container
