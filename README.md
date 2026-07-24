# JULE™ Browser for Android

## JSL Universal Language Engine

**Open Anything.**

This repository contains the Android application source for JULE Browser.

### Product

- Independent Android application
- Opens web addresses directly
- Back, Forward, Home, and Reload controls
- JULE address bar
- Accepts Android web links as a browser
- JSL-ian/JULE branding

### Automatic APK Build

Every push to the `main` branch starts the GitHub Actions workflow:

`Build JULE Android APK`

After the workflow shows a green check:

1. Open **Actions**.
2. Open the latest **Build JULE Android APK** run.
3. Scroll to **Artifacts**.
4. Download **JULE-Browser-Android-APK**.
5. Unzip it to obtain `JULE-Browser-Android-1.0.apk`.

### Upload Requirement

Upload **all files and folders in this package to the root of the `jule-android` repository**. The repository root must show:

```text
.github/
app/
build.gradle
gradle.properties
settings.gradle
README.md
```

The `.github/workflows/build-android-apk.yml` file is essential. Without it, GitHub will not generate the APK.

### Company

© 2026 JSL-ian Technologies Ltd. All Rights Reserved.

Support: support@jsl-ian.com  
Tel: +1 (866) 588-8182  
Website: https://www.jsl-ian.com

JULE™, JSL™, JSL-ian™, Ω Engine™, Ω Coin™, Ziran Series™, and ZiranCoin™ are trademarks of JSL-ian Technologies Ltd.
