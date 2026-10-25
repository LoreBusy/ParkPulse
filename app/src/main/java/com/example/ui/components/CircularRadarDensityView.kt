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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailabilityLevel
import com.example.data.model.DepartureSignal
import com.example.data.model.MicroArea
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class RadarRangeOption(val label: String, val maxMeters: Float) {
    CLOSE("350m", 350f),
    MEDIUM("600m", 600f),
    WIDE("1000m", 1000f)
}

data class ProjectedParkingCluster(
    val area: MicroArea,
    val distanceMeters: Int,
    val bearingDegrees: Double,
    val screenX: Float,
    val screenY: Float,
    val activeSignalsCount: Int
)

@Composable
fun CircularRadarDensityView(
    userLocation: LatLng?,
    microAreas: List<MicroArea>,
    activeSignals: List<DepartureSignal>,
    onSelectArea: (MicroArea) -> Unit,
    onStartNavigation: (MicroArea) -> Unit,
    onOpenMlTrends: (MicroArea) -> Unit,
    modifier: Modifier = Modifier
) {
    // Rome Termini Central Reference if user GPS is not yet acquired
    val defaultCenter = remember { LatLng(41.8992, 12.5028) }
    val effectiveCenter = userLocation ?: defaultCenter

    var selectedRange by remember { mutableStateOf(RadarRangeOption.MEDIUM) }
    var selectedCluster by remember { mutableStateOf<ProjectedParkingCluster?>(null) }
    var isSweepEnabled by remember { mutableStateOf(true) }

    // Radar beam infinite rotation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    // Pulsing beacon animation for user center
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Pre-calculate distance and bearing for each micro-area relative to effective center
    val projectedClusters = remember(effectiveCenter, microAreas, selectedRange, activeSignals) {
        val list = mutableListOf<ProjectedParkingCluster>()
        microAreas.forEach { area ->
            val (dist, bearing) = calculateDistanceAndBearing(
                lat1 = effectiveCenter.latitude,
                lon1 = effectiveCenter.longitude,
                lat2 = area.latitude,
                lon2 = area.longitude
            )
            val signalCount = activeSignals.count { it.microAreaId == area.id }
            if (dist <= selectedRange.maxMeters * 1.05f) {
                list.add(
                    ProjectedParkingCluster(
                        area = area,
                        distanceMeters = dist.toInt(),
                        bearingDegrees = bearing,
                        screenX = 0f,
                        screenY = 0f,
                        activeSignalsCount = signalCount
                    )
                )
            }
        }
        list.sortedBy { it.distanceMeters }
    }

    val highAvailCount = projectedClusters.count { it.area.availabilityLevel == AvailabilityLevel.HIGH }
    val avgAvail = if (projectedClusters.isNotEmpty()) {
        projectedClusters.map { it.area.baselineAvailability }.average().toInt()
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F14))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("circular_radar_density_view")
    ) {
        // Top Control Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TerracottaPrimary.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, TerracottaPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = RomanGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "RADAR DENSITÀ TERMINI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                    Text(
                        text = if (userLocation != null) "Centrato sulla tua posizione GPS" else "Centrato su Piazza dei Cinquecento",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // Range Selector Chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                RadarRangeOption.entries.forEach { option ->
                    val isSelected = selectedRange == option
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) TerracottaPrimary else Color(0xFF1E242E),
                        border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFF2C3542)),
                        onClick = {
                            selectedRange = option
                            selectedCluster = null
                        },
                        modifier = Modifier.testTag("radar_range_${option.label}")
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.LightGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Mini KPI Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF151A22), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF242C38), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(AvailGreen, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${projectedClusters.size} cluster nel raggio",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Disponibilità media: $avgAvail%",
                fontSize = 11.sp,
                color = if (avgAvail >= 60) AvailGreen else if (avgAvail >= 35) AvailAmber else AvailRed,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isSweepEnabled) RomanGold.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
                onClick = { isSweepEnabled = !isSweepEnabled }
            ) {
                Text(
                    text = if (isSweepEnabled) "Beam ON" else "Beam OFF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSweepEnabled) RomanGold else Color.Gray,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Circular Radar Display (Full custom Jetpack Compose Canvas)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF090C10), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1E2632), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Computed cluster screen positions for tap detection
            var clusterHitTargets by remember { mutableStateOf<List<ProjectedParkingCluster>>(emptyList()) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .pointerInput(projectedClusters, selectedRange) {
                        detectTapGestures { tapOffset ->
                            var closest: ProjectedParkingCluster? = null
                            var minDist = Float.MAX_VALUE
                            clusterHitTargets.forEach { cluster ->
                                val dx = tapOffset.x - cluster.screenX
                                val dy = tapOffset.y - cluster.screenY
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist < minDist && dist <= 28.dp.toPx()) {
                                    minDist = dist
                                    closest = cluster
                                }
                            }
                            if (closest != null) {
                                selectedCluster = closest
                                onSelectArea(closest!!.area)
                            } else {
                                selectedCluster = null
                            }
                        }
                    }
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val center = Offset(canvasW / 2f, canvasH / 2f)
                val maxRadarRadius = (minOf(canvasW, canvasH) / 2f) * 0.88f
                val maxMeters = selectedRange.maxMeters

                // 1. Radar Circular Grid & Concentric Rings
                val rings = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                val ringStroke = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                val solidRingStroke = Stroke(width = 1.5.dp.toPx())

                rings.forEachIndexed { index, fraction ->
                    val r = maxRadarRadius * fraction
                    val isOuter = index == rings.lastIndex
                    drawCircle(
                        color = if (isOuter) RomanGold.copy(alpha = 0.5f) else Color(0xFF1B2A38),
                        radius = r,
                        center = center,
                        style = if (isOuter) solidRingStroke else ringStroke
                    )
                }

                // 2. Compass Crosshairs & Diagonal Axes
                val axisColor = Color(0xFF18222D)
                drawLine(axisColor, Offset(center.x - maxRadarRadius, center.y), Offset(center.x + maxRadarRadius, center.y), strokeWidth = 1.dp.toPx())
                drawLine(axisColor, Offset(center.x, center.y - maxRadarRadius), Offset(center.x, center.y + maxRadarRadius), strokeWidth = 1.dp.toPx())

                // 3. Animated Radar Sweep Beam (Rotating Gradient)
                if (isSweepEnabled) {
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())
                    val sweepEnd = Offset(
                        x = (center.x + maxRadarRadius * cos(sweepRad)).toFloat(),
                        y = (center.y + maxRadarRadius * sin(sweepRad)).toFloat()
                    )

                    // Draw glowing sweep line
                    drawLine(
                        color = RomanGold.copy(alpha = 0.7f),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 2.dp.toPx()
                    )

                    // Draw soft sector glow behind the beam
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                RomanGold.copy(alpha = 0.08f),
                                RomanGold.copy(alpha = 0.22f)
                            ),
                            center = center
                        ),
                        startAngle = sweepAngle - 35f,
                        sweepAngle = 35f,
                        useCenter = true,
                        topLeft = Offset(center.x - maxRadarRadius, center.y - maxRadarRadius),
                        size = androidx.compose.ui.geometry.Size(maxRadarRadius * 2f, maxRadarRadius * 2f)
                    )
                }

                // 4. Center User Beacon with Pulsing Glow
                drawCircle(
                    color = TerracottaPrimary.copy(alpha = pulseAlpha),
                    radius = 16.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = TerracottaPrimary,
                    radius = 4.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Project and Draw Parking Availability Clusters
                val targets = mutableListOf<ProjectedParkingCluster>()
                projectedClusters.forEach { cluster ->
                    val distFraction = (cluster.distanceMeters / maxMeters).coerceIn(0.04f, 1.0f)
                    val rPx = maxRadarRadius * distFraction
                    val angleRad = Math.toRadians(cluster.bearingDegrees - 90.0)
                    val cx = (center.x + rPx * cos(angleRad)).toFloat()
                    val cy = (center.y + rPx * sin(angleRad)).toFloat()

                    val isSelected = selectedCluster?.area?.id == cluster.area.id
                    val nodeColor = when (cluster.area.availabilityLevel) {
                        AvailabilityLevel.HIGH -> AvailGreen
                        AvailabilityLevel.MEDIUM -> AvailAmber
                        AvailabilityLevel.LOW -> AvailRed
                    }

                    // Outer density halo
                    val haloRadius = if (isSelected) 18.dp.toPx() else (10.dp.toPx() + (cluster.area.baselineAvailability / 12f).dp.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                nodeColor.copy(alpha = if (isSelected) 0.6f else 0.35f),
                                nodeColor.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = haloRadius
                        ),
                        radius = haloRadius,
                        center = Offset(cx, cy)
                    )

                    // Core Cluster Node
                    val coreRadius = if (isSelected) 7.dp.toPx() else 5.dp.toPx()
                    drawCircle(
                        color = nodeColor,
                        radius = coreRadius,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = coreRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.2.dp.toPx())
                    )

                    // Active Departure Signal indicator badge (community departure happening now)
                    if (cluster.activeSignalsCount > 0) {
                        drawCircle(
                            color = TerracottaPrimary,
                            radius = 3.dp.toPx(),
                            center = Offset(cx + 6.dp.toPx(), cy - 6.dp.toPx())
                        )
                    }

                    targets.add(
                        cluster.copy(screenX = cx, screenY = cy)
                    )
                }
                clusterHitTargets = targets
            }

            // Cardinal direction labels (North, South, East, West)
            Text("N", color = RomanGold, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp))
            Text("S", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp))
            Text("E", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp))
            Text("O", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterStart).padding(start = 10.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Cluster Detail Bottom Card (Clean, Non-Overlapping & Actionable)
        AnimatedVisibility(
            visible = selectedCluster != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedCluster?.let { cluster ->
                val area = cluster.area
                val availColor = when (area.availabilityLevel) {
                    AvailabilityLevel.HIGH -> AvailGreen
                    AvailabilityLevel.MEDIUM -> AvailAmber
                    AvailabilityLevel.LOW -> AvailRed
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171B22)),
                    border = BorderStroke(1.dp, Color(0xFF2C3646)),
                    modifier = Modifier.fillMaxWidth().testTag("radar_selected_cluster_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = area.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = availColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${area.baselineAvailability}% Libero",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = availColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${area.streetName} • a ${cluster.distanceMeters}m (~${(cluster.distanceMeters / 75).coerceAtLeast(1)} min a piedi)",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            IconButton(
                                onClick = { selectedCluster = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Chiudi",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Features & tariff chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1A2A3A)) {
                                Text(area.blueLineTariff, fontSize = 10.sp, color = Color(0xFF64B5F6), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            if (area.whiteLineSpots > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = AvailGreen.copy(alpha = 0.15f)) {
                                    Text("${area.whiteLineSpots} gratis", fontSize = 10.sp, color = AvailGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (area.isZtlBoundary) {
                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFD32F2F).copy(alpha = 0.2f)) {
                                    Text("Varco ZTL", fontSize = 10.sp, color = Color(0xFFFF8A80), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (cluster.activeSignalsCount > 0) {
                                Surface(shape = RoundedCornerShape(4.dp), color = TerracottaPrimary.copy(alpha = 0.2f)) {
                                    Text("🚨 Posto in uscita", fontSize = 10.sp, color = TerracottaPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dual Action Buttons (Naviga & Trend ML)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartNavigation(area) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.3f).height(38.dp).testTag("radar_naviga_btn")
                            ) {
                                Icon(Icons.Default.NearMe, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Naviga (Anti-ZTL)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { onOpenMlTrends(area) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = RomanGold.copy(alpha = 0.12f)),
                                modifier = Modifier.weight(1.0f).height(38.dp).testTag("radar_ml_trend_btn")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RomanGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trend ML", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RomanGold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculates distance in meters and bearing in degrees (0..360) between two coordinates using Haversine & Great Circle bearing.
 */
private fun calculateDistanceAndBearing(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Pair<Double, Double> {
    val r = 6371000.0 // Earth radius in meters
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaPhi = Math.toRadians(lat2 - lat1)
    val deltaLambda = Math.toRadians(lon2 - lon1)

    val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
            cos(phi1) * cos(phi2) *
            sin(deltaLambda / 2) * sin(deltaLambda / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val distance = r * c

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    val bearing = (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0

    return Pair(distance, bearing)
}
