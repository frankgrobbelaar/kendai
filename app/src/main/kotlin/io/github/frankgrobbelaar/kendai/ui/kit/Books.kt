package io.github.frankgrobbelaar.kendai.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.frankgrobbelaar.kendai.R
import io.github.frankgrobbelaar.kendai.library.BookCount
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.ui.library.BookItem
import io.github.frankgrobbelaar.kendai.ui.theme.Literata
import io.github.frankgrobbelaar.kendai.ui.theme.PlexMono
import io.github.frankgrobbelaar.kendai.ui.theme.Washi
import io.github.frankgrobbelaar.kendai.ui.theme.WashiCorners

/**
 * A book's cover, at the size [modifier] gives it, as on the design's Kit · Book card: its own
 * picture, a plain-text guide's first lines, or one drawn from its title.
 */
@Composable
fun BookCover(book: BookItem, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val edge = Modifier.border(Washi.ruleWidth, colors.outlineVariant, WashiCorners.Cover)
    when {
        book.cover != null ->
            AsyncImage(
                model = book.cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                // E-ink has no colour, even on a colour e-ink screen.
                colorFilter = if (Washi.eink) GREYS else null,
                modifier =
                    modifier
                        .clip(WashiCorners.Cover)
                        .background(colors.surfaceContainer)
                        .then(if (Washi.eink) edge else Modifier),
            )
        book.excerpt.isNotEmpty() -> GuideCover(book.excerpt, modifier.then(edge))
        else -> TitleCover(book.title, book.author, modifier)
    }
}

/** A plain-text guide's cover: its first lines in Plex Mono, on a sheet. */
@Composable
private fun GuideCover(excerpt: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier.clip(WashiCorners.Cover).background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (maxWidth >= SMALLEST_WITH_TEXT) {
            Text(
                text = excerpt,
                fontFamily = PlexMono,
                fontSize = (maxWidth.value / 13f).sp,
                lineHeight = (maxWidth.value / 9.3f).sp,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}

/** A cover drawn from a book's title: cloth in a colour its title picks, and the title on it. */
@Composable
private fun TitleCover(title: String, author: String, modifier: Modifier = Modifier) {
    val eink = Washi.eink
    val cloth = if (eink) Color.White else CLOTHS[title.hashCode().mod(CLOTHS.size)]
    val ink = if (eink) Color.Black else if (cloth == OCHRE) INK else PAPER
    BoxWithConstraints(
        modifier
            .clip(WashiCorners.Cover)
            .background(cloth)
            .then(
                if (eink) Modifier.border(Washi.ruleWidth, Color.Black, WashiCorners.Cover)
                else Modifier
            )
    ) {
        // The title scales with the cover: 15 sp on the design's 96 dp covers.
        val width = maxWidth.value
        if (maxWidth >= SMALLEST_WITH_TEXT) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    color = ink,
                    fontFamily = Literata,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (width / 6.4f).sp,
                    lineHeight = (width / 5.6f).sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (author.isNotBlank()) {
                    Text(
                        text = author,
                        color = ink.copy(alpha = 0.85f),
                        fontFamily = Literata,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** A book in a list, as on the design's Kit · Book row: a small cover, its title and its line. */
@Composable
fun BookRow(book: BookItem, modifier: Modifier = Modifier) {
    ListRow(modifier) {
        BookCover(book, Modifier.width(36.dp).height(54.dp))
        RowText(title = book.title, detail = book.meta, modifier = Modifier.weight(1f))
    }
}

/** A book in a grid of covers: the cover, then its title. */
@Composable
fun BookTile(book: BookItem, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BookCover(book, Modifier.fillMaxWidth().aspectRatio(2f / 3f))
        Text(
            text = book.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A folder in a list: its tile, its [name] and a [detail] such as how many books it holds, then
 * [trailing], such as a chevron or a tick.
 */
@Composable
fun FolderRow(
    name: String,
    detail: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    ListRow(modifier) {
        Box(
            modifier =
                Modifier.size(36.dp)
                    .background(colors.surfaceContainer, WashiCorners.Button)
                    .border(Washi.ruleWidth, colors.outlineVariant, WashiCorners.Button),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                KendaiIcons.Folder,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        RowText(title = name, detail = detail, modifier = Modifier.weight(1f))
        trailing()
    }
}

/** A tick box that shows whether a row is chosen; the row itself is what you tap. */
@Composable
fun TickBox(ticked: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val box = modifier.size(24.dp).clip(TICK_CORNERS)
    if (ticked) {
        Box(box.background(colors.primary), contentAlignment = Alignment.Center) {
            Icon(
                KendaiIcons.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    } else {
        Box(box.border(1.5.dp, colors.onSurfaceVariant, TICK_CORNERS))
    }
}

/** How many books there are, saying comics, guides or documents when they're all one kind. */
@Composable
fun bookCountLabel(count: BookCount): String =
    if (count.books == 0) {
        stringResource(R.string.no_books_here)
    } else {
        val plurals =
            when (count.kind) {
                BookKind.Comic -> R.plurals.count_comics
                BookKind.Guide -> R.plurals.count_guides
                BookKind.Document -> R.plurals.count_documents
                BookKind.Book,
                null -> R.plurals.count_books
            }
        pluralStringResource(plurals, count.books, count.books)
    }

/** A row in a list: at least 70 dp tall, above a hairline, as the design's rows are. */
@Composable
private fun ListRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val rule = MaterialTheme.colorScheme.outlineVariant
    val width = Washi.ruleWidth
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 70.dp)
                .drawBehind {
                    val y = size.height - width.toPx() / 2
                    drawLine(rule, Offset(0f, y), Offset(size.width, y), width.toPx())
                }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

/** A row's title and the line under it. */
@Composable
private fun RowText(title: String, detail: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Below this, a drawn cover has no room for words, as in a list's thumbnail. */
private val SMALLEST_WITH_TEXT = 60.dp

private val TICK_CORNERS = RoundedCornerShape(5.dp)

private val GREYS = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

private val INK = Color(0xFF26211C)
private val PAPER = Color(0xFFF5EFE4)
private val OCHRE = Color(0xFFB5893C)

/** The cloths drawn covers come in: indigo, ochre, moss, madder, slate and umber. */
private val CLOTHS =
    listOf(
        Color(0xFF2F3E56),
        OCHRE,
        Color(0xFF5E6B4E),
        Color(0xFF8E2F22),
        Color(0xFF4F5D73),
        Color(0xFF6B4E3D),
    )
