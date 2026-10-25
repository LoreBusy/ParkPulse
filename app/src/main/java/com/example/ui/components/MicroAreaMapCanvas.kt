package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.util.NavigationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.ZtlRestricted

@Composable
fun MicroAreaMapCanvas(
    microAreas: List<MicroArea>,
    activeSignals: List<DepartureSignal>,
    selectedArea: MicroArea?,
    onSelectArea: (MicroArea) -> Unit,
    onSignalClick: (DepartureSignal) -> Unit,
    onReportDeparture: (MicroArea) -> Unit = {},
    onRegisterParkedStay: (MicroArea) -> Unit = {},
    onToggleMapEngine: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13171C))
            .testTag("map_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(microAreas) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        val hit = microAreas.minByOrNull { area ->
                            val cx = area.normX * width
                            val cy = area.normY * height
                            val dx = tapOffset.x - cx
                            val dy = tapOffset.y - cy
                            dx * dx + dy * dy
                        }

                        if (hit != null) {
                            val cx = hit.normX * width
                            val cy = hit.normY * height
                            val distSq = (tapOffset.x - cx) * (tapOffset.x - cx) + (tapOffset.y - cy) * (tapOffset.y - cy)
                            if (distSq < (60.dp.toPx() * 60.dp.toPx())) {
                                onSelectArea(hit)
                            }
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val roadColor = Color(0xFF222831)
            val strokeWidthRoad = 16.dp.toPx()
            val strokeWidthMinor = 8.dp.toPx()

            // Via Giolitti
            drawLine(
                color = roadColor,
                start = Offset(w * 0.10f, h * 0.22f),
                end = Offset(w * 0.85f, h * 0.38f),
                strokeWidth = strokeWidthRoad
            )

            // Via Marsala
            drawLine(
                color = roadColor,
                start = Offset(w * 0.25f, h * 0.08f),
                end = Offset(w * 0.90f, h * 0.20f),
                strokeWidth = strokeWidthRoad
            )

            // Via Cavour
            drawLine(
                color = roadColor,
                start = Offset(w * 0.08f, h * 0.35f),
                end = Offset(w * 0.40f, h * 0.65f),
                strokeWidth = strokeWidthRoad
            )

            // Via Merulana
            drawLine(
                color = roadColor,
                start = Offset(w * 0.38f, h * 0.65f),
                end = Offset(w * 0.60f, h * 0.95f),
                strokeWidth = strokeWidthRoad
            )

            // Viale Manzoni
            drawLine(
                color = roadColor,
                start = Offset(w * 0.55f, h * 0.95f),
                end = Offset(w * 0.95f, h * 0.70f),
                strokeWidth = strokeWidthRoad
            )

            // Via Principe Amedeo & Turati
            drawLine(
                color = roadColor,
                start = Offset(w * 0.25f, h * 0.30f),
                end = Offset(w * 0.70f, h * 0.50f),
                strokeWidth = strokeWidthMinor
            )
            drawLine(
                color = roadColor,
                start = Offset(w * 0.30f, h * 0.38f),
                end = Offset(w * 0.75f, h * 0.60f),
                strokeWidth = strokeWidthMinor
            )

            // Termini Building block
            drawRoundRect(
                color = Color(0xFF2C323C),
                topLeft = Offset(w * 0.35f, h * 0.05f),
                size = Size(w * 0.48f, h * 0.16f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            for (i in 0..5) {
                val yTrack = h * (0.07f + i * 0.022f)
                drawLine(
                    color = Color(0xFF404855),
                    start = Offset(w * 0.45f, yTrack),
                    end = Offset(w * 0.81f, yTrack),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Piazza Vittorio
            drawRoundRect(
                color = Color(0xFF1B2E24),
                topLeft = Offset(w * 0.50f, h * 0.48f),
                size = Size(w * 0.28f, h * 0.24f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF2E7D32).copy(alpha = 0.5f),
                topLeft = Offset(w * 0.50f, h * 0.48f),
                size = Size(w * 0.28f, h * 0.24f),
                cornerRadius = CornerRadius(12.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // ZTL boundary
            val ztlPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
            drawLine(
                color = ZtlRestricted.copy(alpha = 0.75f),
                start = Offset(w * 0.15f, h * 0.25f),
                end = Offset(w * 0.22f, h * 0.60f),
                strokeWidth = 4.dp.toPx(),
                pathEffect = ztlPathEffect
            )

            // Micro-Areas (30-75m anonymous circles)
            microAreas.forEach { area ->
                val cx = area.normX * w
                val cy = area.normY * h
                val baseRadiusPx = (area.radiusMeters * 0.7f).dp.toPx()

                val baseColor = when (area.availabilityLevel) {
                    AvailabilityLevel.HIGH -> AvailGreen
                    AvailabilityLevel.MEDIUM -> AvailAmber
                    AvailabilityLevel.LOW -> AvailRed
                }

                val isSelected = selectedArea?.id == area.id
                val hasSignal = area.activeSignalsCount > 0

                if (hasSignal) {
                    drawCircle(
                        color = baseColor.copy(alpha = 0.25f),
                        radius = baseRadiusPx * pulseScale,
                        center = Offset(cx, cy)
                    )
                }

                drawCircle(
                    color = baseColor.copy(alpha = if (isSelected) 0.38f else 0.20f),
                    radius = baseRadiusPx,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = if (isSelected) Color.White else baseColor,
                    radius = baseRadiusPx,
                    center = Offset(cx, cy),
                    style = Stroke(width = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx())
                )

                drawCircle(
                    color = if (isSelected) Color.White else baseColor,
                    radius = 5.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }

        // Overlay: Termini Label
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF2C323C).copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "ROMA TERMINI",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Overlay: ZTL Warning Banner
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ZtlRestricted.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "ZTL",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CONFINE ZTL CENTRO",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay: Top Right Button to Switch to Google Maps
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceVariant.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold),
            onClick = onToggleMapEngine,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 14.dp, end = 12.dp)
                .testTag("switch_to_google_maps_btn")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = RomanGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Google Maps SDK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Map Floating Legend and Selected Area Quick Card
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Active Signal Alerts Bar if any
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
                        .testTag("active_signal_banner")
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
                                    text = "POSTO IN PARTENZA (${remainingSec / 60}m ${remainingSec % 60}s)",
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
                                text = "Feedback",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Legend card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.95f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(AvailGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alta (>60%)", color = Color.White, fontSize = 10.sp)

                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(AvailAmber, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Media (30-60%)", color = Color.White, fontSize = 10.sp)

                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(AvailRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bassa (<30%)", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    if (selectedArea != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedArea.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${selectedArea.streetName} • Micro-area ~${selectedArea.radiusMeters}m • ${selectedArea.blueLineTariff}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (selectedArea.availabilityLevel) {
                                    AvailabilityLevel.HIGH -> AvailGreen
                                    AvailabilityLevel.MEDIUM -> AvailAmber
                                    AvailabilityLevel.LOW -> AvailRed
                                }
                            ) {
                                Text(
                                    text = "${selectedArea.baselineAvailability}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Navigate button with Google Maps intent
                        Button(
                            onClick = {
                                NavigationHelper.launchGoogleMaps(
                                    context = context,
                                    latitude = selectedArea.latitude,
                                    longitude = selectedArea.longitude,
                                    destinationName = "${selectedArea.name} (${selectedArea.streetName})"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("navigate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Navigate with Google Maps",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Navigate",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dual Action Buttons on Canvas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRegisterParkedStay(selectedArea) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ho parcheggiato", color = RomanGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onReportDeparture(selectedArea) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sto liberando", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
