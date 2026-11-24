package io.github.frankgrobbelaar.kendai.library

/** What sort of thing a book is, for the library's views; guessed from its format, changeable. */
enum class BookKind {
    Book,
    Comic,
    Guide,
    Document,
}

/** Every format kendai reads, by the extensions its files have. */
enum class BookFormat(val label: String, val kind: BookKind, vararg val extensions: String) {
    Epub("EPUB", BookKind.Book, "epub"),
    Fb2("FB2", BookKind.Book, "fb2"),
    Mobi("MOBI", BookKind.Book, "mobi"),
    Azw("AZW", BookKind.Book, "azw"),
    Azw3("AZW3", BookKind.Book, "azw3"),
    Pdf("PDF", BookKind.Document, "pdf"),
    Cbz("CBZ", BookKind.Comic, "cbz"),
    Cbr("CBR", BookKind.Comic, "cbr"),
    Cb7("CB7", BookKind.Comic, "cb7"),
    Cbt("CBT", BookKind.Comic, "cbt"),
    // Plain text is mostly game guides; a novel in a text file can be given another kind.
    Txt("TXT", BookKind.Guide, "txt"),
    Markdown("Markdown", BookKind.Document, "md", "markdown"),
    Html("HTML", BookKind.Document, "html", "htm", "xhtml");

    companion object {
        private val byExtension = entries.flatMap { f -> f.extensions.map { it to f } }.toMap()

        /** The format of a file named [name], or null for one kendai doesn't read. */
        fun of(name: String): BookFormat? =
            byExtension[name.substringAfterLast('.', "").lowercase()]
    }
}
