package io.github.frankgrobbelaar.kendai.library

import android.util.Xml
import java.io.File
import java.util.Base64
import org.xmlpull.v1.XmlPullParser

/**
 * An FB2 book's details and cover, read as it streams past: the description comes first, the
 * cover's picture last, as a Base64 binary.
 */
internal object Fb2 {
    fun read(file: File, fallback: Details): Found =
        file.inputStream().buffered().use { input ->
            val parser = Xml.newPullParser().apply { setInput(input, null) }
            val book = Book()
            while (parser.next() != XmlPullParser.END_DOCUMENT && book.cover == null) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> book.start(parser)
                    XmlPullParser.END_TAG -> book.end(local(parser.name))
                }
            }
            val author =
                listOf(book.first, book.middle, book.last)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
            Found(
                Details(
                    title = book.title.ifBlank { fallback.title },
                    author = author,
                    series = book.series,
                    seriesIndex = book.seriesIndex,
                ),
                book.cover,
            )
        }

    private class Book {
        var title = ""
        var first = ""
        var middle = ""
        var last = ""
        var series = ""
        var seriesIndex: Double? = null
        var coverId: String? = null
        var cover: ByteArray? = null
        private var inTitleInfo = false
        private var inAuthor = false
        private var authorDone = false

        fun start(parser: XmlPullParser) {
            val name = local(parser.name)
            when {
                name == "title-info" -> inTitleInfo = true
                name == "binary" -> binary(parser)
                !inTitleInfo -> Unit
                name == "book-title" -> title = parser.nextText().trim()
                name == "author" && !authorDone -> inAuthor = true
                inAuthor -> author(name, parser)
                name == "sequence" && series.isEmpty() -> {
                    series = parser.getAttributeValue(null, "name").orEmpty()
                    seriesIndex = parser.getAttributeValue(null, "number")?.toDoubleOrNull()
                }
                name == "image" -> coverId = hrefOf(parser)?.removePrefix("#")
            }
        }

        fun end(name: String) {
            when (name) {
                "title-info" -> inTitleInfo = false
                "author" ->
                    if (inAuthor) {
                        inAuthor = false
                        authorDone = true
                    }
            }
        }

        private fun author(name: String, parser: XmlPullParser) {
            when (name) {
                "first-name" -> first = parser.nextText().trim()
                "middle-name" -> middle = parser.nextText().trim()
                "last-name" -> last = parser.nextText().trim()
            }
        }

        private fun binary(parser: XmlPullParser) {
            val id = parser.getAttributeValue(null, "id")
            if (id != null && id == coverId) {
                cover = Base64.getMimeDecoder().decode(parser.nextText().trim())
            }
        }

        /** An image's link, whatever its namespace prefix: `l:href`, `xlink:href`. */
        private fun hrefOf(parser: XmlPullParser): String? =
            (0 until parser.attributeCount)
                .firstOrNull { local(parser.getAttributeName(it)) == "href" }
                ?.let(parser::getAttributeValue)
    }
}
