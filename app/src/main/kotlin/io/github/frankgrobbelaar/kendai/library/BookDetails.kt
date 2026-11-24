package io.github.frankgrobbelaar.kendai.library

import android.util.Log
import java.io.File
import java.io.IOException
import org.xmlpull.v1.XmlPullParserException

/** What a book says about itself. */
data class Details(
    val title: String,
    val author: String = "",
    val series: String = "",
    val seriesIndex: Double? = null,
    /** The first lines of a plain-text guide, to draw its cover from. */
    val excerpt: String = "",
)

/** A book's details, and its cover picture as the book holds it, if it has one. */
class Found(val details: Details, val cover: ByteArray? = null)

/**
 * Reads what a book's file says about itself: the title, author and series, and the cover. What
 * can't be read, from a damaged file or a format not read yet, falls back to the file's name, so
 * the book is still in the library.
 */
object BookDetails {
    // Books are whatever people have on their devices, damaged and odd ones included. One that a
    // reader trips over in a way nobody foresaw is still a book, named after its file; without
    // this, kendai would stop at it on every look, each time it opened.
    @Suppress("TooGenericExceptionCaught")
    fun read(file: File, format: BookFormat): Found {
        val fallback = Details(title = titleFromName(file.name))
        return try {
            readAs(format, file, fallback)
        } catch (_: IOException) {
            Found(fallback)
        } catch (_: XmlPullParserException) {
            Found(fallback)
        } catch (_: IllegalArgumentException) {
            // A cover that isn't valid Base64, or a page Android won't render.
            Found(fallback)
        } catch (_: SecurityException) {
            // A PDF locked with a password.
            Found(fallback)
        } catch (e: RuntimeException) {
            Log.w(TAG, "Couldn't read the details of ${file.name}", e)
            Found(fallback)
        }
    }

    private fun readAs(format: BookFormat, file: File, fallback: Details): Found =
        when (format) {
            BookFormat.Epub -> Epub.read(file, fallback)
            BookFormat.Fb2 -> Fb2.read(file, fallback)
            BookFormat.Cbz -> ComicZip.read(file, fallback)
            BookFormat.Pdf -> PdfCover.read(file, fallback)
            BookFormat.Txt -> Found(fallback.copy(excerpt = PlainText.excerptOf(file)))
            BookFormat.Markdown ->
                Found(fallback.copy(title = PlainText.markdownTitle(file) ?: fallback.title))
            BookFormat.Html ->
                Found(fallback.copy(title = PlainText.htmlTitle(file) ?: fallback.title))
            // Read as they get readers of their own: Kindle files, and the other comic archives.
            else -> Found(fallback)
        }

    /** A title from a file's name: without its extension, with underscores as spaces. */
    internal fun titleFromName(name: String): String =
        name.substringBeforeLast('.').replace('_', ' ').replace(SPACES, " ").trim().ifEmpty { name }

    private const val TAG = "BookDetails"
    private val SPACES = Regex("\\s+")
}
