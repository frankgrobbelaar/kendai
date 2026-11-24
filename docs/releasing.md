# Releasing

Kendai is never on a store. It's sideloaded, as tansu is: the owner shares the APK with people they
know, and the app updates itself from GitHub Releases. This is how it will work once iteration 5
builds it.

## Versions

- `versionName` is semantic: `1.2.0`. It lives in `gradle.properties` as `kendai.version`.
- `versionCode` is derived from it (`major × 10000 + minor × 100 + patch`), so it always rises.
  Android refuses an update whose code isn't higher than the installed one.
- Every change someone would notice gets one plain line under "Unreleased" in `CHANGELOG.md`, in
  the same commit. A release turns that section into its notes.

## The signing key

Android installs an update only if it's signed with the same key as the app already installed.

- One release keystore of kendai's own, not tansu's, made once in iteration 5 and kept outside the
  repo, with a backup somewhere safe: `%USERPROFILE%\kendai-release-key\` on the owner's PC, with a
  note on keeping it.
- CI gets it as repository secrets: `KENDAI_KEYSTORE_BASE64`, `KENDAI_KEYSTORE_PASSWORD` and
  `KENDAI_KEY_PASSWORD`. The key's alias, `kendai`, isn't secret and is set in the workflow.
- The certificate's SHA-256 goes in `.github/release-certificate.sha256`, and the release workflow
  refuses to publish an APK signed by anything else.
- **Lose it and every installed copy has to be uninstalled before a new one goes on**, losing its
  library, groups and places unless they're synced. Treat it like a house key.

## Branches

- `main` is where the work happens. Every push runs CI.
- `release` is what kendai's users get, and the only source of updates. Moving it forward publishes
  a version; nothing is committed to it directly.

## Cutting a release

1. On `main`, raise the version and move "Unreleased" in `CHANGELOG.md` under it.
2. Fast-forward `release` to that commit: `git push origin main:release`.
3. The release workflow checks the version is newer than every release so far, writes the notes
   from `CHANGELOG.md`, runs every check, builds the signed APK (minified with R8), checks its
   signature, tags the commit (`v1.2.0`), and publishes a GitHub Release with the notes,
   `kendai.apk` and `kendai.apk.sha256`. Then it refreshes the downloads page.
4. The asset keeps the same name in every release, so one link always fetches the newest:
   `https://github.com/frankgrobbelaar/kendai/releases/latest/download/kendai.apk`.

## The downloads page

[frankgrobbelaar.github.io/kendai](https://frankgrobbelaar.github.io/kendai/) will be the one link
to send people, drawn in kendai's chosen look, as tansu's is in Kiri. It says, plainly:

- what kendai is, with a Download button for the newest `kendai.apk`, its version and size;
- that kendai isn't on the Play Store or any other store, on purpose: it's a personal app shared
  with people the owner knows, so Android asks before installing it;
- how to install it the first time, on a phone, a tablet or an e-ink reader, and that updates then
  arrive in the app;
- that it's open source, signed by the owner, and has no sign-in, ads or tracking;
- what's new in each version, newest first, and that it needs Android 11 or newer.

## First install

For the people the owner shares it with:

1. Open the link on the device and download `kendai.apk`.
2. Android asks to allow the browser to install apps. Allow it once. On an e-ink reader, the same
   switch is usually under its app or security settings.
3. On Samsung devices, Auto Blocker has to be off while installing. Play Protect may warn about an
   app it doesn't know; choose to install anyway.
4. Open kendai and give it All files access.

## Updates

- When kendai opens, at most once a day, it asks GitHub for the latest release. That request carries
  nothing about the user, and the check can be turned off in Settings.
- A newer version shows in Settings with what changed, from its notes. Download and install shows
  its progress and can be cancelled. The file is checked against `kendai.apk.sha256`, then handed to
  Android's installer, which keeps every setting, the library and every place.
