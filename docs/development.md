# Development

## Setting up a PC

- Install Android Studio. Its bundled JDK 21 runs Gradle, and Gradle fetches everything else,
  including the Android SDK platform it needs.
- Open the repo in Android Studio, or use the commands below from the repo's root.
- Turn on the pre-commit hook once per clone: `git config core.hooksPath .githooks`. It runs
  formatting and detekt before each commit, since `main` has no pull requests to catch them.

## Commands

| To | Run |
| --- | --- |
| Run every check CI runs, and build the debug app | `./gradlew check assembleDebug` |
| Fix formatting | `./gradlew spotlessApply` |
| Run the unit tests | `./gradlew testDebugUnitTest` |
| Install on a device or the emulator | `./gradlew installDebug` |

## The checks

| Check | What it does | Where it's set |
| --- | --- | --- |
| Spotless | ktfmt (kotlinlang style) for Kotlin and Gradle scripts; trailing whitespace and final newlines elsewhere | `build.gradle.kts` |
| detekt | Code smells and complexity, plus compose-rules for Compose. Any issue fails the build | `config/detekt/detekt.yml` |
| Android Lint | Warnings are errors. Checks for newer library versions are off: Dependabot handles those | `app/build.gradle.kts` |
| Kotlin | Compiler warnings are errors | `app/build.gradle.kts` |
| Unit tests | JUnit 4, Robolectric (Android 16, the newest it runs) and Compose UI tests, at phone and tablet sizes | `app/src/test` |

detekt is on a 2.0 alpha because compose-rules needs it and the 1.x line predates Kotlin 2.4. Move
to 2.0 stable when it ships. compose-rules allows one composition local, `LocalEink`, which says
whether e-ink mode is on.

## The emulator

The `Pixel_8` AVD runs Android 17, at the Galaxy S24's size class. A tablet AVD shows the rail and
two-page layouts. To run one without a window:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_8 -no-window -no-audio
```

`adb exec-out screencap -p > screen.png` takes a screenshot to compare with the design boards.
E-ink mode can be turned on in Settings to check it on any emulator, though only a real e-ink
reader shows ghosting.

## Versions

Dependabot opens one grouped pull request a week for Gradle libraries and one for GitHub Actions.
Each must pass CI before it's merged. The versions themselves live in
[gradle/libs.versions.toml](../gradle/libs.versions.toml).
