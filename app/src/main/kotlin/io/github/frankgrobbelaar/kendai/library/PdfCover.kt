package io.github.frankgrobbelaar.kendai.library

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * A PDF's cover: its first page, drawn by Android's own renderer. Its title stays the file's name
 * until kendai reads PDFs with PDFium, which also gives their details.
 */
internal object PdfCover {
    fun read(file: File, fallback: Details): Found =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { pdf ->
                if (pdf.pageCount == 0) return@use Found(fallback)
                pdf.openPage(0).use { page ->
                    val height =
                        (WIDTH.toFloat() * page.height / page.width)
                            .roundToInt()
                            .coerceIn(1, WIDTH * 3)
                    val picture = createBitmap(WIDTH, height)
                    picture.eraseColor(Color.WHITE)
                    page.render(picture, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val jpeg = ByteArrayOutputStream()
                    picture.compress(Bitmap.CompressFormat.JPEG, QUALITY, jpeg)
                    picture.recycle()
                    Found(fallback, jpeg.toByteArray())
                }
            }
        }

    private const val WIDTH = 480
    private const val QUALITY = 85
}
