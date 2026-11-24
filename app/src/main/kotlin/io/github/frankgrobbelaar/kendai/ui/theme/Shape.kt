package io.github.frankgrobbelaar.kendai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Washi's soft corners on Material's scale: 4 to 8 dp for chips, buttons, fields and cards, and 16
 * for sheets and dialogs.
 */
internal val WashiShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(16.dp),
    )

/** The corners docs/design.md names outside Material's scale. */
object WashiCorners {
    val Button = RoundedCornerShape(6.dp)
    val Chip = RoundedCornerShape(8.dp)
    val Tile = RoundedCornerShape(8.dp)
    val Cover = RoundedCornerShape(3.dp)
    val Sheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
