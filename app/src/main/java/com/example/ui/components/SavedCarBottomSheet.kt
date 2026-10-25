package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.SavedCarLocation
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCarBottomSheet(
    savedCar: SavedCarLocation?,
    currentGps: LatLng?,
    onSaveCarLocation: (address: String, notes: String, meterMinutes: Int?, floor: String) -> Unit,
    onClearSavedCar: () -> Unit,
    onAddMeterTime: (minutes: Int) -> Unit,
    onStartWalkingToCar: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("saved_car_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = TerracottaPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Auto Parcheggiata",
                                tint = TerracottaPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dov'è la mia auto?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (savedCar != null) "Posizione veicolo registrata" else "Memorizza la sosta attuale",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (savedCar != null) {
                    IconButton(
                        onClick = onClearSavedCar,
                        modifier = Modifier.testTag("clear_saved_car_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Rimuovi sosta",
                            tint = AvailRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (savedCar == null) {
                // Form to save current spot
                var notes by remember { mutableStateOf("") }
                var floor by remember { mutableStateOf("") }
                var meterMinutes by remember { mutableStateOf("60") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Salva la posizione attuale del tuo veicolo",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentGps != null) {
                                "Coordinate GPS: ${String.format("%.4f, %.4f", currentGps.latitude, currentGps.longitude)}"
                            } else {
                                "Posizione GPS approssimata"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Note (es. Via, civico, vicino al bar)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("car_notes_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = floor,
                                onValueChange = { floor = it },
                                label = { Text("Piano / Settore") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("car_floor_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = meterMinutes,
                                onValueChange = { meterMinutes = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Timer Parcometro (min)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("car_meter_input"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val mins = meterMinutes.toIntOrNull()
                                val addr = if (notes.isNotBlank()) notes else "Parcheggio registrato"
                                onSaveCarLocation(addr, notes, mins, floor)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_save_car_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ho Parcheggiato Qui")
                        }
                    }
                }
            } else {
                // Display saved car details
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.ITALY) }
                val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.ITALY) }
                val parkTimeStr = timeFormat.format(Date(savedCar.parkedAtEpochMs))
                val parkDateStr = dateFormat.format(Date(savedCar.parkedAtEpochMs))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = savedCar.address,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Parcheggiata alle $parkTimeStr ($parkDateStr) • ${String.format("%.4f, %.4f", savedCar.latitude, savedCar.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (savedCar.floorOrZone.isNotBlank() || savedCar.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.size(18.dp), tint = RomanGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    if (savedCar.floorOrZone.isNotBlank()) {
                                        Text(text = "Piano/Settore: ${savedCar.floorOrZone}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                    }
                                    if (savedCar.notes.isNotBlank()) {
                                        Text(text = savedCar.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Meter Countdown
                        Spacer(modifier = Modifier.height(14.dp))
                        val isMeterActive = savedCar.isMeterActive
                        val remainingMin = savedCar.remainingMeterMinutes

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMeterActive) {
                                if (remainingMin <= 10) AvailRed.copy(alpha = 0.15f) else AvailAmber.copy(alpha = 0.15f)
                            } else Color.Gray.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = if (isMeterActive) (if (remainingMin <= 10) AvailRed else AvailAmber) else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isMeterActive) {
                                                "Parcometro: $remainingMin min rimanenti"
                                            } else {
                                                "Nessun parcometro attivo"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (isMeterActive && savedCar.meterExpiryEpochMs != null) {
                                            Text(
                                                text = "Scadenza prevista: ${timeFormat.format(Date(savedCar.meterExpiryEpochMs))}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { onAddMeterTime(15) },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("add_15m_btn")
                                    ) {
                                        Text("+15m", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onAddMeterTime(30) },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("add_30m_btn")
                                    ) {
                                        Text("+30m", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Walking navigation action
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onStartWalkingToCar(LatLng(savedCar.latitude, savedCar.longitude))
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("walk_to_car_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cammina Verso l'Auto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
