package com.example.ui

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.network.*
import com.example.ui.theme.*
import com.example.viewmodel.NutricaViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN WRAPPER COMPOSE SCREENS ---

@Composable
fun DashboardScreen(
    viewModel: NutricaViewModel,
    onNavigateToProfile: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val foodLogs by viewModel.selectedDayFoodLogs.collectAsState()
    val waterLogs by viewModel.selectedDayWaterLogs.collectAsState()
    val weightLogs by viewModel.allWeightLogs.collectAsState()
    val foodStreak by viewModel.foodStreak.collectAsState()
    val waterStreak by viewModel.waterStreak.collectAsState()

    // Aggregate values
    val caloriesToday = foodLogs.sumOf { it.calories }
    val proteinToday = foodLogs.sumOf { it.protein }
    val carbsToday = foodLogs.sumOf { it.carbs }
    val fatToday = foodLogs.sumOf { it.fat }
    val fiberToday = foodLogs.sumOf { it.fiber }
    val sugarToday = foodLogs.sumOf { it.sugar }
    val waterToday = waterLogs.sumOf { it.amountMl }

    val calorieGoal = profile.calorieGoal
    val proteinGoal = profile.proteinGoal
    val carbsGoal = profile.carbsGoal
    val fatGoal = profile.fatGoal
    val waterGoal = profile.waterGoalMl

    val caloriesRemaining = (calorieGoal - caloriesToday).coerceAtLeast(0.0)

    val healthScore = viewModel.calculateDailyHealthScore(
        caloriesToday, proteinToday, waterToday, fiberToday, sugarToday,
        calorieGoal, proteinGoal, waterGoal
    )

    // Modal stats logging
    var showWeightDialog by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var bodyFatInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 1. Header (User Info + Streak + XP Level)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hamburger Menu button
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu",
                                tint = EmeraldGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .clickable { onNavigateToProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(2).uppercase(),
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = "Welcome back,",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                            Text(
                                text = profile.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Streak and Level Badges
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StreakPink.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = StreakPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$foodStreak Days",
                                        color = StreakPink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Level XP Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level ${profile.level}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldGreen
                        )
                        val nextLvlXp = profile.level * 150
                        Text(
                            text = "${profile.xp} / ${nextLvlXp} XP",
                            fontSize = 11.sp,
                            color = SlateTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val nextLvlXp = profile.level * 150
                    LinearProgressIndicator(
                        progress = { (profile.xp.toFloat() / nextLvlXp.toFloat()).coerceIn(0.0f, 1.0f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldGreen,
                        trackColor = EmeraldGreen.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // 2. Circular Calories Ring Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calories_ring_card"),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "Calories Remaining",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextSecondary
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%,d", caloriesRemaining.toInt()),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Goal",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Goal: ${calorieGoal.toInt()} kcal",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = "Eaten",
                                tint = CobaltBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Eaten: ${caloriesToday.toInt()} kcal",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    // Circular Progress Canvas
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            // Track
                            drawCircle(
                                color = EmeraldGreen.copy(alpha = 0.1f),
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Progress
                            val sweepAngle = ((caloriesToday / calorieGoal) * 360f).toFloat().coerceIn(0f, 360f)
                            drawArc(
                                color = EmeraldGreen,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${((caloriesToday / calorieGoal) * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Met",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Macros Progress Row (Protein, Carbs, Fat)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Macronutrients Today",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MacroItem(
                            label = "Protein",
                            current = proteinToday,
                            goal = proteinGoal,
                            color = CobaltBlue,
                            unit = "g",
                            modifier = Modifier.weight(1.0f)
                        )
                        MacroItem(
                            label = "Carbs",
                            current = carbsToday,
                            goal = carbsGoal,
                            color = GoldenAmber,
                            unit = "g",
                            modifier = Modifier.weight(1.0f)
                        )
                        MacroItem(
                            label = "Fat",
                            current = fatToday,
                            goal = fatGoal,
                            color = RoseRed,
                            unit = "g",
                            modifier = Modifier.weight(1.0f)
                        )
                    }
                }
            }
        }

        // 4. Daily AI Health Score Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_health_score_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = GoldenAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily AI Health Score",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Score Circle
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        healthScore >= 80 -> EmeraldGreen.copy(alpha = 0.12f)
                                        healthScore >= 50 -> GoldenAmber.copy(alpha = 0.12f)
                                        else -> RoseRed.copy(alpha = 0.12f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$healthScore%",
                                color = when {
                                    healthScore >= 80 -> EmeraldGreen
                                    healthScore >= 50 -> GoldenAmber
                                    else -> RoseRed
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = when {
                                    healthScore >= 90 -> "Excellent Balance! Streak active."
                                    healthScore >= 75 -> "Great job! Keep monitoring fiber."
                                    healthScore >= 50 -> "Decent, but sugar/fat intake is high."
                                    else -> "Keep logging. Let's hit the hydration goal!"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Calculated in real-time using calories deviation, protein goals, fiber intake, sugar limit, and hydration logs.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Water Tracker Card (with dynamic Bottle Animation)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("water_tracker_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Water Tracker",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$waterToday / $waterGoal ml",
                                fontSize = 13.sp,
                                color = SlateTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanWater.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$waterStreak Days Streak",
                                color = CyanWater,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Custom Water Bottle Animation Drawing
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(2.dp, CyanWater.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                        ) {
                            // Liquid level based on percent
                            val percent = (waterToday.toFloat() / waterGoal.toFloat()).coerceIn(0.0f, 1.0f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(percent)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(CyanWater.copy(alpha = 0.6f), CyanWater)
                                        )
                                    )
                            )
                        }

                        // Hydro Quick Addition Buttons
                        Column(
                            modifier = Modifier.weight(1.0f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.addWaterLog(250) },
                                    modifier = Modifier.weight(1.0f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanWater)
                                ) {
                                    Text("+250 ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.addWaterLog(500) },
                                    modifier = Modifier.weight(1.0f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanWater)
                                ) {
                                    Text("+500 ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.addWaterLog(100) },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, CyanWater)
                            ) {
                                Text("+100 ml Sip", color = CyanWater, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 6. Weight Tracker Trend Graph Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weight_tracker_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Weight Tracker",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Current Weight: ${profile.weightKg} kg",
                                fontSize = 12.sp,
                                color = SlateTextSecondary
                            )
                        }

                        IconButton(
                            onClick = { showWeightDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(EmeraldGreen.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Log Weight",
                                tint = EmeraldGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line Graph Canvas drawing Weight trend
                    if (weightLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No weight history recorded yet.\nTap the + button to log.",
                                color = SlateTextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val sortedLogs = weightLogs.sortedBy { it.timestamp }.takeLast(7)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizeWidth = size.width
                                val sizeHeight = size.height

                                if (sortedLogs.size > 1) {
                                    val weights = sortedLogs.map { it.weightKg }
                                    val minWeight = weights.minOrNull() ?: 0.0
                                    val maxWeight = weights.maxOrNull() ?: 100.0
                                    val range = (maxWeight - minWeight).coerceAtLeast(1.0)

                                    val points = sortedLogs.mapIndexed { idx, log ->
                                        val x = idx.toFloat() / (sortedLogs.size - 1) * sizeWidth
                                        val y = sizeHeight - ((log.weightKg - minWeight) / range * sizeHeight).toFloat()
                                        Offset(x, y)
                                    }

                                    val path = Path()
                                    path.moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        path.lineTo(points[i].x, points[i].y)
                                    }

                                    drawPath(
                                        path = path,
                                        color = EmeraldGreen,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )

                                    // Draw circles on points
                                    points.forEach { pt ->
                                        drawCircle(
                                            color = EmeraldGreen,
                                            radius = 5.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = PureWhite,
                                            radius = 2.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                } else {
                                    // Draw single point centered
                                    drawCircle(
                                        color = EmeraldGreen,
                                        radius = 6.dp.toPx(),
                                        center = Offset(sizeWidth / 2, sizeHeight / 2)
                                    )
                                }
                            }
                        }

                        // Graph labels row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            sortedLogs.forEach { log ->
                                val date = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(log.timestamp))
                                Text(
                                    text = date,
                                    fontSize = 10.sp,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gamification Achievement Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Achievement",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Level up challenge!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Earn 50 XP to unlock 'Nutrica Pro Athlete' badge.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 7. Tailored Daily Feed & Recommendations
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tailored_feed_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Tailored",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Tailored Feed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI Curated",
                                fontSize = 10.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Based on your active Fitness Goal (${profile.goal}) and ${profile.activityLevel} activity, here are today's tailored actions:",
                        fontSize = 12.sp,
                        color = SlateTextSecondary,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Tailored Item 1 (Meal Recommendation)
                    val mealTip = when (profile.goal) {
                        "Lose Weight" -> Pair("Calorie Deficit Breakfast", "Oatmeal with chia seeds, blueberries, and a scoop of whey protein (340 kcal).")
                        "Bulk" -> Pair("High-Calorie Mass Gainer shake", "Blended bananas, peanut butter, whole milk, oats, and 2 scoops of whey (820 kcal).")
                        else -> Pair("Lean Muscle Recomposition Bowl", "Quinoa, roasted chicken breast, mixed greens, avocado, and olive oil vinaigrette (580 kcal).")
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = "Meal Option", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mealTip.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text(mealTip.second, fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }

                    // Tailored Item 2 (Exercise/Activity Recommendation)
                    val activityTip = when (profile.activityLevel) {
                        "Sedentary" -> Pair("Desk-Job Recovery Walk", "Walk for 15 minutes after lunch. Boosts blood sugar clearance and adds 2,000 active steps.")
                        "Active" -> Pair("High-Intensity Core Burner", "Execute a 25-minute HIIT interval session with 45s work / 15s rest cycles to fuel active metabolism.")
                        else -> Pair("Structured Resistance Session", "Incorporate a 40-minute compound lifting session (Squats, Bench Press, Rows) to drive muscle protein synthesis.")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CobaltBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = "Activity Option", tint = CobaltBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activityTip.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text(activityTip.second, fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }

                    // Tailored Item 3 (Macronutrient advice)
                    val macroTip = when (profile.goal) {
                        "Lose Weight" -> Pair("High-Satiety Protein Targets", "Prioritize consuming at least ${profile.proteinGoal.toInt()}g of lean protein to protect muscle tissue from breakdown.")
                        "Bulk" -> Pair("Carbohydrate Energy Loading", "Load up on ${profile.carbsGoal.toInt()}g of complex carbs (brown rice, sweet potato) to sustain muscular power.")
                        else -> Pair("Balanced Muscle Synthesis", "Maintain a pristine macro ratio with ${profile.proteinGoal.toInt()}g Protein / ${profile.carbsGoal.toInt()}g Carbs / ${profile.fatGoal.toInt()}g healthy fats.")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldenAmber.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Egg, contentDescription = "Macro Option", tint = GoldenAmber, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(macroTip.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text(macroTip.second, fontSize = 11.sp, color = SlateTextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Modal Add Weight Dialog
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Log Body Weight") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bodyFatInput,
                        onValueChange = { bodyFatInput = it },
                        label = { Text("Body Fat % (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    onClick = {
                        val wt = weightInput.toDoubleOrNull()
                        val fat = bodyFatInput.toDoubleOrNull() ?: 0.0
                        if (wt != null) {
                            viewModel.addWeightLog(wt, fat, 0.0, 0.0, 0.0)
                            showWeightDialog = false
                            weightInput = ""
                            bodyFatInput = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MacroItem(
    label: String,
    current: Double,
    goal: Double,
    color: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (goal > 0) (current / goal).toFloat().coerceIn(0.0f, 1.0f) else 0.0f

    Column(
        modifier = modifier
            .background(Color(0x0EFFFFFF), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextSecondary
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${current.toInt()}$unit",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "/ ${goal.toInt()}$unit",
            fontSize = 10.sp,
            color = SlateTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

// --- SEARCH & SCAN SCREEN (AI-Powered Scanner & natural language log) ---

@Composable
fun SearchAndScanScreen(
    viewModel: NutricaViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.aiLogLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }

    // Tab scanning mode
    var activeTab by remember { mutableIntStateOf(0) } // 0: Text, 1: AI Camera Scanner, 2: Barcode Scanner

    // Sample Base64 image presets for direct camera scan demo
    val cameraPresets = listOf(
        Pair("Grilled Chicken Plate", "https://images.unsplash.com/photo-1532550907401-a500c9a57435"),
        Pair("Salmon Avocado Salad", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"),
        Pair("Sushi Combo Platter", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c"),
        Pair("Oatmeal Berry Bowl", "https://images.unsplash.com/photo-1517686469429-8faf88b9f7af")
    )

    // Sample base64 placeholder just to trigger the API.
    val placeholderBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="

    // Barcode mock presets
    val barcodePresets = listOf(
        Triple("7613035987321", "Greek Yogurt (Nonfat)", "High Protein, contains Lactose. Allergens: Milk. Nutrition Facts: 100 kcal, P: 15g, C: 4g, F: 0g. Health Rating: Grade A"),
        Triple("5010049100234", "Oats & Honey Granola", "High Fiber, contains Gluten. Allergens: Oats. Nutrition Facts: 220 kcal, P: 6g, C: 35g, F: 5g. Health Rating: Grade B"),
        Triple("0074182001150", "Peanut Fudge Protein Bar", "High Protein, contains Soy, Peanuts. Nutrition Facts: 250 kcal, P: 20g, C: 22g, F: 8g. Health Rating: Grade B")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 64.dp)
    ) {
        // Top Header Row with Menu Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = EmeraldGreen
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log & Scan Food",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        // Mode Selector Tab (Text, Camera, Barcode)
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = EmeraldGreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Smart Text") },
                icon = { Icon(Icons.Default.Keyboard, contentDescription = "Text input") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("AI Camera") },
                icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Camera") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Barcode") },
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Barcode scanner") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meal type selection row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Log Meal:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Breakfast", "Lunch", "Dinner", "Snacks").forEach { mt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedMealType == mt) EmeraldGreen else EmeraldGreen.copy(alpha = 0.1f)
                            )
                            .clickable { selectedMealType = mt }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = mt,
                            color = if (selectedMealType == mt) PureWhite else EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading and Error Messages
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = EmeraldGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nutrica AI is analyzing ingredients & nutrition...",
                        color = SlateTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        errorMsg?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = RoseRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = RoseRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = RoseRed,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1.0f)
                    )
                    IconButton(onClick = { viewModel.clearErrorMessage() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseRed)
                    }
                }
            }
        }

        // --- SUB CONTENT BASED ON TAB ---
        when (activeTab) {
            0 -> {
                // Natural Text Logging Screen
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Natural AI Food Log",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Type exactly what you ate, including quantities. AI calculates oils, ingredients, portion weights, and adds them directly to today's summary.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("E.g., I ate 2 chapatis with a bowl of paneer tikka masala and a glass of buttermilk.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("natural_search_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Microphone icon Voice log simulator
                                Button(
                                    onClick = {
                                        searchQuery = "Had 1 cup Greek Yogurt with 50g strawberries and honey"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CobaltBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("voice_log_button")
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Voice Log")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Say Log")
                                }

                                Button(
                                    onClick = {
                                        if (searchQuery.isNotBlank()) {
                                            viewModel.parseNaturalFoodLog(searchQuery, selectedMealType)
                                            searchQuery = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .testTag("parse_ai_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Analyze")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze Food", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Predefined easy prompts
                    Text(
                        text = "Popular natural searches:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val samples = listOf(
                            "Had one bowl of chicken biryani",
                            "Ate 3 idlis and coconut chutney",
                            "I drank one glass mango shake",
                            "Double cheeseburger and fries"
                        )
                        items(samples) { sample ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clickable { searchQuery = sample }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = sample, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            1 -> {
                // AI Camera Plate Scanner
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "AI Photo Food Scanner",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Select a simulated photo preset. Gemini AI performs segmentation, calorie extraction, ingredient identification, and returns nutrition cards instantly.",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                    }

                    items(cameraPresets) { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.scanPresetImageFood(
                                        placeholderBase64,
                                        preset.first,
                                        selectedMealType
                                    )
                                },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                // Draw stylized color brush block instead of external image if offline, or placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    EmeraldGreen.copy(alpha = 0.4f),
                                                    NightDark.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = preset.first,
                                        color = PureWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to Scan Plate & Analyze with Gemini",
                                        color = PureWhite.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Barcode scanner simulator
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Smart Barcode Scanner",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nutrica cross-checks allergens, ingredients, and retrieves real-time nutritional facts directly. Choose a preset packaging barcode below:",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }

                    barcodePresets.forEach { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Simply trigger parsing and log it
                                    viewModel.addFoodEntryDirect(
                                        preset.second,
                                        selectedMealType,
                                        if (preset.first.contains("761")) 100.0 else if (preset.first.contains("501")) 220.0 else 250.0,
                                        if (preset.first.contains("761")) 15.0 else if (preset.first.contains("501")) 6.0 else 20.0,
                                        if (preset.first.contains("761")) 4.0 else if (preset.first.contains("501")) 35.0 else 22.0,
                                        if (preset.first.contains("761")) 0.0 else if (preset.first.contains("501")) 5.0 else 8.0,
                                        sugar = if (preset.first.contains("761")) 3.0 else 12.0
                                    )
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = preset.second,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "UPC: ${preset.first}",
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = preset.third,
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✔ Tap to Log instantly to $selectedMealType (+XP)",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CALENDAR / FOOD LOG HISTORY SCREEN ---

@Composable
fun CalendarScreen(
    viewModel: NutricaViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate by viewModel.currentDate.collectAsState()
    val foodLogs by viewModel.selectedDayFoodLogs.collectAsState()
    val waterLogs by viewModel.selectedDayWaterLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val calorieGoal = profile.calorieGoal
    val caloriesToday = foodLogs.sumOf { it.calories }
    val proteinToday = foodLogs.sumOf { it.protein }
    val carbsToday = foodLogs.sumOf { it.carbs }
    val fatToday = foodLogs.sumOf { it.fat }
    val waterToday = waterLogs.sumOf { it.amountMl }

    val formattedDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(currentDate.time)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header Row with Menu Button
        item {
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Menu",
                            tint = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calendar History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = {
                    val shareMsg = """
                        📊 My Nutrica Progress on $formattedDate:
                        🔥 Calories: ${caloriesToday.toInt()} / ${calorieGoal.toInt()} kcal
                        💪 Protein: ${proteinToday.toInt()}g
                        🌾 Carbs: ${carbsToday.toInt()}g
                        🥑 Fat: ${fatToday.toInt()}g
                        💧 Water Logged: $waterToday ml

                        ✨ "Be Your Own Nutritionist" with Nutrica!
                    """.trimIndent()

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareMsg)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Daily Progress")
                    context.startActivity(shareIntent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Daily Progress",
                        tint = EmeraldGreen
                    )
                }
            }
        }

        // Calendar Controller
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousDay() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day")
                        }
                        Text(
                            text = formattedDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.0f)
                        )
                        IconButton(onClick = { viewModel.nextDay() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simplified Calendar Days ring colors simulator (Green/Yellow/Red indicators)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        daysOfWeek.forEachIndexed { idx, day ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = day, fontSize = 11.sp, color = SlateTextSecondary)
                                // Ring colored circle depending on calories deviation
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (idx) {
                                                0 -> EmeraldGreen // Met goal
                                                1 -> EmeraldGreen
                                                2 -> GoldenAmber // Slightly over
                                                3 -> RoseRed // Exceeded goal
                                                currentDate.get(Calendar.DAY_OF_WEEK) - 2 -> {
                                                    if (caloriesToday == 0.0) SlateTextSecondary.copy(alpha = 0.2f)
                                                    else if (caloriesToday <= calorieGoal) EmeraldGreen
                                                    else if (caloriesToday <= calorieGoal * 1.15) GoldenAmber
                                                    else RoseRed
                                                }
                                                else -> EmeraldGreen
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${20 + idx}",
                                        color = PureWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily aggregated summary
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Stats Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryColumn(label = "Calories Eaten", value = "${caloriesToday.toInt()} kcal", color = EmeraldGreen)
                        SummaryColumn(label = "Protein", value = "${proteinToday.toInt()}g", color = CobaltBlue)
                        SummaryColumn(label = "Water Logs", value = "${waterToday}ml", color = CyanWater)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AI Health Insight for today:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SlateTextSecondary
                    )
                    Text(
                        text = when {
                            caloriesToday == 0.0 -> "Start logging foods today using our AI Scanner to receive personalized guidance."
                            caloriesToday <= calorieGoal -> "Excellent! You are safely within your calorie ceiling. Protein levels are looking healthy."
                            else -> "You've exceeded your daily calorie ceiling by ${(caloriesToday - calorieGoal).toInt()} kcal. Increase fiber tomorrow to promote fullness."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // List of meals logged
        item {
            Text(
                text = "Logged Items",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (foodLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.RestaurantMenu,
                            contentDescription = "Empty list",
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals logged for this day yet.",
                            color = SlateTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(foodLogs) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = entry.mealType,
                                        fontSize = 9.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Serving: ${entry.servingSize} • P: ${entry.protein.toInt()}g, C: ${entry.carbs.toInt()}g, F: ${entry.fat.toInt()}g",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Text(
                            text = "${entry.calories.toInt()} kcal",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(onClick = { viewModel.deleteFoodEntry(entry.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = RoseRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = SlateTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun StandardizedNutrientGrid(
    calories: String,
    protein: String,
    carbs: String,
    fat: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(Color(0x0EFFFFFF), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "⚡ Standardized Nutrient Summary",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = EmeraldGreen,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Calories card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Calories",
                        tint = StreakPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Calories", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(calories, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Protein card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Protein",
                        tint = CobaltBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Protein", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(protein, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Carbs card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Grain,
                        contentDescription = "Carbs",
                        tint = GoldenAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Carbs", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(carbs, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            // Fat card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Fat",
                        tint = CyanWater,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Fat", fontSize = 9.sp, color = SlateTextSecondary)
                    Text(fat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

data class NutrientData(
    val calories: String,
    val protein: String,
    val carbs: String,
    val fat: String,
    val cleanText: String
)

fun parseNutrientsFromText(text: String): NutrientData? {
    // 1. Flexible tag parser (supports optional units, commas, decimals, spaces, and ignore-case)
    val tagRegex = """\[NUTRIENTS:\s*Calories:\s*([\d\.,\s]+)\s*(?:kcal|calories)?,?\s*Protein:\s*([\d\.,\s]+)\s*g?,?\s*Carbs:\s*([\d\.,\s]+)\s*g?,?\s*Fat:\s*([\d\.,\s]+)\s*g?\]""".toRegex(RegexOption.IGNORE_CASE)
    val tagMatch = tagRegex.find(text)
    if (tagMatch != null) {
        val calories = tagMatch.groupValues[1].trim() + " kcal"
        val protein = tagMatch.groupValues[2].trim() + "g"
        val carbs = tagMatch.groupValues[3].trim() + "g"
        val fat = tagMatch.groupValues[4].trim() + "g"
        return NutrientData(calories, protein, carbs, fat, cleanText = text.replace(tagRegex, "").trim())
    }

    // 2. Smart fallback parser looking for any clear nutritional mentions (both prefix and suffix formats)
    val calRegex = """(?:Calories|calories|Cal|cal|Cals|cals)(?:\s*:\s*|\s+)([\d\.,]+)|([\d\.,]+)\s*(?:kcal|calories|Calories|cals|Cals)""".toRegex(RegexOption.IGNORE_CASE)
    val proteinRegex = """(?:Protein|protein|P\s*:)(?:\s*:\s*|\s+)([\d\.,]+)\s*g?|([\d\.,]+)\s*g?\s*(?:protein|Protein)""".toRegex(RegexOption.IGNORE_CASE)
    val carbsRegex = """(?:Carbs|carbs|Carbohydrates|carbohydrates|C\s*:)(?:\s*:\s*|\s+)([\d\.,]+)\s*g?|([\d\.,]+)\s*g?\s*(?:carbs|Carbs|carbohydrates|Carbohydrates)""".toRegex(RegexOption.IGNORE_CASE)
    val fatRegex = """(?:Fat|fat|F\s*:)(?:\s*:\s*|\s+)([\d\.,]+)\s*g?|([\d\.,]+)\s*g?\s*(?:fat|Fat)""".toRegex(RegexOption.IGNORE_CASE)

    val calMatch = calRegex.find(text)
    val proteinMatch = proteinRegex.find(text)
    val carbsMatch = carbsRegex.find(text)
    val fatMatch = fatRegex.find(text)

    if (calMatch != null || proteinMatch != null || carbsMatch != null || fatMatch != null) {
        val calVal = (calMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } ?: calMatch?.groupValues?.get(2)?.takeIf { it.isNotEmpty() } ?: "--") + " kcal"
        val protVal = (proteinMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } ?: proteinMatch?.groupValues?.get(2)?.takeIf { it.isNotEmpty() } ?: "--") + "g"
        val carbVal = (carbsMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } ?: carbsMatch?.groupValues?.get(2)?.takeIf { it.isNotEmpty() } ?: "--") + "g"
        val fatVal = (fatMatch?.groupValues?.get(1)?.takeIf { it.isNotEmpty() } ?: fatMatch?.groupValues?.get(2)?.takeIf { it.isNotEmpty() } ?: "--") + "g"
        return NutrientData(
            calories = calVal,
            protein = protVal,
            carbs = carbVal,
            fat = fatVal,
            cleanText = text
        )
    }

    return null
}

// --- AI COACH CONVERSATION CHAT SCREEN ---

@Composable
fun CoachScreen(
    viewModel: NutricaViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.coachMessages.collectAsState()
    val isCoachLoading by viewModel.aiCoachLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 64.dp)
    ) {
        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Menu",
                        tint = EmeraldGreen
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Coach Logo",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nutrica AI Coach",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Developed by Parth C • Online",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                }
            }

            TextButton(onClick = { viewModel.clearChat() }) {
                Text("Clear History", color = RoseRed, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chats list area
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "No chats",
                        tint = SlateTextSecondary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ask Nutrica AI Coach anything about your diet, workouts, streaks, or tailored recipes!",
                        textAlign = TextAlign.Center,
                        color = SlateTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic template buttons
                    val templates = listOf(
                        "Can I eat pizza today?",
                        "What should I eat before gym?",
                        "How much protein left?"
                    )
                    templates.forEach { tp ->
                        OutlinedButton(
                            onClick = { viewModel.askCoach(tp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                        ) {
                            Text(text = tp, color = EmeraldGreen, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) EmeraldGreen else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .testTag(if (isUser) "user_message" else "coach_message")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val nutrientData = if (!isUser) parseNutrientsFromText(msg.message) else null
                                    val displayText = nutrientData?.cleanText ?: msg.message

                                    Text(
                                        text = displayText,
                                        color = if (isUser) PureWhite else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )

                                    if (nutrientData != null) {
                                        StandardizedNutrientGrid(
                                            calories = nutrientData.calories,
                                            protein = nutrientData.protein,
                                            carbs = nutrientData.carbs,
                                            fat = nutrientData.fat
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isCoachLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.widthIn(max = 200.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = EmeraldGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Typing...", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message input area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask anything...") },
                modifier = Modifier
                    .weight(1.0f)
                    .testTag("coach_input"),
                shape = RoundedCornerShape(16.dp)
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.askCoach(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreen)
                    .testTag("coach_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PureWhite)
            }
        }
    }
}

// --- AI PLANNER, FOOD SWAPS, GROCERY GENERATOR ---

@Composable
fun MealPlannerScreen(
    viewModel: NutricaViewModel,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plannerLoading by viewModel.aiPlannerLoading.collectAsState()
    val mealPlan by viewModel.mealPlanState.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    var activePlanSubTab by remember { mutableIntStateOf(0) } // 0: AI Planner, 1: Smart Swaps

    var dietPreference by remember { mutableStateOf("Vegetarian") }
    var cuisinePreference by remember { mutableStateOf("Indian") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(bottom = 64.dp)
    ) {
        // Top Header Row with Menu Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = EmeraldGreen
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AI Planner & Swaps",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Toggle bar between Planner and Swaps
        TabRow(
            selectedTabIndex = activePlanSubTab,
            containerColor = Color.Transparent,
            contentColor = EmeraldGreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activePlanSubTab == 0,
                onClick = { activePlanSubTab = 0 },
                text = { Text("AI Meal Planner") },
                icon = { Icon(Icons.Default.Restaurant, contentDescription = "Planner") }
            )
            Tab(
                selected = activePlanSubTab == 1,
                onClick = { activePlanSubTab = 1 },
                text = { Text("Smart Food Swap") },
                icon = { Icon(Icons.Default.PublishedWithChanges, contentDescription = "Swaps") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activePlanSubTab) {
            0 -> {
                // Planner Layout
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Instant AI Meal Planner",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Formulates perfect Breakfast, Lunch, Dinner, and Snacks matching your caloric target precisely. Includes a full integrated Grocery Shopping list.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Dropdowns or Row selectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = dietPreference,
                                    onValueChange = { dietPreference = it },
                                    label = { Text("Diet Preference") },
                                    modifier = Modifier.weight(1.0f)
                                )
                                OutlinedTextField(
                                    value = cuisinePreference,
                                    onValueChange = { cuisinePreference = it },
                                    label = { Text("Cuisine / Style") },
                                    modifier = Modifier.weight(1.0f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.generateMealPlan(dietPreference, cuisinePreference) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_meal_plan_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Meal Plan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (plannerLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = EmeraldGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nutrica AI is constructing custom recipes...", fontSize = 11.sp, color = SlateTextSecondary)
                            }
                        }
                    }

                    mealPlan?.let { plan ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.0f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            // Breakfast Section
                            item {
                                SectionHeader(title = "🍳 Breakfast Recs")
                            }
                            items(plan.breakfast) { meal ->
                                MealPlanCard(meal = meal)
                            }

                            // Lunch Section
                            item {
                                SectionHeader(title = "🍲 Lunch Recs")
                            }
                            items(plan.lunch) { meal ->
                                MealPlanCard(meal = meal)
                            }

                            // Dinner Section
                            item {
                                SectionHeader(title = "🥗 Dinner Recs")
                            }
                            items(plan.dinner) { meal ->
                                MealPlanCard(meal = meal)
                            }

                            // Snacks Section
                            item {
                                SectionHeader(title = "🍎 Snacks Recs")
                            }
                            items(plan.snacks) { meal ->
                                MealPlanCard(meal = meal)
                            }

                            // Grocery List Generator Card
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "🛒 AI Shopping Grocery List",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        plan.groceryList.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = "Item",
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = item, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Swaps Screen
                val swapsLoading by viewModel.aiSwapsLoading.collectAsState()
                val swaps by viewModel.swapsState.collectAsState()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Smart Food Swap Assistant",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nutrica recommends simple, delicious modifications that save hundreds of calories without sacrificing flavor.",
                                fontSize = 11.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = { viewModel.generateHealthySwaps() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Loop, contentDescription = "Swaps")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Food Swaps", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (swapsLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = EmeraldGreen)
                        }
                    } else if (swaps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No swaps loaded yet. Tap 'Generate Swaps'.", color = SlateTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            items(swaps) { swap ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = swap.originalFood,
                                                fontWeight = FontWeight.Bold,
                                                color = RoseRed,
                                                fontSize = 13.sp
                                            )
                                            Icon(
                                                Icons.Default.TrendingFlat,
                                                contentDescription = "to",
                                                tint = SlateTextSecondary
                                            )
                                            Text(
                                                text = swap.healthierAlternative,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = swap.benefits,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 15.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "🔥 Saves ${swap.caloriesSaved.toInt()} calories!",
                                            fontSize = 12.sp,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = EmeraldGreen,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun MealPlanCard(meal: AIMealItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = meal.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1.0f))
                Text(text = "${meal.calories.toInt()} kcal", fontWeight = FontWeight.ExtraBold, color = EmeraldGreen, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Macros: P: ${meal.protein.toInt()}g, C: ${meal.carbs.toInt()}g, F: ${meal.fat.toInt()}g",
                fontSize = 11.sp,
                color = SlateTextSecondary
            )

            if (meal.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ingredients:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateTextSecondary)
                Text(text = meal.ingredients.joinToString(", "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            if (meal.recipeInstructions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Instructions:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateTextSecondary)
                meal.recipeInstructions.forEachIndexed { idx, ins ->
                    Text(text = "${idx + 1}. $ins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                }
            }
        }
    }
}

// --- PROFILE SETTINGS SCREEN (BMR / Target Metabolic calculator) ---

@Composable
fun ProfileScreen(
    viewModel: NutricaViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()

    var nameInput by remember { mutableStateOf(profile.name) }
    var weightInput by remember { mutableStateOf(profile.weightKg.toString()) }
    var heightInput by remember { mutableStateOf(profile.heightCm.toString()) }
    var ageInput by remember { mutableStateOf(profile.age.toString()) }
    var genderInput by remember { mutableStateOf(profile.gender) }
    var activityInput by remember { mutableStateOf(profile.activityLevel) }
    var goalInput by remember { mutableStateOf(profile.goal) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "My Nutrica Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Metabolic Profile Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.0f)
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.0f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age (years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.0f)
                    )
                    OutlinedTextField(
                        value = genderInput,
                        onValueChange = { genderInput = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1.0f)
                    )
                }

                OutlinedTextField(
                    value = activityInput,
                    onValueChange = { activityInput = it },
                    label = { Text("Activity Level (Sedentary, Moderate, Active)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    label = { Text("Goal (Lose Weight, Maintain, Bulk)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val wt = weightInput.toDoubleOrNull() ?: profile.weightKg
                        val ht = heightInput.toDoubleOrNull() ?: profile.heightCm
                        val age = ageInput.toIntOrNull() ?: profile.age

                        // Dynamic BMR & Metabolic calorie goal calculation (Harris-Benedict Equation)
                        val baseBmr = if (genderInput.lowercase().contains("female")) {
                            447.593 + (9.247 * wt) + (3.098 * ht) - (4.330 * age)
                        } else {
                            88.362 + (13.397 * wt) + (4.799 * ht) - (5.677 * age)
                        }

                        val multiplier = when {
                            activityInput.lowercase().contains("sedentary") -> 1.2
                            activityInput.lowercase().contains("active") -> 1.725
                            else -> 1.45 // Moderate
                        }

                        var targetCal = baseBmr * multiplier
                        if (goalInput.lowercase().contains("lose")) {
                            targetCal -= 500.0 // Deficit
                        } else if (goalInput.lowercase().contains("bulk")) {
                            targetCal += 300.0 // Surplus
                        }

                        // Macros formula: 30% Protein, 45% Carbs, 25% Fat
                        val targetProtein = (targetCal * 0.3) / 4.0
                        val targetCarbs = (targetCal * 0.45) / 4.0
                        val targetFat = (targetCal * 0.25) / 9.0

                        val updated = profile.copy(
                            name = nameInput,
                            weightKg = wt,
                            heightCm = ht,
                            age = age,
                            gender = genderInput,
                            activityLevel = activityInput,
                            goal = goalInput,
                            calorieGoal = targetCal,
                            proteinGoal = targetProtein,
                            carbsGoal = targetCarbs,
                            fatGoal = targetFat
                        )

                        viewModel.updateProfileDirect(updated)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_profile_button")
                ) {
                    Text("Recalculate & Save Goals", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developed By Credit Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Verified,
                    contentDescription = "Verified Engineer",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nutrica Nutrition Companion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Developed with ❤ by Parth C",
                    fontSize = 12.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Artificial Intelligence and Data Science Engineer",
                    fontSize = 11.sp,
                    color = SlateTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
