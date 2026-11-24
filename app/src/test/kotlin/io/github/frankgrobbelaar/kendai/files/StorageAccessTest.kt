package io.github.frankgrobbelaar.kendai.files

import android.provider.DocumentsContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StorageAccessTest {
    private val root = "/storage/emulated/0"

    private fun picked(id: String, authority: String = "com.android.externalstorage.documents") =
        pathOfPicked(DocumentsContract.buildTreeDocumentUri(authority, id), root)

    @Test
    fun `a folder picked on the device's own storage is under its top`() {
        assertEquals("$root/Books", picked("primary:Books"))
        assertEquals("$root/Download/Guides", picked("primary:Download/Guides"))
        assertEquals(root, picked("primary:"))
    }

    @Test
    fun `a folder picked on an SD card is under that card`() {
        assertEquals("/storage/1234-ABCD/Comics", picked("1234-ABCD:Comics"))
        assertEquals("/storage/1234-ABCD", picked("1234-ABCD:"))
    }

    @Test
    fun `a folder that isn't on the device has no path`() {
        assertNull(
            picked("acc=1;doc=encoded=abc", authority = "com.google.android.apps.docs.storage")
        )
    }
}
