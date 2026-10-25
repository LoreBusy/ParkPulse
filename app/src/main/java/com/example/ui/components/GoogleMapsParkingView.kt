package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import com.example.data.model.TrafficEtaResult
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Speed
import com.example.data.model.ParkingTypeFilter
import com.example.data.provider.GateOperationalStatus
import com.example.data.provider.ZtlGateLiveIndicator
import com.example.data.provider.ZtlLiveStatusFeed
import com.example.data.provider.ZtlZoneLiveStatus
import com.example.util.HeatmapMetric
import com.example.util.ParkingHeatmapHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.NavigationHelper
import com.example.data.model.ActiveNavigationSession
import com.example.data.model.AvailabilityLevel
import com.example.data.model.DepartureSignal
import com.example.data.model.MicroArea
import com.example.data.model.ParkedStay
import com.example.data.model.ZtlWarningAlert
import com.example.data.model.ZtlZone
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.BlueLineZone
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import kotlinx.coroutines.launch

import com.example.data.model.SavedCarLocation
import com.example.data.model.SpeedCamera
import com.example.data.provider.SpeedCameraWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapsParkingView(
    microAreas: List<MicroArea>,
    activeSignals: List<DepartureSignal>,
    parkedStays: List<ParkedStay>,
    selectedArea: MicroArea?,
    selectedAreaTrafficEta: TrafficEtaResult? = null,
    userLocation: LatLng?,
    hasLocationPermission: Boolean,
    isZtlLayerVisible: Boolean = true,
    ztlZones: List<ZtlZone> = emptyList(),
    ztlLiveFeed: ZtlLiveStatusFeed? = null,
    onRefreshZtlPublicData: () -> Unit = {},
    selectedZtlZone: ZtlZone? = null,
    activeZtlWarning: ZtlWarningAlert? = null,
    activeNavigation: ActiveNavigationSession? = null,
    savedCarLocation: SavedCarLocation? = null,
    isSpeedCameraLayerVisible: Boolean = true,
    speedCameras: List<SpeedCamera> = emptyList(),
    activeSpeedCameraWarning: SpeedCameraWarning? = null,
    onDismissSpeedCameraWarning: () -> Unit = {},
    onQuickFindParking: () -> Unit = {},
    onOpenSavedCar: () -> Unit = {},
    onSelectArea: (MicroArea?) -> Unit = {},
    onReportDeparture: (MicroArea) -> Unit,
    onRegisterParkedStay: (MicroArea) -> Unit,
    onSignalClick: (DepartureSignal) -> Unit,
    onStartNavigation: (MicroArea) -> Unit = {},
    onAdvanceNavStep: () -> Unit = {},
    onToggleNavVoice: () -> Unit = {},
    onToggleNavAvoidZtl: () -> Unit = {},
    onDivertToSpot: (DepartureSignal) -> Unit = {},
    onDismissNavSpotAlert: () -> Unit = {},
    onStopNavigation: () -> Unit = {},
    onToggleZtlLayer: () -> Unit = {},
    onOpenZtlVisibilitySheet: () -> Unit = {},
    currentParkingTypeFilter: ParkingTypeFilter = ParkingTypeFilter.ALL,
    onSelectParkingTypeFilter: (ParkingTypeFilter) -> Unit = {},
    favoriteAreas: List<MicroArea> = emptyList(),
    favoriteIds: Set<String> = emptySet(),
    onToggleFavorite: (String) -> Unit = {},
    onSelectZtlZone: (ZtlZone?) -> Unit = {},
    onDismissZtlWarning: () -> Unit = {},
    onTriggerTestWarning: () -> Unit = {},
    onCameraMoved: (LatLng) -> Unit = {},
    onOpenMlTrends: (MicroArea) -> Unit = {},
    onDismissSelectedArea: () -> Unit = {},
    onRequestLocationPermission: () -> Unit,
    onToggleMapEngine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Camera initialized at real user location if available, or neutral overview without fake city defaults
    val initialCenter = remember(userLocation) {
        userLocation ?: LatLng(42.50, 12.50)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, if (userLocation != null) 16f else 6f)
    }
    var hasCenteredOnUser by remember { mutableStateOf(userLocation != null) }


    var isSatellite by remember { mutableStateOf(false) }
    var isRadarDensityOverlayVisible by remember { mutableStateOf(false) }
    var isHeatmapVisible by remember { mutableStateOf(true) }
    var currentHeatmapMetric by remember { mutableStateOf(HeatmapMetric.SATURATION_DENSITY) }
    val tileOverlayState = rememberTileOverlayState()

    // Real-time dynamic heatmap weighted points representing parking density around Termini-Esquilino
    val heatmapPoints = remember(microAreas, activeSignals, currentHeatmapMetric) {
        ParkingHeatmapHelper.buildRealtimeDensityPoints(
            microAreas = microAreas,
            activeSignals = activeSignals,
            metric = currentHeatmapMetric
        )
    }

    val heatmapTileProvider = remember(heatmapPoints, currentHeatmapMetric) {
        ParkingHeatmapHelper.createHeatmapProvider(
            points = heatmapPoints,
            metric = currentHeatmapMetric
        )
    }

    // Trigger tile cache clearance whenever real-time parking density points or metrics update
    LaunchedEffect(heatmapPoints, currentHeatmapMetric) {
        try {
            tileOverlayState.clearTileCache()
        } catch (e: Exception) {
            // Ignore if overlay not yet attached
        }
    }

    var radarRangeMeters by remember { mutableFloatStateOf(650f) }

    // Animated continuous scanning radar wave for maps-compose Circle
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweepAnimation")
    val radarSweepRadius by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = radarRangeMeters,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepRadius"
    )
    val radarSweepAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepAlpha"
    )

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val mapProperties = remember(isSatellite, hasLocationPermission) {
        MapProperties(
            mapType = if (isSatellite) MapType.HYBRID else MapType.NORMAL,
            isMyLocationEnabled = hasLocationPermission
        )
    }

    // Animate camera when user changes selected area
    LaunchedEffect(selectedArea) {
        if (activeNavigation == null) {
            selectedArea?.let {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16.5f),
                    600
                )
            }
        }
    }

    // Animate camera when active navigation step/vehicle updates
    LaunchedEffect(activeNavigation?.currentVehicleLocation) {
        activeNavigation?.currentVehicleLocation?.let { loc ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(loc, 17f),
                500
            )
        }
    }

    // Proximity trigger when camera movement settles
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            onCameraMoved(cameraPositionState.position.target)
        }
    }

    // Proximity trigger and automatic centering when user location is acquired
    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            onCameraMoved(loc)
            if (!hasCenteredOnUser) {
                hasCenteredOnUser = true
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(loc, 16.0f),
                    700
                )
            }
        }
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("google_maps_view_container")
    ) {
        // Real Google Maps Compose instance
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = {
                // Clicking map background
            }
        ) {
            // Render Rome ZTL Zones (Boundaries & Live Gate Indicators) if layer is active
            if (isZtlLayerVisible) {
                val liveZones = ztlLiveFeed?.zones
                if (liveZones != null && liveZones.isNotEmpty()) {
                    liveZones.forEach { zoneStatus ->
                        val isActive = zoneStatus.isActive
                        val polyFill = if (isActive) Color(0x35D32F2F) else Color(0x1843A047)
                        val polyStroke = if (isActive) Color(0xFFE53935) else Color(0xFF2E7D32)
                        val strokeWidth = if (isActive) 5f else 3.5f

                        Polygon(
                            points = zoneStatus.polygonPoints,
                            fillColor = polyFill,
                            strokeColor = polyStroke,
                            strokeWidth = strokeWidth,
                            clickable = true,
                            onClick = {
                                val baseZone = ztlZones.find { it.id == zoneStatus.zoneId }
                                    ?: ZtlZone(
                                        id = zoneStatus.zoneId,
                                        name = zoneStatus.name,
                                        description = zoneStatus.description,
                                        polygonPoints = zoneStatus.polygonPoints,
                                        dayActiveSchedule = zoneStatus.activeScheduleText,
                                        nightActiveSchedule = zoneStatus.activeScheduleText
                                    )
                                onSelectZtlZone(baseZone)
                            }
                        )

                        // Live Electronic Gate Markers (Varchi Elettronici)
                        zoneStatus.gates.forEach { gate ->
                            val isGateActive = gate.isGateActive
                            val markerHue = if (isGateActive) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                            val title = "${gate.name} (${gate.statusText})"
                            val snippet = "Orari: ${gate.activeHours} • Camera: ${if (gate.isCameraActive) "Attiva" else "Disattiva"}"

                            Marker(
                                state = MarkerState(position = LatLng(gate.latitude, gate.longitude)),
                                title = title,
                                snippet = snippet,
                                icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                onClick = {
                                    val baseZone = ztlZones.find { it.id == zoneStatus.zoneId }
                                    onSelectZtlZone(baseZone)
                                    false
                                }
                            )
                        }
                    }
                } else {
                    ztlZones.forEach { zone ->
                        val isActive = zone.isCurrentlyActive()
                        val polyFill = if (isActive) Color(0x35E53935) else Color(0x1843A047)
                        val polyStroke = if (isActive) Color(0xFFD32F2F) else Color(0xFF2E7D32)

                        Polygon(
                            points = zone.polygonPoints,
                            fillColor = polyFill,
                            strokeColor = polyStroke,
                            strokeWidth = if (isActive) 5f else 3.5f,
                            clickable = true,
                            onClick = { onSelectZtlZone(zone) }
                        )

                        // Gate Markers fallback
                        zone.gates.forEach { gate ->
                            val markerHue = if (isActive) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                            Marker(
                                state = MarkerState(position = LatLng(gate.latitude, gate.longitude)),
                                title = "${gate.name} (${if (isActive) "🔴 VARCO ATTIVO" else "🟢 LIBERO ACCESSO"})",
                                snippet = "Orari: ${gate.activeHours} • Fonte: Roma Mobilità",
                                icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                onClick = {
                                    onSelectZtlZone(zone)
                                    false
                                }
                            )
                        }
                    }
                }
            }

            // Render Speed Cameras / Autovelox (MIT Official Registry / Verified)
            if (isSpeedCameraLayerVisible) {
                speedCameras.forEach { cam ->
                    Marker(
                        state = MarkerState(position = LatLng(cam.latitude, cam.longitude)),
                        title = "${cam.name} (${cam.speedLimitKmh} km/h)",
                        snippet = "${cam.type.label} • Fonte: ${cam.reliability.sourceTag}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            // Render Saved Car Position ("Dove ho parcheggiato?")
            savedCarLocation?.let { car ->
                Marker(
                    state = MarkerState(position = LatLng(car.latitude, car.longitude)),
                    title = "🚗 La tua auto parcheggiata",
                    snippet = "${car.address}${if (car.floorOrZone.isNotBlank()) " • ${car.floorOrZone}" else ""}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
                )
            }


            // -------------------------------------------------------------
            // DYNAMIC REAL-TIME HEATMAP OVERLAY (Google Maps Compose TileOverlay)
            // -------------------------------------------------------------
            if (isHeatmapVisible && heatmapTileProvider != null) {
                TileOverlay(
                    tileProvider = heatmapTileProvider,
                    state = tileOverlayState,
                    fadeIn = true,
                    transparency = 0.15f
                )
            }

            // -------------------------------------------------------------
            // RADAR DENSITY VISUALIZATION OVERLAY (maps-compose Circle components)
            // -------------------------------------------------------------
            if (isRadarDensityOverlayVisible) {
                val radarCenter = userLocation ?: initialCenter

                // 1. Concentric Distance Range Rings (150m, 350m, 600m, 850m)
                val rings = listOf(
                    150.0 to RomanGold.copy(alpha = 0.55f),
                    350.0 to RomanGold.copy(alpha = 0.40f),
                    600.0 to RomanGold.copy(alpha = 0.30f),
                    850.0 to RomanGold.copy(alpha = 0.20f)
                )
                rings.forEach { (radiusMeters, ringColor) ->
                    Circle(
                        center = radarCenter,
                        radius = radiusMeters,
                        strokeColor = ringColor,
                        strokeWidth = 2.5f,
                        fillColor = Color.Transparent
                    )
                }

                // 2. Animated continuous scanning radar pulse sweep
                Circle(
                    center = radarCenter,
                    radius = radarSweepRadius.toDouble(),
                    strokeColor = RomanGold.copy(alpha = radarSweepAlpha),
                    strokeWidth = 3f,
                    fillColor = RomanGold.copy(alpha = radarSweepAlpha * 0.12f)
                )

                // 3. Central Radar Origin Anchor Beacon
                Circle(
                    center = radarCenter,
                    radius = 14.0,
                    strokeColor = Color.White,
                    strokeWidth = 3f,
                    fillColor = TerracottaPrimary
                )

                // 4. Availability Density Cluster Halos around Termini–Esquilino micro-areas
                microAreas.forEach { area ->
                    val areaPos = LatLng(area.latitude, area.longitude)
                    val (haloFill, haloStroke) = when (area.availabilityLevel) {
                        AvailabilityLevel.HIGH -> AvailGreen.copy(alpha = 0.26f) to AvailGreen.copy(alpha = 0.75f)
                        AvailabilityLevel.MEDIUM -> AvailAmber.copy(alpha = 0.22f) to AvailAmber.copy(alpha = 0.70f)
                        AvailabilityLevel.LOW -> AvailRed.copy(alpha = 0.20f) to AvailRed.copy(alpha = 0.65f)
                    }

                    // Cluster Density Halo (maps-compose Circle)
                    Circle(
                        center = areaPos,
                        radius = (area.radiusMeters * 1.6).coerceIn(60.0, 130.0),
                        strokeColor = haloStroke,
                        strokeWidth = 2f,
                        fillColor = haloFill
                    )

                    // Real-Time Departure Signal Vacancy Aura if signal active in cluster
                    val hasActiveDeparture = activeSignals.any { it.microAreaId == area.id }
                    if (hasActiveDeparture) {
                        Circle(
                            center = areaPos,
                            radius = (area.radiusMeters * 2.1).coerceIn(80.0, 160.0),
                            strokeColor = Color(0xFF00E5FF),
                            strokeWidth = 3f,
                            fillColor = Color(0x3500E5FF)
                        )
                    }
                }
            }

            // Render 30m - 75m anonymous circles and custom availability markers
            microAreas.forEach { area ->
                val pos = LatLng(area.latitude, area.longitude)
                val isSelected = selectedArea?.id == area.id

                val (strokeCol, fillCol) = when (area.availabilityLevel) {
                    AvailabilityLevel.HIGH -> AvailGreen to AvailGreen.copy(alpha = if (isSelected) 0.45f else 0.22f)
                    AvailabilityLevel.MEDIUM -> AvailAmber to AvailAmber.copy(alpha = if (isSelected) 0.45f else 0.22f)
                    AvailabilityLevel.LOW -> AvailRed to AvailRed.copy(alpha = if (isSelected) 0.45f else 0.22f)
                }

                val markerHue = when (area.availabilityLevel) {
                    AvailabilityLevel.HIGH -> BitmapDescriptorFactory.HUE_GREEN
                    AvailabilityLevel.MEDIUM -> BitmapDescriptorFactory.HUE_ORANGE
                    AvailabilityLevel.LOW -> BitmapDescriptorFactory.HUE_RED
                }

                // Anonymous micro-area radius circle (30-75 meters)
                Circle(
                    center = pos,
                    radius = area.radiusMeters.toDouble(),
                    strokeColor = if (isSelected) Color.White else strokeCol,
                    strokeWidth = if (isSelected) 7f else 4f,
                    fillColor = fillCol,
                    clickable = true,
                    onClick = { onSelectArea(area) }
                )

                // Custom marker representing the availability cluster near Termini
                Marker(
                    state = MarkerState(position = pos),
                    title = "🅿️ ${area.name}",
                    snippet = "${area.baselineAvailability}% disp. • ${area.blueLineTariff}",
                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                    onClick = {
                        onSelectArea(area)
                        false
                    }
                )
            }

            // Real-time user departure signals (5-minute countdown spots) custom markers
            activeSignals.forEach { sig ->
                val remainingSec = sig.remainingSeconds(System.currentTimeMillis())
                Marker(
                    state = MarkerState(position = LatLng(sig.latitude, sig.longitude)),
                    title = "⚡ POSTO IN LIBERAZIONE (${remainingSec / 60}m ${remainingSec % 60}s)",
                    snippet = "${sig.microAreaName} • Affidabilità ${sig.userReliabilityPercent}%",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = {
                        onSignalClick(sig)
                        false
                    }
                )
            }

            // User location marker if permitted and active
            if (userLocation != null && !hasLocationPermission) {
                Marker(
                    state = MarkerState(position = userLocation),
                    title = "La tua posizione stimata",
                    snippet = "Roma Termini",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                )
            }

            // Active Turn-by-Turn Navigation Route Overlay (Meglio di Waze/Maps/ZigZag)
            if (activeNavigation != null) {
                // Route background outline
                Polyline(
                    points = activeNavigation.routePolyline,
                    color = Color(0xFF0F172A),
                    width = 20f
                )
                // Route glowing active line
                Polyline(
                    points = activeNavigation.routePolyline,
                    color = Color(0xFF00E5FF),
                    width = 12f
                )

                // Vehicle position marker
                Marker(
                    state = MarkerState(position = activeNavigation.currentVehicleLocation),
                    title = "La tua auto in movimento",
                    snippet = "Velocità: ${activeNavigation.currentSpeedKmh} km/h"
                )

                // Destination Parking Spot marker
                Marker(
                    state = MarkerState(position = activeNavigation.destinationLocation),
                    title = "Destinazione: ${activeNavigation.destinationName}",
                    snippet = "${activeNavigation.destinationParkingProbability}% probabilità posto libero"
                )
            }
        }

        // Speed Camera Proximity Warning Banner (MIT Official Registry / Verified)
        if (activeSpeedCameraWarning != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (activeSpeedCameraWarning.isOverSpeedLimit) AvailRed else Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (activeNavigation != null) 100.dp else 56.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .testTag("speed_camera_warning_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Autovelox",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activeSpeedCameraWarning.warningMessage,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Fonte: ${activeSpeedCameraWarning.camera.reliability.label}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismissSpeedCameraWarning,
                        modifier = Modifier.size(28.dp).testTag("dismiss_speed_camera_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // If Active Navigation is in progress, display HUD overlay, otherwise normal controls
        if (activeNavigation != null) {
            SmartNavigatorHud(
                session = activeNavigation,
                onAdvanceStep = onAdvanceNavStep,
                onToggleVoice = onToggleNavVoice,
                onToggleAvoidZtl = onToggleNavAvoidZtl,
                onDivertToSpot = onDivertToSpot,
                onDismissSpotAlert = onDismissNavSpotAlert,
                onStopNavigation = onStopNavigation,
                onRegisterParkedStay = {
                    selectedArea?.let { onRegisterParkedStay(it) }
                        ?: microAreas.firstOrNull()?.let { onRegisterParkedStay(it) }
                }
            )
        } else {
            // Floating Filter System on Map Interface: 'Free', 'Paid', 'EV Charging', 'All'
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Color(0xFF333D4B)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .testTag("map_parking_type_filter_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterOptions = listOf(
                        Triple(ParkingTypeFilter.ALL, "Tutti", "map_filter_all"),
                        Triple(ParkingTypeFilter.FREE_WHITE, "Gratis", "map_filter_free"),
                        Triple(ParkingTypeFilter.METERED_BLUE, "A pagamento", "map_filter_paid"),
                        Triple(ParkingTypeFilter.EV_CHARGING, "EV Hub", "map_filter_ev")
                    )

                    filterOptions.forEach { (type, label, tag) ->
                        val isSelected = currentParkingTypeFilter == type
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) TerracottaPrimary else Color.Transparent,
                            border = if (!isSelected) BorderStroke(1.dp, Color(0x33FFFFFF)) else null,
                            onClick = { onSelectParkingTypeFilter(type) },
                            modifier = Modifier.testTag(tag)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (type == ParkingTypeFilter.EV_CHARGING) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricCar,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else RomanGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                } else if (type == ParkingTypeFilter.FREE_WHITE) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (isSelected) Color.White else AvailGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }

            // Quick-Access Favorites Bar (Saved Parking Areas in Room Database)
            if (favoriteAreas.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF141920).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 54.dp)
                        .testTag("quick_favorites_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Preferiti",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Preferiti:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RomanGold
                        )
                        favoriteAreas.take(3).forEach { fav ->
                            val isSel = selectedArea?.id == fav.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) TerracottaPrimary else Color(0xFF222834),
                                onClick = {
                                    onSelectArea(fav)
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(fav.latitude, fav.longitude),
                                                16.5f
                                            ),
                                            500
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("quick_fav_chip_${fav.id}")
                            ) {
                                Text(
                                    text = "${fav.name.replace("Piazza", "P.za").take(16)} (${fav.baselineAvailability}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Toggle & Live Status Banner for Roman ZTL Layer (Public Data Source)
            val activeZonesCount = ztlLiveFeed?.totalActiveZones ?: ztlZones.count { it.isCurrentlyActive() }
            val activeGatesCount = ztlLiveFeed?.totalActiveGates ?: 0
            val isRestrictedNow = activeZonesCount > 0

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (favoriteAreas.isNotEmpty()) 94.dp else 52.dp, start = 12.dp, end = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Prominent Toggle Pill for Roman ZTL Layer
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isZtlLayerVisible) {
                        if (isRestrictedNow) Color(0xFF2D1214).copy(alpha = 0.95f) else Color(0xFF102816).copy(alpha = 0.95f)
                    } else {
                        DarkSurfaceVariant.copy(alpha = 0.92f)
                    },
                    border = BorderStroke(
                        1.2.dp,
                        if (!isZtlLayerVisible) Color(0xFF3E4856)
                        else if (isRestrictedNow) Color(0xFFE53935)
                        else Color(0xFF43A047)
                    ),
                    onClick = onToggleZtlLayer,
                    modifier = Modifier.testTag("btn_toggle_ztl_overlay")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "ZTL Layer Toggle",
                            tint = if (!isZtlLayerVisible) Color.LightGray else if (isRestrictedNow) Color(0xFFFF5252) else Color(0xFF66BB6A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!isZtlLayerVisible) {
                                "ZTL Roma: Nascosta (Tocca per attivare)"
                            } else if (isRestrictedNow) {
                                "ZTL Roma: ATTIVA ($activeZonesCount zone • $activeGatesCount varchi)"
                            } else {
                                "ZTL Roma: LIBERA (Transito aperto)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isZtlLayerVisible) Color.LightGray else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (!isZtlLayerVisible) Color.Gray
                                    else if (isRestrictedNow) Color(0xFFFF1744)
                                    else Color(0xFF00E676),
                                    CircleShape
                                )
                        )
                    }
                }

                // Interactive Live Status Banner when ZTL layer is visible
                AnimatedVisibility(
                    visible = isZtlLayerVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF13171F).copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, Color(0xFF2C3440)),
                        modifier = Modifier.testTag("ztl_public_data_status_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = RomanGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Fonte Ufficiale: Roma Servizi per la Mobilità",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = ztlLiveFeed?.let { "Sync ${it.lastSyncFormatted} • ${if (isRestrictedNow) "Telecamere Presidiate" else "Varchi Aperti"}" }
                                        ?: "Open Data Roma Capitale • Live",
                                    fontSize = 9.sp,
                                    color = RomanGold
                                )
                            }
                            IconButton(
                                onClick = onRefreshZtlPublicData,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("btn_refresh_ztl_public_data")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Aggiorna Dati Pubblici ZTL",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Sleek Floating Map Controls (ZTL, Satellite, Termini, GPS, Engine)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, Color(0xFF333D4B)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = if (favoriteAreas.isNotEmpty()) 98.dp else 56.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Open ZTL Visibility Bottom Sheet
                    IconButton(
                        onClick = onOpenZtlVisibilitySheet,
                        modifier = Modifier.size(38.dp).testTag("btn_open_ztl_visibility_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Gestione Visibilità Zone ZTL",
                            tint = if (isZtlLayerVisible) RomanGold else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // ZTL Layer toggle
                    IconButton(
                        onClick = onToggleZtlLayer,
                        modifier = Modifier.size(38.dp).testTag("toggle_ztl_layer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Layer ZTL",
                            tint = if (isZtlLayerVisible) Color(0xFFFF5252) else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Dynamic Real-Time Heatmap Overlay toggle
                    IconButton(
                        onClick = { isHeatmapVisible = !isHeatmapVisible },
                        modifier = Modifier.size(38.dp).testTag("toggle_heatmap_overlay_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Mappa Termica Densità Sosta",
                            tint = if (isHeatmapVisible) Color(0xFFFF5722) else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Radar Density Overlay toggle
                    IconButton(
                        onClick = { isRadarDensityOverlayVisible = !isRadarDensityOverlayVisible },
                        modifier = Modifier.size(38.dp).testTag("toggle_radar_density_overlay_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Overlay Radar Densità",
                            tint = if (isRadarDensityOverlayVisible) RomanGold else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Satellite toggle
                    IconButton(
                        onClick = { isSatellite = !isSatellite },
                        modifier = Modifier.size(38.dp).testTag("toggle_satellite_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Mappa Satellite",
                            tint = if (isSatellite) RomanGold else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Quick Find Parking
                    IconButton(
                        onClick = onQuickFindParking,
                        modifier = Modifier.size(38.dp).testTag("quick_find_parking_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = "Trova Parcheggio",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Dov'è la mia auto? (Saved Car)
                    IconButton(
                        onClick = onOpenSavedCar,
                        modifier = Modifier.size(38.dp).testTag("open_saved_car_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Dov'è la mia auto?",
                            tint = if (savedCarLocation != null) TerracottaPrimary else Color.LightGray,
                            modifier = Modifier.size(19.dp)
                        )
                    }


                    // GPS location
                    IconButton(
                        onClick = {
                            if (hasLocationPermission) {
                                userLocation?.let { loc ->
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(loc, 16.5f),
                                            500
                                        )
                                    }
                                }
                            } else {
                                onRequestLocationPermission()
                            }
                        },
                        modifier = Modifier.size(38.dp).testTag("gps_my_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "La mia posizione",
                            tint = if (hasLocationPermission) RomanGold else Color.Gray,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

        // Top Left: Heatmap Density Status & Micro-Area Legend
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isHeatmapVisible) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1B1712).copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.7f)),
                    onClick = {
                        currentHeatmapMetric = if (currentHeatmapMetric == HeatmapMetric.SATURATION_DENSITY) {
                            HeatmapMetric.VACANCY_AVAILABILITY
                        } else {
                            HeatmapMetric.SATURATION_DENSITY
                        }
                    },
                    modifier = Modifier.testTag("heatmap_density_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentHeatmapMetric == HeatmapMetric.SATURATION_DENSITY) "Heatmap Sosta: Saturazione" else "Heatmap Sosta: Disponibilità",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFFFF3D00), CircleShape)
                                )
                            }
                            Text(
                                text = "Tocca per alternare metrica (Live)",
                                color = Color.LightGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            if (isRadarDensityOverlayVisible) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF141920).copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("radar_density_active_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Radar Densità: ${microAreas.size} cluster Termini",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF16191D).copy(alpha = 0.90f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(AvailGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Micro-aree (30-75m)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    if (isZtlLayerVisible) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(Color(0xFFD32F2F), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ZTL", color = Color(0xFFFF8A80), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }



        // Bottom Selected Area Floating Details Card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Active departures announcement banner if present
            if (activeSignals.isNotEmpty()) {
                val topSignal = activeSignals.first()
                val remainingSec = topSignal.remainingSeconds(System.currentTimeMillis())
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaPrimary),
                    onClick = { onSignalClick(topSignal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("maps_active_signal_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "POSTO IN LIBERAZIONE (${remainingSec / 60}m ${remainingSec % 60}s)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${topSignal.microAreaName} • Affidabilità ${topSignal.userReliabilityPercent}%",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Verifica",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Selected Micro-Area card with Dual Actions
            selectedArea?.let { area ->
                val availColor = when (area.availabilityLevel) {
                    AvailabilityLevel.HIGH -> AvailGreen
                    AvailabilityLevel.MEDIUM -> AvailAmber
                    AvailabilityLevel.LOW -> AvailRed
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F24)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("maps_selected_area_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = area.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${area.streetName} • Micro-area ~${area.radiusMeters}m",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = availColor
                                ) {
                                    Text(
                                        text = "${area.baselineAvailability}% Libero",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onToggleFavorite(area.id) },
                                    modifier = Modifier.size(28.dp).testTag("btn_toggle_favorite_maps")
                                ) {
                                    Icon(
                                        imageVector = if (favoriteIds.contains(area.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Salva tra i preferiti",
                                        tint = if (favoriteIds.contains(area.id)) TerracottaPrimary else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onSelectArea(null) },
                                    modifier = Modifier.size(28.dp).testTag("close_selected_area_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Chiudi scheda",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Features & tariff pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BlueLineZone.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = area.blueLineTariff,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64B5F6),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            if (area.whiteLineSpots > 0) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AvailGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${area.whiteLineSpots} bianche gratis",
                                        fontSize = 11.sp,
                                        color = AvailGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (area.hasEvCharging) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = RomanGold.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricCar,
                                            contentDescription = null,
                                            tint = RomanGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("EV Hub", fontSize = 11.sp, color = RomanGold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-Time Google Traffic ETA Card
                        selectedAreaTrafficEta?.let { eta ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF13171D),
                                border = BorderStroke(1.dp, Color(0xFF2E3846)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .testTag("selected_area_traffic_eta_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val etaColor = when (eta.condition) {
                                            com.example.data.model.TrafficCondition.SMOOTH -> AvailGreen
                                            com.example.data.model.TrafficCondition.MODERATE -> AvailAmber
                                            com.example.data.model.TrafficCondition.HEAVY -> AvailRed
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = etaColor.copy(alpha = 0.2f),
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Speed,
                                                    contentDescription = null,
                                                    tint = etaColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "ETA ${eta.trafficDurationMinutes} min",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• ${eta.distanceFormatted}",
                                                    fontSize = 11.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                            Text(
                                                text = "Arrivo alle ${eta.estimatedArrivalTimeFormatted} (${eta.routeSummary})",
                                                fontSize = 10.sp,
                                                color = etaColor
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1E2633)
                                    ) {
                                        Text(
                                            text = "Traffico Live",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RomanGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Row 1: Primary Navigation: 'Navigate' (Google Maps Intent) & Smart Navigator (Anti-ZTL)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    NavigationHelper.launchGoogleMaps(
                                        context = context,
                                        latitude = area.latitude,
                                        longitude = area.longitude,
                                        destinationName = "${area.name} (${area.streetName})"
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(40.dp)
                                    .testTag("navigate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = "Navigate with Google Maps",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Navigate",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { onStartNavigation(area) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(40.dp)
                                    .testTag("maps_start_smart_nav_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Naviga con Smart Navigator",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Naviga (Anti-ZTL)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Row 2: Secondary compact actions (ML Trend, Sosta, Liberando)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenMlTrends(area) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = RomanGold.copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("maps_open_ml_trends_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Trend ML",
                                    color = RomanGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { onRegisterParkedStay(area) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("maps_register_stay_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Parcheggiato",
                                    color = RomanGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { onReportDeparture(area) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("maps_report_departure_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Sto liberando",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // ZTL Zone Info Modal Bottom Sheet if a ZTL zone or gate was tapped
        selectedZtlZone?.let { zone ->
            val liveZone = ztlLiveFeed?.zones?.find { it.zoneId == zone.id }
            val isZoneActive = liveZone?.isActive ?: zone.isCurrentlyActive()
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { onSelectZtlZone(null) },
                sheetState = sheetState,
                containerColor = Color(0xFF1E2228),
                modifier = Modifier.testTag("ztl_zone_details_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isZoneActive) Color(0xFFFF5252) else Color(0xFF4CAF50),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = liveZone?.name ?: zone.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = liveZone?.publicDataSource ?: "Roma Servizi per la Mobilità",
                                    fontSize = 10.sp,
                                    color = RomanGold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isZoneActive) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        ) {
                            Text(
                                text = if (isZoneActive) "ATTIVA ORA" else "LIBERO ACCESSO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = liveZone?.description ?: zone.description,
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Next Status Countdown Card
                    liveZone?.let { lz ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (lz.isActive) Color(0xFF2E1515) else Color(0xFF122818),
                            border = BorderStroke(1.dp, if (lz.isActive) Color(0xFFE53935) else Color(0xFF43A047)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (lz.isActive) Color(0xFFFF5252) else Color(0xFF81C784),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = lz.operationalStatus.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = lz.nextChangeText,
                                        fontSize = 11.sp,
                                        color = if (lz.isActive) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2E35))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Orari Ufficiali di Attivazione:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RomanGold)
                            Text(liveZone?.activeScheduleText ?: "${zone.dayActiveSchedule} • ${zone.nightActiveSchedule}", fontSize = 12.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = liveZone?.fineNotice ?: zone.fineInfo,
                                fontSize = 11.sp,
                                color = Color(0xFFFF8A80),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live electronic gates with status indicators
                    val gatesList = liveZone?.gates ?: zone.gates.map {
                        ZtlGateLiveIndicator(
                            gateId = it.name,
                            name = it.name,
                            street = it.street,
                            latitude = it.latitude,
                            longitude = it.longitude,
                            isCameraActive = isZoneActive,
                            isGateActive = isZoneActive,
                            status = if (isZoneActive) GateOperationalStatus.ACTIVE_RESTRICTED else GateOperationalStatus.INACTIVE_FREE,
                            activeHours = it.activeHours,
                            statusText = if (isZoneActive) "Telecamera Attiva" else "Libero Accesso"
                        )
                    }

                    if (gatesList.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Varchi Elettronici (${gatesList.size}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${gatesList.count { it.isGateActive }} Attivi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isZoneActive) Color(0xFFFF5252) else Color(0xFF81C784)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        gatesList.forEach { gate ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2A2E35),
                                border = BorderStroke(0.5.dp, if (gate.isGateActive) Color(0x66FF5252) else Color(0x4443A047)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    if (gate.isGateActive) Color(0xFFE53935) else Color(0xFF43A047),
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(gate.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${gate.street} • Orari: ${gate.activeHours}", fontSize = 10.sp, color = Color.LightGray)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (gate.isGateActive) Color(0x40E53935) else Color(0x3043A047)
                                    ) {
                                        Text(
                                            text = if (gate.isGateActive) "🔴 TELECAMERA ON" else "🟢 LIBERO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (gate.isGateActive) Color(0xFFFF8A80) else Color(0xFFA5D6A7),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onSelectZtlZone(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Chiudi", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
