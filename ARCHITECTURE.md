# Architecture Documentation

## Overview

Medical Bill Claim Pack follows the **MVVM (Model-View-ViewModel)** architecture pattern with clean architecture principles, built entirely using **Jetpack Compose** for UI and **Kotlin Coroutines** for asynchronous operations.

## Architecture Layers

### 1. Presentation Layer (UI)

**Location:** `app/src/main/java/com/medicalbill/claimpack/ui/`

#### Components:

- **Screens** (`ui/screens/`): Individual composable screens
  - `SplashScreen.kt` - Entry point with branding animation
  - `HomeScreen.kt` - Main dashboard with file picker
  - `ConvertProgressScreen.kt` - Real-time PDF processing progress
  - `PreviewScreen.kt` - Bill data preview with tier restrictions
  - `ClaimPackPreviewScreen.kt` - Generated claim pack summary
  - `ExportScreen.kt` - Export format selection
  - `PaywallScreen.kt` - Monetization and upgrade flow
  - `SettingsScreen.kt` - App settings and preferences

- **Theme** (`ui/theme/`): Material 3 theming
  - `Color.kt` - Healthcare-themed color palette
  - `Theme.kt` - Light/dark theme configuration
  - `Type.kt` - Typography scales

- **Navigation** (`ui/`):
  - `Navigation.kt` - Sealed class for type-safe routes
  - `MedicalBillApp.kt` - Navigation host setup
  - `MainViewModel.kt` - Shared app-level ViewModel

#### Design Patterns:

- **Unidirectional Data Flow (UDF)**: State flows down, events flow up
- **Single Source of Truth**: ViewModels hold UI state
- **State Hoisting**: Stateless composables where possible

### 2. Domain Layer

**Location:** `app/src/main/java/com/medicalbill/claimpack/data/model/`

#### Models:

- `BillData.kt` - Core domain model for extracted bill information
  - Contains: Patient info, hospital details, line items, totals
  - Includes confidence scoring for extraction quality
- `LineItem.kt` - Individual bill line item (embedded in BillData)

#### Business Logic:

While this project doesn't have a separate domain layer with use cases, business logic is contained in:
- `BillDataExtractor.kt` - Data extraction algorithms
- `BillingManager.kt` - Purchase validation logic
- ViewModels - Orchestration of data flow

### 3. Data Layer

**Location:** `app/src/main/java/com/medicalbill/claimpack/data/`

#### Repository Pattern:

- `repository/FileRepository.kt`
  - Abstracts data source (Room database)
  - Provides clean API for ViewModels
  - Handles data operations asynchronously

#### Local Database (Room):

- **Database:** `database/AppDatabase.kt`
  - Singleton pattern for database instance
  - Version 1, single table architecture

- **Entity:** `database/ProcessedFile.kt`
  - Represents persisted bill records
  - Fields: ID, file info, extracted data summary

- **DAO:** `database/ProcessedFileDao.kt`
  - CRUD operations with Flow for reactive updates
  - Queries sorted by processing date

#### Data Flow:

```
UI (Composable) 
  ↓ (events)
ViewModel 
  ↓ (calls)
Repository 
  ↓ (queries)
Room Database
  ↑ (Flow emissions)
Repository
  ↑ (StateFlow)
ViewModel
  ↑ (collectAsState)
UI (recomposition)
```

### 4. Service Layer

#### PDF Processing (`pdf/` package):

- **PdfProcessor.kt**
  - Uses Android's `PdfRenderer` for PDF to bitmap conversion
  - Integrates ML Kit `TextRecognizer` for OCR
  - Returns extracted text as `Result<String>`

- **BillDataExtractor.kt**
  - Pattern matching with regex
  - Heuristic-based data extraction
  - Category classification algorithms
  - Confidence scoring (0-100%)

- **ClaimPackGenerator.kt**
  - PDF generation using iText7
  - CSV export with proper escaping
  - XLSX generation (delegated to CSV currently)
  - File handling via SAF

#### Billing (`billing/` package):

- **BillingManager.kt**
  - Google Play Billing Library v6 integration
  - Purchase flow orchestration
  - Acknowledgment handling
  - Offline entitlement caching
  - State management with sealed classes

### 5. Core/Utilities

- **MainActivity.kt** - Single activity hosting all composable screens
- **Shared Preferences** - Entitlement persistence in BillingManager
- **FileProvider** - Secure file sharing configuration

## Key Design Decisions

### 1. Single Activity Architecture

**Why:** Recommended by Google for Jetpack Compose apps
- Simplifies navigation
- Reduces overhead
- Better shared element transitions

### 2. No Separate Domain Layer

**Why:** App complexity doesn't justify extra layer
- Business logic is straightforward
- No complex use cases requiring orchestration
- Keeps codebase maintainable

### 3. Room for Local Storage

**Why:** Type-safe, battle-tested, and reactive
- Flow integration for UI updates
- Compile-time SQL verification
- Migration support

### 4. ML Kit Over Custom OCR

**Why:** Production-ready with Google backing
- On-device processing (privacy)
- Automatic model management
- High accuracy out-of-box

### 5. StateFlow Over LiveData

**Why:** Kotlin-first, better Compose integration
- Doesn't require lifecycle observer
- Better testability
- Coroutine-native

## State Management

### ViewModel State Pattern

```kotlin
class MainViewModel : AndroidViewModel {
    // State exposed as StateFlow
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()
    
    // Events trigger state updates
    fun onEvent(event: Event) {
        viewModelScope.launch {
            // Update state
            _state.value = newState
        }
    }
}
```

### Composable State Collection

```kotlin
@Composable
fun Screen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()
    
    // UI reacts to state changes
    when (state) {
        is Loading -> LoadingUI()
        is Success -> ContentUI(state.data)
        is Error -> ErrorUI(state.message)
    }
}
```

## Dependency Injection

**Current State:** Manual dependency injection
- ViewModels created via `viewModel()` delegate
- Services instantiated in ViewModels
- Context passed from Activity

**Future Enhancement:** Consider Dagger/Hilt for:
- Larger team collaboration
- Complex dependency graphs
- Better testability

## Navigation Architecture

### Type-Safe Navigation

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Preview : Screen("preview/{fileId}") {
        fun createRoute(fileId: Long) = "preview/$fileId"
    }
}
```

### Navigation Flow

```
Splash → Home ⇄ Settings
         ↓
    ConvertProgress
         ↓
       Preview ⇄ Paywall
         ↓
   ClaimPackPreview
         ↓
       Export
```

## Error Handling Strategy

### Layered Error Handling

1. **Service Layer:** Returns `Result<T>` types
   ```kotlin
   suspend fun extractText(): Result<String>
   ```

2. **ViewModel Layer:** Converts to UI state
   ```kotlin
   sealed class UiState {
       object Loading : UiState()
       data class Success(val data: Data) : UiState()
       data class Error(val message: String) : UiState()
   }
   ```

3. **UI Layer:** Displays appropriate UI
   ```kotlin
   when (uiState) {
       is Error -> ErrorDialog(message)
   }
   ```

## Testing Strategy

### Unit Tests

- **ViewModels:** Test state transitions
- **Extractors:** Test pattern matching
- **Repository:** Test data operations (with fake DAO)

### Integration Tests

- **Database:** Room instrumented tests
- **Billing:** Test with Google's test environment
- **Navigation:** Compose UI tests

### UI Tests

- **Compose Testing:** Semantic tree assertions
- **Screenshot Testing:** Visual regression
- **Accessibility:** TalkBack compatibility

## Performance Considerations

### PDF Processing

- **Off Main Thread:** All PDF operations in IO dispatcher
- **Bitmap Recycling:** Immediate bitmap cleanup
- **Lazy Loading:** Process pages on-demand

### UI Rendering

- **Recomposition Optimization:** Use `remember`, `key`, `derivedStateOf`
- **Stable Parameters:** Data classes marked with `@Stable` where beneficial
- **Lazy Lists:** Only render visible items

### Memory Management

- **ViewModel Lifecycle:** Tied to navigation backstack
- **Coroutine Cancellation:** Automatic when ViewModel cleared
- **Resource Cleanup:** `onCleared()` for services

## Security Considerations

### Data Protection

- **No Storage Permissions:** SAF for file access
- **Encrypted SharedPrefs:** For sensitive entitlement data
- **No Cloud Storage:** All data local by default

### Billing Security

- **Purchase Verification:** Server-side validation recommended
- **Acknowledgment Required:** Prevents duplicate charges
- **Offline Validation:** Cached with refresh on network

## Scalability

### Current Limitations

- Single table database (okay for current scope)
- Manual DI (fine for small team)
- Simplified XLSX export

### Growth Path

1. **Multi-bill Processing:** Add batch operations
2. **Cloud Sync:** Firebase integration
3. **Advanced OCR:** Custom ML models
4. **Templates:** Customizable claim pack layouts
5. **Analytics:** User behavior tracking

## Dependencies Overview

| Category | Library | Version | Purpose |
|----------|---------|---------|---------|
| UI | Jetpack Compose | 1.5.4 | Declarative UI |
| Architecture | ViewModel | 2.7.0 | State management |
| Database | Room | 2.6.1 | Local persistence |
| Async | Coroutines | 1.7.3 | Concurrency |
| Billing | Play Billing | 6.1.0 | In-app purchases |
| ML | ML Kit | 16.0.0 | Text recognition |
| PDF | iText7 | 7.2.5 | PDF generation |
| Excel | Apache POI | 5.2.5 | XLSX export |

## Future Architecture Improvements

1. **UseCase Layer:** Separate business logic
2. **Dependency Injection:** Hilt integration
3. **Modularization:** Feature modules
4. **Remote Data Source:** API integration
5. **Offline-First:** Better sync strategy
6. **WorkManager:** Background processing
7. **DataStore:** Replace SharedPreferences

## Code Organization Rules

1. **Package by Feature:** Related classes together
2. **Composition Over Inheritance:** Prefer delegation
3. **Immutability:** Data classes are immutable
4. **Single Responsibility:** Each class has one job
5. **SOLID Principles:** Applied throughout

## Resources

- [Android App Architecture](https://developer.android.com/topic/architecture)
- [Jetpack Compose Architecture](https://developer.android.com/jetpack/compose/architecture)
- [Kotlin Coroutines Best Practices](https://kotlinlang.org/docs/coroutines-basics.html)
