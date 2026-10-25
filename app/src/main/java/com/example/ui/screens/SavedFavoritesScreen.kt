package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailabilityLevel
import com.example.data.model.MicroArea
import com.example.data.model.UserSettings
import com.example.ui.theme.AvailAmber
import com.example.ui.theme.AvailGreen
import com.example.ui.theme.AvailRed
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import com.example.util.NavigationHelper

@Composable
fun SavedFavoritesScreen(
    savedAreas: List<MicroArea>,
    userSettings: UserSettings,
    isCloudSynced: Boolean,
    onSelectArea: (MicroArea) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onStartNavigation: (MicroArea) -> Unit,
    onExploreMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isItalian = userSettings.language == "it"
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_favorites_screen")
    ) {
        // Section Header
        Surface(
            color = Color(0xFF131820),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isItalian) "I Tuoi Parcheggi Preferiti" else "Your Saved Favorites",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCloudSynced) Color(0xFF1B5E20).copy(alpha = 0.3f) else Color(0xFFE65100).copy(alpha = 0.25f),
                    border = BorderStroke(
                        1.dp,
                        if (isCloudSynced) Color(0xFF81C784) else RomanGold
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (isCloudSynced) Color(0xFF81C784) else RomanGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCloudSynced) (if (isItalian) "Firestore Sincronizzato" else "Cloud Synced")
                                   else (if (isItalian) "Salvataggio Locale" else "Local Storage"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCloudSynced) Color(0xFF81C784) else RomanGold
                        )
                    }
                }
            }
        }

        if (savedAreas.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = TerracottaPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, TerracottaPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isItalian) "Nessun parcheggio salvato" else "No saved favorites yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isItalian)
                        "Tocca l'icona del cuore sui parcheggi nella mappa o nell'elenco per salvarli e sincronizzarli su Cloud Firestore."
                    else
                        "Tap the heart icon on any parking spot on the map or list to save and sync them with Cloud Firestore.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onExploreMap,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("explore_map_from_saved_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isItalian) "Esplora la Mappa" else "Explore Map",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            // List of Saved Favorites
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("saved_favorites_list"),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedAreas, key = { it.id }) { area ->
                    val availColor = when (area.availabilityLevel) {
                        AvailabilityLevel.HIGH -> AvailGreen
                        AvailabilityLevel.MEDIUM -> AvailAmber
                        AvailabilityLevel.LOW -> AvailRed
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF171D25)),
                        border = BorderStroke(1.dp, Color(0xFF2B3645)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("favorite_card_${area.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Row: Name + Bookmark Remove
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = area.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${area.streetName} • ~${userSettings.formatDistance(area.distanceMeters)}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = availColor
                                    ) {
                                        Text(
                                            text = "${area.baselineAvailability}% Libero",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onToggleFavorite(area.id) },
                                        modifier = Modifier.size(30.dp).testTag("remove_fav_btn_${area.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Rimuovi dai preferiti",
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF1E2836)
                                ) {
                                    Text(
                                        text = userSettings.formatTariff(area.blueLineTariff),
                                        fontSize = 11.sp,
                                        color = Color(0xFF90CAF9),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (area.whiteLineSpots > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AvailGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${area.whiteLineSpots} bianche",
                                            fontSize = 11.sp,
                                            color = AvailGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (area.hasEvCharging) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = RomanGold.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ElectricCar,
                                                contentDescription = null,
                                                tint = RomanGold,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("EV", fontSize = 10.sp, color = RomanGold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        NavigationHelper.launchGoogleMaps(
                                            context = context,
                                            latitude = area.latitude,
                                            longitude = area.longitude,
                                            destinationName = area.name
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("nav_google_maps_fav_${area.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { onStartNavigation(area) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("smart_nav_fav_${area.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Smart Nav", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
