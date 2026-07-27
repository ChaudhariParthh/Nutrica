# Nutrica API Integration Guide

This document describes all external APIs integrated into Nutrica, their endpoints, authentication methods, and usage patterns.

---

## Table of Contents

1. [Overview](#overview)
2. [Gemini AI API](#gemini-ai-api)
3. [Barcode/Nutrition Database APIs](#barcodenutrition-database-apis)
4. [Authentication](#authentication)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)
7. [Testing APIs](#testing-apis)

---

## Overview

Nutrica integrates with the following external services:

| Service | Purpose | Authentication | Base URL |
|---------|---------|-----------------|----------|
| Google Gemini | AI Coaching & Recipe Generation | API Key | `https://generativelanguage.googleapis.com/v1beta/` |
| Open Food Facts | Product Nutrition Data | Public | `https://world.openfoodfacts.org/api/v3/` |
| Edamam API | Nutrition Analysis | API Key + ID | `https://api.edamam.com/api/` |

---

## Gemini AI API

### Overview
The Gemini API provides AI-powered features:
- Nutrition coaching and personalized advice
- Recipe generation and modification
- Dietary analysis and recommendations
- Allergen and nutritional warnings

### Authentication

```kotlin
// Environment variable injection
val geminiKey = BuildConfig.GEMINI_API_KEY

// Initialize Gemini client
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = geminiKey,
    generationConfig = generationConfig {
        temperature = 0.9f
        topK = 16
        topP = 0.95f
    }
)
```

### Endpoints

#### 1. Generate AI Coaching Advice

**Endpoint:**
```
POST /v1beta/models/gemini-2.0-flash:generateContent
```

**Request:**
```kotlin
data class CoachingRequest(
    val userProfile: UserProfile,
    val recentLogs: List<MealLog>,
    val goals: List<String>
)

suspend fun getCoachingAdvice(request: CoachingRequest): String {
    val prompt = buildString {
        append("User Profile: ${request.userProfile}\n")
        append("Recent meals: ${request.recentLogs}\n")
        append("Goals: ${request.goals}\n")
        append("Provide personalized nutrition coaching advice.")
    }
    
    val response = generativeModel.generateContent(prompt)
    return response.text ?: ""
}
```

**Response:**
```json
{
  "candidates": [
    {
      "content": {
        "role": "model",
        "parts": [
          {
            "text": "Based on your recent meals..."
          }
        ]
      },
      "finishReason": "STOP",
      "index": 0
    }
  ]
}
```

#### 2. Generate Custom Recipes

**Request:**
```kotlin
data class RecipeGenerationRequest(
    val ingredients: List<String>,
    val dietaryPreferences: List<String>,
    val calorieTarget: Int,
    val servings: Int
)

suspend fun generateRecipe(request: RecipeGenerationRequest): Recipe {
    val prompt = buildString {
        append("Create a recipe with these ingredients: ${request.ingredients.joinToString()}\n")
        append("Dietary restrictions: ${request.dietaryPreferences.joinToString()}\n")
        append("Target calories: ${request.calorieTarget}\n")
        append("Servings: ${request.servings}\n")
        append("Return as JSON with: name, instructions, nutrition")
    }
    
    val response = generativeModel.generateContent(prompt)
    return parseRecipeResponse(response.text ?: "")
}
```

### Rate Limits
- Free tier: 60 requests per minute
- Paid tier: 2000 requests per minute
- Implement exponential backoff for retries

### Usage Monitoring

```kotlin
data class ApiUsage(
    val requestsToday: Int,
    val requestsTotal: Int,
    val quotaExhausted: Boolean
)

fun checkApiQuota(): ApiUsage {
    // Query usage metrics from Gemini Console
}
```

---

## Barcode/Nutrition Database APIs

### Open Food Facts API

#### Overview
Free API providing nutrition data for scanned barcodes.

#### Authentication
Public API - no authentication required.

#### Endpoints

**Search Product by Barcode:**
```kotlin
interface OpenFoodFactsApi {
    @GET("product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ProductResponse
}

data class ProductResponse(
    val code: String,
    val product: Product
)

data class Product(
    val product_name: String,
    val energy_100g: Double,  // kcal per 100g
    val proteins_100g: Double,
    val carbohydrates_100g: Double,
    val fat_100g: Double,
    val ingredients_text: String,
    val allergens: String
)
```

**Usage Example:**
```kotlin
suspend fun scanFoodBarcode(barcode: String): NutritionData? {
    return try {
        val response = openFoodFactsApi.getProductByBarcode(barcode)
        if (response.product != null) {
            response.product.toNutritionData()
        } else null
    } catch (e: Exception) {
        Log.e("BarcodeAPI", "Failed to fetch product", e)
        null
    }
}
```

### Edamam Nutrition Analysis API

#### Overview
Premium API for detailed nutrition analysis of food recipes.

#### Authentication
```kotlin
const val EDAMAM_APP_ID = BuildConfig.EDAMAM_APP_ID
const val EDAMAM_APP_KEY = BuildConfig.EDAMAM_APP_KEY

interface EdamamApi {
    @GET("nutrition-details")
    suspend fun analyzeRecipe(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Body recipe: RecipeAnalysisRequest
    ): NutritionAnalysisResponse
}
```

#### Endpoints

**Analyze Recipe Nutrition:**
```kotlin
data class RecipeAnalysisRequest(
    val ingr: List<String>  // List of ingredients with quantities
)

data class NutritionAnalysisResponse(
    val uri: String,
    val yield: Double,
    val calories: Double,
    val totalCO2Emissions: Double,
    val co2EmissionsClass: String,
    val totalWeight: Double,
    val dietLabels: List<String>,
    val healthLabels: List<String>,
    val nutrients: NutrientMap
)

data class NutrientMap(
    val ENERC_KCAL: NutrientValue,  // Calories
    val PROCNT: NutrientValue,      // Protein
    val FAT: NutrientValue,         // Total fats
    val CHOCDF: NutrientValue       // Carbs
)

data class NutrientValue(
    val label: String,
    val quantity: Double,
    val unit: String
)
```

**Usage Example:**
```kotlin
suspend fun analyzeRecipeNutrition(ingredients: List<String>): NutritionAnalysis? {
    return try {
        val request = RecipeAnalysisRequest(
            ingr = ingredients.map { "1 ${it}" }
        )
        val response = edamamApi.analyzeRecipe(
            appId = EDAMAM_APP_ID,
            appKey = EDAMAM_APP_KEY,
            recipe = request
        )
        response.toNutritionAnalysis()
    } catch (e: Exception) {
        Log.e("EdamamAPI", "Analysis failed", e)
        null
    }
}
```

---

## Authentication

### API Key Management

**Secure Storage:**
```kotlin
// Keys stored in BuildConfig via Secrets Gradle Plugin
val geminiKey = BuildConfig.GEMINI_API_KEY
val edamamId = BuildConfig.EDAMAM_APP_ID
val edamamKey = BuildConfig.EDAMAM_APP_KEY

// Never log or expose keys
Private fun sanitizeUrl(url: String): String {
    return url.replace(Regex("key=[^&]+"), "key=***")
}
```

### Header Injection

```kotlin
@Provides
@Singleton
fun provideRetrofit(): Retrofit {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val urlWithKey = original.url.newBuilder()
                .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                .build()
            
            val request = original.newBuilder()
                .url(urlWithKey)
                .addHeader("Content-Type", "application/json")
                .build()
            
            chain.proceed(request)
        }
        .build()
    
    return Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
}
```

---

## Error Handling

### Common API Errors

```kotlin
sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized : ApiException("Invalid or expired API key")
    class RateLimited : ApiException("Rate limit exceeded")
    class NotFound : ApiException("Resource not found")
    class ServerError : ApiException("Server error")
    class NetworkError(cause: Throwable) : ApiException("Network error: ${cause.message}")
}

// Convert HTTP errors
fun handleApiError(response: Response<*>): ApiException {
    return when (response.code()) {
        401 -> ApiException.Unauthorized()
        404 -> ApiException.NotFound()
        429 -> ApiException.RateLimited()
        in 500..599 -> ApiException.ServerError()
        else -> ApiException.NetworkError(Exception("HTTP ${response.code()}"))
    }
}
```

### Retry Logic

```kotlin
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    delayMs: Long = 1000,
    block: suspend () -> T
): T {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        return try {
            block()
        } catch (e: ApiException.RateLimited) {
            lastException = e
            delay(delayMs * (attempt + 1))
            null
        } catch (e: Exception) {
            throw e
        }
    }
    
    throw lastException ?: Exception("Max retries exceeded")
}

// Usage
suspend fun getProductWithRetry(barcode: String) {
    retryWithBackoff {
        openFoodFactsApi.getProductByBarcode(barcode)
    }
}
```

---

## Rate Limiting

### Request Throttling

```kotlin
class ApiThrottler(private val requestsPerSecond: Int = 5) {
    private val semaphore = Semaphore(requestsPerSecond)
    private val scope = CoroutineScope(Dispatchers.Default)
    
    suspend fun <T> throttle(block: suspend () -> T): T {
        semaphore.acquire()
        try {
            return block()
        } finally {
            scope.launch {
                delay(1000L / requestsPerSecond)
                semaphore.release()
            }
        }
    }
}

// Usage
private val throttler = ApiThrottler(requestsPerSecond = 3)

suspend fun getProductThrottled(barcode: String) {
    throttler.throttle {
        openFoodFactsApi.getProductByBarcode(barcode)
    }
}
```

---

## Testing APIs

### Mock API Responses

```kotlin
class MockNutritionApi : NutritionApi {
    override suspend fun getProductByBarcode(barcode: String): ProductResponse {
        return ProductResponse(
            code = barcode,
            product = Product(
                product_name = "Mock Product",
                energy_100g = 150.0,
                proteins_100g = 20.0,
                carbohydrates_100g = 10.0,
                fat_100g = 5.0,
                ingredients_text = "Water, Salt",
                allergens = "None"
            )
        )
    }
}
```

### Integration Testing

```kotlin
@Test
fun testBarcodeApi() = runTest {
    val api = provideNutritionApi()
    val response = api.getProductByBarcode("5901001123457")
    
    assertNotNull(response.product)
    assertNotNull(response.product.product_name)
    assertTrue(response.product.energy_100g > 0)
}
```

---

For more information, refer to:
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Open Food Facts API](https://world.openfoodfacts.org/api/v3/)
- [Edamam Nutrition API](https://developer.edamam.com/)