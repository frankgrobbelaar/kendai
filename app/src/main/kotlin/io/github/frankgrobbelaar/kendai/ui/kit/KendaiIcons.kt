package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Kendai's icons: thin rounded lines, from the same paths as the design canvas's kit. */
object KendaiIcons {
    val Library: ImageVector by lazy {
        line(
            "library",
            "M3.5 5.5h6a2.5 2.5 0 0 1 2.5 2.5v11.5a2 2 0 0 0-2-2H3.5zM20.5 5.5h-6A2.5 2.5 0 0 0 " +
                "12 8v11.5a2 2 0 0 1 2-2h6.5z",
        )
    }

    val Jobs: ImageVector by lazy { line("jobs", "M12 4v10M8 10l4 4 4-4M5 19.5h14") }

    val Settings: ImageVector by lazy {
        line(
            "settings",
            "M4 7h10M18 7h2M4 17h4M12 17h8M16 5a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM10 15a2 2 0 1 1 0 " +
                "4 2 2 0 0 1 0-4z",
        )
    }
}

/** A 24 dp icon drawn as one rounded 1.6 dp line along [path], in SVG's path syntax. */
private fun line(name: String, path: String): ImageVector =
    ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        .addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()
