package io.github.frankgrobbelaar.kendai

import android.app.Application

open class KendaiApplication : Application() {
    open val container: AppContainer by lazy { deviceContainer(this) }
}
