package io.github.frankgrobbelaar.kendai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.frankgrobbelaar.kendai.R

private fun literata(weight: Int) =
    Font(
        R.font.literata,
        FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )

private fun sourceSans(weight: Int) =
    Font(
        R.font.source_sans_3,
        FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )

/** Literata, bundled: headings, book titles and the text of books. */
internal val Literata = FontFamily(literata(400), literata(600))

/** Source Sans 3, bundled: the interface. */
internal val SourceSans = FontFamily(sourceSans(400), sourceSans(600), sourceSans(700))

/** IBM Plex Mono, bundled: plain-text guides, section codes and server keys. */
internal val PlexMono =
    FontFamily(
        Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
        Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
    )

private fun style(
    family: FontFamily,
    size: Int,
    line: Int,
    weight: Int,
    tracking: Double = 0.0,
) =
    TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = FontWeight(weight),
        letterSpacing = tracking.em,
        // Tabular figures, so pages, counts and progress don't jitter as their digits change.
        fontFeatureSettings = "tnum",
    )

/**
 * Washi's type scale on Material's roles: Literata for titles and headings, Source Sans 3 for the
 * rest. The screen title is headline medium, a section title medium, a bar's title title small,
 * body medium the body, and label small the overline. See docs/design.md.
 */
internal val WashiTypography =
    Typography(
        displaySmall = style(Literata, size = 30, line = 36, weight = 600, tracking = -0.01),
        headlineMedium = style(Literata, size = 26, line = 32, weight = 600, tracking = -0.01),
        headlineSmall = style(Literata, size = 22, line = 28, weight = 600),
        titleLarge = style(Literata, size = 20, line = 26, weight = 600),
        titleMedium = style(Literata, size = 18, line = 24, weight = 600),
        titleSmall = style(SourceSans, size = 16, line = 22, weight = 700),
        bodyLarge = style(SourceSans, size = 16, line = 24, weight = 400),
        bodyMedium = style(SourceSans, size = 15, line = 22, weight = 400),
        bodySmall = style(SourceSans, size = 13, line = 18, weight = 400),
        labelLarge = style(SourceSans, size = 15, line = 20, weight = 700),
        labelMedium = style(SourceSans, size = 13, line = 18, weight = 600),
        labelSmall = style(SourceSans, size = 12, line = 16, weight = 700, tracking = 0.12),
    )

/** The two styles Material has no role for: the text of a book and of a plain-text guide. */
object WashiText {
    val Book =
        TextStyle(
            fontFamily = Literata,
            fontSize = 17.sp,
            lineHeight = 27.5.sp,
            textAlign = TextAlign.Justify,
            hyphens = Hyphens.Auto,
        )

    val Guide = TextStyle(fontFamily = PlexMono, fontSize = 12.sp, lineHeight = 18.5.sp)
}
