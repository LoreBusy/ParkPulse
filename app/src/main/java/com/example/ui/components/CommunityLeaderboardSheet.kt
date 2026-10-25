package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.data.model.Badge
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityLeaderboardSheet(
    userProfile: UserProfile,
    leaderboardEntries: List<LeaderboardEntry>,
    currentLanguage: String = "it",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isItalian = currentLanguage == "it"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141922),
        modifier = modifier.testTag("community_leaderboard_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isItalian) "Classifica Globale & Badge" else "Driver Leaderboard & Badges",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isItalian) "Contribuisci segnalando posti e guadagna punti" else "Report free spots, earn karma & unlock ranks",
                            fontSize = 11.sp,
                            color = RomanGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Hero Contribution Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C232E)),
                border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("user_contribution_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = TerracottaPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = userProfile.username,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = userProfile.levelRank,
                                    fontSize = 11.sp,
                                    color = RomanGold
                                )
                            }
                        }

                        // Points Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RomanGold.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, RomanGold.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "${userProfile.points} PT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = RomanGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            label = if (isItalian) "Affidabilità" else "Karma Trust",
                            value = "${userProfile.karmaScore}%",
                            color = AvailGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = if (isItalian) "Segnalazioni" else "Shared",
                            value = "${userProfile.signalsSharedCount}",
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = if (isItalian) "Verificate" else "Verified",
                            value = "${userProfile.signalsVerifiedCount}",
                            color = TerracottaPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badges Showcase
                    Text(
                        text = if (isItalian) "I Tuoi Badge Sbloccati:" else "Unlocked Badges:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(userProfile.badges) { badge ->
                            BadgePill(badge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isItalian) "Top Driver Mondiali (Firestore Live):" else "Global Leaderboard (Firestore Live):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Leaderboard List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .testTag("leaderboard_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(leaderboardEntries, key = { it.userId }) { entry ->
                    val isTop3 = entry.rank <= 3
                    val rankColor = when (entry.rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> Color.LightGray
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.isCurrentUser) TerracottaPrimary.copy(alpha = 0.22f) else Color(0xFF19202A)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (entry.isCurrentUser) TerracottaPrimary else if (isTop3) rankColor.copy(alpha = 0.6f) else Color(0xFF283342)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("leaderboard_row_${entry.rank}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Rank Badge
                                Surface(
                                    shape = CircleShape,
                                    color = if (isTop3) rankColor.copy(alpha = 0.2f) else Color(0xFF242E3B),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${entry.rank}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isTop3) rankColor else Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.username,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (entry.isCurrentUser) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = TerracottaPrimary
                                            ) {
                                                Text(
                                                    text = if (isItalian) "TU" else "YOU",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${entry.badgeTitle} • Trust ${entry.karmaScore}%",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Text(
                                text = "${entry.points} pt",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = RomanGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131820),
        border = BorderStroke(1.dp, Color(0xFF263242)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun BadgePill(badge: Badge) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (badge.isUnlocked) RomanGold.copy(alpha = 0.15f) else Color(0xFF161C24),
        border = BorderStroke(1.dp, if (badge.isUnlocked) RomanGold else Color(0xFF2C3746))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (badge.isUnlocked) Icons.Default.Verified else Icons.Default.MilitaryTech,
                contentDescription = null,
                tint = if (badge.isUnlocked) RomanGold else Color.Gray,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badge.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (badge.isUnlocked) Color.White else Color.Gray
            )
        }
    }
}
