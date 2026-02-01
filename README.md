# Medical Bill → Claim Pack (India)

A production-ready Android application that converts medical bills (PDFs) into insurance claim packs using OCR and ML Kit.

## Overview

**Medical Bill Claim Pack** is a comprehensive Android application designed to help users in India convert their hospital and pharmacy bills into professional insurance claim packages. The app uses advanced PDF rendering and ML Kit text recognition to extract relevant medical billing information and generate formatted claim documents.

## Features

### Core Functionality
- 📄 **PDF Bill Processing**: Import hospital/pharmacy bills via Storage Access Framework (SAF)
- 🔍 **OCR Text Recognition**: ML Kit powered text extraction from scanned bills
- 📊 **Smart Data Extraction**: Automatically extracts:
  - Patient Name
  - Hospital Name
  - Invoice Date & Bill Number
  - Line Items (Description, Quantity, Rate, Amount)
  - Category Totals (Room Rent, Medicine, Diagnostics, Doctor Fees)
  - Total Amount

### Export Capabilities (Pro Tier)
- 📋 **Claim Pack PDF**: Professional PDF with cover page, summary, and original bill
- 📈 **CSV Export**: Tabular data for easy processing
- 📊 **Excel Export**: XLSX format for spreadsheet analysis
- 🔗 **File Sharing**: Share generated files via FileProvider

### Monetization (Google Play Billing v6+)
- **Free Tier**: 
  - Preview first 10 line items
  - Export functionality disabled
- **Pro Tier (₹399)**:
  - Unlimited line item extraction
  - Full export capabilities (PDF, CSV, XLSX)
  - Edit extracted data
  - Share generated files

## Technical Stack

### Platform
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose
- **Database**: Room for local storage
- **Async**: Kotlin Coroutines & Flow

### Key Libraries
- **Google Play Billing**: v6.1.0 (In-app purchases)
- **ML Kit Text Recognition**: v16.0.0 (OCR)
- **PdfRenderer**: Native Android (PDF processing)
- **iText7**: v7.2.5 (PDF generation)
- **Apache POI**: v5.2.5 (Excel export)
- **Room Database**: v2.6.1 (Data persistence)

## Project Structure

```
app/src/main/
├── java/com/medicalbill/claimpack/
│   ├── billing/
│   │   └── BillingManager.kt          # Google Play Billing integration
│   ├── data/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt         # Room database
│   │   │   ├── ProcessedFile.kt       # Entity
│   │   │   └── ProcessedFileDao.kt    # DAO
│   │   ├── model/
│   │   │   └── BillData.kt            # Data models
│   │   └── repository/
│   │       └── FileRepository.kt       # Repository pattern
│   ├── pdf/
│   │   ├── PdfProcessor.kt            # PDF rendering & OCR
│   │   ├── BillDataExtractor.kt       # Data extraction logic
│   │   └── ClaimPackGenerator.kt      # Export generation
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── SplashScreen.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── ConvertProgressScreen.kt
│   │   │   ├── PreviewScreen.kt
│   │   │   ├── ClaimPackPreviewScreen.kt
│   │   │   ├── ExportScreen.kt
│   │   │   ├── PaywallScreen.kt
│   │   │   └── SettingsScreen.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   ├── MainViewModel.kt
│   │   ├── MedicalBillApp.kt
│   │   └── Navigation.kt
│   └── MainActivity.kt
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    └── xml/
        ├── file_paths.xml
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Building the Project

1. **Clone the repository**:
   ```bash
   git clone https://github.com/zaheerabbas7892034214-ai/Medical-Bill-PDF-Insurance-Claim-Pack-India-.git
   cd Medical-Bill-PDF-Insurance-Claim-Pack-India-
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Configure Google Play Billing** (for testing):
   - Add your app to Google Play Console
   - Create an in-app product with ID: `claim_pro_unlock`
   - Set price to ₹399
   - Add test accounts in Google Play Console

4. **Build the project**:
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

## App Screens

### 1. Splash Screen
- Displays app branding with animation
- Loads billing entitlement information

### 2. Home Screen
- "Pick PDF" button to import bills
- List of recently processed files
- Upgrade to Pro CTA (for free users)

### 3. Convert Progress Screen
- Real-time progress indicators:
  - PDF Rendering
  - OCR Processing
  - Data Extraction
- Retry mechanism for failures

### 4. Preview Screen
- Display extracted bill information
- Show line items table
- Free tier: Limited to first 10 items (blurred beyond)
- Pro tier: Full access with unlock button

### 5. Claim Pack Preview Screen
- Summary of categorized expenses
- Total breakdowns
- Export button (Pro only)

### 6. Export Screen
- PDF, CSV, and XLSX export options
- Locked for free users
- Direct share functionality

### 7. Paywall Screen
- Feature comparison (Free vs Pro)
- Purchase button (₹399)
- Restore purchase option
- Billing error handling

### 8. Settings Screen
- Purchase status display
- Restore purchases
- Clear history
- Privacy policy link
- About section

## Free vs Pro Tier

| Feature | Free | Pro (₹399) |
|---------|------|------------|
| Preview line items | First 10 only | Unlimited |
| Export PDFs | ❌ | ✅ |
| Export CSV/XLSX | ❌ | ✅ |
| Edit extracted data | ❌ | ✅ |
| Share files | ❌ | ✅ |
| Claim pack generation | Limited | Full |

## Data Extraction Heuristics

The app uses pattern matching and regex to extract:

1. **Patient Name**: Looks for "Patient Name:", "Name:", etc.
2. **Hospital Name**: Searches for keywords like "Hospital", "Clinic", "Medical Center"
3. **Invoice Date**: Matches date patterns (DD/MM/YYYY, etc.)
4. **Bill Number**: Finds "Bill No:", "Invoice No:", "Receipt No:"
5. **Line Items**: Detects table structures with columns
6. **Categories**: 
   - Doctor Fees: "doctor", "consultation", "physician"
   - Room Rent: "room", "bed", "accommodation"
   - Medicine: "medicine", "drug", "tablet", "injection"
   - Diagnostics: "test", "lab", "x-ray", "scan"

## Offline Entitlement Management

- **Room Database**: Stores processed files and metadata
- **SharedPreferences**: Fallback for billing entitlement
- **BillingManager**: Handles purchase acknowledgment and restoration

## Security & Privacy

- No storage permissions required (uses SAF)
- Billing data backed up with exclusions
- ProGuard rules for production builds
- Secure file handling via FileProvider

## Error Handling

- Graceful PDF loading failures
- OCR retry mechanisms
- Low confidence data warnings
- Editable fields for manual corrections
- User-friendly error messages

## Testing

The app includes:
- Unit tests for data extraction logic
- Instrumented tests for UI components
- Billing flow testing with test accounts

Run tests:
```bash
./gradlew test           # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Known Limitations

- OCR accuracy depends on PDF quality
- Complex bill formats may require manual editing
- XLSX export uses simplified CSV format (Apache POI implementation can be enhanced)
- Requires active internet for ML Kit model downloads (first time)

## Future Enhancements

- Multi-language support
- Cloud backup integration
- Batch processing
- PDF template customization
- Enhanced table detection algorithms
- Support for more bill formats

## License

This project is proprietary software. All rights reserved.

## Support

For issues or questions, please contact:
- Email: support@medicalbillclaimpack.com
- GitHub Issues: [Create an issue](https://github.com/zaheerabbas7892034214-ai/Medical-Bill-PDF-Insurance-Claim-Pack-India-/issues)

## Credits

Developed using:
- Android Jetpack libraries
- Material Design 3
- Google ML Kit
- iText PDF library
- Apache POI

---

**Version**: 1.0  
**Last Updated**: February 2026  
**Minimum Android Version**: 7.0 (API 24)  
**Target Android Version**: 14 (API 34)
