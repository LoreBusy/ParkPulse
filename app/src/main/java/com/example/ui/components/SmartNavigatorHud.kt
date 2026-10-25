package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveNavigationSession
import com.example.data.model.DepartureSignal
import com.example.data.model.ManeuverType
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.util.NavigationHelper

@Composable
fun SmartNavigatorHud(
    session: ActiveNavigationSession,
    onAdvanceStep: () -> Unit,
    onToggleVoice: () -> Unit,
    onToggleAvoidZtl: () -> Unit,
    onDivertToSpot: (DepartureSignal) -> Unit,
    onDismissSpotAlert: () -> Unit,
    onStopNavigation: () -> Unit,
    onRegisterParkedStay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentStep = session.currentStep
    val nextStep = session.nextStep
    var showExternalMapOptions by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_navigator_hud_root")
    ) {
        // TOP: Turn-by-Turn Maneuver Header Card
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2A)),
                border = BorderStroke(1.5.dp, if (currentStep?.isAntiZtlSafeManeuver == true) RomanGold else Color(0xFF2E4057)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nav_maneuver_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Maneuver Icon Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (currentStep?.maneuver) {
                                ManeuverType.ARRIVE_DESTINATION -> AvailGreen
                                ManeuverType.AVOID_ZTL_DETOUR -> RomanGold
                                else -> TerracottaPrimary
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getManeuverIcon(currentStep?.maneuver),
                                    contentDescription = currentStep?.maneuver?.label ?: "Manovra",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (session.isArrived) "ARRIVATO" else "${currentStep?.distanceMeters ?: 0} m",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (currentStep?.isAntiZtlSafeManeuver == true) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RomanGold.copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, RomanGold)
                                    ) {
                                        Text(
                                            text = "ANTI-VARCO ZTL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RomanGold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = currentStep?.instruction ?: "Segui l'itinerario verso ${session.destinationName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2
                            )
                        }

                        // Exit Navigation Button
                        IconButton(
                            onClick = onStopNavigation,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("nav_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Termina navigazione",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Next Maneuver Preview Sub-Row
                    if (nextStep != null && !session.isArrived) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Poi:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = getManeuverIcon(nextStep.maneuver),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${nextStep.streetName} (${nextStep.distanceMeters}m)",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    // Hazard / Safety Advice Note
                    currentStep?.roadHazardNote?.let { hazard ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (currentStep.isAntiZtlSafeManeuver) RomanGold else AvailAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = hazard,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentStep.isAntiZtlSafeManeuver) RomanGold else AvailAmber
                            )
                        }
                    }
                }
            }
        }

        // FLOATING TELEMETRY & WAZE-STYLE ROAD HAZARD PILLS (LEFT & RIGHT)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Speedometer & Speed Limit sign
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B),
                border = BorderStroke(2.dp, TerracottaPrimary),
                modifier = Modifier.size(54.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${session.currentSpeedKmh}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "km/h",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Speed Limit Sign
            currentStep?.let { step ->
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.5.dp, Color.Red),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${step.roadSpeedLimitKmh}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // ZIGZAG ANTI-ZTL / CAMERA RADAR PILL (RIGHT SIDE)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            // ZigZag Status Shield
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (session.avoidZtlEnabled) Color(0xFF0F382A) else Color(0xFF381515),
                border = BorderStroke(1.dp, if (session.avoidZtlEnabled) AvailGreen else AvailRed),
                modifier = Modifier
                    .clickable { onToggleAvoidZtl() }
                    .testTag("nav_toggle_ztl_shield")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (session.avoidZtlEnabled) Icons.Default.Shield else Icons.Default.Security,
                        contentDescription = "ZTL Mode",
                        tint = if (session.avoidZtlEnabled) AvailGreen else AvailRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (session.avoidZtlEnabled) "ZTL Evitata" else "ZTL Libera",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "ZigZag Attivo",
                            fontSize = 8.sp,
                            color = if (session.avoidZtlEnabled) AvailGreen else AvailRed
                        )
                    }
                }
            }

            // Voice Toggle Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B),
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onToggleVoice() }
                    .testTag("nav_voice_toggle")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (session.isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Voce guida",
                        tint = if (session.isVoiceEnabled) RomanGold else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick advance / simulation button (for rapid testing)
            Surface(
                shape = CircleShape,
                color = TerracottaPrimary,
                modifier = Modifier
                    .size(42.dp)
                    .clickable { onAdvanceStep() }
                    .testTag("nav_advance_step_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Avanza simulazione",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // PROACTIVE COMMUNITY OPPORTUNITY BANNER: SPOT ABOUT TO BE FREED (OUR KILLER FEATURE!)
        AnimatedVisibility(
            visible = session.liveFreeSpotNearbyAlert != null && !session.isArrived,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 125.dp, start = 12.dp, end = 12.dp)
        ) {
            session.liveFreeSpotNearbyAlert?.let { freeSpot ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3B2B)),
                    border = BorderStroke(1.5.dp, AvailGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nav_free_spot_alert_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AvailGreen.copy(alpha = 0.25f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalParking,
                                            contentDescription = null,
                                            tint = AvailGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🚨 POSTO IN LIBERAZIONE A ~180M!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AvailGreen
                                    )
                                    Text(
                                        text = "${freeSpot.streetName} (${freeSpot.microAreaName})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissSpotAlert,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Ignora",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onDivertToSpot(freeSpot) },
                                colors = ButtonDefaults.buttonColors(containerColor = AvailGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("nav_divert_to_spot_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Devia qui subito",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onDismissSpotAlert,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = "Continua",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // ARRIVAL BANNER
        AnimatedVisibility(
            visible = session.isArrived,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 125.dp, start = 12.dp, end = 12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D24)),
                border = BorderStroke(2.dp, AvailGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nav_arrived_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AvailGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SEI ARRIVATO A DESTINAZIONE!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "${session.destinationName} • +25 Punti Community",
                        fontSize = 12.sp,
                        color = RomanGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRegisterParkedStay,
                            colors = ButtonDefaults.buttonColors(containerColor = RomanGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ho parcheggiato qui",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onStopNavigation,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "Termina",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM BAR: TRIP HUD (ETA, DISTANCE, PROBABILITY, CONTROLS)
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Color(0xFF2E3846)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("nav_bottom_hud")
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Trip metrics: ETA, Remaining Dist, Arrival Time, Smart Parking Prob
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = session.formattedRemainingDuration,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = AvailGreen
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = session.formattedRemainingDistance,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                        Text(
                            text = "Destinazione: ${session.destinationName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }

                    // Parking Probability Pill at Destination
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalParking,
                                contentDescription = null,
                                tint = RomanGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Column {
                                Text(
                                    text = "${session.destinationParkingProbability}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RomanGold
                                )
                                Text(
                                    text = "Posto Libero",
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stop Navigation Button
                    Button(
                        onClick = onStopNavigation,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("nav_bottom_stop_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Esci",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Advance step button (Simula / Prosegui)
                    Button(
                        onClick = onAdvanceStep,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(42.dp)
                            .testTag("nav_bottom_advance_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (session.isArrived) "Ricomincia" else "Avanza Guida",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Fallback external navigation apps (Waze / Google Maps)
                    OutlinedButton(
                        onClick = {
                            NavigationHelper.openInExternalMaps(
                                context = context,
                                latitude = session.destinationLocation.latitude,
                                longitude = session.destinationLocation.longitude,
                                destinationName = "${session.destinationName} (${session.destinationAddress})"
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("nav_bottom_external_maps_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Maps esterno",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getManeuverIcon(maneuver: ManeuverType?): ImageVector {
    return when (maneuver) {
        ManeuverType.DEPART -> Icons.Default.PlayArrow
        ManeuverType.CONTINUE_STRAIGHT -> Icons.Default.NearMe
        ManeuverType.TURN_RIGHT -> Icons.Default.TurnRight
        ManeuverType.TURN_LEFT -> Icons.Default.TurnLeft
        ManeuverType.SLIGHT_RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        ManeuverType.SLIGHT_LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        ManeuverType.ROUNDABOUT -> Icons.AutoMirrored.Filled.AltRoute
        ManeuverType.AVOID_ZTL_DETOUR -> Icons.Default.Shield
        ManeuverType.ARRIVE_DESTINATION -> Icons.Default.CheckCircle
        null -> Icons.Default.NearMe
    }
}
