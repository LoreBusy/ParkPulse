package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Badge
import com.example.data.model.BadgeCategory
import com.example.data.model.UserActivityRecord
import com.example.data.model.UserActivityType
import com.example.data.model.UserProfile
import com.example.data.model.VehiclePermitType
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.util.NavigationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyActivityDashboard(
    userProfile: UserProfile,
    activities: List<UserActivityRecord>,
    selectedFilter: UserActivityType?,
    onSelectFilter: (UserActivityType?) -> Unit,
    onTriggerTestCongestionAlert: () -> Unit,
    onTriggerTestSpotAlert: () -> Unit,
    onTriggerTestTrafficAlert: () -> Unit = {},
    onNavigateToSpot: (UserActivityRecord) -> Unit,
    onOpenVehicleDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showNotificationTester by remember { mutableStateOf(false) }
    var selectedBadgeCategory by remember { mutableStateOf<BadgeCategory?>(null) }
    var selectedBadgeForDetails by remember { mutableStateOf<Badge?>(null) }

    val filteredActivities = remember(activities, selectedFilter) {
        if (selectedFilter == null) activities
        else activities.filter { it.type == selectedFilter }
    }

    val totalPointsEarned = remember(activities) {
        activities.sumOf { it.rewardPoints }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
            .testTag("my_activity_dashboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: My Community Dashboard & Reward Points
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activity_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RomanGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = RomanGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LA MIA ATTIVITÀ",
                                    fontSize = 11.sp,
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
                        }

                        // Reliability Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AvailGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AvailGreen.copy(alpha = 0.6f))
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Points Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Punti Community Guadagnati",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${userProfile.points}",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PT",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RomanGold,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                            }
                        }

                        // Community Impact Metrics
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ImpactBox(
                                label = "Reportati",
                                value = "${userProfile.signalsSharedCount}",
                                icon = Icons.Default.LocalParking,
                                color = TerracottaPrimary
                            )
                            ImpactBox(
                                label = "Verificati",
                                value = "${userProfile.signalsVerifiedCount}",
                                icon = Icons.Default.ThumbUp,
                                color = AvailGreen
                            )
                            ImpactBox(
                                label = "CO₂ Evitata",
                                value = "~2.8kg",
                                icon = Icons.Default.Eco,
                                color = RomanGold
                            )
                        }
                    }
                }
            }
        }

        // VEICOLO & PERMESSO ZTL (MANDATORY REQUIREMENT)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_permit_dashboard_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RomanGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = RomanGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Veicolo Registrato & ZTL",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Avviso preventivo varchi e multe",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Italian License Plate Graphic
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            ) {
                                Surface(color = Color(0xFF003399), shape = RoundedCornerShape(2.dp)) {
                                    Text("I", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userProfile.licensePlate,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(color = Color(0xFF003399), shape = RoundedCornerShape(2.dp)) {
                                    Text("•", color = Color.Yellow, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 1.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.vehicleModel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (userProfile.vehiclePermit == VehiclePermitType.NONE) AvailAmber.copy(alpha = 0.2f) else AvailGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = if (userProfile.vehiclePermit == VehiclePermitType.NONE) "Nessun permesso ZTL" else "Permesso: ${userProfile.vehiclePermit.label}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (userProfile.vehiclePermit == VehiclePermitType.NONE) AvailAmber else AvailGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (userProfile.vehiclePermit != VehiclePermitType.NONE && userProfile.permitNumber.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "#${userProfile.permitNumber}",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onOpenVehicleDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("dashboard_edit_vehicle_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modifica", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SISTEMA REPUTAZIONE & BADGE COMUNITARI (MANDATORY REQUIREMENT)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reputation_badges_section_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header Row with Trophy & Level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = RomanGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = RomanGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SISTEMA REPUTAZIONE & BADGE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = RomanGold
                                )
                                Text(
                                    text = "Riconoscimenti Community Roma",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Badges Unlocked Pill
                        val unlockedCount = userProfile.badges.count { it.isUnlocked }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RomanGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
                        ) {
                            Text(
                                text = "$unlockedCount / ${userProfile.badges.size} Sbloccati",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RomanGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reputation Criteria Explanation Banner
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161E28),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = RomanGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Criteri Assegnazione Badge:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Frequenza: Condividi più posti per avanzare di rango (Scout -> Sentinel -> Centurione)\n• Accuratezza: Ricevi conferme positive dai guidatori (min 90%-95% affidabilità)\n• ZTL & Sicurezza: Zero infrazioni varchi e percorsi anti-multa ZigZag rispettati",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedBadgeCategory == null,
                                onClick = { selectedBadgeCategory = null },
                                label = { Text("Tutti (${userProfile.badges.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RomanGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                        items(BadgeCategory.entries) { cat ->
                            val count = userProfile.badges.count { it.category == cat }
                            FilterChip(
                                selected = selectedBadgeCategory == cat,
                                onClick = { selectedBadgeCategory = if (selectedBadgeCategory == cat) null else cat },
                                label = { Text("${cat.label} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RomanGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badges List
                    val displayedBadges = remember(userProfile.badges, selectedBadgeCategory) {
                        if (selectedBadgeCategory == null) userProfile.badges
                        else userProfile.badges.filter { it.category == selectedBadgeCategory }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayedBadges.forEach { badge ->
                            BadgeItemCard(
                                badge = badge,
                                onClick = { selectedBadgeForDetails = badge }
                            )
                        }
                    }
                }
            }
        }

        // Live Real-Time Notification Service Banner & Interactive Tester
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_service_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Servizio Notifiche Real-Time Attivo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Avviso avvicinamento aree sature & posti liberi preferiti",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showNotificationTester = !showNotificationTester },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (showNotificationTester) "Nascondi Test" else "Simula Avviso",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedVisibility(visible = showNotificationTester) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Testa la ricezione immediata delle notifiche di sistema:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onTriggerTestCongestionAlert,
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_congestion_notif_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Allerta Congestione",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = onTriggerTestSpotAlert,
                                    colors = ButtonDefaults.buttonColors(containerColor = AvailGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_spot_notif_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Posto Libero Trovato",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = onTriggerTestTrafficAlert,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_workmanager_traffic_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🚨 Notifica Emergenza Traffico WorkManager",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title & Filter Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cronologia Segnalazioni & Posizioni",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "${filteredActivities.size} eventi",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Activity Type Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { onSelectFilter(null) },
                            label = { Text("Tutti (${activities.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(UserActivityType.entries) { type ->
                        val count = activities.count { it.type == type }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = { onSelectFilter(if (selectedFilter == type) null else type) },
                                label = { Text("${type.label} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Activities List
        if (filteredActivities.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nessuna attività registrata con questo filtro",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tocca 'Sto liberando un posto' o registra una sosta per accumulare punti.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredActivities, key = { it.id }) { record ->
                ActivityRecordCard(
                    record = record,
                    onNavigate = { onNavigateToSpot(record) },
                    onOpenInMaps = {
                        NavigationHelper.openInExternalMaps(
                            context = context,
                            latitude = record.latitude,
                            longitude = record.longitude,
                            destinationName = "${record.streetAddress} (${record.microAreaName})"
                        )
                    }
                )
            }
        }
    }

    // Badge Details Modal Dialog
    selectedBadgeForDetails?.let { badge ->
        BadgeDetailsDialog(
            badge = badge,
            onDismiss = { selectedBadgeForDetails = null }
        )
    }
}

@Composable
private fun BadgeItemCard(
    badge: Badge,
    onClick: () -> Unit
) {
    val tierColor = when (badge.tier) {
        "Platino" -> Color(0xFFE2E8F0)
        "Oro" -> RomanGold
        "Argento" -> Color(0xFFB0BEC5)
        else -> Color(0xFFCD7F32)
    }

    val tierIcon = when (badge.tier) {
        "Platino" -> Icons.Default.WorkspacePremium
        "Oro" -> Icons.Default.MilitaryTech
        "Argento" -> Icons.Default.Shield
        else -> Icons.Default.Star
    }

    val progressFraction = remember(badge.currentProgress, badge.targetProgress) {
        if (badge.targetProgress > 0) {
            (badge.currentProgress.toFloat() / badge.targetProgress.toFloat()).coerceIn(0f, 1f)
        } else 1f
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Color(0xFF1E2836) else Color(0xFF161C24)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (badge.isUnlocked) 1.5.dp else 1.dp,
            if (badge.isUnlocked) tierColor else tierColor.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("badge_card_${badge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tier Medal Icon
            Surface(
                shape = CircleShape,
                color = if (badge.isUnlocked) tierColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (badge.isUnlocked) tierColor else Color.Gray.copy(alpha = 0.4f)
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (badge.isUnlocked) tierIcon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) tierColor else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tierColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "${badge.tier} • ${badge.category.label}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar or Unlocked Badge
                if (badge.isUnlocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AvailGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sbloccato con successo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AvailGreen
                            )
                        }

                        Text(
                            text = "+${badge.karmaReward} Karma",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RomanGold
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Avanzamento: ${badge.currentProgress} / ${badge.targetProgress}",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "+${badge.karmaReward} Karma",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RomanGold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            color = tierColor,
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeDetailsDialog(
    badge: Badge,
    onDismiss: () -> Unit
) {
    val tierColor = when (badge.tier) {
        "Platino" -> Color(0xFFE2E8F0)
        "Oro" -> RomanGold
        "Argento" -> Color(0xFFB0BEC5)
        else -> Color(0xFFCD7F32)
    }

    val tierIcon = when (badge.tier) {
        "Platino" -> Icons.Default.WorkspacePremium
        "Oro" -> Icons.Default.MilitaryTech
        "Argento" -> Icons.Default.Shield
        else -> Icons.Default.Star
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = tierColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (badge.isUnlocked) tierIcon else Icons.Default.Lock,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = badge.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Grado ${badge.tier} • ${badge.category.label}",
                        fontSize = 11.sp,
                        color = tierColor
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = badge.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stato:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (badge.isUnlocked) "SBLOCCATO 🎉" else "IN PROGRESS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badge.isUnlocked) AvailGreen else RomanGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progresso Requisito:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${badge.currentProgress} / ${badge.targetProgress}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ricompensa Reputazione:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "+${badge.karmaReward} Punti Karma",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RomanGold
                            )
                        }
                    }
                }

                Text(
                    text = if (badge.isUnlocked) {
                        "Complimenti! Hai dimostrato grande senso civico aiutando la mobilità urbana di Roma."
                    } else {
                        "Suggerimento: Continua a segnalare stalli liberati o verifica le segnalazioni di altri utenti per sbloccare questo riconoscimento."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
            ) {
                Text("Ho capito", color = Color.White)
            }
        },
        containerColor = Color(0xFF1E2228),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ActivityRecordCard(
    record: UserActivityRecord,
    onNavigate: () -> Unit,
    onOpenInMaps: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ITALY) }
    val formattedDate = remember(record.timestampEpochMs) {
        val now = System.currentTimeMillis()
        val diffMinutes = (now - record.timestampEpochMs) / (60 * 1000L)
        when {
            diffMinutes < 60 -> "$diffMinutes min fa"
            diffMinutes < 1440 -> "${diffMinutes / 60} ore fa"
            else -> sdf.format(Date(record.timestampEpochMs))
        }
    }

    val typeColor = when (record.type) {
        UserActivityType.REPORTED_DEPARTURE -> TerracottaPrimary
        UserActivityType.REGISTERED_STAY -> RomanGold
        UserActivityType.VERIFIED_SPOT -> AvailGreen
        UserActivityType.FOUND_SPOT -> AvailGreen
        UserActivityType.COMMUNITY_BONUS -> RomanGold
    }

    val typeIcon = when (record.type) {
        UserActivityType.REPORTED_DEPARTURE -> Icons.Default.LocalParking
        UserActivityType.REGISTERED_STAY -> Icons.Default.HourglassTop
        UserActivityType.VERIFIED_SPOT -> Icons.Default.RateReview
        UserActivityType.FOUND_SPOT -> Icons.Default.CheckCircle
        UserActivityType.COMMUNITY_BONUS -> Icons.Default.EmojiEvents
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_card_${record.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type, Title, Points Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = typeColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = record.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Reward Points Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RomanGold.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RomanGold)
                ) {
                    Text(
                        text = "+${record.rewardPoints} pt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RomanGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Address Card with Smart Navigator and External Maps Actions
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = record.streetAddress,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = record.microAreaName,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Smart Navigator Button
                        Button(
                            onClick = onNavigate,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("open_maps_btn_${record.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Naviga",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // External Maps Option (Waze / Google Maps)
                        IconButton(
                            onClick = onOpenInMaps,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Apri in Maps o Waze",
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Notes & Impact Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "• ${record.statusNotes}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            if (record.impactMetric.isNotBlank()) {
                Text(
                    text = "🌱 ${record.impactMetric}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = AvailGreen
                )
            }
        }
    }
}

@Composable
private fun ImpactBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.width(64.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
