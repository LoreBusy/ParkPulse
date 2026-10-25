package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.NavigationHelper
import kotlin.math.roundToInt

/**
 * Density Radar view using Recharts visualization style to display a heatmap of current
 * parking availability intensity around Roma Termini, helping users visualize high-congestion
 * zones at a glance and quickly launch navigation.
 */
@Composable
fun RechartsDensityRadarView(
    microAreas: List<MicroArea>,
    activeSignals: List<DepartureSignal>,
    onSelectArea: (MicroArea) -> Unit,
    onStartNavigation: (MicroArea) -> Unit,
    onOpenMlTrends: (MicroArea) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAreaState by remember { mutableStateOf<MicroArea?>(microAreas.firstOrNull()) }
    val activeArea = selectedAreaState ?: microAreas.firstOrNull()

    // Calculate aggregated metrics
    val avgAvailability = if (microAreas.isNotEmpty()) {
        microAreas.map { it.baselineAvailability }.average().roundToInt()
    } else 48
    val highCongestionCount = microAreas.count { it.availabilityLevel == AvailabilityLevel.LOW }
    val bestArea = remember(microAreas) {
        microAreas.maxByOrNull { it.baselineAvailability }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
            .testTag("recharts_density_radar_view")
    ) {
        // Density Radar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RomanGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, RomanGold),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "RADAR DENSITÀ & HEATMAP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Intensità disponibilità e saturazione Termini (Recharts)",
                        fontSize = 11.sp,
                        color = RomanGold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF202632)
            ) {
                Text(
                    text = "${microAreas.size} Zone",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Key Summary Metrics Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Disponibilità Media", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "$avgAvailability%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            avgAvailability >= 60 -> AvailGreen
                            avgAvailability >= 35 -> AvailAmber
                            else -> AvailRed
                        }
                    )
                    Text("Asse FS Termini", fontSize = 9.sp, color = Color.LightGray)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Zone ad Alta Saturazione", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "$highCongestionCount / ${microAreas.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (highCongestionCount > 3) AvailRed else AvailAmber
                    )
                    Text("Attesa > 10 min", fontSize = 9.sp, color = Color.LightGray)
                }

                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Zona Più Scorrevole", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = bestArea?.name ?: "P.zza Vittorio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AvailGreen,
                        maxLines = 1
                    )
                    Text(
                        text = "${bestArea?.baselineAvailability ?: 70}% stalli liberi",
                        fontSize = 9.sp,
                        color = RomanGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recharts Heatmap Intensity Legend
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            modifier = Modifier.fillMaxWidth().testTag("recharts_heatmap_legend")
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scala di Intensità di Saturazione:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Stile Recharts Heatmap",
                        fontSize = 9.sp,
                        color = RomanGold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Continuous gradient bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    AvailGreen,
                                    AvailAmber,
                                    Color(0xFFFF7043),
                                    AvailRed
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0% (Libero/Scorrevole)", fontSize = 8.sp, color = AvailGreen)
                    Text("50% (Medio)", fontSize = 8.sp, color = AvailAmber)
                    Text("80% Soglia Critica", fontSize = 8.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    Text("100% (Saturo)", fontSize = 8.sp, color = AvailRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recharts-style Congestion Bar Chart
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141920)),
            modifier = Modifier.fillMaxWidth().testTag("recharts_congestion_bar_chart")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confronto Congestione Zone (Recharts Bar)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tocca per selezionare",
                        fontSize = 9.sp,
                        color = RomanGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Comparative Horizontal Bar Rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    microAreas.forEach { area ->
                        val isSelected = activeArea?.id == area.id
                        val congestionPercent = (100 - area.baselineAvailability).coerceIn(0, 100)
                        val barFraction = (congestionPercent / 100f).coerceIn(0.05f, 1f)
                        val barColor = when {
                            congestionPercent >= 75 -> AvailRed
                            congestionPercent >= 45 -> AvailAmber
                            else -> AvailGreen
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAreaState = area
                                    onSelectArea(area)
                                }
                                .padding(vertical = 2.dp)
                                .testTag("heatmap_zone_bar_${area.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = area.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        maxLines = 1
                                    )
                                    if (activeSignals.any { it.microAreaId == area.id }) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color(0xFF00E5FF), CircleShape)
                                        )
                                    }
                                }
                                Text(
                                    text = "$congestionPercent% Saturo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Horizontal Bar Track and Fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(Color(0x18FFFFFF), RoundedCornerShape(5.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(barFraction)
                                        .fillMaxHeight()
                                        .background(barColor, RoundedCornerShape(5.dp))
                                        .then(
                                            if (isSelected) Modifier.border(1.dp, Color.White, RoundedCornerShape(5.dp))
                                            else Modifier
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Zone Interactive Card with Navigate & Details
        activeArea?.let { area ->
            val totalSpots = (area.radiusMeters * 0.8).roundToInt().coerceIn(25, 60)
            val occupiedSpots = (totalSpots * (100 - area.baselineAvailability) / 100.0).roundToInt().coerceIn(0, totalSpots)
            val freeSpots = (totalSpots - occupiedSpots).coerceAtLeast(0)
            val hasSignal = activeSignals.any { it.microAreaId == area.id }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E)),
                border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharts_radar_selected_area_card")
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${area.streetName} • Micro-area ~${area.radiusMeters}m",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (area.availabilityLevel) {
                                AvailabilityLevel.HIGH -> AvailGreen
                                AvailabilityLevel.MEDIUM -> AvailAmber
                                AvailabilityLevel.LOW -> AvailRed
                            }
                        ) {
                            Text(
                                text = "${area.baselineAvailability}% Libero",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stalli Stimati & Tariffa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF14181E),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("Posti Liberi Stimati", fontSize = 9.sp, color = Color.Gray)
                                Text("~$freeSpots su $totalSpots", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AvailGreen)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF14181E),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text("Regime Tariffario", fontSize = 9.sp, color = Color.Gray)
                                Text(area.blueLineTariff, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6), maxLines = 1)
                            }
                        }

                        if (hasSignal) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x2500E5FF),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text("Segnalazione Live", fontSize = 9.sp, color = Color(0xFF00E5FF))
                                    Text("Posto in Uscita!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dual Actions: Navigate (Google Maps intent) & ML Trends
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Navigate Button (triggers Google Maps intent with coordinates)
                        Button(
                            onClick = {
                                NavigationHelper.launchGoogleMaps(
                                    context = context,
                                    latitude = area.latitude,
                                    longitude = area.longitude,
                                    destinationName = "${area.name} (${area.streetName})"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("navigate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Naviga con Google Maps",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Naviga (Google Maps)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // ML Predictions Sheet
                        OutlinedButton(
                            onClick = { onOpenMlTrends(area) },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RomanGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("radar_open_ml_trends_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = RomanGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trend ML",
                                fontSize = 11.sp,
                                color = RomanGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }
}
