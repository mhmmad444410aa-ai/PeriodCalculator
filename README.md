# Period Calculator — حساب الدورة الشهرية — Calculadora Menstrual

Android application source project, version 1.0.0 / build 1. Package: `com.mhmmad.periodcalculator`.

> **Build status:** The pure Kotlin cycle engine has passed local smoke checks. The Android project **has not been compiled or run on an Android device in the generating environment** because Android SDK and dependency downloads were unavailable. GitHub Actions provides a build-and-test route, but its first run and any failures must be reviewed. Do not market a release as tested until CI and manual QA succeed.

## Illustrated UI refresh (November 2026)

The updated code now includes 12 bundled, niche-specific illustrated assets, botanical/gradient hero cards, an illustrated cycle progress ring, icon-led symptom controls, responsive cards, dark/light banner variants, and locale-aware weekday ordering. The edits are implemented in `ui/Visuals.kt`, `Screens.kt`, `UtilityScreens.kt`, `PeriodRoot.kt`, and `Theme.kt`. See [`VISUAL-REFRESH.md`](VISUAL-REFRESH.md) for a file-by-file change summary. `assets/visual-refresh-preview.png` is a **design concept**, not a screenshot captured from a real device. The updated UI still requires an Android build and manual QA.

## Functionality in source

- Compose + Material 3 app UI; setup, home, calendar, recorded periods, daily symptoms, cycle-length trends, basic symptom comparison, fertility warning, reminders, history, settings and privacy text.
- Room on-device SQLite database, DataStore preferences, WorkManager generic notifications, SAF CSV/PDF export, optional biometric/device credential lock.
- Resolved calendar arithmetic uses `LocalDate`; tests for leap years, across-month bleeding, variable cycle lengths and limited-data cases.
- Three complete translated Android `strings.xml` variants. Default and English (UK), Arabic RTL, and Portuguese (Brazil). In-app language picker uses AndroidX AppCompat, and Android 13+ supports OS per-app language configuration.
- Light/dark styles, sample screenshot design assets (not genuine captures), store descriptions and local privacy policy in three languages.
- Offline flavor without Google or Huawei ad dependencies. Separate Huawei flavor prepared with optional Ads SDK, default release-approval gate **OFF**.

## IMPORTANT: Health privacy and medical limitations

This is a personal diary and a calendar-based estimator, **not** a diagnostic, contraceptive or pregnancy-confirmation tool. Fertile dates are withheld for low-confidence predictions; even when shown, these are approximate and are not reliable for avoiding pregnancy. The local data is stored in an app-private SQLite file, **not a custom encrypted database**. Android device encryption protects data at rest on supported secured devices. A biometric UI gate protects access after the app is opened; it is not a substitute for disk encryption. Screenshot/recents protection uses `FLAG_SECURE`. Cloud backups are disabled by manifest. Exports selected via SAF are **unencrypted** files.

No medical notes, cycle length, symptoms or dates are sent to ad providers. Avoid telemetry, crash SDKs and ad targeting derived from this information. The offline variant carries no ad SDK/network permissions. The Huawei flavor packages an advertising SDK; the SDK itself may process technical identifiers and usage data (see vendor's policy). Therefore **do not publish the Huawei flavor with ad requests enabled** without a regional consent workflow, consent-SDK validation, legal review, an updated externally hosted privacy statement and live-device QA. The current app-level advertising preference is a privacy gate, **not a certified consent management platform**.

## Repository creation (Public) and GitHub Actions

1. Create an empty **Public** repository named `PeriodCalculator` in your GitHub account. Do **not** upload an APK, keystore or secrets into Git.
2. Upload the **extracted files and folders at the repository root** (including hidden `.github`) using GitHub's web editor, your Git client or a phone Git client. Uploading the ZIP alone does **not** create a buildable repository.
3. Open the `Actions` tab and run `Android CI and release` manually or push to `main`. The runner installs Java 17, Android API 36, Gradle 8.13, generates and validates an official Gradle wrapper, executes unit tests, and builds `offlineDebug`.
4. For a **downloadable debug APK via GitHub Releases without relying on short-lived artifacts**, manually run the workflow with `publish_preview=true`. It publishes a `preview-NNN` pre-release containing `app-offline-debug.apk`. Debug builds are **not for store submission**.
5. For a **signed offline release**, add GitHub Actions secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`, then push a `v1.0.0` tag. The workflow fails instead of uploading an unsigned release if secrets are unavailable. GitHub Releases then hosts `app-offline-release.apk`.

### Signing warning

An existing app must keep its original signing keystore and alias across updates. Do not generate a new certificate to update an existing published package. You can encode a local JKS file with `base64 -w0 release.jks` from a trusted environment; add that string as GitHub Secret. Never put the raw JKS or key password into the Public repository, the README, commits, issue comments or Action logs. The release build requires the real keystore file in the runner; an arbitrary password cannot substitute for it.

### Gradle wrapper bootstrapping

The archive does **not** contain an official `gradle-wrapper.jar` because the SDK/Gradle distribution could not be fetched in the generating environment. On GitHub Actions, the officially installed Gradle 8.13 performs `gradle wrapper --gradle-version 8.13 --distribution-type bin` before validation and build. For building from an Android Studio computer, first install Gradle 8.13 and run the same command once, or commit the **official generated wrapper script/JAR** after obtaining them from a trusted installation. Avoid unknown online wrapper JARs.

### Local builds after wrapper bootstrap

```bash
./gradlew :app:testOfflineDebugUnitTest :app:assembleOfflineDebug
./gradlew :app:assembleOfflineRelease \
  -PreleaseStoreFile=/absolute/path/to/release.jks \
  -PreleaseStorePassword=... -PreleaseKeyAlias=... -PreleaseKeyPassword=...
```

Compile SDK: API 36. Kotlin 2.2.21, AGP 8.11.1, Gradle 8.13, JDK 17, AndroidX Room 2.8.5, WorkManager 2.10.5. Use the versions pinned in `build.gradle.kts`; do not casually upgrade one plugin at a time. First compile may need Gradle/Maven and Huawei Maven network access. The default offline build needs Google and Maven Central repositories; there is no dependency on Google Play Services.

## Architecture

`data/` has Room entities, DAO, repository, DataStore settings and view-model. `domain/CycleEngine.kt` is the pure Kotlin date engine; `ui/` holds Compose screens. `reminders/` schedules generic periodic WorkManager tasks (timing not exact). `export/` implements file export using the Android Storage Access Framework. `ads/` has privacy gating and separate build-flavor adapters. `store/` contains multilingual listing copy. `docs/privacy.html` is a publishable privacy-policy file.

### Prediction methodology

- Ordered **start-to-start** periods yield intervals measured in civil dates rather than elapsed milliseconds.
- The last six plausible intervals are averaged; fewer than three observations give *low confidence*.
- High variation widens the estimate window and decreases confidence.
- Old period data may require extrapolation and are labelled lower-confidence.
- Fertility is hidden for low confidence or old extrapolations; other estimates are **not trustworthy contraception**.
- Limitations: recording errors, missed periods, hormonal changes, postpartum changes, irregular cycles and medical conditions can invalidate calendar prediction.

## Huawei Ads notes

The Huawei build has `ads-prime` and `ads-consent` SDK dependencies pinned to `3.4.91.300`. It is deliberately **not** assembled by the default release path. To experiment, supply project Gradle properties `huaweiBannerId`, `huaweiInterstitialId`; the additional flag `enableAdRequests=true` must only be set after completing a lawful consent workflow and verifying SDK behavior. No advertising code receives period data or dates. The app does not give users an ordinary monetisation on/off switch; privacy revocation is offered only for an advertising-enabled build. Interstitial display is explicitly disabled in v1 even when a unit ID is configured. Legal compliance and ad functionality are **not certified** by this source package.

For Huawei / AppGallery target devices without HMS Core, `ads-prime` is intended for a wider range of Android hardware; verify on real Huawei and AOSP devices before publishing. The offline variant is the safest initial release.

## Store assets

- `assets/app-icon-512.png` — 512×512, RGB PNG, also copied into launcher resources.
- `assets/feature-1024x500.png` — 1024×500, RGB PNG promotional image.
- `assets/previews-not-actual-screenshots/*.png` — 1080×1920, RGB PNG illustrative design previews from the source UI specifications. **These are not on-device screenshots. Do not submit them as genuine AppGallery screen captures.** After the debug APK is installed, capture the actual interface in each required locale using real device screenshots. Dimensions 1080×1920 meet the supplied AppGallery ratio/dimension limits.
- `store/ar/listing.md`, `store/en-GB/listing.md`, `store/pt-BR/listing.md` — short/long descriptions, app names, release notes.

## Privacy policy hosting

`docs/privacy.html` is supplied locally but the stores normally require an **accessible public HTTPS URL**. Once the repository is created, enable GitHub Pages from `/docs` in repository Settings. Confirm the real published URL loads in a browser before adding it to AppGallery and Uptodown. Replace store contact details with your genuine publisher contact. Review privacy policy and store data-safety declarations for your exact release, country list and monetisation configuration.

## Pre-release QA / open items

The following checks remain mandatory before a store release:

- [ ] GitHub CI succeeds for `testOfflineDebugUnitTest` and `assembleOfflineDebug` (not yet run).
- [ ] Install on at least one Huawei EMUI and one non-Huawei Android phone.
- [ ] Test all screens, nav back gestures, database persistence, duplicate date editing/deletion and app restart.
- [ ] Test Arabic RTL and all displayed text on Android 6, 12, 13+; verify launcher name follows device locale.
- [ ] Test notification denied/granted and WorkManager delivery with real device power restrictions.
- [ ] Test optional lock, activity background/resume, secure-screen and low-memory behavior.
- [ ] Test actual exported PDF rendering, CSV opening and local file deletion; protect exported files.
- [ ] Capture **real** screenshots from the app and replace previews for store publishing.
- [ ] Host verified policy HTTPS, add correct publisher contact and complete store health/privacy declarations.
- [ ] Use stable signing key, sign and validate final release with Android `apksigner`.
- [ ] For ads: update disclosure & actual consent SDK flow, review ad data collection, test on target devices, and only then activate requests.

## Status / limitations

This is an implemented, editable Android **source project** rather than a confirmed build output. Do not claim that it compiles, passes full UI tests, has been deployed, or is approved by AppGallery until CI and device validation provide evidence. No APK is bundled. The date-engine smoke tests were compiled and executed independently with the available Kotlin JVM toolchain.
