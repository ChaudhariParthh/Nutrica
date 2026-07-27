package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.NutricaViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: NutricaViewModel,
    modifier: Modifier = Modifier
) {
    val CoolGrayBackground = MaterialTheme.colorScheme.background
    val NightCard = MaterialTheme.colorScheme.surface
    val NightBorder = MaterialTheme.colorScheme.outline
    val SlateTextDark = MaterialTheme.colorScheme.onBackground

    var isSignUp by remember { mutableStateOf(false) }
    
    // Inputs
    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    // SSO Google simulation dialog state
    var showSsoDialog by remember { mutableStateOf(false) }
    var ssoEmail by remember { mutableStateOf("parthchaudhari974@gmail.com") }
    var ssoName by remember { mutableStateOf("Parth Chaudhari") }

    val authError by viewModel.authError.collectAsState()

    // Password validations as user types
    val hasMinLength = password.length >= 6
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CoolGrayBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo and Brand Header
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Nutrica Brand Logo",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NUTRICA",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                letterSpacing = 4.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "FUELING A BETTER TOMORROW",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = EmeraldGreen,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Be Your Own Nutritionist",
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Main Card Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NightCard),
                border = BorderStroke(1.dp, NightBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Sign In / Sign Up tab toggles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CoolGrayBackground, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { 
                                isSignUp = false
                                viewModel.clearAuthError()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isSignUp) NightCard else Color.Transparent,
                                contentColor = if (!isSignUp) EmeraldGreen else SlateTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_signin")
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { 
                                isSignUp = true
                                viewModel.clearAuthError()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUp) NightCard else Color.Transparent,
                                contentColor = if (isSignUp) EmeraldGreen else SlateTextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_signup")
                        ) {
                            Text("Sign Up", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Alert
                    authError?.let { err ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            color = RoseRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RoseRed.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = RoseRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(err, color = RoseRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Form Fields
                    if (isSignUp) {
                        Text(
                            text = "Full Name",
                            color = SlateTextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("e.g. Parth Chaudhari", color = SlateTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = NightBorder,
                                focusedContainerColor = CoolGrayBackground,
                                unfocusedContainerColor = CoolGrayBackground,
                                focusedTextColor = SlateTextDark,
                                unfocusedTextColor = SlateTextDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .testTag("input_fullname")
                        )
                    }

                    Text(
                        text = "Gmail or Phone Number",
                        color = SlateTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        placeholder = { Text("e.g. user@gmail.com", color = SlateTextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NightBorder,
                            focusedContainerColor = CoolGrayBackground,
                            unfocusedContainerColor = CoolGrayBackground,
                            focusedTextColor = SlateTextDark,
                            unfocusedTextColor = SlateTextDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("input_email_phone")
                    )

                    Text(
                        text = "Password",
                        color = SlateTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Enter secret password", color = SlateTextSecondary) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = SlateTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NightBorder,
                            focusedContainerColor = CoolGrayBackground,
                            unfocusedContainerColor = CoolGrayBackground,
                            focusedTextColor = SlateTextDark,
                            unfocusedTextColor = SlateTextDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("input_password")
                    )

                    // Interactive Password Policy Checklist for Sign Up
                    if (isSignUp) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            color = CoolGrayBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NightBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Password Security Policy:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasMinLength) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Min Length",
                                        tint = if (hasMinLength) EmeraldGreen else SlateTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("At least 6 characters long", fontSize = 11.sp, color = if (hasMinLength) SlateTextDark else SlateTextSecondary)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasDigit) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Contains Number",
                                        tint = if (hasDigit) EmeraldGreen else SlateTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contains at least one digit (0-9)", fontSize = 11.sp, color = if (hasDigit) SlateTextDark else SlateTextSecondary)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasSpecialChar) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Contains Special",
                                        tint = if (hasSpecialChar) EmeraldGreen else SlateTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Contains at least one special char (e.g., @, #, $, !)", fontSize = 11.sp, color = if (hasSpecialChar) SlateTextDark else SlateTextSecondary)
                                }
                            }
                        }
                    }

                    // Main Submit Button
                    Button(
                        onClick = {
                            if (isSignUp) {
                                viewModel.signUpUser(fullName, emailOrPhone, password, onSuccess = {})
                            } else {
                                viewModel.signInUser(emailOrPhone, password, onSuccess = {})
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NightDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_auth")
                    ) {
                        Text(
                            text = if (isSignUp) "Create Account" else "Sign In Safely",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // SSO Google Button Separator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NightBorder)
                        Text(
                            text = "OR CONTINUE WITH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NightBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SSO Google Button
                    OutlinedButton(
                        onClick = { showSsoDialog = true },
                        border = BorderStroke(1.dp, NightBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_google_sso")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet, // Representative SSO lock/token icon
                                contentDescription = "Google Logo Representation",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign In with Google SSO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Interactive Google SSO mock dialog to select or enter any Google Account credentials
    if (showSsoDialog) {
        AlertDialog(
            onDismissRequest = { showSsoDialog = false },
            containerColor = NightCard,
            titleContentColor = SlateTextDark,
            textContentColor = SlateTextSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = "Google SSO", tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google Identity SSO", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        "Google SSO authenticates your profile instantly. Select your Google account or edit the identity fields below to register securely:",
                        fontSize = 13.sp,
                        color = SlateTextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text("Google Name", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateTextDark)
                    OutlinedTextField(
                        value = ssoName,
                        onValueChange = { ssoName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NightBorder,
                            focusedContainerColor = CoolGrayBackground,
                            unfocusedContainerColor = CoolGrayBackground,
                            focusedTextColor = SlateTextDark,
                            unfocusedTextColor = SlateTextDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Text("Google Email address", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SlateTextDark)
                    OutlinedTextField(
                        value = ssoEmail,
                        onValueChange = { ssoEmail = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = NightBorder,
                            focusedContainerColor = CoolGrayBackground,
                            unfocusedContainerColor = CoolGrayBackground,
                            focusedTextColor = SlateTextDark,
                            unfocusedTextColor = SlateTextDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSsoDialog = false
                        viewModel.signInWithGoogleSso(ssoEmail, ssoName, onSuccess = {})
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NightDark)
                ) {
                    Text("Authorize SSO", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSsoDialog = false }) {
                    Text("Cancel", color = RoseRed)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: NutricaViewModel,
    modifier: Modifier = Modifier
) {
    val CoolGrayBackground = MaterialTheme.colorScheme.background
    val NightCard = MaterialTheme.colorScheme.surface
    val NightBorder = MaterialTheme.colorScheme.outline
    val SlateTextDark = MaterialTheme.colorScheme.onBackground

    // We have three steps/tabs as requested:
    // Tab 1: Username selection
    // Tab 2: Fitness Goal selection (Weight gain, Weight Loss, Muscle Gain + Weight Gain)
    // Tab 3: Current Activity selection (low, medium, high)
    var currentStep by remember { mutableStateOf(1) }
    
    var username by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("Weight Loss") }
    var selectedActivity by remember { mutableStateOf("medium") }

    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CoolGrayBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Welcome to Nutrica!",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = SlateTextDark
            )
            Text(
                text = "Let's customize your fitness journey",
                color = SlateTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Step Progress bar (Tabs equivalent)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (step in 1..3) {
                    val isActive = step == currentStep
                    val isCompleted = step < currentStep
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) EmeraldGreen 
                                    else if (isCompleted) EmeraldGreen.copy(alpha = 0.3f) 
                                    else NightCard
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) EmeraldGreen else NightBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = "Done", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            } else {
                                Text(
                                    text = step.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) NightDark else SlateTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = when (step) {
                                1 -> "Username"
                                2 -> "Fitness Goal"
                                else -> "Activity"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) EmeraldGreen else SlateTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (step < 3) {
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(bottom = 14.dp),
                            color = if (step < currentStep) EmeraldGreen.copy(alpha = 0.5f) else NightBorder
                        )
                    }
                }
            }

            // Central Onboarding Screen Content Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NightCard),
                border = BorderStroke(1.dp, NightBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        1 -> {
                            Text(
                                "Choose Your Unique Username",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SlateTextDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "Your username will be displayed on your personal dashboard, log summary, and achievements screen.",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            if (showError && username.isBlank()) {
                                Text(
                                    text = "Username cannot be empty!",
                                    color = RoseRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            OutlinedTextField(
                                value = username,
                                onValueChange = { 
                                    username = it
                                    showError = false
                                },
                                placeholder = { Text("e.g. nutrition_master", color = SlateTextSecondary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = NightBorder,
                                    focusedContainerColor = CoolGrayBackground,
                                    unfocusedContainerColor = CoolGrayBackground,
                                    focusedTextColor = SlateTextDark,
                                    unfocusedTextColor = SlateTextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_onboarding_username")
                            )
                        }

                        2 -> {
                            Text(
                                "Select Your Fitness Goal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SlateTextDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "We use this to calculate and tailor your baseline daily calories and macronutrient breakdown.",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            val goals = listOf(
                                Pair("Weight Loss", "Focus on a healthy calorie deficit while keeping proteins high to retain muscle mass."),
                                Pair("Weight gain", "Target a structured calorie surplus to support healthy mass and weight addition."),
                                Pair("Muscle Gain + Weight Gain", "Maximize muscle protein synthesis with highly targeted caloric surplus and maximum protein limits.")
                            )

                            goals.forEach { (goalName, desc) ->
                                val isSelected = selectedGoal == goalName
                                Card(
                                    onClick = { selectedGoal = goalName },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .testTag("goal_card_$goalName"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.1f) else CoolGrayBackground
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isSelected) EmeraldGreen else NightBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedGoal = goalName },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = goalName,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) EmeraldGreen else SlateTextDark,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = desc,
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            Text(
                                "What is Your Current Activity Level?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SlateTextDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                "Daily activity level is vital for calibrating your active energy expenditure and refining your tailored food feed.",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            val activities = listOf(
                                Triple("low", "Low Activity", "Mainly desk job, sedentary habits with minimal structured physical exercises."),
                                Triple("medium", "Medium Activity", "Moderate walking, light exercise sessions, or standard active lifestyle patterns."),
                                Triple("high", "High Activity", "Intense sports, physical construction jobs, heavy lifting, or regular vigorous daily training.")
                            )

                            activities.forEach { (activityId, label, desc) ->
                                val isSelected = selectedActivity == activityId
                                Card(
                                    onClick = { selectedActivity = activityId },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .testTag("activity_card_$activityId"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) EmeraldGreen.copy(alpha = 0.1f) else CoolGrayBackground
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isSelected) EmeraldGreen else NightBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedActivity = activityId },
                                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldGreen)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = label,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) EmeraldGreen else SlateTextDark,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = desc,
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Row (Back / Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        border = BorderStroke(1.dp, NightBorder),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextDark),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("onboarding_back")
                    ) {
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (currentStep == 1) {
                            if (username.isBlank()) {
                                showError = true
                            } else {
                                currentStep = 2
                            }
                        } else if (currentStep == 2) {
                            currentStep = 3
                        } else {
                            viewModel.submitOnboarding(username, selectedGoal, selectedActivity, onSuccess = {})
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = NightDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("onboarding_next")
                ) {
                    Text(
                        text = if (currentStep == 3) "Finish & Tailor Feed" else "Continue",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
