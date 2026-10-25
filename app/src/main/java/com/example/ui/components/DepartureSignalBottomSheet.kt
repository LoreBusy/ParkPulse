package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DepartureSliderMode
import com.example.data.model.MicroArea
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureSignalBottomSheet(
    microAreas: List<MicroArea>,
    selectedAreaId: String,
    onSelectAreaId: (String) -> Unit,
    initialDurationMinutes: Int = 5,
    initialMode: DepartureSliderMode = DepartureSliderMode.FREE_SPOT_WINDOW,
    onConfirmDeparture: (durationMinutes: Int, mode: DepartureSliderMode) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentMode by remember { mutableStateOf(initialMode) }
    // Slider state for spot remaining free (5, 10, 15 min)
    var freeDurationMinutes by remember { mutableFloatStateOf(initialDurationMinutes.coerceIn(5, 15).toFloat()) }
    // Slider state for occupying until departure (5, 10, 15, 20, 30, 45, 60 min)
    var occupyDurationMinutes by remember { mutableFloatStateOf(15f) }

    val effectiveMinutes = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) {
        // Snap to 5, 10, or 15 minutes
        val raw = freeDurationMinutes.roundToInt()
        when {
            raw <= 7 -> 5
            raw <= 12 -> 10
            else -> 15
        }
    } else {
        val raw = occupyDurationMinutes.roundToInt()
        when {
            raw <= 7 -> 5
            raw <= 12 -> 10
            raw <= 17 -> 15
            raw <= 25 -> 20
            raw <= 37 -> 30
            raw <= 52 -> 45
            else -> 60
        }
    }

    val expectedDepartureTimeStr = remember(effectiveMinutes) {
        val departureEpochMs = System.currentTimeMillis() + (effectiveMinutes * 60 * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.ITALY)
        sdf.format(Date(departureEpochMs))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("departure_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sto liberando un posto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) {
                            "Posto libero stimato per $effectiveMinutes min per chi cerca sosta"
                        } else {
                            "Partenza prevista alle ore $expectedDepartureTimeStr (~$effectiveMinutes min)"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Selector Tabs: Spot Remaining Free vs Will Occupy Until Departure
            val modes = listOf(
                DepartureSliderMode.FREE_SPOT_WINDOW to "Posto libero per",
                DepartureSliderMode.OCCUPY_UNTIL_DEPARTURE to "Lo occuperò ancora per"
            )
            TabRow(
                selectedTabIndex = modes.indexOfFirst { it.first == currentMode },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = TerracottaPrimary,
                indicator = { tabPositions ->
                    val index = modes.indexOfFirst { it.first == currentMode }
                    if (index >= 0 && index < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = TerracottaPrimary,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("duration_mode_tab_row")
            ) {
                modes.forEach { (mode, title) ->
                    val isSelected = currentMode == mode
                    Tab(
                        selected = isSelected,
                        onClick = { currentMode = mode },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TerracottaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Duration Slider Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth().testTag("duration_slider_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) Icons.Default.Timer else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) TerracottaPrimary else RomanGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) {
                                    "Disponibilità stimata:"
                                } else {
                                    "Tempo prima di partire:"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Prominent Minutes Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) TerracottaPrimary else RomanGold
                        ) {
                            Text(
                                text = "$effectiveMinutes minuti",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // The Slider
                    if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) {
                        Slider(
                            value = freeDurationMinutes,
                            onValueChange = { freeDurationMinutes = it },
                            valueRange = 5f..15f,
                            steps = 1, // snaps between 5, 10, 15
                            colors = SliderDefaults.colors(
                                thumbColor = TerracottaPrimary,
                                activeTrackColor = TerracottaPrimary,
                                inactiveTrackColor = TerracottaPrimary.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("free_duration_slider")
                        )

                        // Quick Chips for 5, 10, 15 minutes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(5 to "5 min", 10 to "10 min", 15 to "15 min").forEach { (mins, label) ->
                                val isSelected = effectiveMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { freeDurationMinutes = mins.toFloat() },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TerracottaPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = TerracottaPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (effectiveMinutes) {
                                5 -> "⚡ 5 min: Ideale per automobilisti già in transito sulla via."
                                10 -> "🚗 10 min: Finestra ottimale per chi sta girando nei paraggi."
                                else -> "⏳ 15 min: Finestra comoda per chi si avvicina dalle vie adiacenti."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Slider(
                            value = occupyDurationMinutes,
                            onValueChange = { occupyDurationMinutes = it },
                            valueRange = 5f..60f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = RomanGold,
                                activeTrackColor = RomanGold,
                                inactiveTrackColor = RomanGold.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("occupy_duration_slider")
                        )

                        // Quick Chips for 5m, 10m, 15m, 30m, 1h
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(5 to "5m", 10 to "10m", 15 to "15m", 30 to "30m", 60 to "1h").forEach { (mins, label) ->
                                val isSelected = effectiveMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { occupyDurationMinutes = mins.toFloat() },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RomanGold.copy(alpha = 0.2f),
                                        selectedLabelColor = RomanGold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Partenza programmata alle ore $expectedDepartureTimeStr. Gli altri utenti vedranno la sosta in liberazione in anticipo!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Micro-area choice list
            Text(
                text = "Micro-area di partenza:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(microAreas, key = { it.id }) { area ->
                    val isChosen = area.id == selectedAreaId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChosen) TerracottaPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isChosen) BorderStroke(1.5.dp, TerracottaPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAreaId(area.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = area.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${area.streetName} • Micro-area ~${area.radiusMeters}m",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (isChosen) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isChosen) TerracottaPrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Privacy & Karma pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.PrivacyTip,
                        contentDescription = null,
                        tint = AvailGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS e targa anonimi (raggio ~50m)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AvailGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+50 Pt Sosta • +2 Karma",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AvailGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Submit Button
            Button(
                onClick = { onConfirmDeparture(effectiveMinutes, currentMode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) TerracottaPrimary else RomanGold
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_departure_button")
            ) {
                Icon(
                    imageVector = Icons.Default.LocalParking,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentMode == DepartureSliderMode.FREE_SPOT_WINDOW) {
                        "Pubblica Segnale (Libero per $effectiveMinutes Min)"
                    } else {
                        "Condividi Partenza (Tra $effectiveMinutes Min)"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Text(text = "Annulla", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
