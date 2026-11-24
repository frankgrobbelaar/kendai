package io.github.frankgrobbelaar.kendai.library

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * A book's fingerprint: what kendai knows it by, wherever its file is. A hash of the file's size
 * and of samples of its contents, from its start to its end: a 1 GB PDF takes milliseconds, and the
 * same file moved, renamed or on a server has the same fingerprint.
 */
object Fingerprint {
    private const val SAMPLE = 64 * 1024
    private const val SAMPLES = 5
    private const val LENGTH = 32

    fun of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        RandomAccessFile(file, "r").use { data ->
            val size = data.length()
            digest.update(ByteBuffer.allocate(Long.SIZE_BYTES).putLong(size).array())
            val buffer = ByteArray(SAMPLE)
            for (offset in offsetsOf(size)) {
                val length = minOf(SAMPLE.toLong(), size - offset).toInt()
                data.seek(offset)
                data.readFully(buffer, 0, length)
                digest.update(buffer, 0, length)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.take(LENGTH)
    }

    /** Where the samples start: all of a small file, else five spread evenly to its very end. */
    internal fun offsetsOf(size: Long): List<Long> =
        if (size <= SAMPLE.toLong() * SAMPLES) {
            (0 until size step SAMPLE.toLong()).toList()
        } else {
            (0 until SAMPLES).map { it * (size - SAMPLE) / (SAMPLES - 1) }
        }
}
