package io.github.frankgrobbelaar.kendai

import android.os.Build

/**
 * Whether this device's screen is e-ink. Android has no flag for it, so its maker tells: these make
 * only e-ink readers. Anything else can still turn e-ink mode on in Settings.
 */
object EinkScreen {
    private val makers =
        setOf("onyx", "boox", "bigme", "meebook", "boyue", "likebook", "pocketbook")

    fun detect(manufacturer: String = Build.MANUFACTURER, brand: String = Build.BRAND): Boolean =
        manufacturer.lowercase() in makers || brand.lowercase() in makers
}

/** Whether e-ink mode is on, as chosen, on a device whose screen is e-ink or not. */
fun EinkMode.isOn(einkScreen: Boolean): Boolean =
    when (this) {
        EinkMode.Automatic -> einkScreen
        EinkMode.On -> true
        EinkMode.Off -> false
    }
