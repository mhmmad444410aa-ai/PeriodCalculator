# Period Calculator — Illustrated UI refresh

## What changed

This pass was based on a 23-second recording of the existing application. The earlier interface relied on simple dark surfaces, text and standard material icons. The code now uses an illustrated, cycle-tracking-specific design language:

- Six bundled, self-contained vector-style illustrations (WebP) for wellbeing/onboarding, calendar, cycle, fertility, insights and privacy.
- Six custom symptom pictograms for cramps, headache, bloating, fatigue, mood and sleep.
- A reusable `IllustratedHeader` with gradients and botanical accents; coordinated light and dark variants.
- An accessible labelled circular cycle progress ring drawn directly in Compose. The numeric estimate still comes from the existing `CycleEngine` and the low-confidence branch still avoids false precision.
- A bespoke no-records illustration and recording action.
- More pronounced content cards with restrained shadow and outlines, plus accented statistics and bar visualisations.
- More descriptive symptom rows and a clear current-day outline on the calendar.
- Locale-correct weekday start via `WeekFields.of(locale)`, respecting automatic RTL layout for Arabic.
- Illustrated headers also applied to history, reminders, privacy and settings, not just the home page.

## Source files

- `app/src/main/java/com/mhmmad/periodcalculator/ui/Visuals.kt` — new Compose illustration/header/chart UI primitives.
- `ui/Screens.kt` — onboarding, dashboard, calendar, editors, insights and fertility integration.
- `ui/UtilityScreens.kt` — settings, history, reminders and privacy integration.
- `ui/Theme.kt` and `ui/PeriodRoot.kt` — updated Material3 color and navigation polish.
- `app/src/main/res/drawable-nodpi/art_*.webp` and `symptom_*.webp` — bundled, optimised local images.
- `assets/visual-refresh-preview.png` — **concept visualisation only**, not a screenshot from an Android device and **not** suitable for store upload as an actual screenshot.

## Privacy and accessibility

All artwork is stored inside the APK. Nothing is fetched when an illustration is displayed. The app's health data is not embedded in the illustration resources or supplied to image generators. Existing translated labels and medical disclosures are retained. Decorative images have `contentDescription = null`; actionable controls retain labels. Light/dark themes use explicit contrasting text on illustrated banners.

## Build and QA status

Static checks: XML parsing, image decoding, resource reference presence, Gradle/YAML file presence, and ZIP consistency have been performed. **Android full compilation, emulator screenshots and device QA have not been performed in this environment.** Run `:app:testOfflineDebugUnitTest` and `:app:assembleOfflineDebug` via GitHub Actions, then install and verify both light and dark mode on a physical phone. Preserve existing app signing certificate when submitting an update.
