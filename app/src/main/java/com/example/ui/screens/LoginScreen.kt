package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.FirebaseAuthService
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.BrandNeonCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authService: FirebaseAuthService,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onLoginSuccess: (FirebaseUser) -> Unit,
    onContinueAsGuest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Forgot Password Flow state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resetErrorMessage by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }

    // Email Verification Gate state
    var pendingVerificationUser by remember { mutableStateOf<FirebaseUser?>(null) }
    var verificationStatusMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingVerification by remember { mutableStateOf(false) }

    val isItalian = currentLanguage == "it"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D16),
                        Color(0xFF0F172A),
                        Color(0xFF0B111E)
                    )
                )
            )
            .testTag("login_screen_container")
    ) {
        // Top Language Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, BrandNeonCyan.copy(alpha = 0.4f)),
                onClick = {
                    val nextLang = if (isItalian) "en" else "it"
                    onLanguageChange(nextLang)
                },
                modifier = Modifier.testTag("language_toggle_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = BrandNeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isItalian) "🇮🇹 Italiano" else "🇬🇧 English",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Center Scrollable Content Card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 46.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon & Title
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0B111E),
                border = BorderStroke(1.5.dp, BrandNeonCyan.copy(alpha = 0.7f)),
                modifier = Modifier.size(76.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "ParkPulse Brand Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ParkPulse",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
            )

            Text(
                text = if (isItalian) "Mappa Smart & Sicurezza Segnalazioni" else "Smart Mobility & Verified Reports",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandNeonCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isItalian)
                    "Accesso obbligatorio con account autenticato per garantire la veridicità e la trasparenza di ogni segnalazione della community."
                else
                    "Authenticated login is required to guarantee accuracy and accountability for all community parking reports.",
                fontSize = 12.sp,
                color = Color.LightGray.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // -------------------------------------------------------------
            // EMAIL VERIFICATION GATE (Mandatory Step Before App Access)
            // -------------------------------------------------------------
            if (pendingVerificationUser != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.5.dp, RomanGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_verification_card")
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RomanGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailRead,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isItalian) "Verifica il tuo indirizzo email" else "Verify your email address",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isItalian)
                                "Abbiamo inviato un link di conferma a:\n${pendingVerificationUser?.email}\n\nPer proteggere la community da segnalazioni false o spam, conferma la tua email prima di continuare."
                            else
                                "A verification email was sent to:\n${pendingVerificationUser?.email}\n\nPlease click the link in your email to authenticate your account before accessing reporting features.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        if (verificationStatusMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, BrandNeonCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = verificationStatusMessage ?: "",
                                    fontSize = 11.sp,
                                    color = BrandNeonCyan,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Check status / Refresh Button
                        Button(
                            onClick = {
                                isCheckingVerification = true
                                coroutineScope.launch {
                                    val reloadResult = authService.reloadUser()
                                    isCheckingVerification = false
                                    reloadResult.onSuccess { reloaded ->
                                        if (reloaded != null && reloaded.isEmailVerified) {
                                            Toast.makeText(
                                                context,
                                                if (isItalian) "Email verificata con successo! Benvenuto!" else "Email verified successfully! Welcome!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onLoginSuccess(reloaded)
                                        } else {
                                            verificationStatusMessage = if (isItalian)
                                                "Email non ancora verificata. Clicca sul link ricevuto (controlla anche la cartella Spam)."
                                            else
                                                "Email not verified yet. Please click the link sent to your inbox (check spam folder)."
                                        }
                                    }.onFailure {
                                        verificationStatusMessage = it.localizedMessage ?: "Errore verifica"
                                    }
                                }
                            },
                            enabled = !isCheckingVerification,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_check_email_verification")
                        ) {
                            if (isCheckingVerification) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isItalian) "Ho verificato l'email (Controlla)" else "I've verified (Refresh)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Resend verification email
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = authService.sendEmailVerification(pendingVerificationUser)
                                    res.onSuccess {
                                        verificationStatusMessage = if (isItalian) "Nuova email di verifica inviata!" else "Verification email resent!"
                                    }.onFailure {
                                        verificationStatusMessage = it.localizedMessage ?: "Errore invio"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_resend_verification_email")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = RomanGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isItalian) "Invia di nuovo l'email" else "Resend email", fontSize = 12.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    authService.signOut(context)
                                    pendingVerificationUser = null
                                    verificationStatusMessage = null
                                }
                            }
                        ) {
                            Text(if (isItalian) "Accedi con un altro account" else "Sign in with another account", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                // -------------------------------------------------------------
                // MAIN AUTH CARD (Sign In / Register)
                // -------------------------------------------------------------
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = BorderStroke(1.dp, Color(0xFF2E384D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Google Sign-In Primary Action Button
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = authService.signInWithGoogle(context)
                                    isLoading = false
                                    result.onSuccess { user ->
                                        Toast.makeText(context, if (isItalian) "Benvenuto, ${user.displayName ?: "Utente"}!" else "Welcome, ${user.displayName ?: "Driver"}!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(user)
                                    }.onFailure { error ->
                                        errorMessage = error.localizedMessage ?: "Google Sign-In failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_sign_in_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF4285F4),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (isItalian) "Accedi con Google" else "Sign in with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Or divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF384354)
                            )
                            Text(
                                text = if (isItalian) "oppure via email" else "or with email",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF384354)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign In / Register Tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFF131C2E),
                            contentColor = TerracottaPrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = TerracottaPrimary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF2E384D), RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0; errorMessage = null },
                                text = {
                                    Text(
                                        text = if (isItalian) "Accedi" else "Sign In",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 0) Color.White else Color.Gray
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1; errorMessage = null },
                                text = {
                                    Text(
                                        text = if (isItalian) "Nuovo Account" else "Register",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 1) Color.White else Color.Gray
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Optional Display Name input when registering
                        AnimatedVisibility(visible = selectedTab == 1) {
                            Column {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    label = { Text(if (isItalian) "Nome e Cognome" else "Full Name", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandNeonCyan,
                                        unfocusedBorderColor = Color(0xFF333D4B),
                                        focusedContainerColor = DarkSurface,
                                        unfocusedContainerColor = DarkSurface
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("register_name_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text(if (isItalian) "Email" else "Email Address", fontSize = 12.sp) },
                            placeholder = { Text("driver@example.com", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNeonCyan,
                                unfocusedBorderColor = Color(0xFF333D4B),
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text(if (isItalian) "Password (min. 6 caratteri)" else "Password (min. 6 characters)", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNeonCyan,
                                unfocusedBorderColor = Color(0xFF333D4B),
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        // "Forgot Password" TextButton (only on Sign In tab)
                        if (selectedTab == 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        resetEmail = email.trim()
                                        resetSuccessMessage = null
                                        resetErrorMessage = null
                                        showForgotPasswordDialog = true
                                    },
                                    modifier = Modifier.testTag("btn_open_forgot_password")
                                ) {
                                    Text(
                                        text = if (isItalian) "Hai dimenticato la password?" else "Forgot password?",
                                        fontSize = 12.sp,
                                        color = BrandNeonCyan
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Error message banner
                        AnimatedVisibility(visible = errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFB71C1C).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Submit Email Action Button
                        Button(
                            onClick = {
                                if (email.isBlank() || password.length < 6) {
                                    errorMessage = if (isItalian)
                                        "Inserisci un'email valida e una password di almeno 6 caratteri."
                                    else
                                        "Please enter a valid email and at least 6 characters password."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    if (selectedTab == 0) {
                                        val result = authService.signInWithEmail(email, password)
                                        isLoading = false
                                        result.onSuccess { user ->
                                            if (user.isEmailVerified) {
                                                onLoginSuccess(user)
                                            } else {
                                                pendingVerificationUser = user
                                            }
                                        }.onFailure { error ->
                                            errorMessage = error.localizedMessage ?: "Credenziali non valide"
                                        }
                                    } else {
                                        val result = authService.createAccountWithEmail(email, password, displayName)
                                        isLoading = false
                                        result.onSuccess { user ->
                                            // New email account requires verification step
                                            pendingVerificationUser = user
                                        }.onFailure { error ->
                                            errorMessage = error.localizedMessage ?: "Errore durante la registrazione"
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("email_auth_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) {
                                        if (isItalian) "Accedi" else "Sign In"
                                    } else {
                                        if (isItalian) "Registrati e Verifica Email" else "Register & Verify Email"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security & Anti-Fraud Compliance Notice (Replaces Guest Mode)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFF2E384D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BrandNeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isItalian)
                            "Accesso verificato: ogni segnalazione e feedback sulla sosta viene registrato e protetto nel database a garanzia di trasparenza e legalità."
                        else
                            "Verified authentication: every parking report and feedback is attributed and protected to ensure trust and transparency.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // FORGOT PASSWORD MODAL DIALOG (Firebase Auth Account Recovery)
        // -------------------------------------------------------------
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isResetLoading) showForgotPasswordDialog = false
                },
                shape = RoundedCornerShape(18.dp),
                containerColor = DarkSurfaceVariant,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RomanGold,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = if (isItalian) "Recupero Password" else "Reset Password",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isItalian)
                                "Inserisci l'indirizzo email del tuo account. Ti invieremo un link sicuro Firebase per reimpostare la password."
                            else
                                "Enter your registered email address. We will send a secure Firebase reset link to update your password.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it; resetErrorMessage = null },
                            label = { Text("Email", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNeonCyan,
                                unfocusedBorderColor = Color(0xFF3B4861),
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_email_input")
                        )

                        if (resetSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AvailGreen.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, AvailGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AvailGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(resetSuccessMessage ?: "", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        if (resetErrorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFB71C1C).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = resetErrorMessage ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF8A80),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank()) {
                                resetErrorMessage = if (isItalian) "Inserisci un indirizzo email." else "Please enter an email address."
                                return@Button
                            }
                            isResetLoading = true
                            resetErrorMessage = null
                            resetSuccessMessage = null
                            coroutineScope.launch {
                                val result = authService.sendPasswordResetEmail(resetEmail)
                                isResetLoading = false
                                result.onSuccess {
                                    resetSuccessMessage = if (isItalian)
                                        "Link inviato! Controlla la tua casella di posta per reimpostare la password."
                                    else
                                        "Link sent! Check your inbox to reset your password."
                                }.onFailure { error ->
                                    resetErrorMessage = error.localizedMessage ?: "Errore durante l'invio del link."
                                }
                            }
                        },
                        enabled = !isResetLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier.testTag("btn_send_password_reset")
                    ) {
                        if (isResetLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isItalian) "Invia Link" else "Send Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false },
                        enabled = !isResetLoading
                    ) {
                        Text(if (isItalian) "Chiudi" else "Close", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            )
        }
    }
}
