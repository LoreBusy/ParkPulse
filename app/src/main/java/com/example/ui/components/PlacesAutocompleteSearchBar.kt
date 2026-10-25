package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParkingTypeCategory
import com.example.data.model.PlacePrediction
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary

@Composable
fun PlacesAutocompleteSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    predictions: List<PlacePrediction>,
    isSearching: Boolean,
    selectedCategory: ParkingTypeCategory,
    onSelectCategory: (ParkingTypeCategory) -> Unit,
    onSelectPrediction: (PlacePrediction) -> Unit,
    onUseCurrentLocation: () -> Unit,
    currentLanguage: String = "it",
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isItalian = currentLanguage == "it"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("places_search_bar_container")
    ) {
        // Floating Search Input Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B222C)),
            border = BorderStroke(1.dp, Color(0xFF2E3B4D)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = RomanGold,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = if (isItalian) "Dove vuoi andare?" else "Where do you want to go?",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(TerracottaPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("places_search_input")
                    )
                }

                if (isSearching) {
                    CircularProgressIndicator(
                        color = TerracottaPrimary,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(24.dp).testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Current GPS Location Target Icon
                IconButton(
                    onClick = onUseCurrentLocation,
                    modifier = Modifier.size(28.dp).testTag("search_gps_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use GPS Location",
                        tint = RomanGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Dropdown Suggestions Overlay
        AnimatedVisibility(
            visible = query.isNotBlank() && predictions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A22)),
                border = BorderStroke(1.dp, Color(0xFF2C394A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .testTag("places_predictions_dropdown")
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((predictions.size * 56).coerceAtMost(220).dp)
                ) {
                    items(predictions, key = { it.placeId }) { pred ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    onSelectPrediction(pred)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (pred.isCity) RomanGold.copy(alpha = 0.2f) else TerracottaPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (pred.isCity) Icons.Default.LocationCity else Icons.Default.Place,
                                        contentDescription = null,
                                        tint = if (pred.isCity) RomanGold else TerracottaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pred.primaryText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = pred.secondaryText,
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Parking Type Filter Chips Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("parking_type_filter_row"),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ParkingTypeCategory.values()) { category ->
                val isSelected = selectedCategory == category
                val label = if (isItalian) category.labelIt else category.labelEn
                val icon = when (category) {
                    ParkingTypeCategory.ALL -> Icons.Default.LocalParking
                    ParkingTypeCategory.PUBLIC -> Icons.Default.DirectionsCar
                    ParkingTypeCategory.PRIVATE -> Icons.Default.Warehouse
                    ParkingTypeCategory.EV_CHARGING -> Icons.Default.ElectricCar
                    ParkingTypeCategory.ACCESSIBLE -> Icons.AutoMirrored.Filled.Accessible
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) TerracottaPrimary else Color(0xFF161C24).copy(alpha = 0.9f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) TerracottaPrimary else Color(0xFF2F3B4A)
                    ),
                    modifier = Modifier
                        .clickable { onSelectCategory(category) }
                        .testTag("filter_chip_${category.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else RomanGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
