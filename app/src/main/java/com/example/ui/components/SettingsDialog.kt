package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyFormat
import com.example.data.model.DistanceUnit
import com.example.data.model.ThemeMode
import com.example.data.model.UserSettings
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary

@Composable
fun SettingsDialog(
    userSettings: UserSettings,
    onUpdateSettings: (UserSettings) -> Unit,
    onReplayOnboarding: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isItalian = userSettings.language == "it"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("app_settings_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isItalian) "Impostazioni App" else "App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isItalian) "Personalizza interfaccia, unità e lingua" else "Customize theme, units, and language",
                        fontSize = 11.sp,
                        color = RomanGold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Theme Mode
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isItalian) "Tema Grafico (Material 3)" else "Appearance (Material 3)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSelected = userSettings.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFF1E2530),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) TerracottaPrimary else Color(0xFF2E3846)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(userSettings.copy(themeMode = mode)) }
                                    .testTag("theme_btn_${mode.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.LIGHT -> Icons.Default.LightMode
                                            ThemeMode.DARK -> Icons.Default.DarkMode
                                            ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (mode) {
                                            ThemeMode.LIGHT -> if (isItalian) "Chiaro" else "Light"
                                            ThemeMode.DARK -> if (isItalian) "Scuro" else "Dark"
                                            ThemeMode.SYSTEM -> if (isItalian) "Auto" else "Auto"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Language Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isItalian) "Lingua dell'Applicazione" else "App Language",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isItalian) TerracottaPrimary else Color(0xFF1E2530),
                            border = BorderStroke(1.dp, if (isItalian) TerracottaPrimary else Color(0xFF2E3846)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings(userSettings.copy(language = "it")) }
                                .testTag("settings_lang_it")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🇮🇹 Italiano", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isItalian) TerracottaPrimary else Color(0xFF1E2530),
                            border = BorderStroke(1.dp, if (!isItalian) TerracottaPrimary else Color(0xFF2E3846)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings(userSettings.copy(language = "en")) }
                                .testTag("settings_lang_en")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🇬🇧 English", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Section 3: Distance Measurement Units
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isItalian) "Unità di Misura Distanza" else "Distance Units",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isMetric = userSettings.distanceUnit == DistanceUnit.METRIC
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMetric) TerracottaPrimary else Color(0xFF1E2530),
                            border = BorderStroke(1.dp, if (isMetric) TerracottaPrimary else Color(0xFF2E3846)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings(userSettings.copy(distanceUnit = DistanceUnit.METRIC)) }
                                .testTag("unit_metric_btn")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isItalian) "Metrico" else "Metric",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("m / km", fontSize = 10.sp, color = RomanGold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isMetric) TerracottaPrimary else Color(0xFF1E2530),
                            border = BorderStroke(1.dp, if (!isMetric) TerracottaPrimary else Color(0xFF2E3846)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateSettings(userSettings.copy(distanceUnit = DistanceUnit.IMPERIAL)) }
                                .testTag("unit_imperial_btn")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isItalian) "Imperiale" else "Imperial",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("ft / mi", fontSize = 10.sp, color = RomanGold)
                            }
                        }
                    }
                }

                // Section 4: Currency Format
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = RomanGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isItalian) "Valuta Tariffe Parcheggio" else "Parking Currency",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurrencyFormat.values().forEach { curr ->
                            val isSelected = userSettings.currencyFormat == curr
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TerracottaPrimary else Color(0xFF1E2530),
                                border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else Color(0xFF2E3846)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateSettings(userSettings.copy(currencyFormat = curr)) }
                                    .testTag("currency_btn_${curr.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = curr.symbol,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) Color.White else RomanGold
                                    )
                                    Text(
                                        text = curr.code,
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Preview Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141A22)),
                    border = BorderStroke(1.dp, Color(0xFF2C3746)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isItalian) "Anteprima Formattazione:" else "Live Formatting Preview:",
                            fontSize = 10.sp,
                            color = RomanGold,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distanza: ${userSettings.formatDistance(350)} • ${userSettings.formatDistance(2400)}",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Tariffa: ${userSettings.formatTariff("€1,80/h")}",
                                fontSize = 11.sp,
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Replay Onboarding Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onReplayOnboarding()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("replay_onboarding_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = RomanGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isItalian) "Rivedi Tutorial Introduttivo" else "Replay Onboarding Guide",
                        fontSize = 12.sp,
                        color = RomanGold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.testTag("close_settings_btn")
            ) {
                Text(if (isItalian) "Chiudi" else "Close", color = Color.White)
            }
        }
    )
}
