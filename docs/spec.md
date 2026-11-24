# Spec

What kendai does and what it leaves out. How it looks is in [design.md](design.md), how it's built
is in [architecture.md](architecture.md), and what was decided is in [CLAUDE.md](../CLAUDE.md).

## The idea

An e-reader that reads everything and never loses your place: novels, scanned game guides, comics
and documents, from the device or your own servers, with no sign-in and no ads. It's made for one
person and the people they share it with, on Android 11 or newer. The main device is a Galaxy S24;
tablets and Android e-ink readers get layouts of their own.

## Devices

| Device | What's different |
| --- | --- |
| Phone | The reference: one page at a time, a bottom navigation bar |
| Tablet | A navigation rail; contents, search and bookmarks in a side panel; two pages side by side in landscape |
| E-ink reader | E-ink mode: no motion, black and white, page turns instead of scrolling, the device's page-turn buttons, an optional full refresh |

Kindle, Kobo and iPhone can't run Android apps and are out.

## Formats

| Kind | Formats | Notes |
| --- | --- | --- |
| Ebooks | EPUB 2 and 3, FB2, MOBI, AZW, AZW3 | Reflowed to the reader's font and size. Kindle files only without DRM. |
| PDF | PDF | Fixed pages, zoomed and fitted; scans can be given a text layer by Clean up |
| Comics | CBZ, CBR, CB7, CBT | Page by page or two pages at a time; right to left for manga |
| Text | TXT, Markdown, HTML | A plain-text guide keeps a fixed-width font and its layout; prose can be reflowed instead |

Books with DRM are out, from Kindle, Adobe or anyone else, and so are DjVu, DOCX and CHM.

## Where books come from

| Place | What it is | Needs |
| --- | --- | --- |
| Device | The device's own storage | All files access |
| SMB server | A share on a NAS or PC, over SMB 2 or 3 | Host, share, optional folder, username and password (or guest) |
| SFTP server | Any SSH server | Host, port (22), optional folder, username, and a password or private key |

Servers are added and kept exactly as in tansu: credentials encrypted, SFTP host keys checked, and
every problem explained. The library holds the folders the user adds, on the device and on
servers. Kendai never shows the rest of the device.

## Flows

These are the workflows the design canvas draws, one board each.

### 1. First run

1. Kendai says why it needs All files access: to read books where they are, without copying them.
2. It finds the folders on the device that hold books, starting with Books, Download and
   Documents, and the user ticks the ones to add. More can be added any time.
3. The library opens on its home page, with a quiet card offering to add a server.

### 2. Add a server

1. As in tansu: SMB or SFTP, the details, and a check that they work before anything is saved.
2. Then the user picks which folders on the server join the library.

### 3. The library

1. The home page: Continue reading (each book with its cover and how far through it is), pinned
   groups, and books added recently.
2. Groups: the user's own, nested, each opening like a folder, its groups first and then its books.
3. Folders: the real folder tree of the device and each server, showing only books and the folders
   that hold them.
4. Automatic views: by author, by series in reading order, and by kind (books, comics, guides,
   documents). Kind is guessed from the format and can be changed in a book's details.
5. Search by title, author or series; filters for kind, format, reading status and place, which
   combine; sorting by title, author, last read, date added or progress; a cover grid or a compact
   list.

### 4. Open a book and read

1. A book opens where it was left, every time, without asking.
2. The page fills the screen. A tap in the middle shows the bars: the title, contents, search,
   bookmarks, appearance and how far through the book the reader is. Taps at the sides, swipes and
   the volume or page-turn buttons turn pages.
3. Pages or scrolling, per book, with a default for each kind.

### 5. Reading tools

- Contents, from the book's own: EPUB and Kindle navigation, PDF outlines, FB2 sections, Markdown
  headings, and a text guide's own table of contents.
- Search the whole book, with results listed in context as they're found, and Cancel.
- Bookmarks, named if the reader likes.
- Highlights in a few colours, each with an optional note, listed and exportable as text.
- Look up a word in whatever dictionary app is installed.
- Read aloud with the device's voices, following along on the page, with a sleep timer and controls
  in the notification.
- Appearance: light, sepia, dark and black themes; fonts, size, spacing, margins and alignment for
  reflowed books; fit, zoom and two-page spreads for PDFs and comics.
- Keep the screen on while reading.
- After any jump (contents, search, a link, a bookmark), Back returns to where it started.

### 6. Game guides

Large guides get every reading tool, and:

- A plain-text guide keeps its fixed-width layout, ASCII art and all, sized to fit the screen's
  width. Its sections come from the guide's own table of contents, including section codes like
  `[WLK01]`, so a jump goes straight there.
- A long PDF guide gets its outline, page thumbnails and fast search, and opens on the right page
  however big it is.
- Split screen works, so a guide can sit beside a game; layouts hold up at any size.

### 7. Organise

1. Make a group anywhere, and groups inside groups.
2. Add books to groups, or move them from one group to another, without moving a file.
3. Pin groups to the home page; rename, move and delete groups. Deleting a group that holds others
   asks once whether to delete them too. Books are never deleted with a group.
4. Mark books new, reading or finished, and star favourites.
5. Hide books, or whole folders, from the library without deleting anything.

### 8. Manage books

One book, or a selection made by long-pressing and tapping. "Select all" takes everything in the
view, so a filter and select-all make a bulk action.

| Action | What it does | Works on |
| --- | --- | --- |
| Add to group, move to group, remove from group | Changes groups only; the files stay | many |
| Mark new, reading or finished; favourite | Changes the book's status | many |
| Hide | Keeps books out of the library | many |
| Clear reading data | Forgets the place, bookmarks and highlights, after asking | many |
| View details | Title, author, series, description, format, size, pages, where it's stored, date added, last read, progress and groups | one |
| Edit details | Title, author, series and number, language, kind and description. Several books can share an author or a series, numbered in order. | many |
| Change cover | A page of the book, or an image | one |
| Export highlights and notes | As a text file, or shared | many |
| Delete | Deletes the files for good | many |
| Rename | Renames the files, or names them from their details, like "Author – Title" | many |
| Move or copy to… | Moves or copies the files to another folder or server | many |
| Keep offline | Downloads server books so they open without a connection, or frees the space again | many |
| Share | Sends the files to another app | many |
| Open with… | Opens the file in another app | one |
| Show in folder | Opens the folder view where the file lives | one |
| Clean up | Opens Clean up | one, or many to give several scans a text layer |
| Convert to EPUB | For MOBI and FB2 | many |

Edited details are kept in kendai, so they work for every format and on read-only servers. For
EPUB, PDF, FB2 and comics, they can also be written into the file. Everything that changes a real
file asks once, saying what, how much and where.

### 9. Clean up

1. Pick the fixes:
   - Scans and comics: straighten, crop borders and margins, whiten the paper and remove
     shadows, sharpen and remove specks, turn black and white, rotate, reorder and delete pages,
     drop blank pages, split two-page scans.
   - Recognise text: add an invisible text layer to a scan, so search, selection, highlights and
     read-aloud work.
   - Shrink: recompress oversized scans at a sensible resolution.
   - Details: fix the title, author, series and cover inside the file.
   - Convert: MOBI and FB2 to EPUB, CBR to CBZ, garbled text guides to UTF-8.
2. Preview the result on any page, before and after.
3. It runs as a job, with progress, pause and cancel.
4. Save it as a new file, next to the original or anywhere on the device or a server, or replace
   the original. Replacing asks once, and the original is swapped only when the new file is whole.
5. The new file keeps the original's place, bookmarks, highlights and groups.

### 10. Sync, and when something's wrong

- Sync: pick a folder on one of the servers, and kendai keeps places, bookmarks, highlights,
  groups, statuses and edited details in step between devices through it.
- A server that can't be reached says why, as in tansu, and its books that are kept offline still
  open.
- A file that can't be read says so, and offers Open with… and Clean up where they might help.
- A book whose file has gone stays in its groups, marked missing, until it turns up again or the
  user removes it.

### 11. Settings and updates

Settings, then a new version from GitHub with what changed, as in tansu.

## Where you left off

The most important thing kendai does.

- **Saved as you read**, on every page turn and a moment after scrolling stops, and when the book is
  left. A crash, or Android closing kendai, never loses the place.
- **The right place for each format:**
  - Reflowed books (EPUB, FB2, Kindle files, Markdown, HTML): the spot in the text, kept with a few
    words around it, so it survives a new font, size, spacing, screen or two-page layout.
  - PDF: the page, with its zoom and where on the page the reader was.
  - Comics: the page.
  - Text guides: the exact line.
- **Known by contents, not location.** A book moved or renamed, on the device or a server, or the
  same file in two places, keeps one place.
- **Always the reader's own place:** a book opens there without asking. After a jump, the place is
  where the reader went, and Back returns to where the jump started.
- **Visible everywhere:** progress on every cover, Continue reading on the home page, and in the
  reader, the page or percentage, and the time left in the chapter.
- A finished book offers to start again.

## Sync between devices

- The user picks one folder on one of their servers. Every device pointed at it shares places,
  bookmarks, highlights and notes, groups, statuses, favourites, hidden books and edited details.
- Each change carries when it was made and on which device; the newest wins, item by item, and
  deletions are remembered.
- It syncs when a book opens and closes, a while after the place changes, when kendai starts, and
  on pulling down the library. Changes made offline wait until the server is back.
- Opening a book another device got further in offers to jump there: "Your tablet got to page 214".

## Jobs and progress

Downloads for offline reading, file changes on servers, and Clean up are jobs. As in tansu, each
one says what it's doing, how far it got and how long is left, in the app and in a notification.
Downloads and Clean up can be paused, and any job can be cancelled. Jobs keep going when the user
leaves kendai, and a download that breaks off carries on where it was.

## E-ink mode

On by itself on known e-ink readers, and a switch in Settings:

- No animation anywhere: pages, sheets and screens appear at once.
- Black on white, with no greys that ghost, no shadows and no colour, even on colour e-ink.
- Page turns, not scrolling, in the library as well as in books.
- The device's page-turn buttons and volume keys turn pages; tap zones are large.
- An optional full refresh every few pages, to clear ghosting.
- Nothing moves while idle: no spinners left running, no clocks ticking on screen.

## Tablets

- A navigation rail instead of the bottom bar, and a library grid that fills the width.
- Contents, search, bookmarks and highlights open in a side panel beside the page.
- Landscape shows two pages side by side: reflowed books in two columns, PDFs and comics as spreads.
- Everything also works in split screen and in small windows.

## Settings

- Appearance: system, light or dark; dynamic colour; e-ink mode (automatic, on or off).
- Reading defaults: theme, font, size, spacing, margins, alignment, pages or scrolling for each
  kind, tap zones, volume keys turning pages, keeping the screen on.
- Library: its folders, hidden books and folders, the default view and sort.
- Servers: add, edit and remove, as in tansu.
- Sync: the folder, when it last synced, and Sync now.
- Text recognition: the languages it knows, downloaded from GitHub when asked.
- About: the version, updates (checked once a day unless turned off) and the licences.

## Security and privacy

- No accounts, no analytics, no crash reporting, no ads.
- Server passwords and keys are encrypted with an Android Keystore key and never leave the device,
  and they're kept out of backups.
- Books from servers are cached in kendai's own storage, out of other apps' reach.
- Kendai talks only to the user's servers and to GitHub: for updates, and for text-recognition
  languages when the user asks for one. A dictionary lookup goes to the app the user picked;
  read-aloud uses the device's own speech engine.
- Minimal permissions: All files access, network, notifications for jobs, and a foreground service
  for jobs and read-aloud.

## Quality bar

- A 500 MB PDF or a 5 MB text guide opens in a moment, on the page it was left on.
- Pages turn instantly. Search shows results as it finds them, and can be cancelled.
- The library stays fast with thousands of books.
- It's usable on a slow e-ink reader, not just on the S24.
- Text at 4.5:1 contrast or better, 48 dp touch targets, and TalkBack labels throughout.

## Distribution

As tansu: sideloaded, never on a store. Releases are on GitHub, with a downloads page that explains
installing it, including on e-ink readers, and kendai updates itself. See
[releasing.md](releasing.md).

## Not in version 1

- Books with DRM, and DjVu, DOCX and CHM.
- Cloud services such as Google Drive or Dropbox, and online catalogues (OPDS, such as Calibre's).
- Audiobooks.
- Drawing on pages, and editing a book's text.
- Flattening curved pages, and AI upscaling of low-resolution comics.
- iPhone, Kindle and Kobo.
