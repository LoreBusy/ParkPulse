package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RomanGold
import com.example.ui.theme.TerracottaPrimary
import kotlinx.coroutines.launch

data class OnboardingStep(
    val titleIt: String,
    val titleEn: String,
    val descriptionIt: String,
    val descriptionEn: String,
    val icon: ImageVector,
    val accentColor: Color,
    val tag: String
)

@Composable
fun OnboardingWizardScreen(
    currentLanguage: String = "it",
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isItalian = currentLanguage == "it"
    val coroutineScope = rememberCoroutineScope()

    val steps = listOf(
        OnboardingStep(
            titleIt = "Parcheggi Smart nel Mondo",
            titleEn = "Global Smart Parking",
            descriptionIt = "Mappa termica dinamica in tempo reale. Trova stalli liberi a Roma, Milano, Parigi, Londra, New York e in tutto il mondo.",
            descriptionEn = "Dynamic real-time density heatmap. Find available spots in Rome, Milan, Paris, London, New York and worldwide.",
            icon = Icons.Default.Public,
            accentColor = TerracottaPrimary,
            tag = "step_global_map"
        ),
        OnboardingStep(
            titleIt = "ETA con Traffico Google Live",
            titleEn = "Live Google Traffic ETA",
            descriptionIt = "Calcola l'ora di arrivo esatta considerando rallentamenti e congestione prima di scegliere il tuo posto auto.",
            descriptionEn = "Calculate precise arrival time and driving delay based on real-time traffic before picking your spot.",
            icon = Icons.Default.Speed,
            accentColor = RomanGold,
            tag = "step_traffic_eta"
        ),
        OnboardingStep(
            titleIt = "Salvati & Sincronizzati Cloud",
            titleEn = "Saved & Cloud Synchronized",
            descriptionIt = "Salva le tue aree preferite e ritrovale su tutti i tuoi dispositivi grazie a Google Sign-In e Cloud Firestore.",
            descriptionEn = "Bookmark frequent parking spots and sync seamlessly across devices with Google Sign-In and Cloud Firestore.",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFFF5252),
            tag = "step_favorites"
        ),
        OnboardingStep(
            titleIt = "Community & Punti Sosta",
            titleEn = "Community & Rewards",
            descriptionIt = "Segnala quando stai per liberare un posto, guadagna punti e scala la classifica globale dei guidatori.",
            descriptionEn = "Broadcast when you are leaving a spot, earn karma points and climb the global driver leaderboard.",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFFFFB300),
            tag = "step_community"
        )
    )

    val pagerState = rememberPagerState(pageCount = { steps.size })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1015),
                        Color(0xFF131821),
                        Color(0xFF0A0D12)
                    )
                )
            )
            .testTag("onboarding_wizard_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TerracottaPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "ParkPulse",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("skip_onboarding_btn")
                ) {
                    Text(
                        text = if (isItalian) "Salta" else "Skip",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_pager")
            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hero Icon Circle with Glow
                    Surface(
                        shape = CircleShape,
                        color = step.accentColor.copy(alpha = 0.15f),
                        border = BorderStroke(2.dp, step.accentColor),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.accentColor,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = if (isItalian) step.titleIt else step.titleEn,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isItalian) step.descriptionIt else step.descriptionEn,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = Color.LightGray.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Bottom Navigation & Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(steps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .background(
                                    color = if (isSelected) TerracottaPrimary else Color(0xFF2E3846),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Action Buttons
                val isLastPage = pagerState.currentPage == steps.size - 1

                if (isLastPage) {
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("get_started_btn")
                    ) {
                        Text(
                            text = if (isItalian) "Inizia ad Esplorare" else "Get Started",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF333E4D)),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("onboarding_prev_btn")
                            ) {
                                Text(
                                    text = if (isItalian) "Indietro" else "Back",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("onboarding_next_btn")
                        ) {
                            Text(
                                text = if (isItalian) "Avanti" else "Next",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
