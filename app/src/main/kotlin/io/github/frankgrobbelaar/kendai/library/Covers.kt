package io.github.frankgrobbelaar.kendai.library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.File

/** Where covers are kept: one small JPEG per book, named by its fingerprint. */
class Covers(private val folder: File) {
    fun fileOf(fingerprint: String): File = File(folder, "$fingerprint.jpg")

    /**
     * Keeps [picture], in any format Android decodes, as the cover of the book with [fingerprint],
     * made small. False if it isn't a picture. Written aside first, so a half-written cover is
     * never shown.
     */
    fun keep(fingerprint: String, picture: ByteArray): Boolean {
        val small = smallOf(picture) ?: return false
        folder.mkdirs()
        val partial = File(folder, "$fingerprint.part")
        val written =
            partial.outputStream().use { small.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        if (!written) partial.delete()
        return written && partial.renameTo(fileOf(fingerprint))
    }

    /** [picture] decoded at most [HEIGHT] tall, or null if it isn't a picture. */
    private fun smallOf(picture: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(picture, 0, picture.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply { inSampleSize = sampleFor(bounds) }
        return BitmapFactory.decodeByteArray(picture, 0, picture.size, options)?.let {
            if (it.height > HEIGHT) {
                it.scale((it.width * HEIGHT / it.height).coerceAtLeast(1), HEIGHT)
            } else {
                it
            }
        }
    }

    /** The largest halving that keeps the cover at least [HEIGHT] tall and within [LONGEST]. */
    private fun sampleFor(bounds: BitmapFactory.Options): Int {
        var sample = 1
        while (
            bounds.outHeight / (sample * 2) >= HEIGHT ||
                maxOf(bounds.outWidth, bounds.outHeight) / sample > LONGEST
        ) {
            sample *= 2
        }
        return sample
    }

    private companion object {
        const val HEIGHT = 720
        const val LONGEST = 4096
        const val QUALITY = 85
    }
}
