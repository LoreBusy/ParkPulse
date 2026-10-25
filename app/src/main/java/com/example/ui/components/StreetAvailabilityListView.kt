package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.util.NavigationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.NearMe
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
import com.example.data.model.AvailabilityLevel
import com.example.data.model.MicroArea
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.BlueLineZone
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.ZtlCaution

@Composable
fun StreetAvailabilityListView(
    microAreas: List<MicroArea>,
    favoriteIds: Set<String>,
    onSelectArea: (MicroArea) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onReportDeparture: (MicroArea) -> Unit,
    onRegisterParkedStay: (MicroArea) -> Unit,
    onStartNavigation: ((MicroArea) -> Unit)? = null,
    onOpenMlTrends: ((MicroArea) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("street_availability_list"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Distanze calcolate dall'atrio di Stazione Termini",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Micro-aree anonime (30-75m) per rispetto della privacy cittadina",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(microAreas, key = { it.id }) { area ->
            MicroAreaCard(
                area = area,
                isFavorite = favoriteIds.contains(area.id),
                onClick = { onSelectArea(area) },
                onToggleFavorite = { onToggleFavorite(area.id) },
                onReportDeparture = { onReportDeparture(area) },
                onRegisterParkedStay = { onRegisterParkedStay(area) },
                onStartNavigation = if (onStartNavigation != null) { { onStartNavigation(area) } } else null,
                onOpenMlTrends = if (onOpenMlTrends != null) { { onOpenMlTrends(area) } } else null
            )
        }
    }
}

@Composable
private fun MicroAreaCard(
    area: MicroArea,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReportDeparture: () -> Unit,
    onRegisterParkedStay: () -> Unit,
    onStartNavigation: (() -> Unit)? = null,
    onOpenMlTrends: (() -> Unit)? = null
) {
    val levelColor = when (area.availabilityLevel) {
        AvailabilityLevel.HIGH -> AvailGreen
        AvailabilityLevel.MEDIUM -> AvailAmber
        AvailabilityLevel.LOW -> AvailRed
    }

    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area_card_${area.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name + Navigation + Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = area.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${area.streetName} • ~${area.distanceMeters}m",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            NavigationHelper.openInExternalMaps(
                                context = context,
                                latitude = area.latitude,
                                longitude = area.longitude,
                                destinationName = "${area.name} (${area.streetName})"
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("list_nav_btn_${area.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Naviga con Maps o Waze",
                            tint = Color(0xFF1E88E5)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Preferito",
                            tint = if (isFavorite) TerracottaPrimary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Probability Bar & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Probabilità stallo libero",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${area.baselineAvailability}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { area.baselineAvailability / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = levelColor,
                        trackColor = levelColor.copy(alpha = 0.2f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags / Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Radius tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Raggio ~${area.radiusMeters}m",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Blue line tariff tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BlueLineZone.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = area.blueLineTariff,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BlueLineZone,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (area.whiteLineSpots > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AvailGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${area.whiteLineSpots} bianche",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AvailGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (area.hasEvCharging) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RomanGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "EV",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RomanGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Upcoming departure forecast note
            if (area.upcomingDeparturesCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RomanGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = RomanGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${area.upcomingDeparturesCount} sosta programmata in partenza nelle prossime ore",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ZTL or cleaning warning if applicable
            if (area.isZtlBoundary || area.cleaningSchedule.contains("RIMOZIONE")) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ZtlCaution.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ZtlCaution,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (area.isZtlBoundary) area.ztlZoneName else area.cleaningSchedule,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Navigate button: launches Google Maps intent with coordinates
                Button(
                    onClick = {
                        NavigationHelper.launchGoogleMaps(
                            context = context,
                            latitude = area.latitude,
                            longitude = area.longitude,
                            destinationName = "${area.name} (${area.streetName})"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("navigate_btn_${area.id}")
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (onStartNavigation != null) {
                    Button(
                        onClick = onStartNavigation,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("nav_to_area_${area.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Naviga (Anti-ZTL)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Compact secondary actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onOpenMlTrends != null) {
                    OutlinedButton(
                        onClick = onOpenMlTrends,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = RomanGold.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("ml_trends_btn_${area.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Previsione ML Gemini",
                            tint = RomanGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Trend ML",
                            color = RomanGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRegisterParkedStay,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("register_stay_btn_${area.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = RomanGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Sosta",
                        color = RomanGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onReportDeparture,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("report_departure_btn_${area.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Libero",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
