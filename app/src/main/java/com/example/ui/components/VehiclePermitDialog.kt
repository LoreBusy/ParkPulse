package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.UserProfile
import com.example.data.model.VehiclePermitType
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary

@Composable
fun VehiclePermitDialog(
    userProfile: UserProfile,
    onSave: (plate: String, model: String, permit: VehiclePermitType, permitNumber: String, expiry: String, isActive: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var plate by remember { mutableStateOf(userProfile.licensePlate) }
    var model by remember { mutableStateOf(userProfile.vehicleModel) }
    var selectedPermit by remember { mutableStateOf(userProfile.vehiclePermit) }
    var permitNumber by remember { mutableStateOf(userProfile.permitNumber) }
    var expiryDate by remember { mutableStateOf(userProfile.permitExpiryDate) }
    var isPermitActive by remember { mutableStateOf(userProfile.isPermitActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2228),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = RomanGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dati Veicolo & Permesso ZTL",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configura la targa per verificare automaticamente se i percorsi entrano in zone ZTL non autorizzate.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                // License Plate Input (with European Plate Preview)
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Targa Veicolo (es. RM 782 ED)", fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Pin, contentDescription = null, tint = RomanGold)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RomanGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_plate")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modello Veicolo (es. Fiat 500 Hybrid)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RomanGold,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_model")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Permit Status Card
                Text(
                    text = "Tipologia Permesso ZTL Roma:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                VehiclePermitType.entries.forEach { permit ->
                    val isChecked = selectedPermit == permit
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) Color(0xFF2A313C) else Color(0xFF14181E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isChecked) 1.5.dp else 0.5.dp,
                            if (isChecked) RomanGold else Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { selectedPermit = permit }
                            .testTag("permit_choice_${permit.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isChecked,
                                onClick = { selectedPermit = permit },
                                colors = RadioButtonDefaults.colors(selectedColor = RomanGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = permit.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) RomanGold else Color.White
                                )
                                Text(
                                    text = permit.description,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedPermit != VehiclePermitType.NONE) {
                    OutlinedTextField(
                        value = permitNumber,
                        onValueChange = { permitNumber = it },
                        label = { Text("Numero / Codice Permesso (es. RM-2026-98124)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RomanGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_permit_number")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Data di Scadenza Permesso (es. 31/12/2026)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RomanGold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_permit_expiry")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Permesso Valido & Attivo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPermitActive) "Riconosciuto dai varchi elettronici" else "Sospeso o scaduto",
                                    fontSize = 11.sp,
                                    color = if (isPermitActive) AvailGreen else Color(0xFFFF8A80)
                                )
                            }
                            Switch(
                                checked = isPermitActive,
                                onCheckedChange = { isPermitActive = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RomanGold)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(plate, model, selectedPermit, permitNumber, expiryDate, isPermitActive)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_save_vehicle_data")
            ) {
                Text("Salva Veicolo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = Color.Gray)
            }
        }
    )
}
