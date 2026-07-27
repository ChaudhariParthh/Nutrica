# Nutrica Architecture Guide

This document describes the overall system architecture, design patterns, and technical organization of the Nutrica application.

---

## Table of Contents

1. [System Overview](#system-overview)
2. [MVVM Architecture](#mvvm-architecture)
3. [Package Structure](#package-structure)
4. [Data Flow](#data-flow)
5. [Database Layer](#database-layer)
6. [Networking Layer](#networking-layer)
7. [UI Layer](#ui-layer)
8. [Dependency Injection](#dependency-injection)

---

## System Overview

Nutrica is built on a layered, modular architecture that separates concerns into:

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                   │
│         Screens, Components, Navigation                 │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│              ViewModel Layer (MVVM)                     │
│      State Management, Business Logic                   │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│          Repository Layer (Data Access)                │
│      Abstraction over Data Sources                      │
└──┬────────────────────────────────────────────┬──────────┘
   │                                            │
┌──▼──────────────────┐            ┌��──────────▼──────────┐
│   Local Data Source  │            │  Remote Data Source  │
│  (Room Database)     │            │   (Retrofit API)     │
└──────────────────────┘            └──────────────────────┘
```

---

## MVVM Architecture

### Model
Data models represent the app's domain objects:
- **Entities:** Room database entities (e.g., `FoodItem`, `UserProfile`, `MealLog`)
- **DTOs:** Data transfer objects for API responses (e.g., `NutritionResponse`)
- **State Classes:** Immutable state holders for UI state management

### ViewModel
ViewModels contain business logic and manage UI state:

```kotlin
class NutritionViewModel(
    private val repository: NutritionRepository,
    private val aiCoach: AiCoachService
) : ViewModel() {
    
    private val _foodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val foodLogs: StateFlow<List<FoodLog>> = _foodLogs.asStateFlow()
    
    private val _aiAdvice = MutableStateFlow<String>("")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()
    
    fun addFoodLog(item: FoodItem) {
        viewModelScope.launch {
            repository.insertFoodLog(item)
            _foodLogs.value = repository.getAllLogs()
        }
    }
    
    fun requestAiAdvice() {
        viewModelScope.launch {
            val advice = aiCoach.generateAdvice(_foodLogs.value)
            _aiAdvice.value = advice
        }
    }
}
```

**Key Principles:**
- ViewModels survive configuration changes
- Use `StateFlow` or `LiveData` for observable state
- Launch coroutines in `viewModelScope` for automatic cleanup
- Never pass UI references to ViewModels

### View (Compose)
Composable functions display UI based on ViewModel state:

```kotlin
@Composable
fun NutritionScreen(viewModel: NutritionViewModel) {
    val foodLogs by viewModel.foodLogs.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.aiAdvice.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FoodLogsList(foodLogs)
        AiCoachCard(aiAdvice)
    }
}
```

---

## Package Structure

```
app/src/main/java/com/example/nutrica/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt              # Room database configuration
│   │   ├── entity/
│   │   │   ├── FoodItemEntity.kt
│   │   │   ├── MealLogEntity.kt
│   │   │   └── UserProfileEntity.kt
│   │   └── dao/
│   │       ├── FoodItemDao.kt
│   │       ├── MealLogDao.kt
│   │       └── UserProfileDao.kt
│   ├── remote/
│   │   ├── NutritionApi.kt             # Retrofit API interface
│   │   ├── AiCoachApi.kt
│   │   └── dto/
│   │       ├── NutritionResponse.kt
│   │       └── AiCoachResponse.kt
│   └── repository/
│       ├── NutritionRepository.kt
│       ├── UserRepository.kt
│       └── AiCoachRepository.kt
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── FoodScannerScreen.kt
│   │   ├── MealPlannerScreen.kt
│   │   └── AiCoachScreen.kt
│   ├── components/
│   │   ├── NutritionCard.kt
│   │   ├── BarcodeScanner.kt
│   │   ├── ProgressBar.kt
│   │   └── RecipeCard.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── navigation/
│   │   └── NavGraph.kt
│   └── modifier/
│       └── CustomModifiers.kt
├── viewmodel/
│   ├── NutritionViewModel.kt
│   ├── ProfileViewModel.kt
│   ├── AiCoachViewModel.kt
│   └── MainViewModel.kt
├── util/
│   ├── Constants.kt
│   ├── Extension.kt
│   └── DateUtils.kt
├── di/
│   └── AppModule.kt                   # Hilt dependency injection
└── MainActivity.kt
```

---

## Data Flow

### User Action Flow

```
1. User Input
   ↓
2. UI (Composable) → Calls ViewModel function
   ↓
3. ViewModel → Calls Repository function
   ↓
4. Repository → Routes to Local or Remote Data Source
   ↓
5. Data Source → Fetches/Updates data
   ↓
6. Repository → Transforms data (DTOs → Entities)
   ↓
7. ViewModel → Updates StateFlow/LiveData
   ↓
8. UI → Recomposes with new state
```

### Example: Adding a Food Log

```kotlin
// 1. UI triggers
Button(onClick = { viewModel.addFoodLog(selectedFood) }) {
    Text("Log Food")
}

// 2. ViewModel handles
fun addFoodLog(item: FoodItem) {
    viewModelScope.launch {
        val entity = item.toEntity()
        repository.insertFoodLog(entity)
        refreshLogs()
    }
}

// 3. Repository processes
suspend fun insertFoodLog(entity: MealLogEntity) {
    mealLogDao.insert(entity)
    syncWithRemote(entity)  // Optional sync
}

// 4. DAO persists
@Insert
suspend fun insert(log: MealLogEntity)
```

---

## Database Layer

### Room Configuration

```kotlin
@Database(
    entities = [
        FoodItemEntity::class,
        MealLogEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "nutrica_database"
                ).build().also { instance = it }
            }
        }
    }
}
```

### Entity Example

```kotlin
@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

### DAO Example

```kotlin
@Dao
interface MealLogDao {
    @Insert
    suspend fun insert(log: MealLogEntity)
    
    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<MealLogEntity>>
    
    @Query("SELECT * FROM meal_logs WHERE timestamp BETWEEN :start AND :end")
    suspend fun getLogsBetween(start: Long, end: Long): List<MealLogEntity>
    
    @Delete
    suspend fun delete(log: MealLogEntity)
}
```

---

## Networking Layer

### Retrofit Configuration

```kotlin
@Provides
@Singleton
fun provideRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(httpClient)
        .build()
}
```

### API Service

```kotlin
interface NutritionApi {
    @POST("api/nutrition/scan")
    suspend fun scanBarcode(
        @Body request: BarcodeScanRequest
    ): Response<NutritionResponse>
    
    @GET("api/nutrition/food/{id}")
    suspend fun getFoodDetails(
        @Path("id") foodId: String
    ): Response<FoodDetailsResponse>
}
```

---

## UI Layer

### Composable Structure

```kotlin
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    when (val currentState = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Success -> SuccessContent(currentState.data)
        is UiState.Error -> ErrorScreen(currentState.error)
    }
}
```

### Theme System

```kotlin
// Color definitions
val NutrientGreen = Color(0xFF2E7D32)
val CaloricRed = Color(0xFFD32F2F)
val ProteinBlue = Color(0xFF1976D2)
val CarbsOrange = Color(0xFFF57C00)
val FatsYellow = Color(0xFFFBC02D)

@Composable
fun NutricaTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = NutrientGreen,
        secondary = CaloricRed,
        tertiary = ProteinBlue
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NutricaTypography,
        content = content
    )
}
```

---

## Dependency Injection

Nutrica uses Hilt for compile-time dependency injection:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideMealLogDao(db: AppDatabase): MealLogDao {
        return db.mealLogDao()
    }
    
    @Provides
    @Singleton
    fun provideNutritionApi(): NutritionApi {
        return provideRetrofit().create(NutritionApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideNutritionRepository(
        localDao: MealLogDao,
        remoteApi: NutritionApi
    ): NutritionRepository {
        return NutritionRepository(localDao, remoteApi)
    }
}
```

### Using Injected Dependencies

```kotlin
class NutritionViewModel @Inject constructor(
    private val repository: NutritionRepository,
    private val aiCoachService: AiCoachService
) : ViewModel() {
    // Implementation
}

@Composable
fun MyScreen() {
    val viewModel: NutritionViewModel = hiltViewModel()
}
```

---

## Error Handling

### Repository Pattern for Error Handling

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

suspend fun loadFoodLogs(): Result<List<MealLog>> {
    return try {
        val logs = mealLogDao.getAllLogs().first()
        Result.Success(logs.map { it.toDomain() })
    } catch (e: Exception) {
        Result.Error(e)
    }
}
```

---

## State Management Best Practices

1. **Use StateFlow for reactive updates:**
   - Immutable state
   - Lifecycle-aware collection
   - Efficient recomposition

2. **Separate UI state from business logic:**
   - `UiState` for UI-specific state (loading, error, success)
   - `DomainState` for business-level data

3. **Avoid state leaks:**
   - Always cancel flows in viewModelScope
   - Use `collectAsStateWithLifecycle()` in Compose

---

## Testing Strategy

### Unit Tests
- Test ViewModels with mock repositories
- Test Repository logic with mock DAOs and APIs
- Use JUnit 4 and Mockk

### UI Tests
- Robolectric for Android framework simulation
- Roborazzi for screenshot comparisons
- Espresso for integration testing on devices

---

This architecture ensures scalability, maintainability, and testability of the Nutrica application.