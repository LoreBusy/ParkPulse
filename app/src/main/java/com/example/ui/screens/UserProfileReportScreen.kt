package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedCarLocation
import com.example.data.model.UserActivityRecord
import com.example.data.model.UserActivityType
import com.example.data.model.UserProfile
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.BrandNeonCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileReportScreen(
    userProfile: UserProfile,
    currentUser: FirebaseUser?,
    isGuestMode: Boolean,
    savedCarLocation: SavedCarLocation?,
    activities: List<UserActivityRecord>,
    selectedFilter: UserActivityType?,
    onSelectFilter: (UserActivityType?) -> Unit,
    onOpenDepartureModal: () -> Unit,
    onOpenRegisterStay: () -> Unit,
    onOpenSavedCar: () -> Unit,
    onOpenVehiclePermitDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSpot: (UserActivityRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredActivities = remember(activities, selectedFilter) {
        if (selectedFilter == null) activities
        else activities.filter { it.type == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .testTag("user_profile_report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. User Profile Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, Color(0xFF2E384D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_identity_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BrandNeonCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.5.dp, BrandNeonCyan),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Avatar",
                                        tint = BrandNeonCyan,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                val name = currentUser?.displayName
                                    ?: currentUser?.email?.substringBefore("@")
                                    ?: "Pilota ParkPulse"

                                Text(
                                    text = name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                val authStatus = when {
                                    currentUser == null -> "Accesso richiesto"
                                    currentUser.isEmailVerified -> "Account Verificato & Protetto"
                                    else -> "In attesa di verifica email"
                                }
                                Text(
                                    text = authStatus,
                                    fontSize = 12.sp,
                                    color = if (currentUser?.isEmailVerified == true) AvailGreen else RomanGold
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_profile_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Impostazioni",
                                tint = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Karma & Reliability Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LIVELLO COMMUNITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = RomanGold
                            )
                            Text(
                                text = userProfile.levelRank,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AvailGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AvailGreen.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AvailGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${userProfile.karmaScore}% Affidabilità",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AvailGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Points & Progress to Next Level
                    val pointsNeeded = remember(userProfile.points) { 100 - (userProfile.points % 100) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = RomanGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${userProfile.points} Punti",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "Prossimo rank: $pointsNeeded pt",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val progressFraction = (userProfile.points % 100).toFloat() / 100f
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = RomanGold,
                        trackColor = Color(0xFF263248)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vehicle & Permit button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenVehiclePermitDialog,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B4861)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_profile_vehicle_permit")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RomanGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Veicolo & ZTL", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onOpenLeaderboard,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2B45),
                                contentColor = RomanGold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_profile_leaderboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RomanGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Classifica", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 2. Report & Action Center ("Centro Segnalazioni")
        item {
            Text(
                text = "CENTRO SEGNALAZIONI & SOSTA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = RomanGold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Action 1: "Sto Liberando un Posto" (Primary Creation)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaPrimary.copy(alpha = 0.12f)),
                border = BorderStroke(1.5.dp, TerracottaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDepartureModal() }
                    .testTag("action_report_departure_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalParking,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Sto liberando un posto",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Segnala in anticipo per guadagnare +15 pt karma",
                                fontSize = 12.sp,
                                color = Color(0xFFFFB499)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenDepartureModal,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_report_departure_now")
                    ) {
                        Text("Segnala", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action 2: "Dove ho parcheggiato?" (Saved Car Status & Timer)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, if (savedCarLocation != null) BrandNeonCyan else Color(0xFF2E384D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSavedCar() }
                    .testTag("action_saved_car_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (savedCarLocation != null) BrandNeonCyan.copy(alpha = 0.2f) else Color(0xFF263248),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = if (savedCarLocation != null) BrandNeonCyan else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dove ho parcheggiato?",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (savedCarLocation != null) {
                                val meterStatus = if (savedCarLocation.isMeterActive) {
                                    "Scadenza sosta: ${savedCarLocation.remainingMeterMinutes} min"
                                } else {
                                    "Auto salvata (${savedCarLocation.address})"
                                }
                                Text(
                                    text = meterStatus,
                                    fontSize = 12.sp,
                                    color = if (savedCarLocation.isMeterActive) RomanGold else BrandNeonCyan
                                )
                            } else {
                                Text(
                                    text = "Nessuna auto memorizzata. Tocca per salvare.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenSavedCar,
                        modifier = Modifier.testTag("btn_open_saved_car_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer Parcometro",
                            tint = if (savedCarLocation != null) BrandNeonCyan else Color.LightGray
                        )
                    }
                }
            }
        }

        // Action 3: "Registra Sosta Corrente" (Check-in)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, Color(0xFF2E384D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenRegisterStay() }
                    .testTag("action_register_stay_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AvailGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = AvailGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Registra Sosta e Orario",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Imposta promemoria scadenza e pulizia strade",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // 3. Activity History Section ("Storico Segnalazioni")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORICO ATTIVITÀ & FEEDBACK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = RomanGold
                )

                Text(
                    text = "${filteredActivities.size} eventi",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Activity Type Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { onSelectFilter(null) },
                        label = { Text("Tutti", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandNeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = BrandNeonCyan
                        ),
                        modifier = Modifier.testTag("filter_all_activities")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == UserActivityType.REPORTED_DEPARTURE,
                        onClick = { onSelectFilter(UserActivityType.REPORTED_DEPARTURE) },
                        label = { Text("Partenze Segnalate", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TerracottaPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = TerracottaPrimary
                        ),
                        modifier = Modifier.testTag("filter_departure_activities")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == UserActivityType.REGISTERED_STAY,
                        onClick = { onSelectFilter(UserActivityType.REGISTERED_STAY) },
                        label = { Text("Soste Registrate", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AvailGreen.copy(alpha = 0.25f),
                            selectedLabelColor = AvailGreen
                        ),
                        modifier = Modifier.testTag("filter_stay_activities")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == UserActivityType.VERIFIED_SPOT,
                        onClick = { onSelectFilter(UserActivityType.VERIFIED_SPOT) },
                        label = { Text("Verifiche", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RomanGold.copy(alpha = 0.25f),
                            selectedLabelColor = RomanGold
                        ),
                        modifier = Modifier.testTag("filter_feedback_activities")
                    )
                }
            }
        }

        // Activities List or Empty State
        if (filteredActivities.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFF263248)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nessuna attività registrata",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Segnala una partenza o registra una sosta per guadagnare punti karma!",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(filteredActivities, key = { it.id }) { activity ->
                ActivityRecordItemCard(
                    activity = activity,
                    onNavigateToSpot = { onNavigateToSpot(activity) }
                )
            }
        }
    }
}

@Composable
private fun ActivityRecordItemCard(
    activity: UserActivityRecord,
    onNavigateToSpot: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd MMM", Locale.ITALY) }
    val formattedTime = remember(activity.timestampEpochMs) {
        dateFormat.format(Date(activity.timestampEpochMs))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, Color(0xFF2E384D)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSpot() }
            .testTag("activity_record_${activity.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = when (activity.type) {
                        UserActivityType.REPORTED_DEPARTURE -> TerracottaPrimary.copy(alpha = 0.2f)
                        UserActivityType.REGISTERED_STAY -> AvailGreen.copy(alpha = 0.2f)
                        UserActivityType.VERIFIED_SPOT, UserActivityType.FOUND_SPOT -> RomanGold.copy(alpha = 0.2f)
                        UserActivityType.COMMUNITY_BONUS -> BrandNeonCyan.copy(alpha = 0.2f)
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (activity.type) {
                                UserActivityType.REPORTED_DEPARTURE -> Icons.Default.LocalParking
                                UserActivityType.REGISTERED_STAY -> Icons.Default.HourglassTop
                                UserActivityType.VERIFIED_SPOT, UserActivityType.FOUND_SPOT -> Icons.Default.RateReview
                                UserActivityType.COMMUNITY_BONUS -> Icons.Default.EmojiEvents
                            },
                            contentDescription = null,
                            tint = when (activity.type) {
                                UserActivityType.REPORTED_DEPARTURE -> TerracottaPrimary
                                UserActivityType.REGISTERED_STAY -> AvailGreen
                                UserActivityType.VERIFIED_SPOT, UserActivityType.FOUND_SPOT -> RomanGold
                                UserActivityType.COMMUNITY_BONUS -> BrandNeonCyan
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = activity.microAreaName.ifBlank { activity.title },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$formattedTime • ${activity.statusNotes.ifBlank { activity.streetAddress }}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RomanGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "+${activity.rewardPoints} pt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RomanGold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}
