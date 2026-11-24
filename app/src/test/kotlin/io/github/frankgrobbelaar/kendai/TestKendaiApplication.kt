package io.github.frankgrobbelaar.kendai

/** Kendai for Robolectric: settings in memory, and a screen that isn't e-ink. */
class TestKendaiApplication : KendaiApplication() {
    override val container: AppContainer by lazy {
        AppContainer(settings = KendaiSettings(MemoryStore()), einkScreen = false)
    }
}
