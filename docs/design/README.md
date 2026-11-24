# Kendai's design pages

Snapshots of the [Kendai Design canvas](https://claude.ai/artifact/TwNJSttz17PRff9saV6fr8) as plain
pages: open [index.html](index.html) in a browser and click through. The canvas is the source of
truth (the owner's, and private); these pages are the shared copy.

| Board | Page |
| --- | --- |
| Kendai workflows: every flow, with links | [index.html](index.html) |
| Washi 和紙: colour, type, shape and motion, and the library in light and dark | [washi.html](washi.html) |
| The three directions, Washi chosen, and what they share | [directions.html](directions.html) |
| 1 · First run | [first-run.html](first-run.html) |
| 2 · Add a server | [add-server.html](add-server.html) |
| 3 · The library | [library.html](library.html) |
| 4 · Read a book | [read-book.html](read-book.html) |
| 5 · Reading tools | [reading-tools.html](reading-tools.html) |
| 6 · Game guides | [game-guides.html](game-guides.html) |
| 7 · Organise | [organise.html](organise.html) |
| 8 · Manage books | [manage-books.html](manage-books.html) |
| 9 · Clean up and jobs | [clean-up.html](clean-up.html) |
| 10 · Sync, and when something's wrong | [problems.html](problems.html) |
| 11 · Settings and updates | [settings.html](settings.html) |
| 12 · Tablets and e-ink | [tablets-eink.html](tablets-eink.html) |

Phone screens are drawn at the Galaxy S24's 360 × 780 dp and tablets at 1040 × 650 dp in
landscape, with public-domain books, a made-up game guide, and made-up servers. Fonts load from
Google Fonts, so the pages need the internet to look exactly right.

## The Kit

The canvas's Kit boards are the parts every screen reuses: the top bar, the navigation bar, the
chips, a book card, a book row, a group tile, the reader's page bar, a job card and the snackbar.
Boards mount them with `<dc-import>`, so a change to a Kit board shows up on every screen. The
snapshot draws them into each page; they have no page of their own.

## Building from these

The markup is plain HTML with inline styles, so every size, colour and spacing can be read off
directly. The values match the tokens in [design.md](../design.md) one for one, and each Kit part
maps to one Compose component.

## Refreshing the snapshot

The pages are generated; don't edit them. When the canvas changes, ask Claude to read it (its
`project/` files) into a folder, then:

```powershell
node scripts/design-snapshot.mjs --design <folder>
```
