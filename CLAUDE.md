# CLAUDE.md

## Project

Kendai (見台) is a personal Android e-reader for books, game guides and comics. A kendai is the stand
a book rests on while it's read. Kendai reads from the device's own storage and, like tansu, from
the owner's SMB and SFTP servers, at home or remotely via Tailscale. Tailscale runs as the system
VPN through its own app, so kendai doesn't integrate with it; it just connects to a server by its
tailnet name or IP.

It exists because the readers on the Play Store are poorly written, dated-looking, paid or full of
ads, and none of them handles everything from an EPUB novel to a scanned game guide well. Kendai
should be the opposite:

- Clean, modern UI.
- Solid, well-structured code.
- No ads, tracking, analytics or paywalls.

## Product

Decided with the owner on 2026-11-24:

- No sign-in and no accounts: kendai stores nothing about the user. The only secrets are server
  credentials, kept encrypted on the device.
- Android only: phones, tablets and Android e-ink readers such as Boox, Bigme and Meebook. Kindle,
  Kobo and iPhone are out. Tablets get layouts of their own from the first screen, with two pages
  side by side in landscape. An e-ink mode drops motion and colour, turns pages instead of
  scrolling, and can refresh the whole screen every few pages to clear ghosting.
- Formats, everything readers commonly use: EPUB 2 and 3, FB2, DRM-free MOBI, AZW and AZW3, PDF,
  comics (CBZ, CBR, CB7 and CBT), and TXT, Markdown and HTML. A plain-text guide keeps a
  fixed-width font and its layout. Books with DRM are out, and so are DjVu, DOCX and CHM.
- Books live on the device first. SMB and SFTP servers, added as in tansu, bring more folders into
  the library.
- Where the reader left off matters most, in every format (the owner: "saving where a user left off
  is really important"). The place is saved as it's read, not only when the book closes, in the
  form each format needs: a spot in the text, a page with its zoom, a line. Books are known by their
  contents, so a moved or renamed file, or the same file on a server, keeps its place. The library
  opens on Continue reading.
- Reading: contents, search, bookmarks, highlights and notes, a dictionary through whatever
  dictionary app is installed, read-aloud with the device's own voices, themes and fonts, pages or
  scrolling, keeping the screen on, and split screen. Large game guides get all of it, and Back
  returns to where a jump started.
- The library has organisation tools, not one long list (the owner: "Some organisation tools will
  be great instead of seeing just all of them"):
  - A home page: Continue reading, pinned groups and recently added.
  - Groups the user makes, nested as deep as they like ("nested groups is cool"). A group can hold
    books from anywhere and a book can sit in several. Groups never move a file.
  - The real folder tree of the device and each server.
  - Automatic views by author, series and kind.
  - Reading status, set automatically and by hand, and favourites.
  - Search, filters and sorting, and hiding books or folders.
- Managing books works on one book or a selection; only viewing details is one at a time.
  - Groups, status, favourites, hiding and edited details live in kendai and never touch the files.
  - Deleting, renaming, moving or copying files, and writing details into a file, change the real
    files. Each asks once, saying what, how much and where. Deleting is for good; there's no
    trash.
- Clean up mends poor files:
  - Scans: straighten, crop, whiten, sharpen, black and white, rotate, reorder or remove pages,
    split two-page scans, add a text layer with on-device text recognition, and shrink.
  - Any book: fix its details and cover, and convert MOBI and FB2 to EPUB.
  - It runs as a job with progress, pause and cancel. It saves a new file or replaces the original,
    and the result keeps its place, bookmarks, highlights and groups. It comes after the reader
    itself.
- Reading places, bookmarks, highlights, groups and edited details sync between the user's devices
  through a folder they pick on one of their servers. When another device got further, kendai
  offers to jump there.
- Sideloaded only, never on a store, as with tansu: the owner shares the APK with people they know.
  Releases go on GitHub, and kendai updates itself from there; see
  [docs/releasing.md](docs/releasing.md). Keep the release signing key safe: losing it means
  everyone reinstalls.
- Besides the user's own servers, kendai only contacts GitHub: to look for a new version at most
  once a day (a check that can be turned off) and to fetch text-recognition languages when asked.
- The full spec is [docs/spec.md](docs/spec.md); the planned architecture is
  [docs/architecture.md](docs/architecture.md).

## Repository

- Public and MIT-licensed. Doesn't accept contributions, but forks are welcome.
- Every library has to fit the MIT licence: no GPL or AGPL code. That rules out MuPDF, which would
  read most formats on its own (decided 2026-11-24).
- Work directly on `main`. No feature branches or PRs, apart from the ones Dependabot opens.
- `release` is the branch kendai's users get updates from. It only moves forward to a commit on
  `main`, which publishes that version; nothing is committed to it directly. Every release also
  refreshes a downloads page on GitHub Pages, as for tansu, Kura and Maku, so the owner can send
  people one link. The page must look good and explain that kendai isn't on the Play Store on
  purpose, and how to install it. See [docs/releasing.md](docs/releasing.md).
- Build in iterations (develop, test, review, repeat), tracked in [docs/plan.md](docs/plan.md).
  Push a slice only when every check passes.
- LF line endings everywhere except Windows scripts (`*.bat`, `*.cmd`), enforced by
  `.gitattributes`. Editor settings are in `.editorconfig`.
- Leave the working tree clean: remove agent worktrees once merged, and the local copy of the
  design canvas once its changes are published.

## Tech stack

- Native Android: Kotlin + Jetpack Compose with Material 3 Expressive. Minimum SDK 30 (Android 11),
  so Android e-ink readers can run it; compile and target SDK 37. The main device is a Galaxy S24.
- One activity. ViewModels expose state as `StateFlow`; coroutines and Flow for async work;
  Navigation 3. Layouts adapt to the window's size class.
- Manual dependency injection to start; Hilt only if the app grows.
- Reading engines, all behind one book interface:
  - Readium's Kotlin toolkit for EPUB, and for FB2, MOBI, AZW3, Markdown and HTML once converted
    to the same kind of web publication.
  - PDFium, through Readium's adapter, for PDF.
  - Readium's image reader for comics, with archive readers for CBR, CB7 and CBT.
  - Kendai's own reader for plain-text guides.
- Storage backends, all behind one file-system interface, as in tansu: device storage (`java.nio`
  with All files access, `MANAGE_EXTERNAL_STORAGE`), SMB (smbj) and SFTP (sshj).
- Room for the library and reading state: places, bookmarks, highlights, groups and edited details.
  DataStore for settings and saved servers; SMB and SFTP passwords encrypted with an Android
  Keystore key.
- Coil 3 for covers. Read-aloud with Android's `TextToSpeech` in a media playback foreground
  service. Downloads, file changes and Clean up run as jobs in a data sync foreground service.
- For Clean up, when it's built: Tesseract for text recognition, Leptonica and OpenCV for cleaning
  pages, and PDFBox for writing PDFs.
- One `:app` module, Gradle Kotlin DSL with a version catalog. The same Gradle, AGP and Kotlin
  versions as tansu to start; versions live in `gradle/libs.versions.toml`.

## Commands

Run from the repo root (details in [docs/development.md](docs/development.md)):

- `./gradlew check assembleDebug`: every check CI runs (Spotless, detekt, Android Lint, unit tests)
  and a debug build. It must pass before every push.
- `./gradlew spotlessApply`: fix formatting.
- `./gradlew installDebug`: install on the emulator or a device.

## Design

- The look is **Washi** (和紙, handmade paper): warm paper and ink, a vermilion seal for the accent,
  Literata for headings and reading, Source Sans 3 for the interface, IBM Plex Mono for guides,
  hairline rules, soft corners and calm motion; warm charcoal in dark. The owner picked it from
  three directions (2026-11-24: "Go with washi").
- Boards: the [Kendai Design canvas](https://claude.ai/artifact/TwNJSttz17PRff9saV6fr8). Its tokens
  and rules go in `docs/design.md`, and the canvas is snapshotted in `docs/design/` by
  `scripts/design-snapshot.mjs`, as for tansu.
- Build screens to the boards and tokens; don't change the look unasked.

## Docs

[docs/README.md](docs/README.md) is the map. Docs explain how things work; this file records what
was decided. Update docs in the same commit as the code they describe. `docs/design/` is generated;
don't edit it by hand.

## Code style

Lean, simple, clean-to-read code comes first. Linting and formatting are strict: zero warnings, no
baselines.

- Formatting: ktfmt (kotlinlang style) via Spotless, which also covers `.gradle.kts`, XML, YAML and
  Markdown.
- Static analysis: detekt with zero issues allowed, plus the compose-rules plugin.
- Android Lint and the Kotlin compiler treat warnings as errors.
- CI fails on any finding, and a pre-commit hook runs Spotless and detekt.

## Principles

### Android (Kotlin + Compose)

1. Unidirectional data flow: one immutable `UiState` per screen, exposed as a `StateFlow`.
   Composables are stateless and take state and callbacks as parameters.
2. Clear layers: UI → ViewModel → repository → data source, with one owner per piece of data. The
   UI never touches smbj, sshj, `java.nio` or a book's bytes directly.
3. Main-safe structured concurrency: suspend functions move themselves to `Dispatchers.IO`. Work is
   scoped and cancellable; no `GlobalScope`.
4. Design for flaky networks: timeouts on every remote call, reconnects on dropped sessions, typed
   errors the UI can explain.
5. Respect the lifecycle: `collectAsStateWithLifecycle()`, survive process death, save the reading
   place as it changes, and run long work (downloads, Clean up, read-aloud) in a foreground
   service, never in a ViewModel.
6. Fast lists, big books: immutable UI models, stable lazy-list keys, no sorting or filtering during
   composition, and no book loaded whole when a page will do. Judge performance on release builds,
   and on a slow e-ink reader too.
7. Secure by default: credentials encrypted with an Android Keystore key and excluded from backups,
   SFTP host keys saved on first connect and checked after, no secrets in logs, minimal
   permissions.
8. One design system: Material 3 theme tokens, dynamic colour, dark theme, edge-to-edge, predictive
   back, layouts for phones and tablets, an e-ink mode, accessible touch targets and content
   descriptions.
9. Test at the right level: unit tests against sample books and a fake file system (converters,
   places, sync), backend tests against real Samba and OpenSSH servers, a few Compose UI tests for
   key flows. CI runs it all.
10. Simple until it hurts: one module, manual dependency injection, Jetpack first. Every extra
    library has to earn its place, and its licence has to fit MIT.

### General

1. Keep it simple.
2. You aren't gonna need it: build for today's requirements.
3. Don't repeat yourself, but wait until something repeats three times before abstracting it.
4. High cohesion, low coupling.
5. Make illegal states unrepresentable.
6. Fail fast and loudly; never swallow exceptions.
7. Write for the reader: clear names, small functions, comments that explain why.
8. Make it work, then right, then fast. Optimise only what measurements show is slow.
9. Small, safe steps: every commit builds and passes the tests.
10. Leave it better than you found it.
