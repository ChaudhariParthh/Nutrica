package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateTextSecondary
import com.example.viewmodel.NutricaViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
    val route: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {
    private val viewModel: NutricaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                val loggedInUser by viewModel.loggedInUser.collectAsState()

                if (loggedInUser == null) {
                    AuthScreen(viewModel = viewModel)
                } else if (!loggedInUser!!.isOnboarded) {
                    OnboardingScreen(viewModel = viewModel)
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.background,
                            drawerContentColor = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.width(310.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                            ) {
                                // Header brand
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_logo),
                                        contentDescription = "Brand Logo",
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Nutrica",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Be Your Own Nutritionist",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EmeraldGreen
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Account Section
                                Text(
                                    text = "ACCOUNT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Surface(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("profile")
                                    },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Profile",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "My Profile",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "View stats & daily goals",
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Settings Section
                                Text(
                                    text = "SETTINGS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Surface(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("profile")
                                    },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "App Settings",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Edit weight, goals, age",
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Share Section
                                Text(
                                    text = "SHARE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                val context = LocalContext.current
                                Surface(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Achieve your fitness goals with Nutrica - Your Personal AI-Powered Nutritionist! Be Your Own Nutritionist. Check it out: https://ais-pre-jqsjzmxc24hg7kpqusq543-59722351035.asia-southeast1.run.app")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Nutrica")
                                        context.startActivity(shareIntent)
                                    },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Share with Friends",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Spread the healthy lifestyle",
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Theme Mode Section
                                Text(
                                    text = "THEME MODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = "Theme Icon",
                                            tint = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFFFB923C),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = if (isDarkMode) "Dark Mode" else "Light Mode",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = { isDarkMode = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldGreen,
                                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Log Out Session Button
                                Surface(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        viewModel.logoutUser()
                                    },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Log Out",
                                            tint = Color(0xFFF87171), // Rose red
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Log Out",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFFF87171)
                                            )
                                            Text(
                                                text = "Exit current active session",
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1.0f))

                                // Footer developer credit
                                Text(
                                    text = "Nutrica v1.0 • Sleek Edition\nDeveloped by Parth C",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        bottomBar = {
                            if (currentRoute != "profile") {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = EmeraldGreen,
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .testTag("bottom_nav_bar")
                                ) {
                                    val navItems = listOf(
                                        NavigationItem("Dashboard", "dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
                                        NavigationItem("Log & Scan", "log", Icons.Outlined.PhotoCamera, Icons.Filled.PhotoCamera),
                                        NavigationItem("Calendar", "calendar", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
                                        NavigationItem("AI Coach", "coach", Icons.Outlined.Forum, Icons.Filled.Forum),
                                        NavigationItem("AI Planner", "planner", Icons.Outlined.Restaurant, Icons.Filled.Restaurant)
                                    )

                                    navItems.forEach { item ->
                                        val isSelected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = EmeraldGreen,
                                                selectedTextColor = EmeraldGreen,
                                                unselectedIconColor = SlateTextSecondary,
                                                unselectedTextColor = SlateTextSecondary,
                                                indicatorColor = EmeraldGreen.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${item.route}")
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "dashboard",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToProfile = { navController.navigate("profile") },
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("log") {
                                SearchAndScanScreen(
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("calendar") {
                                CalendarScreen(
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("coach") {
                                CoachScreen(
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("planner") {
                                MealPlannerScreen(
                                    viewModel = viewModel,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
                } // Close the authenticated 'else' block
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

