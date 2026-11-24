package io.github.frankgrobbelaar.kendai.library

import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/** The most of one entry kendai reads into memory: a cover or a package document, never more. */
internal const val MAX_ENTRY_BYTES = 16 * 1024 * 1024

/** The entry called [name] as text, or null if there's none. */
internal fun ZipFile.textOf(name: String): String? = bytesOf(name)?.toString(Charsets.UTF_8)

/** The entry called [name], or null if there's none. */
internal fun ZipFile.bytesOf(name: String): ByteArray? = getEntry(name)?.let(::bytesOf)

/** [entry]'s bytes, refusing one bigger than [MAX_ENTRY_BYTES], whatever its header claims. */
internal fun ZipFile.bytesOf(entry: ZipEntry): ByteArray =
    getInputStream(entry).use { input ->
        val bytes = input.readUpTo(MAX_ENTRY_BYTES + 1)
        if (bytes.size > MAX_ENTRY_BYTES) throw IOException("${entry.name} is too big to read")
        bytes
    }

/**
 * [entry]'s bytes as a cover, or null if it's too big to keep or can't be read. A book without its
 * cover keeps the rest of its details.
 */
internal fun ZipFile.coverOf(entry: ZipEntry): ByteArray? =
    try {
        bytesOf(entry)
    } catch (_: IOException) {
        null
    }

/** Up to [limit] bytes from the stream, fewer if it ends first. */
internal fun InputStream.readUpTo(limit: Int): ByteArray {
    val bytes = ByteArray(limit)
    var read = 0
    while (read < limit) {
        val got = read(bytes, read, limit - read)
        if (got < 0) break
        read += got
    }
    return bytes.copyOf(read)
}

/**
 * Names in the order people number things: `page2` before `page10`. Digits compare by value, the
 * rest by letter, ignoring case.
 */
internal val NaturalOrder: Comparator<String> = Comparator { a, b ->
    val left = chunksOf(a)
    val right = chunksOf(b)
    for (i in 0 until minOf(left.size, right.size)) {
        val x = left[i]
        val y = right[i]
        val order =
            if (x.first().isDigit() && y.first().isDigit()) {
                compareValuesBy(x, y, { it.trimStart('0').length }, { it.trimStart('0') })
            } else {
                x.compareTo(y, ignoreCase = true)
            }
        if (order != 0) return@Comparator order
    }
    left.size - right.size
}

private val CHUNKS = Regex("\\d+|\\D+")

private fun chunksOf(name: String): List<String> = CHUNKS.findAll(name).map { it.value }.toList()
