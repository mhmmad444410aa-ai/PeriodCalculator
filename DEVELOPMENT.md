# PeriodCalculator — Android

Public-source Android app, package `com.mhmmad.periodcalculator`.

- Kotlin / Jetpack Compose / Room / API 23–36
- English (UK), Arabic, Portuguese (Brazil)
- Local-only menstrual and symptom records
- No account, network permission, analytics, or ad SDK in this initial build

## Build

On GitHub, open Actions > Android Preview Build. A successful build automatically publishes a *prerelease* containing a **debug APK** under Releases. Debug APK is for installation/testing only and **cannot replace a signed release for AppGallery or Uptodown**.

Build locally with JDK 17, Android SDK 36 and Gradle 8.13:

`gradle :app:testDebugUnitTest :app:assembleDebug`

No keystore belongs in a public repository. A future production build will use secure repository secrets or a local signing process.
