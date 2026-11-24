package io.github.frankgrobbelaar.kendai.ui

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.frankgrobbelaar.kendai.library.BookKind
import io.github.frankgrobbelaar.kendai.ui.kit.BookCover
import io.github.frankgrobbelaar.kendai.ui.library.BookItem
import io.github.frankgrobbelaar.kendai.ui.theme.KendaiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BooksTest {
    @get:Rule val compose = createComposeRule()

    private fun book(title: String) =
        BookItem(
            fingerprint = title,
            path = "/storage/emulated/0/Books/$title.epub",
            title = title,
            author = "",
            meta = "",
            cover = null,
            kind = BookKind.Book,
            excerpt = "",
            addedAt = 0,
        )

    @Test
    fun `a drawn cover picks its cloth from any title`() {
        // The one title whose hash has no positive opposite.
        val title = "polygenelubricants"
        assertEquals(Int.MIN_VALUE, title.hashCode())

        compose.setContent {
            KendaiTheme { BookCover(book(title), Modifier.size(width = 96.dp, height = 144.dp)) }
        }

        compose.onNodeWithText(title).assertExists()
    }
}
