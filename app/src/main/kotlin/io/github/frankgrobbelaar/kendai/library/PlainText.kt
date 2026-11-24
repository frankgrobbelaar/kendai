package io.github.frankgrobbelaar.kendai.library

import java.io.File

/** What plain text, Markdown and HTML say about themselves, from their first few kilobytes. */
internal object PlainText {
    /** A guide's first lines, as its cover shows them: a dozen, cut to the cover's width. */
    fun excerptOf(file: File): String =
        headOf(file)
            .lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .take(EXCERPT_LINES)
            .joinToString("\n") { it.take(EXCERPT_WIDTH) }

    /** A Markdown document's first top-level heading. */
    fun markdownTitle(file: File): String? =
        headOf(file).lineSequence().firstOrNull { it.startsWith("# ") }?.removePrefix("# ")?.trim()

    /** An HTML page's title. */
    fun htmlTitle(file: File): String? =
        TITLE.find(headOf(file))
            ?.groupValues
            ?.get(1)
            ?.replace("&amp;", "&")
            ?.replace("&lt;", "<")
            ?.replace("&gt;", ">")
            ?.replace("&quot;", "\"")
            ?.replace("&#39;", "'")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    private fun headOf(file: File): String =
        file.inputStream().use { it.readUpTo(HEAD_BYTES) }.toString(Charsets.UTF_8)

    private const val HEAD_BYTES = 16 * 1024
    private const val EXCERPT_LINES = 12
    private const val EXCERPT_WIDTH = 28
    private val TITLE =
        Regex(
            "<title[^>]*>(.*?)</title>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
}
