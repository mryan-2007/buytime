# BuyTime

Minimal, production-clean Android application skeleton for **BuyTime** (`com.imagination.buytime`).

## Step 1: Project Skeleton & Build Pipeline

This repository hosts the initial foundation for BuyTime, strictly scoped to the project skeleton, modern Gradle Kotlin DSL setup, a minimalist Jetpack Compose UI shell, and an automated GitHub Actions CI/CD pipeline.

- **Package Name:** `com.imagination.buytime`
- **Application ID:** `com.imagination.buytime`
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 35
- **Min SDK:** 26 (Android 8.0)
- **Build Tools:** 35.0.0
- **Android Gradle Plugin (AGP):** 8.7.2
- **Kotlin & Compose Compiler Plugin:** 2.0.21
- **Compose BOM:** 2024.10.01
- **Java / JVM Target:** JDK 17

---

## Local Build Instructions

To build the debug APK locally using the Gradle wrapper:

```bash
# Ensure execution permissions for the wrapper
chmod +x gradlew

# Build the debug APK
./gradlew assembleDebug
```

The generated APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## GitHub Actions CI/CD & Downloading the Debug APK

Every push and pull request targeting `main` automatically triggers the GitHub Actions workflow defined in [build.yml](.github/workflows/build.yml).

### How to Download and Install the Debug APK:
1. Navigate to the **Actions** tab on the GitHub repository.
2. Click on the latest workflow run for your commit or pull request (e.g., `Build BuyTime Debug APK`).
3. Scroll down to the **Artifacts** section at the bottom of the summary page.
4. Download the `BuyTime-debug-apk` zip archive.
5. Extract the zip archive to obtain `app-debug.apk`.
6. Transfer `app-debug.apk` to an Android device (running Android 8.0 / API 26 or newer) and install it (allow installation from unknown sources if prompted), or install via ADB:
   ```bash
   adb install -r app-debug.apk
   ```
