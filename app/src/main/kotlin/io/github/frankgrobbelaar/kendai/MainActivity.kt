package io.github.frankgrobbelaar.kendai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.frankgrobbelaar.kendai.ui.ChosenTheme
import io.github.frankgrobbelaar.kendai.ui.KendaiRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { ChosenTheme { KendaiRoot() } }
    }
}
