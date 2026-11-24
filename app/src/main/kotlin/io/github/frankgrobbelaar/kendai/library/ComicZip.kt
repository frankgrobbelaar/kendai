package io.github.frankgrobbelaar.kendai.library

import android.util.Xml
import java.io.File
import java.io.StringReader
import java.util.zip.ZipFile
import org.xmlpull.v1.XmlPullParser

/**
 * A CBZ comic's details, from its `ComicInfo.xml` if it has one, and its first page as the cover.
 */
internal object ComicZip {
    fun read(file: File, fallback: Details): Found =
        ZipFile(file).use { zip ->
            val entries = zip.entries().asSequence().filter { !it.isDirectory }.toList()
            val pages =
                entries
                    .filter { isPicture(it.name) && !it.name.startsWith("__MACOSX") }
                    .sortedWith(compareBy(NaturalOrder) { it.name })
            val info = entries.find { it.name.equals(COMIC_INFO, ignoreCase = true) }
            val details = info?.let {
                comicInfo(zip.bytesOf(it).toString(Charsets.UTF_8), fallback)
            }
            Found(details ?: fallback, pages.firstOrNull()?.let(zip::coverOf))
        }

    private fun comicInfo(xml: String, fallback: Details): Details {
        val parser = Xml.newPullParser().apply { setInput(StringReader(xml)) }
        val fields = mutableMapOf<String, String>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name in FIELDS) {
                fields[parser.name] = parser.nextText().trim()
            }
        }
        val series = fields["Series"].orEmpty()
        val number = fields["Number"].orEmpty()
        val title =
            fields["Title"]?.takeIf { it.isNotBlank() }
                ?: listOf(series, number).filter { it.isNotBlank() }.joinToString(" ")
        return Details(
            title = title.ifBlank { fallback.title },
            author = fields["Writer"].orEmpty(),
            series = series,
            seriesIndex = number.toDoubleOrNull(),
        )
    }

    private fun isPicture(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in PICTURES

    private const val COMIC_INFO = "ComicInfo.xml"
    private val FIELDS = setOf("Title", "Series", "Number", "Writer")
    private val PICTURES = setOf("jpg", "jpeg", "png", "webp", "gif")
}
