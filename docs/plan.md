# Build plan

Kendai is built in small iterations, as tansu was. Each one goes through the same loop before it
lands on `main`:

1. **Develop** the slice, test first where it pays off.
2. **Test**: formatting, detekt, Android Lint, unit tests and a build all pass. Screens are checked
   on the emulator against the design boards, on a phone, a tablet and in e-ink mode.
3. **Review**: a fresh review of the change, looking for bugs and for drift from the principles in
   [CLAUDE.md](../CLAUDE.md). Findings are fixed.
4. **Repeat** until the slice is right, then commit and push.

| # | Iteration | What it delivers | Status |
| --- | --- | --- | --- |
| 0 | Design | Three design languages to choose from, then the chosen one and every workflow on the design canvas | Under way: Washi is chosen; the workflow boards are being drawn |
| 1 | Foundation | The Gradle project, linting and formatting, CI, the theme, and the app shell, with phone, tablet and e-ink layouts | |
| 2 | The library on the device | All files access, picking folders, finding books, their details and covers, the home page, list and grid | |
| 3 | Reading EPUB | Readium, the place saved as you read, contents, appearance, pages or scrolling | |
| 4 | PDF and comics | PDFium with zoom, fit and spreads; CBZ, CBR, CB7 and CBT | |
| 5 | Releases and updates | The `release` branch and its workflow, signing, the downloads page, and the in-app update | |
| 6 | Text and the converters | The text-guide reader, Markdown, HTML and FB2 | |
| 7 | Reading tools | Search, bookmarks, highlights and notes, the dictionary, read-aloud, keeping the screen on, Back after a jump | |
| 8 | Servers | SMB and SFTP as in tansu, server folders in the library, keeping books offline | |
| 9 | Organise | Nested groups, folders, automatic views, status and favourites, search, filters and sorting, hiding | |
| 10 | Manage books | Selection, details and editing, delete, rename, move and copy, share and open with | |
| 11 | Kindle files | MOBI, AZW and AZW3 | |
| 12 | Sync | Places, bookmarks, highlights, groups and edits kept in step through a server folder | |
| 13 | Tablets and e-ink | Checking and polishing both on real devices | |
| 14 | Clean up | Mending scans, recognising text, shrinking, fixing details and converting; saving a new file or replacing the old | |
| 15 | Polish | Accessibility, performance, and the small things | |

Releases come early, at iteration 5, so kendai can go onto a tablet or an e-ink reader for testing
from then on.

## Left for later

Nothing yet.
