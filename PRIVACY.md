# Privacy — Period Calculator / حساب الدورة الشهرية / Calculadora Menstrual

**Scope:** This describes the current **offline Android build** (package `com.mhmmad.periodcalculator`). It should be kept consistent with the in-app privacy text and the public policy at `docs/privacy.html`.

## Information stored on the device
The app lets users record menstrual cycle dates, daily symptoms, notes, and preferences. Records are stored locally in the app's private Android storage (Room/SQLite and DataStore). There is no account or app-operated cloud sync. The offline flavor contains no advertising SDK or analytics SDK, and no health journal records are intentionally transmitted to any developer-controlled server. Device backups of app data are disabled in the Android manifest.

## User controls and optional features
Users can view and edit entries and erase their records within Settings. They may export a CSV or PDF document to a location of their choice using Android's system file picker. **Exported files are not encrypted by the app** and may remain accessible to other apps or people with access to that destination. Optional reminder notifications are scheduled on the device; notification permission may be required. The app may provide an optional biometric/device-credential gate. This is an access control, not a substitute for encrypted database storage.

## Ads and third-party SDKs
The **offline release flavor** does not contain Huawei Ads SDK or make ad requests. A distinct Huawei flavor exists in source but must not be published with ad requests enabled until the actual SDK data processing, required disclosures, and a legally adequate consent mechanism have been reviewed and tested. The app must never pass menstrual dates, fertility predictions, symptom entries, or notes to ad providers. If a future published build introduces advertising or network analytics, this policy and the in-app notices must be updated **before** release.

## Health disclaimer
The app makes approximate calendar-based predictions and is not a medical device, diagnostic service, pregnancy test, or reliable method of contraception. Irregular cycles or other circumstances may reduce accuracy.

## Contact and policy
For privacy queries, use the developer contact published on the store listing. This repository copy is informational; AppGallery should receive an accessible public HTTPS privacy-policy URL and, where required, a data-rights URL consistent with this policy.

Last reviewed: 13 November 2026.
