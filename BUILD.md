# Android Build Instructions

## Prerequisites

Before building this project, ensure you have the following installed:

1. **Android Studio** (Hedgehog 2023.1.1 or later)
2. **JDK 17** (Java Development Kit)
3. **Android SDK** with the following components:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools
4. **Android SDK Command-line Tools**

## Project Setup

### 1. Clone and Open Project

```bash
git clone https://github.com/zaheerabbas7892034214-ai/Medical-Bill-PDF-Insurance-Claim-Pack-India-.git
cd Medical-Bill-PDF-Insurance-Claim-Pack-India-
```

Open Android Studio and select "Open an Existing Project", then navigate to this directory.

### 2. Gradle Sync

Android Studio will automatically trigger a Gradle sync. If not, click:
```
File → Sync Project with Gradle Files
```

Wait for all dependencies to download. This may take several minutes on first run.

### 3. Launcher Icons (Important!)

**Note:** The project currently uses XML-based adaptive icons. For a production release, you should:

1. Generate proper PNG launcher icons using Android Studio's Image Asset Studio:
   - Right-click `res` folder → New → Image Asset
   - Choose "Launcher Icons (Adaptive and Legacy)"
   - Configure your icon design
   - Generate for all densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)

2. Or use an external tool like:
   - https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html

Required icon sizes:
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)

### 4. Google Play Billing Setup (Required for Testing)

To test in-app purchases:

1. Create app in Google Play Console
2. Add in-app product:
   - Product ID: `claim_pro_unlock`
   - Type: In-app product (one-time purchase)
   - Price: ₹399
3. Add test accounts in Play Console
4. Install app via internal testing track

**For development:** The app will work without billing setup, but purchase functionality will show errors.

## Building the Project

### Command Line Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### Android Studio Build

1. **Debug Build:**
   - Click "Run" button (green play icon)
   - Or: `Run → Run 'app'`

2. **Generate Signed APK:**
   - `Build → Generate Signed Bundle / APK`
   - Follow wizard to create keystore and sign

## Build Outputs

After successful build, find APKs at:
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## Common Build Issues and Solutions

### Issue: Gradle Sync Failed - SDK not found

**Solution:** 
- Set `ANDROID_HOME` environment variable
- Or create `local.properties` with:
  ```
  sdk.dir=/path/to/Android/Sdk
  ```

### Issue: Java version incompatibility

**Solution:**
- Ensure JDK 17 is installed
- In Android Studio: `File → Project Structure → SDK Location → JDK Location`
- Set to JDK 17 path

### Issue: Dependency download failures

**Solution:**
- Check internet connection
- Clear Gradle cache: `./gradlew clean --refresh-dependencies`
- Delete `.gradle` folder and sync again

### Issue: ML Kit download errors at runtime

**Solution:**
- Ensure device has internet connection on first run
- ML Kit downloads text recognition models (~10-20 MB)
- Models are cached after first download

### Issue: Billing errors in testing

**Solution:**
- Ensure app is published to internal testing track
- Test account must be added in Play Console
- Wait 2-4 hours after adding test account

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests (requires connected device/emulator)
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] App launches successfully
- [ ] Splash screen displays and navigates to home
- [ ] PDF picker opens from SAF
- [ ] PDF processes with progress indicator
- [ ] Preview shows extracted data
- [ ] Free tier limits work (10 items visible)
- [ ] Paywall screen displays correctly
- [ ] Purchase flow initiates (if billing configured)
- [ ] Settings screen loads
- [ ] All navigation works

## Release Checklist

Before releasing to production:

1. [ ] Replace placeholder launcher icons with proper designs
2. [ ] Configure ProGuard rules for release build
3. [ ] Create and configure signing key
4. [ ] Set up Google Play Billing products
5. [ ] Test purchase flow with real test accounts
6. [ ] Add Privacy Policy URL
7. [ ] Test on multiple devices and Android versions
8. [ ] Verify all strings are production-ready
9. [ ] Test OCR with real medical bills
10. [ ] Verify export functionality (PDF, CSV, XLSX)

## Minimum Device Requirements

- Android 7.0 (API 24) or higher
- 100 MB free storage
- Internet connection (for ML Kit model download)
- Recommended: 2 GB RAM or more

## Development Environment

Developed and tested with:
- Android Studio Hedgehog | 2023.1.1
- Gradle 8.2
- Kotlin 1.9.20
- AGP (Android Gradle Plugin) 8.2.0
- JDK 17

## Troubleshooting Resources

- [Android Developer Docs](https://developer.android.com/)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Google Play Billing](https://developer.android.com/google/play/billing)
- [ML Kit Documentation](https://developers.google.com/ml-kit)

## Support

For build issues specific to this project:
1. Check this BUILD.md file
2. Review GitHub Issues
3. Contact: support@medicalbillclaimpack.com
