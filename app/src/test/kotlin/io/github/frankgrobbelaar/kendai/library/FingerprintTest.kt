package io.github.frankgrobbelaar.kendai.library

import java.io.File
import java.io.RandomAccessFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FingerprintTest {
    @get:Rule val folder = TemporaryFolder()

    private fun file(name: String, bytes: ByteArray) =
        File(folder.root, name).apply { writeBytes(bytes) }

    @Test
    fun `the same book has the same fingerprint wherever it is and whatever it's called`() {
        val bytes = ByteArray(200_000) { (it % 251).toByte() }

        val one = Fingerprint.of(file("Moby-Dick.epub", bytes))
        val other = Fingerprint.of(file("moby_dick (1).epub", bytes))

        assertEquals(one, other)
        assertEquals(32, one.length)
    }

    @Test
    fun `a change anywhere in a small book changes its fingerprint`() {
        val bytes = ByteArray(100_000)
        val changed = bytes.copyOf().also { it[99_999] = 1 }

        assertNotEquals(Fingerprint.of(file("a", bytes)), Fingerprint.of(file("b", changed)))
    }

    @Test
    fun `a big book is sampled from its start to its very end`() {
        val big = File(folder.root, "big.pdf")
        RandomAccessFile(big, "rw").use { it.setLength(10_000_000) }
        val before = Fingerprint.of(big)

        RandomAccessFile(big, "rw").use {
            it.seek(10_000_000 - 1)
            it.write(1)
        }

        assertNotEquals(before, Fingerprint.of(big))
        assertEquals(
            listOf(0L, 2_483_616L, 4_967_232L, 7_450_848L, 9_934_464L),
            Fingerprint.offsetsOf(10_000_000),
        )
    }

    @Test
    fun `a small book is read whole, and an empty one is known by its size`() {
        assertEquals(listOf(0L, 65_536L), Fingerprint.offsetsOf(100_000))
        assertEquals(emptyList<Long>(), Fingerprint.offsetsOf(0))
        assertEquals(32, Fingerprint.of(file("empty.txt", ByteArray(0))).length)
    }
}
