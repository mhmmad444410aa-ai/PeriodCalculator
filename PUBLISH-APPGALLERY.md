# Period Calculator — AppGallery first publication

**App ID**: `com.mhmmad.periodcalculator`  
**Version for the first store release**: `1.0.1` (`versionCode = 2`)  
**Tested CI variant**: `offlineDebug` (NOT for store upload)  
**Target signed store variant**: `offlineRelease` (no ad SDK, no ad requests)

## 1. Protect the permanent signing key

A first-release upload key has been generated separately in a private ChatGPT conversation. It must NEVER be uploaded into this public GitHub repository, a commit, issue, release or artifact. Keep two offline backups and a copy of its password. Use the exact same key for all future uploads requiring this signature. The selected key alias is `periodcalculator`. A signing certificate and its SHA-256 fingerprint should be retained for identity checks.

### GitHub Actions secrets — values are PRIVATE

Go to [Repository Settings → Secrets and variables → Actions](https://github.com/mhmmad444410aa-ai/PeriodCalculator/settings/secrets/actions) and create four **repository secrets**:

| Secret name | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Exact one-line Base64 representation of the private JKS file |
| `RELEASE_STORE_PASSWORD` | Private keystore password |
| `RELEASE_KEY_ALIAS` | `periodcalculator` |
| `RELEASE_KEY_PASSWORD` | Private key password (same as keystore password in the generated key) |

Do not store these as plain repository variables or include them in screenshots.

## 2. Build and verify a signed release

Once all four secrets have been saved, open [Actions → Android CI and release](https://github.com/mhmmad444410aa-ai/PeriodCalculator/actions/workflows/android-build.yml), tap **Run workflow**, select the `main` branch and enable **publish_signed_release**, then run. This is the easiest route on a phone. Alternatively, pushing a new `v1.0.1` tag on the verified `main` commit also builds the release. The workflow will:
- Run offline unit tests and build the debug variant;
- Restore the JKS privately in the GitHub runner;
- Build `assembleOfflineRelease` with the private key;
- Verify the final APK with Android `apksigner`;
- Publish the signed `app-offline-release.apk` plus its SHA-256 checksum under [Releases](https://github.com/mhmmad444410aa-ai/PeriodCalculator/releases).

**A published `preview-XX` file with `.preview` package suffix is NOT a store release.** If the signed workflow fails, do not upload any old or debug APK as a substitute.

## 3. Host privacy and data-rights pages

[Privacy policy](docs/privacy.html) and [data rights](docs/data-rights.html) are present in `docs/` in three languages. Publish them on a real HTTPS web page before submitting. One option:
1. Repository → Settings → Pages.
2. Build and deployment: Deploy from a branch.
3. Select branch `main` and folder `/docs`, then Save.
4. Confirm both pages open publicly in a browser before entering the URLs in AppGallery:

- Expected policy URL **after successful enablement**: `https://mhmmad444410aa-ai.github.io/PeriodCalculator/privacy.html`
- Expected data-rights URL **after successful enablement**: `https://mhmmad444410aa-ai.github.io/PeriodCalculator/data-rights.html`

Do not use an unverified URL. Provide a valid publisher contact on the store listing and update the policy if the published SDKs change.

## 4. Prepare truthful store listing

- App names: `حساب الدورة الشهرية` / `Period Calculator` / `Calculadora Menstrual`.
- Store descriptions: `store/ar/listing.md`, `store/en-GB/listing.md` and `store/pt-BR/listing.md`.
- Package type: Android app, not game; package name must match `com.mhmmad.periodcalculator`.
- Promotional assets: app icon and feature graphic are in the source release assets.
- **Take authentic screenshots of the real running signed or debug app** in the required languages using a phone; existing design previews are not genuine app screenshots and must not be submitted as such.
- Mention the calendar-based estimate limitations; do not make medical diagnoses, pregnancy-confirmation or contraception claims.
- Describe how local menstrual and symptom data are stored, exported and erased. Complete any health/privacy declarations truthfully.

## 5. App signing choice on AppGallery

For a first-time Android app, AppGallery Connect may offer its App Signing service. If you let Huawei generate and manage the app's **distribution signing key**, your locally generated key becomes an **upload key** and the distributed signature may differ. This can complicate cross-store updates with a different signing certificate. If the app is also distributed on Uptodown, carefully decide the cross-store signature strategy **before first publication**. Huawei documentation permits self-managed APK signing where supported; protect the same keystore carefully if this is selected. Do not switch signing strategies casually after launch.

## 6. Upload and review

In Huawei AppGallery Connect, create/select the app, configure distribution and version, submit the **signed release APK**, enter verified policy/data-rights URLs and screenshots, review the requested permissions and data safety declarations, then submit to Huawei for review.

There is no completed AppGallery submission as of this document's creation; uploading to GitHub does not publish the app to AppGallery.

Official guidance:
- https://developer.huawei.com/consumer/en/doc/appgallery-connect-guides/agc-appsigning-introduction-0000001051379577
- https://developer.huawei.com/consumer/en/doc/app/agc-help-release-app-privacy-state-0000002278878296/
- https://developer.huawei.com/consumer/en/doc/app/agc-help-release-app-guide-0000002287176372/
