package io.github.frankgrobbelaar.kendai.library

import android.util.Xml
import java.io.File
import java.io.StringReader
import java.net.URLDecoder
import java.util.zip.ZipFile
import org.xmlpull.v1.XmlPullParser

/** An EPUB's details and cover, from its package document (the OPF). */
internal object Epub {
    fun read(file: File, fallback: Details): Found =
        ZipFile(file).use { zip ->
            val opfPath = zip.textOf(CONTAINER)?.let(::rootfileOf) ?: return@use Found(fallback)
            val opf = parse(zip.textOf(opfPath) ?: return@use Found(fallback))
            val details =
                Details(
                    title = opf.title.ifBlank { fallback.title },
                    author = opf.creator,
                    series = opf.series,
                    seriesIndex = opf.seriesIndex,
                )
            val cover =
                opf.coverHref()?.let { zip.getEntry(resolve(opfPath, it)) }?.let(zip::coverOf)
            Found(details, cover)
        }

    /** The package document's path, from `META-INF/container.xml`. */
    private fun rootfileOf(container: String): String? {
        val parser = parserOf(container)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && local(parser.name) == "rootfile") {
                return parser.getAttributeValue(null, "full-path")
            }
        }
        return null
    }

    private fun parse(xml: String): Opf {
        val parser = parserOf(xml)
        val opf = Opf()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (local(parser.name)) {
                "title" -> if (opf.title.isEmpty()) opf.title = parser.nextText().trim()
                "creator" -> if (opf.creator.isEmpty()) opf.creator = parser.nextText().trim()
                "meta" -> meta(parser, opf)
                "item" ->
                    opf.items +=
                        Item(
                            id = parser.getAttributeValue(null, "id").orEmpty(),
                            href = parser.getAttributeValue(null, "href").orEmpty(),
                            mediaType = parser.getAttributeValue(null, "media-type").orEmpty(),
                            properties = parser.getAttributeValue(null, "properties").orEmpty(),
                        )
            }
        }
        return opf
    }

    /** EPUB 2's named metas (the cover, Calibre's series) and EPUB 3's collection. */
    private fun meta(parser: XmlPullParser, opf: Opf) {
        val content = parser.getAttributeValue(null, "content").orEmpty()
        when (parser.getAttributeValue(null, "name")) {
            "cover" -> opf.coverId = content
            "calibre:series" -> opf.series = content
            "calibre:series_index" -> opf.seriesIndex = content.toDoubleOrNull()
        }
        when (parser.getAttributeValue(null, "property")) {
            "belongs-to-collection" ->
                if (opf.series.isEmpty()) opf.series = parser.nextText().trim()
            "group-position" ->
                if (opf.seriesIndex == null)
                    opf.seriesIndex = parser.nextText().trim().toDoubleOrNull()
        }
    }

    private class Opf {
        var title = ""
        var creator = ""
        var series = ""
        var seriesIndex: Double? = null
        var coverId: String? = null
        val items = mutableListOf<Item>()

        /** EPUB 3's cover-image, else EPUB 2's cover meta, else a picture that says it's one. */
        fun coverHref(): String? {
            val pictures = items.filter { it.mediaType.startsWith("image/") }
            return (pictures.find { "cover-image" in it.properties.split(' ') }
                    ?: pictures.find { it.id == coverId }
                    ?: pictures.find {
                        "cover" in it.id.lowercase() || "cover" in it.href.lowercase()
                    })
                ?.href
        }
    }

    private class Item(
        val id: String,
        val href: String,
        val mediaType: String,
        val properties: String,
    )

    /** [href], relative to the package document at [opfPath], as a path in the zip. */
    private fun resolve(opfPath: String, href: String): String {
        val decoded = URLDecoder.decode(href.replace("+", "%2B"), "UTF-8")
        val folder = opfPath.substringBeforeLast('/', "")
        val parts = ArrayDeque<String>()
        for (part in (if (folder.isEmpty()) decoded else "$folder/$decoded").split('/')) {
            when (part) {
                "",
                "." -> Unit
                ".." -> parts.removeLastOrNull()
                else -> parts.addLast(part)
            }
        }
        return parts.joinToString("/")
    }

    private fun parserOf(xml: String): XmlPullParser =
        Xml.newPullParser().apply { setInput(StringReader(xml)) }

    private const val CONTAINER = "META-INF/container.xml"
}

/** An element's name without its namespace prefix: `dc:title` is `title`. */
internal fun local(name: String): String = name.substringAfter(':')
