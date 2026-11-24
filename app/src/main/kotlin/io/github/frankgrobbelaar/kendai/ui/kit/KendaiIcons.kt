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

    val Folder: ImageVector by lazy {
        line(
            "folder",
            "M3.5 7.5a2 2 0 0 1 2-2h3.6l2.2 2.4h7.2a2 2 0 0 1 2 2v7.6a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2z",
        )
    }

    val Chevron: ImageVector by lazy { line("chevron", "M9 6l6 6-6 6") }

    val Back: ImageVector by lazy { line("back", "M19 12H5M11 6l-6 6 6 6") }

    /** A folder with a tick: only the folders you choose. */
    val FolderChosen: ImageVector by lazy {
        line(
            "folder-chosen",
            "M3.5 7.5a2 2 0 0 1 2-2h3.6l2.2 2.4h7.2a2 2 0 0 1 2 2v7.6a2 2 0 0 1-2 2h-13a2 2 0 0 1-2-2z" +
                "M9 13.6l2.2 2.2 4.3-4.3",
        )
    }

    /** A shield with a tick: nothing is sent anywhere. */
    val Private: ImageVector by lazy {
        line(
            "private",
            "M12 3.5l7 3v5.2c0 4.3-2.9 7.4-7 8.8-4.1-1.4-7-4.5-7-8.8V6.5zM9 12l2.2 2.2 4.3-4.2",
        )
    }

    val Check: ImageVector by lazy { line("check", "M5 12.5l4.5 4.5L19 7.5", width = 2.4f) }

    val More: ImageVector by lazy {
        filled(
            "more",
            "M12 4.3a1.4 1.4 0 1 1 0 2.8 1.4 1.4 0 0 1 0-2.8zM12 10.6a1.4 1.4 0 1 1 0 2.8 1.4 1.4 0 " +
                "0 1 0-2.8zM12 16.9a1.4 1.4 0 1 1 0 2.8 1.4 1.4 0 0 1 0-2.8z",
        )
    }

    val Lock: ImageVector by lazy {
        line("lock", "M6.5 10.5h11v9h-11zM8.5 10.5V8a3.5 3.5 0 0 1 7 0v2.5")
    }

    val Add: ImageVector by lazy { line("add", "M12 5v14M5 12h14") }

    val Settings: ImageVector by lazy {
        line(
            "settings",
            "M4 7h10M18 7h2M4 17h4M12 17h8M16 5a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM10 15a2 2 0 1 1 0 " +
                "4 2 2 0 0 1 0-4z",
        )
    }
}

/**
 * A 24 dp icon drawn as a rounded line, 1.6 dp unless [width] says, along [path] in SVG's syntax.
 */
private fun line(name: String, path: String, width: Float = 1.6f): ImageVector =
    builder(name)
        .addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
        .build()

/** A 24 dp icon filled in along [path], for shapes too small to draw as lines, like dots. */
private fun filled(name: String, path: String): ImageVector =
    builder(name)
        .addPath(
            pathData = PathParser().parsePathString(path).toNodes(),
            fill = SolidColor(Color.Black),
        )
        .build()

private fun builder(name: String) =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
