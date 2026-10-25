package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.CopilotAnalysisResult
import com.example.data.ai.CopilotModelChoice
import com.example.data.model.ZtlGate
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.BlueLineZone
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.ZtlCaution
import com.example.ui.theme.ZtlRestricted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiZtlCopilotSheet(
    targetStreet: String,
    modelChoice: CopilotModelChoice,
    isLoading: Boolean,
    result: CopilotAnalysisResult?,
    ztlGates: List<ZtlGate>,
    onSelectStreet: (String) -> Unit,
    onSelectModel: (CopilotModelChoice) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("gemini_ztl_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RomanGold,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Copilot ZTL & Rischi Sosta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intelligenza Gemini per la guida e sosta a Roma",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Model Switcher Bar
            Text(
                text = "Motore di intelligenza:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = modelChoice == CopilotModelChoice.MAPS_GROUNDED_FLASH,
                    onClick = { onSelectModel(CopilotModelChoice.MAPS_GROUNDED_FLASH) },
                    label = { Text("Gemini 3.5 Flash (Maps)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("model_chip_flash")
                )

                FilterChip(
                    selected = modelChoice == CopilotModelChoice.FAST_LITE,
                    onClick = { onSelectModel(CopilotModelChoice.FAST_LITE) },
                    label = { Text("Gemini 3.1 Flash-Lite (Rapido)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("model_chip_lite")
                )

                FilterChip(
                    selected = modelChoice == CopilotModelChoice.HIGH_THINKING_PRO,
                    onClick = { onSelectModel(CopilotModelChoice.HIGH_THINKING_PRO) },
                    label = { Text("Gemini 3.1 Pro (Pensiero Alto)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Psychology, null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TerracottaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("model_chip_pro")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Street Selection
            Text(
                text = "Seleziona o verifica via o settore:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            val romanStreets = listOf(
                "Via Cavour",
                "Via Giovanni Giolitti",
                "Piazza Vittorio Emanuele II",
                "Via Merulana",
                "Via Principe Amedeo"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                romanStreets.forEach { street ->
                    val isChosen = targetStreet.contains(street.take(8), ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChosen) TerracottaPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isChosen) androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary) else null,
                        modifier = Modifier.testTag("quick_street_${street.take(6)}")
                    ) {
                        Text(
                            text = street,
                            fontSize = 12.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) TerracottaPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable { onSelectStreet(street) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Card
            if (isLoading) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = TerracottaPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analisi regole ZTL, rimozione forzata e tariffe in corso...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (result != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Esito Analisi: $targetStreet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RomanGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = result.modelUsed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RomanGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // ZTL Alert
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ZtlRestricted.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ZtlRestricted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "STATO ZTL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZtlRestricted
                                )
                                Text(
                                    text = result.ztlRisk,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cleaning Risk
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ZtlCaution.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = ZtlCaution,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "LAVAGGIO STRADE / RIMOZIONE AMA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZtlCaution
                                )
                                Text(
                                    text = result.streetCleaningRisk,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Blue line tariff
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlueLineZone.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalParking,
                                contentDescription = null,
                                tint = BlueLineZone,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "TARIFFA SOSTA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueLineZone
                                )
                                Text(
                                    text = result.tariffAdvice,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = result.summary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Vie consigliate adiacenti:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        result.suggestedAlternatives.forEach { alt ->
                            Text(
                                text = "• $alt",
                                fontSize = 11.sp,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Varco Directory
            Text(
                text = "Presidio Varchi Elettronici Roma Centro:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            ztlGates.forEach { gate ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gate.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${gate.street} • ${gate.activeHours}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ZtlRestricted.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "TELECAMERA ATTIVA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZtlRestricted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
