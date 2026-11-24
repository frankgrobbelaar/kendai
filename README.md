<div align="center">

# kendai

**見台 · a calm e-reader for your books, guides and comics**

A kendai is the stand a Japanese book rests on while it's read. This one holds the books on your
phone, tablet or e-ink reader, and the ones on your own servers, and always opens where you left
off.

[What's planned](docs/spec.md) · [How it will be built](docs/README.md)

[![MIT license](https://img.shields.io/badge/license-MIT-A89A8A)](LICENSE)
[![Android 11 or newer](https://img.shields.io/badge/Android-11%2B-5E6B4E)](docs/spec.md)

</div>

## What it will do

- Read everything readers commonly use: EPUB, FB2, MOBI and AZW3, PDF, CBZ, CBR, CB7 and CBT
  comics, and TXT, Markdown and HTML.
- Remember where you left off in every book, whatever its format, and carry it between your devices.
- Bring in folders from your own SMB and SFTP servers, at home or over Tailscale.
- Organise the library with nested groups that never move a file, or browse it by folder, author,
  series and kind.
- Make large game guides easy: jump to a section, search, bookmark, keep the screen on, and keep a
  plain-text guide in its fixed-width layout.
- Clean up poor scans: straighten, crop and sharpen them, and make their text searchable.
- Work on phones, tablets and Android e-ink readers.
- Update itself from GitHub, checking once a day unless you turn that off.

## What it doesn't do

No sign-in, accounts, ads or tracking, and no books with DRM. Server passwords stay on your device,
encrypted. Besides your own servers, kendai only talks to GitHub.

## Status

Being built in iterations, tracked in [docs/plan.md](docs/plan.md); the
[spec](docs/spec.md), the [architecture](docs/architecture.md) and the [design](docs/design.md)
are in [docs/](docs/README.md). Kendai will be shared the way tansu is: not
on any store, on purpose, with a downloads page that explains installing it.

## License

[MIT](LICENSE). Kendai is a personal project and doesn't accept contributions, but you're welcome
to fork it.
