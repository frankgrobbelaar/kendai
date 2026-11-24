package io.github.frankgrobbelaar.kendai.library

import java.io.File
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Small books of each format, written into tests' temporary folders. */
object Samples {
    /** A one-pixel PNG: the smallest real picture, for covers. */
    val PNG: ByteArray =
        ("89504E470D0A1A0A0000000D4948445200000001000000010806000000" +
                "1F15C4890000000A49444154789C63000100000500010D0A2DB40000000049454E44AE426082")
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

    /** An EPUB 3 with a title, an author, a series and a cover. */
    fun epub(
        file: File,
        title: String = "Moby-Dick",
        author: String = "Herman Melville",
        cover: ByteArray? = PNG,
    ): File {
        val coverItem =
            if (cover == null) ""
            else
                """<item id="c" href="images/the%20cover.png" media-type="image/png" properties="cover-image"/>"""
        val opf =
            """<?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" version="3.0">
              <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                <dc:title>$title</dc:title>
                <dc:creator>$author</dc:creator>
                <meta property="belongs-to-collection" id="s">Sea stories</meta>
                <meta property="group-position" refines="#s">2</meta>
              </metadata>
              <manifest>
                <item id="text" href="text.xhtml" media-type="application/xhtml+xml"/>
                $coverItem
              </manifest>
            </package>"""
                .trimIndent()
        return zip(
            file,
            "mimetype" to "application/epub+zip".toByteArray(),
            "META-INF/container.xml" to
                """
                <?xml version="1.0"?>
                                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                                  <rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles>
                                </container>
                """
                    .trimIndent()
                    .toByteArray(),
            "OEBPS/content.opf" to opf.toByteArray(),
            "OEBPS/text.xhtml" to "<html><body><p>Call me Ishmael.</p></body></html>".toByteArray(),
            *(if (cover == null) emptyArray() else arrayOf("OEBPS/images/the cover.png" to cover)),
        )
    }

    /** An FB2 with its title, author, series and a cover kept as Base64. */
    fun fb2(file: File, title: String = "The Valley of Fear"): File {
        file.writeText(
            """<?xml version="1.0" encoding="UTF-8"?>
            <FictionBook xmlns="http://www.gribuser.ru/xml/fictionbook/2.0" xmlns:l="http://www.w3.org/1999/xlink">
              <description>
                <title-info>
                  <author><first-name>Arthur</first-name><middle-name>Conan</middle-name><last-name>Doyle</last-name></author>
                  <book-title>$title</book-title>
                  <coverpage><image l:href="#cover.png"/></coverpage>
                  <sequence name="Sherlock Holmes" number="4"/>
                </title-info>
              </description>
              <body><section><p>Chapter one.</p></section></body>
              <binary id="cover.png" content-type="image/png">${Base64.getMimeEncoder().encodeToString(PNG)}</binary>
            </FictionBook>"""
                .trimIndent()
        )
        return file
    }

    /** A CBZ with pages numbered the way people do, and a ComicInfo.xml. */
    fun cbz(file: File): File =
        zip(
            file,
            "page10.png" to byteArrayOf(1, 2, 3),
            "page2.png" to PNG,
            "__MACOSX/page1.png" to byteArrayOf(9),
            "ComicInfo.xml" to
                """
                <?xml version="1.0"?>
                                <ComicInfo><Series>Little Nemo</Series><Number>1</Number><Writer>Winsor McCay</Writer></ComicInfo>
                """
                    .trimIndent()
                    .toByteArray(),
        )

    fun text(file: File, text: String): File {
        file.writeText(text)
        return file
    }

    private fun zip(file: File, vararg entries: Pair<String, ByteArray>): File {
        file.parentFile?.mkdirs()
        ZipOutputStream(file.outputStream()).use { zip ->
            for ((name, bytes) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return file
    }
}
