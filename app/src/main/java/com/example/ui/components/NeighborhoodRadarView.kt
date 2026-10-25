package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DistrictSector
import com.example.data.model.RadarSubBlockDensity
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.ZtlRestricted
import com.example.util.NavigationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

enum class HeatmapFilter {
    ALL,
    HIGH_CONGESTION,
    EASY_PARKING,
    OUTSIDE_ZTL
}

enum class TimeSimulation(val label: String, val hour: Int) {
    NOW("Ora Reale", -1),
    MORNING_PEAK("Mattina (08:30)", 8),
    AFTERNOON("Pomeriggio (14:30)", 14),
    NIGHT_ZTL("Notte ZTL (22:30)", 22)
}

@Composable
fun NeighborhoodRadarView(
    sectors: List<DistrictSector>,
    radarSubBlocks: List<RadarSubBlockDensity>,
    selectedSubBlock: RadarSubBlockDensity?,
    onSelectSubBlock: (RadarSubBlockDensity?) -> Unit,
    onSelectSector: (DistrictSector) -> Unit,
    isOfflineMode: Boolean = false,
    lastCacheSyncEpochMs: Long = System.currentTimeMillis(),
    onToggleOfflineMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(HeatmapFilter.ALL) }
    var selectedTimeSim by remember { mutableStateOf(TimeSimulation.NOW) }

    // Calculate simulation adjustments based on selected time of day
    val adjustedSubBlocks = remember(radarSubBlocks, selectedTimeSim) {
        radarSubBlocks.map { block ->
            when (selectedTimeSim) {
                TimeSimulation.NOW -> block
                TimeSimulation.MORNING_PEAK -> {
                    // Morning rush increases Termini and Turati congestion
                    val boost = if (block.id.contains("termini") || block.id.contains("turati")) 6 else -4
                    val newCongestion = (block.congestionPercent + boost).coerceIn(15, 98)
                    block.copy(
                        congestionPercent = newCongestion,
                        estimatedSearchTimeMinutes = (block.estimatedSearchTimeMinutes * 1.3f).toInt().coerceAtLeast(3)
                    )
                }
                TimeSimulation.AFTERNOON -> {
                    // Afternoon has more turnover near Piazza Vittorio
                    val boost = if (block.id.contains("vittorio")) -8 else 2
                    val newCongestion = (block.congestionPercent + boost).coerceIn(15, 95)
                    block.copy(
                        congestionPercent = newCongestion,
                        estimatedSearchTimeMinutes = (block.estimatedSearchTimeMinutes * 0.9f).toInt().coerceAtLeast(2)
                    )
                }
                TimeSimulation.NIGHT_ZTL -> {
                    // Night activates ZTL San Lorenzo and evening restaurant pressure
                    val boost = if (block.id.contains("san_lorenzo") || block.id.contains("merulana")) 10 else -12
                    val newCongestion = (block.congestionPercent + boost).coerceIn(10, 96)
                    block.copy(
                        congestionPercent = newCongestion,
                        estimatedSearchTimeMinutes = if (block.id.contains("san_lorenzo")) 25 else 4
                    )
                }
            }
        }
    }

    // Filter sub-blocks according to active filter chip
    val filteredSubBlocks = remember(adjustedSubBlocks, selectedFilter) {
        adjustedSubBlocks.filter { block ->
            when (selectedFilter) {
                HeatmapFilter.ALL -> true
                HeatmapFilter.HIGH_CONGESTION -> block.congestionPercent >= 60
                HeatmapFilter.EASY_PARKING -> block.congestionPercent < 50
                HeatmapFilter.OUTSIDE_ZTL -> !block.ztlStatusNote.contains("ZTL", ignoreCase = true) || block.ztlStatusNote.contains("Fuori", ignoreCase = true)
            }
        }
    }

    val activeSelectedBlock = selectedSubBlock?.let { sel ->
        adjustedSubBlocks.find { it.id == sel.id }
    } ?: adjustedSubBlocks.firstOrNull()

    val avgCongestion = if (adjustedSubBlocks.isNotEmpty()) {
        adjustedSubBlocks.map { it.congestionPercent }.average().toInt()
    } else 50

    val syncTimeStr = remember(lastCacheSyncEpochMs) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ITALY)
        sdf.format(Date(lastCacheSyncEpochMs))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("radar_density_screen"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card: Radar Density District Overview & Cache status
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3846)),
                modifier = Modifier.fillMaxWidth().testTag("radar_density_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(RomanGold.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompassCalibration,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RADAR DENSITÀ ESQUILINO",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mappa termica congestione Termini-Esquilino",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Room Offline Cache status pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOfflineMode) Color(0xFFD32F2F).copy(alpha = 0.2f) else Color(0xFF2E7D32).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isOfflineMode) Color(0xFFEF5350) else Color(0xFF66BB6A)
                            ),
                            modifier = Modifier.clickable { onToggleOfflineMode() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (isOfflineMode) Color(0xFFFF8A80) else Color(0xFF81C784),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOfflineMode) "Offline (Room)" else "Cache Room ($syncTimeStr)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineMode) Color(0xFFFF8A80) else Color(0xFF81C784)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // District summary KPI bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF13171C), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CONGESTIONE MEDIA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "$avgCongestion%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (avgCongestion >= 60) AvailRed else if (avgCongestion >= 40) AvailAmber else AvailGreen
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2A313C)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SOTTO-BLOCCHI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "${radarSubBlocks.size} Aree",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2A313C)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIGLIOR ZONA ORA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "P.zza Vittorio / Dante",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AvailGreen
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Heatmap Visualization Canvas
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15191E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29323F)),
                modifier = Modifier.fillMaxWidth().testTag("radar_heatmap_canvas_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MAPPA TERMICA LIVE • TERMINI - ESQUILINO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RomanGold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tocca un blocco per dettagli",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Heatmap Canvas with Radial Gradients and Sub-block Centroids
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFF0E1116), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF222933), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(adjustedSubBlocks) {
                                    detectTapGestures { tapOffset ->
                                        val w = size.width
                                        val h = size.height
                                        // Find closest sub-block to tap
                                        var closest: RadarSubBlockDensity? = null
                                        var minDist = Float.MAX_VALUE
                                        for (b in adjustedSubBlocks) {
                                            val bx = b.normX * w
                                            val by = b.normY * h
                                            val dx = tapOffset.x - bx
                                            val dy = tapOffset.y - by
                                            val dist = sqrt(dx * dx + dy * dy)
                                            if (dist < minDist && dist <= 75.dp.toPx()) {
                                                minDist = dist
                                                closest = b
                                            }
                                        }
                                        closest?.let { onSelectSubBlock(it) }
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw District Outline Roads and Guides
                            val roadColor = Color(0xFF1F2630)
                            drawLine(roadColor, Offset(w * 0.10f, h * 0.30f), Offset(w * 0.90f, h * 0.20f), strokeWidth = 5.dp.toPx())
                            drawLine(roadColor, Offset(w * 0.15f, h * 0.45f), Offset(w * 0.85f, h * 0.40f), strokeWidth = 4.dp.toPx())
                            drawLine(roadColor, Offset(w * 0.20f, h * 0.65f), Offset(w * 0.90f, h * 0.60f), strokeWidth = 4.dp.toPx())
                            drawLine(roadColor, Offset(w * 0.40f, h * 0.10f), Offset(w * 0.45f, h * 0.90f), strokeWidth = 5.dp.toPx())
                            drawLine(roadColor, Offset(w * 0.65f, h * 0.10f), Offset(w * 0.70f, h * 0.90f), strokeWidth = 4.dp.toPx())

                            // Termini Station Building representation
                            drawRoundRect(
                                color = Color(0xFF252B35),
                                topLeft = Offset(w * 0.28f, h * 0.05f),
                                size = Size(w * 0.52f, h * 0.16f),
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )

                            // Piazza Vittorio Garden representation
                            drawRoundRect(
                                color = Color(0xFF17291F),
                                topLeft = Offset(w * 0.52f, h * 0.44f),
                                size = Size(w * 0.26f, h * 0.22f),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                            drawRoundRect(
                                color = Color(0xFF2E7D32).copy(alpha = 0.4f),
                                topLeft = Offset(w * 0.52f, h * 0.44f),
                                size = Size(w * 0.26f, h * 0.22f),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // ZTL Boundary dashed line
                            val ztlDash = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                            drawLine(
                                color = ZtlRestricted.copy(alpha = 0.6f),
                                start = Offset(w * 0.18f, h * 0.20f),
                                end = Offset(w * 0.24f, h * 0.75f),
                                strokeWidth = 3.dp.toPx(),
                                pathEffect = ztlDash
                            )

                            // Render Thermal Heatmap Blobs for Sub-blocks
                            for (block in adjustedSubBlocks) {
                                val cx = block.normX * w
                                val cy = block.normY * h
                                val radiusPx = (block.heatRadiusDp).dp.toPx()

                                val heatColor = when {
                                    block.congestionPercent >= 80 -> Color(0xFFFF1744) // Bright Red
                                    block.congestionPercent >= 60 -> Color(0xFFFF9100) // Deep Orange
                                    block.congestionPercent >= 40 -> Color(0xFFFFD600) // Amber Yellow
                                    else -> Color(0xFF00E676) // Vibrant Green
                                }

                                val radialBrush = Brush.radialGradient(
                                    colors = listOf(
                                        heatColor.copy(alpha = 0.45f),
                                        heatColor.copy(alpha = 0.25f),
                                        heatColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = radiusPx
                                )

                                drawCircle(
                                    brush = radialBrush,
                                    radius = radiusPx,
                                    center = Offset(cx, cy)
                                )

                                // Highlight ring if selected
                                val isSelected = activeSelectedBlock?.id == block.id
                                if (isSelected) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = 18.dp.toPx(),
                                        center = Offset(cx, cy),
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )
                                }

                                // Centroid dot
                                drawCircle(
                                    color = heatColor,
                                    radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }

                        // Heatmap Overlay Landmark labels
                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "ZTL Centro (Ovest)",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFF8A80),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.padding(8.dp).align(Alignment.TopCenter)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "Stazione Roma Termini",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Box(modifier = Modifier.padding(8.dp).align(Alignment.CenterEnd)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "Piazza Vittorio",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AvailGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Heatmap Scale Legend Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0E1116), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(9.dp).background(Color(0xFF00E676), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fluida (<40%)", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(9.dp).background(Color(0xFFFFD600), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Moderata (40-60%)", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF9100), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Elevata (60-80%)", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(9.dp).background(Color(0xFFFF1744), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Critica (>80%)", fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        // 3. Time-of-Day Simulation Tabs
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2027)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PREVISIONE CONGESTIONE PER FASCIA ORARIA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(
                        selectedTabIndex = TimeSimulation.entries.indexOf(selectedTimeSim),
                        containerColor = Color(0xFF13171C),
                        contentColor = RomanGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[TimeSimulation.entries.indexOf(selectedTimeSim)]),
                                color = RomanGold
                            )
                        }
                    ) {
                        TimeSimulation.entries.forEach { sim ->
                            Tab(
                                selected = selectedTimeSim == sim,
                                onClick = { selectedTimeSim = sim },
                                text = {
                                    Text(
                                        text = sim.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selectedTimeSim == sim) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTimeSim == sim) RomanGold else Color.LightGray
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. Filter Chips: Density Level Selection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == HeatmapFilter.ALL,
                    onClick = { selectedFilter = HeatmapFilter.ALL },
                    label = { Text("Tutti (${adjustedSubBlocks.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RomanGold,
                        selectedLabelColor = Color.Black
                    )
                )

                FilterChip(
                    selected = selectedFilter == HeatmapFilter.HIGH_CONGESTION,
                    onClick = { selectedFilter = HeatmapFilter.HIGH_CONGESTION },
                    label = { Text("Alta Congestione", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AvailRed,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == HeatmapFilter.EASY_PARKING,
                    onClick = { selectedFilter = HeatmapFilter.EASY_PARKING },
                    label = { Text("Posti Liberi (>50%)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AvailGreen,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedFilter == HeatmapFilter.OUTSIDE_ZTL,
                    onClick = { selectedFilter = HeatmapFilter.OUTSIDE_ZTL },
                    label = { Text("No ZTL", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2A5298),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // 5. Selected Sub-Block Inspection Detail Card with Direct Navigation
        activeSelectedBlock?.let { block ->
            item {
                val blockColor = when {
                    block.congestionPercent >= 80 -> AvailRed
                    block.congestionPercent >= 60 -> AvailAmber
                    block.congestionPercent >= 40 -> Color(0xFFFFD600)
                    else -> AvailGreen
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242C)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, blockColor),
                    modifier = Modifier.fillMaxWidth().testTag("selected_subblock_detail_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = block.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = block.description,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = blockColor
                            ) {
                                Text(
                                    text = "${block.congestionPercent}% Congestione",
                                    color = if (block.congestionPercent in 40..60) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Row: Search Time + Estimated Spots + ZTL Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF13171C), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = blockColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RICERCA MEDIA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "~${block.estimatedSearchTimeMinutes} min",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = blockColor
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2A313C)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalParking,
                                        contentDescription = null,
                                        tint = RomanGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("STALLI DISPONIBILI", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "~${block.availableSpotsEstimate} posti",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF2A313C)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (block.ztlStatusNote.contains("ZTL Notturna")) AvailRed else Color.Gray,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ZTL STATO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = if (block.ztlStatusNote.contains("ZTL")) "Attenzione" else "OK",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (block.ztlStatusNote.contains("ZTL")) AvailRed else AvailGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Recommendation note
                        Text(
                            text = "💡 ${block.recommendation}",
                            fontSize = 11.sp,
                            color = RomanGold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Open in external navigation apps (Google Maps / Waze)
                        Button(
                            onClick = {
                                NavigationHelper.openInExternalMaps(
                                    context = context,
                                    latitude = block.latitude,
                                    longitude = block.longitude,
                                    destinationName = block.name
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_nav_subblock_${block.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Naviga nel sotto-blocco",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Naviga in questo sotto-blocco (Maps / Waze)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Sub-Block Congestion Ranking List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dettaglio Sotto-Blocchi Termini-Esquilino (${filteredSubBlocks.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ordinati per densità",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        items(filteredSubBlocks, key = { it.id }) { block ->
            SubBlockDensityCard(
                block = block,
                isSelected = activeSelectedBlock?.id == block.id,
                onClick = { onSelectSubBlock(block) },
                onNavigate = {
                    NavigationHelper.openInExternalMaps(
                        context = context,
                        latitude = block.latitude,
                        longitude = block.longitude,
                        destinationName = block.name
                    )
                }
            )
        }
    }
}

@Composable
private fun SubBlockDensityCard(
    block: RadarSubBlockDensity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigate: () -> Unit
) {
    val barColor = when {
        block.congestionPercent >= 80 -> AvailRed
        block.congestionPercent >= 60 -> AvailAmber
        block.congestionPercent >= 40 -> Color(0xFFFFD600)
        else -> AvailGreen
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF222B36) else MaterialTheme.colorScheme.surface),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, RomanGold) else null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag("subblock_card_${block.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${block.shortName} • ~${block.availableSpotsEstimate} stalli stimati",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = barColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${block.congestionPercent}% Occupato",
                            color = barColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigate,
                        modifier = Modifier.size(32.dp).testTag("nav_btn_${block.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Naviga con Maps o Waze",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Congestion Linear Gauge
            LinearProgressIndicator(
                progress = { block.congestionPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tempo medio ricerca: ~${block.estimatedSearchTimeMinutes} min",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = block.ztlStatusNote,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (block.ztlStatusNote.contains("ZTL Notturna")) AvailRed else TerracottaPrimary
                )
            }
        }
    }
}
