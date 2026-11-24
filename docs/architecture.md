# Architecture

How kendai is planned to be built. It changes as the iterations land; the decisions themselves are
in [CLAUDE.md](../CLAUDE.md) and what kendai does is in [spec.md](spec.md).

## Layers

UI → ViewModel → repository → data source, with one owner for each piece of data.

| Package | What it holds |
| --- | --- |
| `files/` | The file-system interface and its device, SMB and SFTP backends, as in tansu |
| `library/` | Finding books in the library's folders, their details and covers, and the views |
| `books/` | Opening each format as something a reader can show, and the converters |
| `places/` | Fingerprints, where each book was left, bookmarks and highlights |
| `reader/` | The reading screens: Readium's readers, the text-guide reader, and the bars and panels they share |
| `sync/` | Keeping devices in step through the server folder |
| `jobs/` | Downloads, file changes and Clean up, in a foreground service |
| `cleanup/` | Mending pages, recognising text, and writing the new file |
| `speech/` | Read-aloud |
| `updates/` | The update check and install, as in tansu |
| `ui/` | One package per feature, and `ui/theme` |

## Opening a book

A book is known by its fingerprint, and has one or more copies: a file on the device or on a
server. A device file is opened where it is. A server file is downloaded to kendai's cache first,
with progress and Cancel, and kept there; "Keep offline" pins it.

| Format | How it's read |
| --- | --- |
| EPUB | Readium's streamer and EPUB reader, reflowable or fixed layout |
| FB2, MOBI, AZW, AZW3, Markdown, HTML | Converted on the device into a web publication in the cache (XHTML chapters, styles, images, contents), then read by the same EPUB reader |
| PDF | Readium's PDF reader with its PDFium adapter: pages, zoom, the outline, text search and selection |
| CBZ, CBR, CB7, CBT | Readium's image reader. CBZ opens directly; the others are read through archive readers behind Readium's container interface, without unpacking whole archives |
| TXT (guides) | Kendai's own reader, described below |

- **FB2** is XML: its sections become chapters, its binaries images, its description the details.
- **Kindle files** are written up from the format's own documentation: PalmDOC and HUFF/CDIC
  decompression, KF8's skeletons and fragments reassembled into HTML, older MOBI markup and its
  `filepos` links turned into HTML, and EXTH records for the details and cover.
- **Markdown** goes through commonmark; **HTML** is cleaned up with jsoup into a single chapter.
- **Archives:** Apache Commons Compress reads 7z and tar. The RAR reader is chosen in the comics
  iteration, since RAR5 support differs between them.
- **Text guides** are indexed once into line offsets, kept in the cache, and shown as a lazy list of
  lines in a fixed-width font sized to the guide's width. Sections come from the guide's own table
  of contents and section codes. Search streams through the file. Prose in a text file can switch
  to the reflowed reader by being converted like Markdown.
- **Licences:** Readium is BSD, PDFium BSD and Apache, Commons Compress Apache, commonmark BSD and
  jsoup MIT. All fit kendai's MIT licence.

## Where you left off

- **Fingerprints:** a hash of the file's size and samples of its contents (its start, its end and a
  few points between). A 1 GB PDF is fingerprinted in milliseconds, and the same file anywhere gets
  the same fingerprint.
- **Places:** for the Readium formats, Readium's locator (the chapter, the progression through it
  and the book, and a few words of text either side); for text guides, the line. Each place records
  when it was made and on which device.
- **Saving:** the reader's ViewModel writes the place to Room a moment after each change, and when
  the book is left. Room's write-ahead log makes each write survive the process ending. A place is
  never held only in memory.
- **Back after a jump:** the reader keeps a history of places, so Back after a jump to the
  contents, a search result, a link or a bookmark returns to where it started.
- **After Clean up:** a new file inherits its original's places, bookmarks, highlights and groups.
  Pages that stay the same keep their places, and a reordered or converted book has its places
  mapped across.

## The library

- **Room** holds the books (fingerprint, details, kind, whether there's a cover, and a plain-text
  guide's first lines), their copies (path, the library folder it was found in, size and modified
  time) and the library's folders. Statuses, favourites, hidden books and folders, groups, places,
  bookmarks, highlights and edited details join as their iterations land. Each version of the
  schema is kept in `app/schemas`, so later ones can be migrated to.
- **Finding books:** `Library` walks the device's folders with `java.nio` each time kendai opens and
  when the list is pulled down, skipping hidden folders and the storage's `Android` folder. One
  look runs at a time; asking during one queues one more, so a folder added meanwhile is never
  missed. A file whose size and modified time haven't changed isn't read again, a gone one is
  dropped, and a folder that can't be read, such as an SD card taken out, keeps what was found in
  it. Server folders will be listed when they're opened and on pulling down.
- **Details and covers** are read once per fingerprint: the EPUB package, FB2's description and
  binary cover, a CBZ's `ComicInfo.xml` and first page, a PDF's first page drawn by Android's
  renderer, a Markdown heading, an HTML title and a text guide's first lines, falling back to the
  file name. Kindle EXTH records and the PDF's own details come with their readers. Covers are kept
  as small JPEGs in kendai's files and shown with Coil.
- **Views** are worked out from the library's books off the main thread, never during composition:
  the folder view from the copies' paths, and the home page from Room's most recently added.
- **Folders** are chosen in the first run from the ones at the top of the storage that hold books,
  or anywhere with Android's folder picker, whose answer is turned back into a path. A folder inside
  one the library holds isn't added twice, and one the library holds inside a new one gives way to
  it.

## Screens and navigation

One activity. The first run shows until the library has folders; then the three places, each with
its own Navigation 3 back stack, saved with the state of its screens and ViewModels. A screen opened
from a place, such as Settings › Library folders, covers the bar or rail; back from a place's first
screen goes to the library. The folder view's way down is the library screen's own state, so back
goes up a folder before it leaves.

## Files

The file-system interface and its backends come from tansu, with their timeouts, reconnects and
errors the UI can explain. Every change to a real file (delete, rename, move, copy, or Clean up
replacing a book) goes through it. A replacement is written to a hidden file first and renamed over
the original only once it's whole.

## Sync

- **The folder:** a `kendai` folder inside the one the user picks, holding a JSON file per book,
  named by its fingerprint, and one file for the groups.
- **Items:** each place, bookmark, highlight, membership, status and edit has an id, when it
  changed and on which device. Deleting leaves a marker, so it reaches the other devices.
- **Merging:** item by item, the newest wins. A place from another device that's further on is
  offered, never just applied.
- **When:** as a book opens and closes, a while after a change, when kendai starts, and on pulling
  down the library. Changes made offline wait for the server.

## Jobs

Downloads, file changes and Clean up run in a data sync foreground service, as in tansu: progress in
the app and a notification, pause and cancel, and surviving Android closing the app.

## Read-aloud

Readium's speech reader drives Android's `TextToSpeech` for the Readium formats and highlights what
it's reading; the guide reader speaks line by line. It runs in a media playback foreground service
with a media session, for the notification and headset buttons.

## Clean up

Pages come from the PDF through PDFium, or from the comic archive. Leptonica and OpenCV mend them
(straightening, cropping, evening out the background, black and white, specks, splitting spreads),
Tesseract recognises text and writes the invisible text layer, and PDFBox writes the new PDF:
pages, their order, details and compression. Comics are zipped again as CBZ. Each fix can be
previewed on one page before it runs on all of them as a job. Tesseract's language data comes from
its repository on GitHub when the user asks for a language. Tesseract and OpenCV are Apache,
Leptonica BSD, and PDFBox's Android port Apache.

## Tablets and e-ink

- **Size classes** pick the layouts: Material 3's adaptive navigation (a bar or a rail) and
  list-detail panes. In landscape, Readium's EPUB reader shows two columns, and PDFs and comics
  show spreads.
- **E-ink mode** is a theme of its own (black and white, no shadows), motion set to nothing through
  a composition local, and paged lists. It's switched on for known e-ink makers (Onyx, Bigme,
  Meebook and others), with a setting to override. Page-turn buttons arrive as page and volume key
  events. The optional full refresh draws a black frame for an instant between pages.

## Updates

As in tansu: at most once a day, kendai asks GitHub for the latest release. A newer one can be
downloaded with progress and Cancel, is checked against its checksum, and is handed to Android's
installer.

## Servers and secrets

As in tansu: server details in DataStore, passwords and keys encrypted with an Android Keystore key
and kept out of backups, and SFTP host keys saved on first connect and checked after.
