# Release gate checklist

Status: **NOT YET STORE READY**. The current environment lacks Android SDK and Gradle dependencies.

1. Upload extracted sources to a Public GitHub repository; keep `.github/` and all subfolders.
2. Run Actions build. Fix any Kotlin/Gradle/Android errors before requesting store review.
3. Download debug preview APK from GitHub Release and test offline behavior and localized name.
4. Fix UX defects from device QA; capture real 1080×1920 screenshots.
5. Host `docs/privacy.html` publicly through GitHub Pages; add true publisher contact and review the privacy policy.
6. Use a unique permanent JKS signing key. Add 4 secrets in GitHub Actions, tag `v1.0.0`, verify signature and release APK.
7. Do not activate Huawei Ads before full disclosures, consent provider verification and device-level audits.
8. Upload signed release APK, actual screenshots, localized descriptions and live privacy-policy URL to AppGallery and Uptodown.
