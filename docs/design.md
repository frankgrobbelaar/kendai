# Design

How kendai looks and behaves. The
[Kendai Design canvas](https://claude.ai/artifact/TwNJSttz17PRff9saV6fr8) holds the boards (the
owner's, and private); [design/](design/README.md) has them as pages you can open locally.

## Washi 和紙

Material 3 Expressive through Compose Material 3, with Washi's tokens on top. Washi is named after
the handmade paper Japanese books are printed on: quiet and bookish, with warm paper and ink, a
vermilion seal for the accent, Literata for headings and reading, hairline rules instead of boxes,
and calm motion, like turning a page. The owner picked it over Sumi (ink: focused, minimal and dark
first) and Hanabi (fireworks: bold and playful); all three are summarised on the canvas's
directions board.

### Colour

Material 3 colour roles. Dynamic colour is an option, off by default: it swaps the app's colours
for the wallpaper's, and the reading themes, type, shapes and motion stay.

| Token | Role | Light | Dark | Used for |
| --- | --- | --- | --- | --- |
| Paper | Surface | `#F5EFE4` | `#1D1A16` | The screen |
| Sheet | Surface container | `#FBF7F0` | `#27231E` | Sheets, cards, fields, group tiles |
| Rule | Outline variant | `#DCD2C2` | `#3A342C` | Hairlines, chip borders, dividers |
| Ink | On surface | `#26211C` | `#EEE6DA` | Main text, the primary button, the selected chip |
| Ink 2 | On surface variant | `#625649` | `#B5A898` | Secondary text and icons |
| Seal | Primary | `#A9402B` | `#E58A6E` | Progress, the current place, links, the destructive button |
| Seal soft | Primary container | `#F2D9CF` | `#4A2A20` | Highlights and selection |

Dark is a warm charcoal, never black, with the seal lightened so it still reads. Text keeps at
least 4.5:1 contrast against its background in both modes.

**Reading themes** apply to the page of a book, separately from the app's light or dark:

| Theme | Page | Text |
| --- | --- | --- |
| Paper | `#F5EFE4` | `#26211C` |
| Sepia | `#EADCC2` | `#3A2E21` |
| Dusk | `#2B2621` | `#E8DDCC` |
| Night | `#0E0D0C` | `#CDC3B4` |

**Highlights** come in four colours: seal `#F2D9CF`, ochre `#F1E1BD`, sage `#DDE3D0` and slate
`#D9DEE7`, each with a darker edge so they're told apart by more than hue.

**E-ink** uses only black `#000000` and white `#FFFFFF`: 2 px black rules, no greys, no shadows
and no colour, even on colour e-ink.

### Type

Three open-source families (OFL), bundled with the app. Numbers use tabular figures.

| Family | Used for |
| --- | --- |
| Literata | Screen titles, section headings, book titles, and the text of books |
| Source Sans 3 | The interface: bars, buttons, lists, labels |
| IBM Plex Mono | Plain-text guides, section codes, server keys |

| Style | Family | Size | Weight | Example |
| --- | --- | --- | --- | --- |
| Screen title | Literata | 26 | 600 | Library |
| Section | Literata | 18 | 600 | Continue reading |
| Bar title | Source Sans 3 | 16 | 700 | Moby-Dick |
| Body | Source Sans 3 | 15 | 400 | Natsume Sōseki · EPUB · NAS |
| Secondary | Source Sans 3 | 12–13 | 400 | 6 min left in chapter |
| Overline | Source Sans 3 | 12 | 700, tracked, capitals | GROUPS INSIDE |
| Book text | Literata | 17 (a setting) | 400, line height 1.62, justified, hyphenated | Call me Ishmael. |
| Guide text | IBM Plex Mono | 11.5–12 (fits the guide's width) | 400, line height 1.55 | [WLK03] THE SUNKEN ARCHIVE |

Readers can pick other reading fonts (Atkinson Hyperlegible and IBM Plex Serif to start); the
interface stays in Source Sans 3.

### Shape and motion

- Corners: 4 to 8 dp for chips, buttons, fields and cards; 16 dp at the top of sheets; covers 2 to
  3 dp, with a soft paper shadow in light.
- Hairline rules divide lists and sections instead of boxes; the current item gets a 2 dp seal
  rule (under a tab or a navigation label, beside a chapter in the contents).
- Motion is calm: fades and short slides of 200 to 300 ms with no overshoot. Pages turn by
  sliding, fading or not at all, as the reader chooses.
- Icons are thin rounded lines (1.6 stroke, 2 in e-ink).

## Components

The canvas's Kit boards are the parts every screen reuses; each becomes one Compose component.

| Part | What it is |
| --- | --- |
| Top bar | Back, Close or nothing on the left; a Literata screen title, or a smaller title with a subtitle (a chapter, a place); actions on the right |
| Navigation bar | Library, Jobs and Settings, the current one with a seal rule under its label, a count on Jobs |
| Chips | The library's views (Home, Groups, Folders, Authors, Series) or filters; the selected one in ink |
| Book card | A cover (printed, a guide drawn from its own first lines, or a PDF page), the title, a hairline progress rule and how far through |
| Book row | A cover or folder tile, the title, details and an optional progress rule, with More, a chevron or a check |
| Group tile | Four covers from the group, its name and count, and a pin when it's pinned to the home page |
| Page bar | The reader's bottom bar: the progress rule with its knob, the page and time left, and two actions such as read-aloud and night |
| Job card | What a job is doing, a progress bar, how far and how long, and pause, resume, cancel or try again |
| Snackbar | A dark strip for what just happened, with Undo or another single action |

Dialogs ask one question with at most three answers, the safe one first. Sheets rise for choices
about the current screen (appearance, sort and view, contents, a group's menu).

## Rules every screen follows

1. **Material 3 Expressive underneath.** Compose's components, accessibility and motion; Washi
   only sets colour, type, shape and motion.
2. **Light, dark and the reading themes.** The app follows the device, with an override in
   Settings; each book has its own theme.
3. **Where you left off, everywhere.** Progress on every cover and row, Continue reading first in
   the library, and every book opening at its place.
4. **Organising never moves a file.** Groups, statuses and edited details live in kendai; what
   changes a real file says so and asks once.
5. **Phones, tablets and e-ink.** A bottom bar on phones and a rail with side panels on tablets;
   e-ink mode in black and white without motion.
6. **Readable first.** Text at 4.5:1 or better, 48 dp touch targets, TalkBack labels, and kinds
   told apart by shape and words before colour.

## Reading

1. The page fills the screen. A tap in the middle shows the bars; taps at the sides, swipes and the
   volume or page-turn buttons turn pages.
2. A book opens at its place every time, without asking.
3. Back after any jump (contents, a search result, a link, a bookmark) returns to where it started,
   and says where: "Back to WLK02 · line 1,204".
4. Plain-text guides keep their fixed-width layout and are never reflowed.
5. Highlights sit on the text in their colour; the selection toolbar floats above it and never
   covers it.
6. The screen stays on when the reader asks, per kind, and the bars say so.

## Animation

1. Motion explains change; it never decorates.
2. Short and calm: 100 to 200 ms for small elements, 200 to 300 ms for screens and sheets.
3. One motion system: the same curves everywhere, interruptible, picking up a swipe's speed.
4. Spatial continuity: a book opens out of its cover and closes back into it; deeper slides one
   way, back the other.
5. Show what changed: books added to a group slide in, removed ones fade out.
6. Gestures lead: predictive back previews the previous screen, and drags follow the finger.
7. Animate cheap properties (move, scale, fade), never layout on every frame.
8. Never block input during a transition.
9. With "Remove animations" on, or in e-ink mode, everything is instant, and no meaning depends on
   motion.
10. Measure on release builds, and on a slow e-ink reader.

## Navigation

1. Three top-level places, Library, Jobs and Settings, in a bottom bar on phones and a rail on
   tablets.
2. The library's views (Home, Groups, Folders, Authors, Series) are chips under the top bar, each
   keeping its own scroll position, sort and view.
3. Back in the library goes up a group or a folder, then to the view's top, then out.
4. A book is a place of its own: Back leaves it for wherever it was opened from, unless a jump
   inside the book can be undone first.
5. Each tab keeps its history, even after Android closes the app.
6. Entry points land in context: a job's notification opens that job, and a book opened from
   another app opens at its place.
7. Short tasks with Cancel; no dialogs on top of dialogs.
8. Navigation is state: one typed back stack, saved and testable.

## Experience

1. Fast, always: books open at once at their place, covers come from the cache, and every tap gets
   instant feedback.
2. Honest progress: what, how far and how long, with pause, cancel and a summary at the end.
3. Errors that help: the problem, what kendai already tried, and the next step.
4. Every state is designed: loading, empty, error, offline, no permission and partly failed.
5. Confirm only what can't be undone; reversible actions get Undo.
6. Never lose a place, a highlight or a file: places are saved as they change, a replaced file is
   swapped only when the new one is whole, and deleting says what, how much and where.
7. Plain words with human units: "1.4 GB", "6 min left in chapter", "Yesterday".
8. Respect the reader: no sign-in, ads or tracking, and permissions asked for when needed, with a
   reason.

## The design pages

[design/](design/README.md) holds the canvas as plain pages, written by
`scripts/design-snapshot.mjs`. They're generated, so don't edit them. When the canvas changes, ask
Claude to read it (its `project/` files) into a folder, then:

```powershell
node scripts/design-snapshot.mjs --design <folder>
```
