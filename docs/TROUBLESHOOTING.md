# Nutrica Troubleshooting Guide

This document provides solutions to common issues encountered during development and deployment of Nutrica.

---

## Table of Contents

1. [Build and Compilation Issues](#build-and-compilation-issues)
2. [Runtime Issues](#runtime-issues)
3. [API Integration Issues](#api-integration-issues)
4. [Database Issues](#database-issues)
5. [UI and Layout Issues](#ui-and-layout-issues)
6. [Testing Issues](#testing-issues)
7. [Performance Issues](#performance-issues)

---

## Build and Compilation Issues

### Gradle Sync Fails: "Duplicate class"

**Symptom:**
```
E: Duplicate class com.google.android.material.Theme found in modules
```

**Causes:**
- Conflicting dependency versions
- Multiple versions of same library

**Solution:**
```kotlin
// build.gradle.kts
dependencies {
    // Use version catalog for consistency
    implementation(libs.androidx.material3)
    
    // Exclude conflicting transitive dependencies
    implementation("com.library:core") {
        exclude(group = "com.google.android.material")
    }
}

// Check dependency tree
// ./gradlew :app:dependencies
```

### Build Error: "Java compiler is out of range"

**Symptom:**
```
Java compiler is out of range: 21. Please use 17 or earlier
```

**Cause:** JDK version mismatch

**Solution:**
```bash
# Check Java version
java -version

# Set JDK 17 in Android Studio
# File > Project Structure > SDK > JDK location

# Or via environment variable
export JAVA_HOME=/path/to/jdk-17
```

### Gradle Build Error: "Could not determine artifacts for"

**Symptom:**
```
Could not determine artifacts for com.example:library:1.0.0
```

**Causes:**
- Network connectivity issue
- Repository not configured
- Invalid dependency version

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean

# Clear local Maven cache
rm -rf ~/.m2/repository

# Rebuild with dependency download
./gradlew build --refresh-dependencies

# Check repositories in build.gradle.kts
repositories {
    google()
    mavenCentral()
}
```

### "Cannot resolve symbol" Errors

**Symptom:**
```
Cannot resolve symbol 'viewModel'
```

**Solutions:**
```bash
# 1. Invalidate Android Studio cache
# File > Invalidate Caches > Invalidate and Restart

# 2. Rebuild project
./gradlew clean build

# 3. Restart Android Studio

# 4. Check imports are correct
```

---

## Runtime Issues

### App Crashes on Startup

**Symptom:**
```
E/AndroidRuntime: FATAL EXCEPTION: main
E/AndroidRuntime: java.lang.RuntimeException: Unable to start activity
```

**Diagnosis:**
```bash
# View full stack trace
adb logcat | grep AndroidRuntime
```

**Common Causes & Solutions:**

1. **Missing API Key:**
   ```kotlin
   // Check BuildConfig
   if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
       throw IllegalStateException("GEMINI_API_KEY not set in .env")
   }
   ```

2. **Database Migration Issue:**
   ```kotlin
   // Check Room database version
   @Database(
       entities = [...],
       version = 2,  // Increment if schema changed
       autoMigrations = [AutoMigration(from = 1, to = 2)]
   )
   ```

3. **Memory Leak:**
   ```kotlin
   // Use lifecycle-aware observers
   viewModel.data.observe(viewLifecycleOwner) { data ->
       updateUI(data)  // Automatically cleaned up
   }
   ```

### App Crashes When Scanning Barcode

**Symptom:**
```
NullPointerException: Attempt to invoke virtual method on null object
```

**Solution:**
```kotlin
// Add null checks
suspend fun scanBarcode(barcode: String) {
    val product = apiService.getProductByBarcode(barcode)
    
    if (product?.name == null) {
        showError("Product not found")
        return
    }
    
    addToLog(product)
}
```

### Memory Leak: Activity Not Garbage Collected

**Symptom:**
```
W/System.err: java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```kotlin
// 1. Use WeakReference for listeners
private val listeners = mutableListOf<WeakReference<ApiListener>>()

// 2. Unregister listeners in onDestroy
override fun onDestroy() {
    listeners.clear()
    super.onDestroy()
}

// 3. Use viewLifecycleOwner in fragments
lifecycleScope.launch {
    viewModel.data.collect { data ->
        updateUI(data)
    }
}
```

---

## API Integration Issues

### 401 Unauthorized: "Invalid API Key"

**Symptom:**
```
E/API: Error 401: Invalid authentication credentials
```

**Solutions:**

1. **Verify API Key in .env:**
   ```bash
   cat .env | grep GEMINI_API_KEY
   ```

2. **Check key format:**
   ```properties
   # Correct - no quotes
   GEMINI_API_KEY=AIza...xyz
   
   # Incorrect - with quotes
   GEMINI_API_KEY="AIza...xyz"
   ```

3. **Rebuild project to inject key:**
   ```bash
   ./gradlew clean assembleDebug
   ```

4. **Verify in BuildConfig:**
   ```kotlin
   Log.d("API", "Key exists: ${BuildConfig.GEMINI_API_KEY.isNotEmpty()}")
   ```

### 429 Too Many Requests: "Rate Limited"

**Symptom:**
```
E/API: Error 429: Rate limit exceeded
```

**Solutions:**

```kotlin
// 1. Implement exponential backoff
suspend fun apiCallWithBackoff(): Result {
    var delayMs = 1000L
    repeat(3) {
        return try {
            apiService.call()
        } catch (e: RateLimitException) {
            delay(delayMs)
            delayMs *= 2
            null
        }
    }
}

// 2. Add request throttling
private val throttler = ApiThrottler(requestsPerSecond = 2)

// 3. Cache responses
@Cache
suspend fun getCachedProduct(barcode: String): Product
```

### Network Timeout: "Connection Timeout"

**Symptom:**
```
E/API: Timeout after 30 seconds
```

**Solutions:**

```kotlin
// Increase timeout
val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

// Or retry with different endpoint
suspend fun apiCallWithFallback(): Result {
    return try {
        primaryApi.call()
    } catch (e: TimeoutException) {
        fallbackApi.call()
    }
}
```

---

## Database Issues

### "Room database is locked"

**Symptom:**
```
E/Database: database is locked
```

**Causes:**
- Multiple threads accessing database simultaneously
- Long-running transaction

**Solution:**
```kotlin
// Use database transactions properly
withContext(Dispatchers.IO) {
    database.runInTransaction {
        // Multiple operations within transaction
        foodItemDao.insert(item)
        mealLogDao.insert(log)
    }
}

// Or use coroutines
launchIO {
    dao.insert(item)
}
```

### Room Migration Error

**Symptom:**
```
E/Room: Cannot find migration from 1 to 2
```

**Solution:**
```kotlin
// Define migration
val migration1To2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE food_cache (" +
            "id INTEGER PRIMARY KEY, " +
            "name TEXT NOT NULL)"
        )
    }
}

// Add to Room builder
Room.databaseBuilder(context, AppDatabase::class.java, "nutrica_db")
    .addMigrations(migration1To2)
    .build()
```

---

## UI and Layout Issues

### Text Overflow: "The following assertion was not true: hasMaxWidth"

**Symptom:**
```
Exception: The following assertion was not true: hasMaxWidth
```

**Solution:**
```kotlin
Text(
    text = "Long text",
    modifier = Modifier.fillMaxWidth(),  // Add constraint
    maxLines = 2,                        // Limit lines
    overflow = TextOverflow.Ellipsis     // Add ellipsis
)
```

### Navigation Not Working

**Symptom:**
```
Back press not handled, App crashes
```

**Solution:**
```kotlin
// Ensure NavGraph is set up correctly
NavHost(
    navController = navController,
    startDestination = "home_route"
) {
    composable("home_route") { HomeScreen() }
    composable("detail_route/{id}") { DetailScreen() }
}

// Handle back press
BackHandler(enabled = true) {
    if (!navController.popBackStack()) {
        // No more screens, close app
    }
}
```

### Compose Recomposition Performance

**Symptom:**
```
App UI becomes sluggish, excessive recompositions
```

**Solution:**
```kotlin
// 1. Use remember for expensive calculations
val expensiveValue = remember {
    computeExpensiveValue()
}

// 2. Use stable data classes
@Immutable
data class UserData(
    val id: Int,
    val name: String
)

// 3. Avoid state in function parameters
@Composable
fun ProfileCard(user: UserData) {  // Stable parameter
    // No recomposition if user object is unchanged
}
```

---

## Testing Issues

### Unit Test Fails: "Cannot use Mockk with Compose"

**Symptom:**
```
Exception: Cannot instantiate @Composable function
```

**Solution:**
```kotlin
// Use Robolectric for Android-dependent tests
@RunWith(RobolectricTestRunner::class)
class NutritionScreenTest {
    @Test
    fun testUI() {
        // Robolectric provides Android context
    }
}

// Mock non-Composable dependencies
@Test
fun testViewModel() {
    val mockRepository = mockk<NutritionRepository>()
    val viewModel = NutritionViewModel(mockRepository)
    
    // Test ViewModel logic
}
```

### Instrumented Test Fails: "Activity not found"

**Symptom:**
```
android.content.ActivityNotFoundException
```

**Solution:**
```bash
# Run tests on emulator/device
adb shell am instrument -w com.example.nutrica.test/androidx.test.runner.AndroidJUnitRunner

# Or use Android Studio UI test runner
```

---

## Performance Issues

### App Startup Time Too Slow

**Profiling:**
```bash
# Use Android Studio Profiler
# Run > Profile 'app'

# Check startup time
adb shell am start -W com.example.nutrica/.MainActivity
```

**Optimization:**
```kotlin
// 1. Lazy initialize expensive resources
private val aiService: AiCoachService by lazy {
    AiCoachService()
}

// 2. Move heavy operations to background
launchIO {
    loadInitialData()
}

// 3. Use WorkManager for background tasks
OneTimeWorkRequestBuilder<DataSyncWorker>()
    .build()
    .also { WorkManager.getInstance(context).enqueueUniqueWork(...) }
```

### High Memory Usage

**Profiling:**
```bash
# Android Studio Memory Profiler
# Profiler tab > Memory
```

**Solutions:**

```kotlin
// 1. Cache images with proper size
Glide.with(context)
    .load(url)
    .override(300, 300)  // Limit size
    .into(imageView)

// 2. Release resources in onDestroy
override fun onDestroy() {
    viewModel.cancelJobs()
    imageLoader.shutdown()
    super.onDestroy()
}
```

---

## Getting Help

If you encounter issues not listed here:

1. **Search existing issues:** [GitHub Issues](https://github.com/ChaudhariParthh/Nutrica/issues)
2. **Check logs:** `adb logcat | grep nutrica`
3. **Enable debug logging:** Set `DEBUG=true` in BuildConfig
4. **Create new issue:** Include stack trace, Android version, device model
5. **Ask community:** [GitHub Discussions](https://github.com/ChaudhariParthh/Nutrica/discussions)

---

**Last Updated:** 2024
**Maintained by:** Nutrica Development Team