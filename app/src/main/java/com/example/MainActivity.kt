package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.setValue
import com.example.data.model.MapRendererType
import com.example.data.model.ViewMode
import com.example.ui.components.CitySelectorBottomSheet
import com.example.ui.components.AccountPrivacyDialog
import com.example.ui.screens.LoginScreen
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Public
import com.example.ui.components.AdvancedFilterSheet
import com.example.ui.components.CircularRadarDensityView
import com.example.ui.components.DepartureSignalBottomSheet
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.FilterChipsRow
import com.example.ui.components.GeminiParkingMlTrendsSheet
import com.example.ui.components.GeminiZtlCopilotSheet
import com.example.ui.components.GoogleMapsParkingView
import com.example.ui.components.MicroAreaMapCanvas
import com.example.ui.components.MyActivityDashboard
import com.example.ui.components.NeighborhoodRadarView
import com.example.ui.components.RechartsDensityRadarView
import com.example.ui.components.RegisterParkedStayBottomSheet
import com.example.ui.components.StreetAvailabilityListView
import com.example.ui.components.VehiclePermitDialog
import com.example.ui.components.ZtlUnauthorizedRouteWarningDialog
import com.example.ui.components.ZtlVisibilityBottomSheet
import com.example.ui.components.SettingsDialog
import com.example.ui.components.CommunityLeaderboardSheet
import com.example.ui.components.PlacesAutocompleteSearchBar
import com.example.ui.components.SavedCarBottomSheet
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.SavedFavoritesScreen
import com.example.ui.screens.UserProfileReportScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.BrandNeonCyan
import com.example.ui.theme.BrandCoralAccent
import com.example.data.model.ThemeMode
import com.example.data.model.ParkingTypeCategory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Leaderboard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.ParkingViewModel
import com.example.util.NavigationHelper
import com.example.util.ParkingNotificationService
import com.example.util.ZtlGpsLocationMonitorService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ParkingViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (state.userSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDark) {
                ParkingAppScreen(viewModel = viewModel)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchDeviceLocation(context: Context, onLocationResult: (LatLng) -> Unit) {
    val fineGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (fineGranted || coarseGranted) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(LatLng(location.latitude, location.longitude))
            } else {
                try {
                    fusedClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { curLoc ->
                        if (curLoc != null) {
                            onLocationResult(LatLng(curLoc.latitude, curLoc.longitude))
                        }
                    }
                } catch (e: Exception) {}
            }
        }.addOnFailureListener {
            try {
                fusedClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { curLoc ->
                    if (curLoc != null) {
                        onLocationResult(LatLng(curLoc.latitude, curLoc.longitude))
                    }
                }
            } catch (e: Exception) {}
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
            val gpsLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
            val netLoc = locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            val best = gpsLoc ?: netLoc
            if (best != null) {
                onLocationResult(LatLng(best.latitude, best.longitude))
            }
        } catch (e: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingAppScreen(viewModel: ParkingViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept hardware Back button when in Active Navigation or on sub-views
    BackHandler(enabled = state.activeNavigation != null || state.viewMode != ViewMode.MAP) {
        if (state.activeNavigation != null) {
            viewModel.stopNavigation()
        } else if (state.viewMode != ViewMode.MAP) {
            viewModel.setViewMode(ViewMode.MAP)
        }
    }

    // Android Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val granted = fineGranted || coarseGranted
        viewModel.updateLocationPermission(granted)
        if (granted) {
            ZtlGpsLocationMonitorService.startMonitoring(context)
            fetchDeviceLocation(context) { latLng ->
                viewModel.updateLocationPermission(true, latLng, context)
            }
        } else {
            ZtlGpsLocationMonitorService.stopMonitoring(context)
        }
    }

    // Android Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* User handled notification permission */ }

    // Initial checks on launch
    LaunchedEffect(Unit) {
        viewModel.initDatabase(context)
        ParkingNotificationService.createAllNotificationChannels(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            ZtlGpsLocationMonitorService.startMonitoring(context)
            viewModel.updateLocationPermission(true, null, context)
            fetchDeviceLocation(context) { latLng ->
                viewModel.updateLocationPermission(true, latLng, context)
            }
        } else {
            // Prompt for location immediately on first launch
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Sync vehicle permit state with ZTL GPS Background Monitor
    LaunchedEffect(state.userProfile) {
        ZtlGpsLocationMonitorService.currentVehiclePermit = state.userProfile.vehiclePermit
        ZtlGpsLocationMonitorService.currentPermitActive = state.userProfile.isPermitActive
    }

    // Register active GPS location callback from ZtlGpsLocationMonitorService
    DisposableEffect(context) {
        ZtlGpsLocationMonitorService.locationUpdateListener = { loc, _ ->
            viewModel.updateLocationPermission(true, loc, context)
        }
        onDispose {
            ZtlGpsLocationMonitorService.locationUpdateListener = null
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    // Initialize Firestore sync manager
    LaunchedEffect(state.currentUser) {
        try {
            val syncManager = com.example.data.firestore.FirestoreSyncManager(context)
            viewModel.initFirestoreSync(syncManager)
        } catch (e: Exception) {}
    }

    // Auth Gate: Mandatory Authentication Gate (No Guest mode allowed for anti-abuse & audit tracking)
    if (state.currentUser == null || state.showLoginScreen) {
        LoginScreen(
            authService = viewModel.authService,
            currentLanguage = state.languagePreference,
            onLanguageChange = { viewModel.setLanguagePreference(it) },
            onLoginSuccess = { viewModel.onUserSignedIn(it) }
        )
        return
    }

    // Onboarding Gate: guide new users through core features
    if (!state.userSettings.hasCompletedOnboarding) {
        OnboardingWizardScreen(
            onFinish = { viewModel.completeOnboarding() }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .background(DarkSurface)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0B111E),
                            border = BorderStroke(1.dp, BrandNeonCyan.copy(alpha = 0.6f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "ParkPulse Brand Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ParkPulse",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BrandNeonCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BrandNeonCyan,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E2633),
                                onClick = { viewModel.toggleCityPicker(true) },
                                modifier = Modifier.testTag("top_bar_city_selector_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${state.selectedCity.name} (${state.selectedCity.country})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RomanGold,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Cambia Città",
                                        tint = RomanGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var isTopMenuOpen by remember { mutableStateOf(false) }

                        // Gamification Points Badge (Tappable -> Opens My Activity Dashboard)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RomanGold.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold.copy(alpha = 0.6f)),
                            onClick = { viewModel.setViewMode(ViewMode.ACTIVITY) },
                            modifier = Modifier.testTag("gamification_score_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Punti",
                                    tint = RomanGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.userProfile.points} pt",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RomanGold
                                )
                            }
                        }

                        // Community Leaderboard Action Button
                        IconButton(
                            onClick = { viewModel.toggleLeaderboardSheet(true) },
                            modifier = Modifier.size(36.dp).testTag("open_leaderboard_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "Classifica Community",
                                tint = RomanGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // "Dove ho parcheggiato?" Quick Access Button
                        IconButton(
                            onClick = { viewModel.toggleSavedCarSheet(true) },
                            modifier = Modifier.size(36.dp).testTag("top_open_saved_car_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Dov'è la mia auto?",
                                tint = if (state.savedCarLocation != null) BrandNeonCyan else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Gemini ZTL Copilot Action Button
                        IconButton(
                            onClick = { viewModel.toggleZtlSheet(true) },
                            modifier = Modifier.size(36.dp).testTag("open_copilot_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "ZTL Copilot",
                                tint = RomanGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings Action Button (Theme, Language, Units, Currency)
                        IconButton(
                            onClick = { viewModel.toggleSettingsDialog(true) },
                            modifier = Modifier.size(36.dp).testTag("open_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Impostazioni",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Overflow Options Dropdown Menu
                        Box {
                            IconButton(
                                onClick = { isTopMenuOpen = true },
                                modifier = Modifier.size(36.dp).testTag("open_app_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu opzioni",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isTopMenuOpen,
                                onDismissRequest = { isTopMenuOpen = false },
                                containerColor = Color(0xFF1E242E)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Impostazioni Globali (Tema, Lingua, Unità)", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.toggleSettingsDialog(true)
                                    },
                                    modifier = Modifier.testTag("menu_settings_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("Classifica Community & Punti", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Leaderboard,
                                            contentDescription = null,
                                            tint = RomanGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.toggleLeaderboardSheet(true)
                                    },
                                    modifier = Modifier.testTag("menu_leaderboard_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text("Seleziona Metropoli / Regione", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.toggleCityPicker(true)
                                    },
                                    modifier = Modifier.testTag("menu_select_city_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text(if (state.currentUser != null) "Gestisci Account (${state.currentUser?.displayName ?: "Google"})" else "Accedi con Google", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = RomanGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.openLoginScreen()
                                    },
                                    modifier = Modifier.testTag("menu_login_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("Previsioni ML & Trend", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = null,
                                            tint = RomanGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.toggleMlTrendsSheet(true)
                                    },
                                    modifier = Modifier.testTag("open_ml_trends_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("Veicolo & Permesso ZTL", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.toggleVehiclePermitDialog(true)
                                    },
                                    modifier = Modifier.testTag("open_vehicle_permit_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("Profilo & Impostazioni", color = Color.White, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.togglePrivacyDialog(true)
                                    },
                                    modifier = Modifier.testTag("open_privacy_dialog_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("Simula Allerta Varchi ZTL", color = Color.Yellow, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color.Yellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.triggerTestZtlWarning(context)
                                    },
                                    modifier = Modifier.testTag("trigger_test_ztl_btn")
                                )

                                DropdownMenuItem(
                                    text = { Text("🚨 Simula Allerta Traffico WorkManager", color = Color(0xFFFF8A80), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFFF8A80),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        isTopMenuOpen = false
                                        viewModel.triggerWorkManagerTrafficAlert(context)
                                    },
                                    modifier = Modifier.testTag("trigger_workmanager_traffic_alert_btn")
                                )
                            }
                        }
                    }
                }

                // Search Bar and Real-Time Filter Chips only shown for Map, List, and Saved modes
                if (state.viewMode == ViewMode.MAP || state.viewMode == ViewMode.LIST || state.viewMode == ViewMode.SAVED) {
                    PlacesAutocompleteSearchBar(
                        query = state.searchQuery,
                        onQueryChange = {
                            viewModel.setSearchQuery(it)
                            viewModel.searchPlaces(it)
                        },
                        predictions = state.searchPredictions,
                        isSearching = state.isSearchingPlaces,
                        selectedCategory = state.selectedCategory,
                        onSelectCategory = { cat ->
                            viewModel.setParkingCategory(cat)
                        },
                        onSelectPrediction = { pred ->
                            viewModel.selectPlacePrediction(pred)
                        },
                        onUseCurrentLocation = {
                            state.userLocation?.let { loc ->
                                viewModel.setNearestCityFromLocation(loc)
                            } ?: run {
                                fetchDeviceLocation(context) { loc ->
                                    viewModel.updateLocationPermission(true, loc, context)
                                    viewModel.setNearestCityFromLocation(loc)
                                }
                            }
                        },
                        currentLanguage = state.languagePreference,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

            }
        },
        bottomBar = {
            if (state.activeNavigation == null) {
                NavigationBar(
                    containerColor = Color(0xFF0B111E),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple(ViewMode.MAP, "Mappa", Icons.Default.Map),
                        Triple(ViewMode.LIST, "Elenco", Icons.AutoMirrored.Filled.FormatListBulleted),
                        Triple(ViewMode.ACTIVITY, "Profilo & Report", Icons.Default.AccountCircle)
                    )

                    navItems.forEach { (mode, label, icon) ->
                        val isSelected = state.viewMode == mode
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setViewMode(mode) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandNeonCyan,
                                selectedTextColor = BrandNeonCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = BrandNeonCyan.copy(alpha = 0.16f)
                            ),
                            modifier = Modifier.testTag("nav_item_${mode.name.lowercase()}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.activeNavigation == null && (state.viewMode == ViewMode.MAP || state.viewMode == ViewMode.LIST) && state.selectedMicroArea == null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openDepartureModal() },
                    containerColor = TerracottaPrimary,
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = "Sto liberando",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("fab_report_departure")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.viewMode) {
                ViewMode.MAP -> {
                    // Render Google Maps SDK view or fallback to Tactical Vector Canvas based on user preference
                    if (state.mapRendererType == MapRendererType.GOOGLE_MAPS) {
                        GoogleMapsParkingView(
                            microAreas = state.filteredMicroAreas,
                            activeSignals = state.activeSignals,
                            parkedStays = state.activeParkedStays,
                            selectedArea = state.selectedMicroArea,
                            selectedAreaTrafficEta = state.selectedAreaTrafficEta,
                            userLocation = state.userLocation,
                            hasLocationPermission = state.hasLocationPermission,
                            isZtlLayerVisible = state.isZtlLayerVisible,
                            ztlZones = state.ztlZones,
                            ztlLiveFeed = state.ztlLiveFeed,
                            onRefreshZtlPublicData = { viewModel.refreshZtlPublicData() },
                            selectedZtlZone = state.selectedZtlZone,
                            activeZtlWarning = state.activeZtlWarning,
                            activeNavigation = state.activeNavigation,
                            savedCarLocation = state.savedCarLocation,
                            isSpeedCameraLayerVisible = state.isSpeedCameraLayerVisible,
                            speedCameras = state.speedCameras,
                            activeSpeedCameraWarning = state.activeSpeedCameraWarning,
                            onDismissSpeedCameraWarning = { viewModel.dismissSpeedCameraAlert() },
                            onQuickFindParking = { viewModel.searchParkingForCurrentOrMapCenter() },
                            onOpenSavedCar = { viewModel.toggleSavedCarSheet(true) },
                            onSelectArea = { viewModel.selectMicroArea(it) },
                            onReportDeparture = { viewModel.openDepartureModal(it.id) },
                            onRegisterParkedStay = { viewModel.openRegisterStayModal(it) },
                            onSignalClick = { viewModel.openFeedbackDialog(it) },
                            onStartNavigation = { area ->
                                viewModel.startNavigation(
                                    destinationId = area.id,
                                    destinationName = area.name,
                                    destinationAddress = area.streetName,
                                    destinationLocation = LatLng(area.latitude, area.longitude),
                                    parkingProbability = area.baselineAvailability
                                )
                            },
                            onAdvanceNavStep = { viewModel.advanceNavigationStep() },
                            onToggleNavVoice = { viewModel.toggleNavigationVoice() },
                            onToggleNavAvoidZtl = { viewModel.toggleNavigationAvoidZtl() },
                            onDivertToSpot = { viewModel.divertNavigationToFreeSpot(it) },
                            onDismissNavSpotAlert = { viewModel.dismissNavigationSpotAlert() },
                            onStopNavigation = { viewModel.stopNavigation() },
                            onToggleZtlLayer = { viewModel.toggleZtlLayer() },
                            onOpenZtlVisibilitySheet = { viewModel.toggleZtlVisibilitySheet(true) },
                            currentParkingTypeFilter = state.searchFilter.parkingType,
                            onSelectParkingTypeFilter = { viewModel.setParkingTypeFilter(it) },
                            onSelectZtlZone = { viewModel.selectZtlZone(it) },
                            onDismissZtlWarning = { viewModel.dismissZtlWarning(context) },
                            onTriggerTestWarning = { viewModel.triggerTestZtlWarning(context) },
                            onCameraMoved = { loc ->
                                viewModel.checkZtlProximity(loc, context)
                                viewModel.checkCongestionProximity(loc, context)
                            },
                            onOpenMlTrends = { area ->
                                viewModel.toggleMlTrendsSheet(true, area)
                            },
                            onRequestLocationPermission = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            onToggleMapEngine = { viewModel.toggleMapRenderer() }
                        )
                    } else {
                        MicroAreaMapCanvas(
                            microAreas = state.filteredMicroAreas,
                            activeSignals = state.activeSignals,
                            selectedArea = state.selectedMicroArea,
                            onSelectArea = { viewModel.selectMicroArea(it) },
                            onSignalClick = { viewModel.openFeedbackDialog(it) },
                            onReportDeparture = { viewModel.openDepartureModal(it.id) },
                            onRegisterParkedStay = { viewModel.openRegisterStayModal(it) },
                            onToggleMapEngine = { viewModel.toggleMapRenderer() }
                        )
                    }
                }

                ViewMode.LIST -> {
                    StreetAvailabilityListView(
                        microAreas = state.filteredMicroAreas,
                        favoriteIds = state.userProfile.favoriteAreaIds,
                        onSelectArea = { viewModel.selectMicroArea(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onReportDeparture = { viewModel.openDepartureModal(it.id) },
                        onRegisterParkedStay = { viewModel.openRegisterStayModal(it) },
                        onStartNavigation = { area ->
                            viewModel.startNavigation(
                                destinationId = area.id,
                                destinationName = area.name,
                                destinationAddress = area.streetName,
                                destinationLocation = LatLng(area.latitude, area.longitude),
                                parkingProbability = area.baselineAvailability
                            )
                        },
                        onOpenMlTrends = { area ->
                            viewModel.toggleMlTrendsSheet(true, area)
                        }
                    )
                }

                ViewMode.RADAR -> {
                    var radarDisplayMode by remember { androidx.compose.runtime.mutableIntStateOf(0) }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (radarDisplayMode == 0) TerracottaPrimary else Color(0xFF1E242E),
                                onClick = { radarDisplayMode = 0 },
                                modifier = Modifier.testTag("radar_tab_recharts")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Densità Recharts",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (radarDisplayMode == 1) TerracottaPrimary else Color(0xFF1E242E),
                                onClick = { radarDisplayMode = 1 },
                                modifier = Modifier.testTag("radar_tab_circular")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Radar Circolare",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (radarDisplayMode == 2) TerracottaPrimary else Color(0xFF1E242E),
                                onClick = { radarDisplayMode = 2 },
                                modifier = Modifier.testTag("radar_tab_sectors")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Mappa Settori",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        when (radarDisplayMode) {
                            0 -> {
                                RechartsDensityRadarView(
                                    microAreas = state.filteredMicroAreas,
                                    activeSignals = state.activeSignals,
                                    onSelectArea = { viewModel.selectMicroArea(it) },
                                    onStartNavigation = { area ->
                                        viewModel.startNavigation(
                                            destinationId = area.id,
                                            destinationName = area.name,
                                            destinationAddress = area.streetName,
                                            destinationLocation = LatLng(area.latitude, area.longitude),
                                            parkingProbability = area.baselineAvailability
                                        )
                                    },
                                    onOpenMlTrends = { area ->
                                        viewModel.toggleMlTrendsSheet(true, area)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            1 -> {
                                CircularRadarDensityView(
                                    userLocation = state.userLocation,
                                    microAreas = state.filteredMicroAreas,
                                    activeSignals = state.activeSignals,
                                    onSelectArea = { viewModel.selectMicroArea(it) },
                                    onStartNavigation = { area ->
                                        viewModel.startNavigation(
                                            destinationId = area.id,
                                            destinationName = area.name,
                                            destinationAddress = area.streetName,
                                            destinationLocation = LatLng(area.latitude, area.longitude),
                                            parkingProbability = area.baselineAvailability
                                        )
                                    },
                                    onOpenMlTrends = { area ->
                                        viewModel.toggleMlTrendsSheet(true, area)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            else -> {
                                NeighborhoodRadarView(
                                    sectors = state.sectors,
                                    radarSubBlocks = state.radarSubBlocks,
                                    selectedSubBlock = state.selectedSubBlock,
                                    onSelectSubBlock = { viewModel.selectSubBlock(it) },
                                    onSelectSector = { /* highlights sector */ },
                                    isOfflineMode = state.isOfflineMode,
                                    lastCacheSyncEpochMs = state.lastCacheSyncEpochMs,
                                    onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                ViewMode.SAVED -> {
                    SavedFavoritesScreen(
                        savedAreas = state.savedMicroAreas,
                        userSettings = state.userSettings,
                        isCloudSynced = state.currentUser != null,
                        onSelectArea = { viewModel.selectMicroArea(it) },
                        onToggleFavorite = { areaId -> viewModel.toggleFavorite(areaId) },
                        onStartNavigation = { area ->
                            viewModel.startNavigation(
                                destinationId = area.id,
                                destinationName = area.name,
                                destinationAddress = area.streetName,
                                destinationLocation = LatLng(area.latitude, area.longitude),
                                parkingProbability = area.baselineAvailability
                            )
                        },
                        onExploreMap = { viewModel.setViewMode(ViewMode.MAP) }
                    )
                }

                ViewMode.ACTIVITY -> {
                    UserProfileReportScreen(
                        userProfile = state.userProfile,
                        currentUser = state.currentUser,
                        isGuestMode = state.isGuestMode,
                        savedCarLocation = state.savedCarLocation,
                        activities = state.userActivities,
                        selectedFilter = state.filterActivityType,
                        onSelectFilter = { viewModel.setFilterActivityType(it) },
                        onOpenDepartureModal = { viewModel.openDepartureModal() },
                        onOpenRegisterStay = { viewModel.openRegisterStayModal(state.selectedMicroArea ?: state.microAreas.firstOrNull()) },
                        onOpenSavedCar = { viewModel.toggleSavedCarSheet(true) },
                        onOpenVehiclePermitDialog = { viewModel.toggleVehiclePermitDialog(true) },
                        onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                        onOpenLeaderboard = { viewModel.toggleLeaderboardSheet(true) },
                        onLoginClick = { viewModel.openLoginScreen() },
                        onNavigateToSpot = { act ->
                            viewModel.startNavigation(
                                destinationId = act.id,
                                destinationName = act.microAreaName,
                                destinationAddress = act.streetAddress,
                                destinationLocation = LatLng(act.latitude, act.longitude),
                                parkingProbability = 88
                            )
                        }
                    )
                }
            }

            // In-App Alert Banners (ZTL Warning, Emergency Traffic WorkManager, Congestion, Nearby Spot Found)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Real-Time WorkManager Emergency Traffic Congestion & Street Closure Alerts
                state.activeTrafficIncidents.forEach { incident ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF5252)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workmanager_traffic_alert_banner")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Emergenza Traffico WorkManager",
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = incident.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${incident.streetAffected} • Rallentamenti: +${incident.delayMinutes}m",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Yellow
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.dismissTrafficIncident(incident.id) },
                                    modifier = Modifier.size(28.dp).testTag("dismiss_traffic_incident_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Chiudi avviso",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = incident.description,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "👉 ${incident.suggestedAlternativeRoute}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RomanGold
                            )
                        }
                    }
                }

                // High-Priority In-App ZTL Warning Alert Banner (Mandatory Safety Banner)
                state.activeZtlWarning?.let { warning ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF5252)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ztl_high_priority_banner_alert")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Allerta ZTL",
                                        tint = Color.Yellow,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "⚠️ ALLERTA ZTL: ${warning.zone.name}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (warning.isInside) "SEI ALL'INTERNO DELL'AREA ZTL ATTIVA!" else "Ti trovi a soli ${warning.distanceMeters}m dal varco!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Yellow
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.dismissZtlWarning(context) },
                                    modifier = Modifier.size(28.dp).testTag("dismiss_ztl_banner_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Chiudi",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Orario attivo: ${warning.activeHours}. Sanzione da 83€ a 332€ per transito non autorizzato.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.95f)
                            )

                            val plateText = if (state.userProfile.licensePlate.isNotBlank()) {
                                "Veicolo: ${state.userProfile.licensePlate} (${state.userProfile.vehiclePermit.label})"
                            } else {
                                "Nessun veicolo registrato"
                            }
                            Text(
                                text = plateText,
                                fontSize = 10.sp,
                                color = RomanGold
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.dismissZtlWarning(context)
                                        viewModel.toggleVehiclePermitDialog(true)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Verifica Permesso", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.dismissZtlWarning(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Ho Permesso / Chiudi", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // In-App Real-Time Congestion Proximity Banner
                state.activeCongestionAlertSubBlock?.let { block ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TerracottaPrimary.copy(alpha = 0.95f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_app_congestion_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Allerta Congestione: ${block.shortName} (${block.congestionPercent}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tempo attesa ~${block.estimatedSearchTimeMinutes} min. ${block.recommendation}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.setViewMode(ViewMode.RADAR) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Radar", fontSize = 10.sp, color = RomanGold, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.dismissCongestionAlert() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Chiudi",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // In-App Real-Time Spot Available Preference Banner
                state.activeSpotAvailableAlertSignal?.let { sig ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.AvailGreen.copy(alpha = 0.95f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_app_spot_available_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Posto Libero: ${sig.streetName}!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Un automobilista sta liberando il posto (~${sig.remainingSeconds(System.currentTimeMillis()) / 60} min rimasti)",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        NavigationHelper.openInExternalMaps(
                                            context = context,
                                            latitude = sig.latitude,
                                            longitude = sig.longitude,
                                            destinationName = "${sig.streetName} (${sig.microAreaName})"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Naviga", fontSize = 10.sp, color = com.example.ui.theme.AvailGreen, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.dismissSpotAlert() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Chiudi",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Top Persistent Active Parked Stay Banner (if user has marked where they parked)
                state.userProfile.activeStay?.let { stay ->
                    val remainingMins = stay.remainingMinutes(System.currentTimeMillis())
                    val sdf = SimpleDateFormat("HH:mm", Locale.ITALY)
                    val expectedStr = sdf.format(Date(stay.expectedDepartureEpochMs))

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_user_stay_banner")
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = RomanGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Auto parcheggiata in ${stay.microAreaName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Partenza stimata ore $expectedStr (~$remainingMins min rimasti)",
                                    fontSize = 11.sp,
                                    color = RomanGold
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.convertMyStayToDeparture() },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("leave_early_button")
                            ) {
                                Text(
                                    text = "Sto partendo!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { viewModel.cancelMyParkedStay() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Rimuovi sosta",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Modal: Sto liberando un posto (with Duration Slider: 5, 10, 15 min or Occupy)
    if (state.showDepartureModal) {
        DepartureSignalBottomSheet(
            microAreas = state.microAreas,
            selectedAreaId = state.departureAreaId,
            onSelectAreaId = { viewModel.setDepartureAreaId(it) },
            initialDurationMinutes = state.departureSliderDuration,
            initialMode = state.departureSliderMode,
            onConfirmDeparture = { duration, mode ->
                viewModel.submitDepartureSignal(duration, mode, context)
            },
            onDismiss = { viewModel.closeDepartureModal() }
        )
    }

    // Modal: Ho parcheggiato qui (imposta durata sosta: 5m, 10m, 15m, 30m, 1h, 2h, 3h, 4h+)
    if (state.showRegisterStayModal && state.areaForStayRegistration != null) {
        RegisterParkedStayBottomSheet(
            area = state.areaForStayRegistration!!,
            onConfirmStay = { viewModel.confirmParkedStay(it) },
            onDismiss = { viewModel.closeRegisterStayModal() }
        )
    }

    // Modal: Community Feedback (Trovato, Non trovato, Segnala dato errato)
    state.signalForFeedback?.let { signal ->
        FeedbackDialog(
            signal = signal,
            onFeedback = { viewModel.submitSignalFeedback(it) },
            onDismiss = { viewModel.closeFeedbackDialog() }
        )
    }

    // Sheet: Gemini ZTL & Hazard Copilot
    if (state.showZtlSheet) {
        GeminiZtlCopilotSheet(
            targetStreet = state.copilotTargetStreet,
            modelChoice = state.copilotModel,
            isLoading = state.copilotIsLoading,
            result = state.copilotResult,
            ztlGates = state.ztlGates,
            onSelectStreet = { viewModel.runCopilotAssessment(it) },
            onSelectModel = { viewModel.setCopilotModel(it) },
            onDismiss = { viewModel.toggleZtlSheet(false) }
        )
    }

    // Dialog: Profile & Gamification & Privacy
    if (state.showPrivacyDialog) {
        AccountPrivacyDialog(
            userProfile = state.userProfile,
            currentLanguage = state.languagePreference,
            onLanguageChange = { viewModel.setLanguagePreference(it) },
            currentUserEmail = state.currentUser?.email,
            currentUserName = state.currentUser?.displayName,
            onSignOut = { viewModel.signOut(context) },
            onOpenLogin = {
                viewModel.togglePrivacyDialog(false)
                viewModel.openLoginScreen()
            },
            onToggleNotifications = { viewModel.toggleNotifications(it) },
            onDeleteAccount = { viewModel.deleteAccountAndData() },
            onOpenVehicleDialog = {
                viewModel.togglePrivacyDialog(false)
                viewModel.toggleVehiclePermitDialog(true)
            },
            onDismiss = { viewModel.togglePrivacyDialog(false) }
        )
    }

    // Sheet: Global City & Region Selector
    if (state.showCityPickerSheet) {
        CitySelectorBottomSheet(
            selectedCity = state.selectedCity,
            currentLanguage = state.languagePreference,
            userLocation = state.userLocation,
            onCitySelected = { viewModel.selectCity(it) },
            onUseCurrentLocation = {
                state.userLocation?.let { loc ->
                    viewModel.setNearestCityFromLocation(loc)
                } ?: run {
                    fetchDeviceLocation(context) { loc ->
                        viewModel.updateLocationPermission(true, loc, context)
                        viewModel.setNearestCityFromLocation(loc)
                    }
                }
            },
            onDismiss = { viewModel.toggleCityPicker(false) }
        )
    }

    // Sheet: Advanced Search Filters (Time of day, Duration, Parking type)
    if (state.showFilterSheet) {
        AdvancedFilterSheet(
            currentFilter = state.searchFilter,
            matchingCount = state.filteredMicroAreas.size,
            onFilterChange = { viewModel.updateSearchFilter(it) },
            onResetFilter = { viewModel.resetSearchFilter() },
            onDismiss = { viewModel.toggleFilterSheet(false) }
        )
    }

    // Safety Dialog: Unauthorized ZTL Route Breach Warning
    state.pendingZtlRouteBreach?.let { breach ->
        ZtlUnauthorizedRouteWarningDialog(
            warning = breach,
            onRerouteAvoidZtl = { viewModel.reroutePlannedRouteAvoidingZtl() },
            onEditVehicleData = {
                viewModel.dismissZtlRouteBreachWarning()
                viewModel.toggleVehiclePermitDialog(true)
            },
            onProceedAnyway = { viewModel.proceedAnywayWithZtlBreach() },
            onDismiss = { viewModel.dismissZtlRouteBreachWarning() }
        )
    }

    // Dialog: Vehicle Plate & ZTL Permit Configuration
    if (state.showVehiclePermitDialog) {
        VehiclePermitDialog(
            userProfile = state.userProfile,
            onSave = { plate, model, permit, num, exp, active ->
                viewModel.updateVehicleData(plate, model, permit, num, exp, active)
            },
            onDismiss = { viewModel.toggleVehiclePermitDialog(false) }
        )
    }

    // Sheet: Gemini Machine Learning Availability Trends
    if (state.showMlTrendsSheet) {
        GeminiParkingMlTrendsSheet(
            targetArea = state.mlTargetArea,
            prediction = state.mlPrediction,
            isLoading = state.mlPredictionIsLoading,
            onRefreshPrediction = { forceLocal ->
                state.mlTargetArea?.let { viewModel.requestMlAvailabilityTrend(it, forceLocal) }
            },
            onDismiss = { viewModel.toggleMlTrendsSheet(false) }
        )
    }

    // Bottom Sheet: ZTL Visibility Layer Management
    if (state.showZtlVisibilitySheet) {
        ZtlVisibilityBottomSheet(
            isZtlLayerVisible = state.isZtlLayerVisible,
            visibleZtlZoneIds = state.visibleZtlZoneIds,
            ztlZones = state.ztlZones,
            onToggleMasterZtlLayer = { viewModel.toggleZtlLayer() },
            onToggleZoneVisibility = { viewModel.toggleZtlZoneVisibility(it) },
            onSetAllZonesVisible = { viewModel.setAllZtlZonesVisible(it) },
            onSetOnlyActiveZonesVisible = { viewModel.setOnlyActiveZtlZonesVisible() },
            onDismiss = { viewModel.toggleZtlVisibilitySheet(false) }
        )
    }

    // Dialog: User Settings (Language, Dynamic M3 Light/Dark Theme, Units, Currency, Onboarding replay)
    if (state.showSettingsDialog) {
        SettingsDialog(
            userSettings = state.userSettings,
            onUpdateSettings = { viewModel.updateUserSettings(it) },
            onReplayOnboarding = {
                viewModel.toggleSettingsDialog(false)
                viewModel.updateUserSettings(state.userSettings.copy(hasCompletedOnboarding = false))
            },
            onDismiss = { viewModel.toggleSettingsDialog(false) }
        )
    }

    // Sheet: Community Leaderboard & Gamification Contributions
    if (state.showLeaderboardSheet) {
        CommunityLeaderboardSheet(
            userProfile = state.userProfile,
            leaderboardEntries = state.leaderboard,
            currentLanguage = state.languagePreference,
            onDismiss = { viewModel.toggleLeaderboardSheet(false) }
        )
    }

    // Bottom Sheet: "Dove ho parcheggiato?" (Phase 9)
    if (state.showSavedCarSheet) {
        SavedCarBottomSheet(
            savedCar = state.savedCarLocation,
            currentGps = state.userLocation,
            onSaveCarLocation = { addr, notes, meter, floor ->
                viewModel.saveCarParkedLocation(
                    currentGps = state.userLocation,
                    address = addr,
                    notes = notes,
                    meterMinutes = meter,
                    floor = floor
                )
            },
            onClearSavedCar = { viewModel.clearSavedCarLocation() },
            onAddMeterTime = { mins -> viewModel.updateMeterTimer(mins) },
            onStartWalkingToCar = { carLoc ->
                val origin = state.userLocation ?: LatLng(41.8992, 12.5028)
                viewModel.startNavigation(
                    destinationId = "saved_car",
                    destinationName = "La Mia Auto",
                    destinationAddress = state.savedCarLocation?.address ?: "Posizione veicolo",
                    destinationLocation = carLoc,
                    bypassZtlWarning = true
                )
            },
            onDismiss = { viewModel.toggleSavedCarSheet(false) }
        )
    }
}

