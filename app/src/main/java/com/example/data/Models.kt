package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mealType: String, // Breakfast, Lunch, Dinner, Snacks
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val potassium: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0,
    val vitC: Double = 0.0,
    val vitD: Double = 0.0,
    val vitB12: Double = 0.0,
    val cholesterol: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val servingSize: String = "1 serving"
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weightKg: Double,
    val bodyFat: Double = 0.0,
    val waistCm: Double = 0.0,
    val hipCm: Double = 0.0,
    val chestCm: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single profile row
    val name: String = "Parth C",
    val weightKg: Double = 75.0,
    val heightCm: Double = 178.0,
    val age: Int = 24,
    val gender: String = "Male",
    val bodyFat: Double = 15.0,
    val activityLevel: String = "Moderate", // Sedentary, Moderate, Active, Athlete
    val goal: String = "Lose Weight", // Lose Weight, Maintain, Bulk, Recomp
    val calorieGoal: Double = 2000.0,
    val proteinGoal: Double = 140.0,
    val carbsGoal: Double = 220.0,
    val fatGoal: Double = 60.0,
    val waterGoalMl: Int = 2500,
    val xp: Int = 0,
    val level: Int = 1
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val emailOrPhone: String, // Email or phone used as login identifier
    val fullName: String,
    val passwordHash: String, // Securely hashed password (SHA-256)
    val isGoogleSso: Boolean = false,
    val username: String? = null,
    val fitnessGoal: String? = null, // "Weight gain", "Weight Loss", "Muscle Gain + Weight Gain"
    val activityLevel: String? = null, // "low", "medium", "high"
    val isOnboarded: Boolean = false
)

@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "coach"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
