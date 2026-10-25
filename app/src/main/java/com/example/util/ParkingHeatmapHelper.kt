package com.example.util

import android.graphics.Color
import com.example.data.model.DepartureSignal
import com.example.data.model.MicroArea
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng

enum class HeatmapMetric {
    SATURATION_DENSITY,   // High density = saturated parking / high congestion (Red/Orange)
    VACANCY_AVAILABILITY  // High density = available spots / easy parking (Green/Cyan)
}

object ParkingHeatmapHelper {

    // Classic thermal gradient for Parking Saturation / Congestion
    val SATURATION_GRADIENT: Gradient by lazy {
        val colors = intArrayOf(
            Color.argb(0, 76, 175, 80),      // Transparent Green (low congestion)
            Color.rgb(139, 195, 74),          // Light Green
            Color.rgb(255, 235, 59),          // Amber / Yellow (medium saturation)
            Color.rgb(255, 152, 0),           // Deep Orange (high saturation)
            Color.rgb(229, 57, 53),           // Red (critical saturation)
            Color.rgb(183, 28, 28)            // Dark Crimson (100% full / no spots)
        )
        val startPoints = floatArrayOf(0.0f, 0.20f, 0.45f, 0.68f, 0.88f, 1.0f)
        Gradient(colors, startPoints)
    }

    // Modern cool-to-warm gradient for Available Spots / Vacancies
    val VACANCY_GRADIENT: Gradient by lazy {
        val colors = intArrayOf(
            Color.argb(0, 33, 150, 243),     // Transparent Blue
            Color.rgb(38, 198, 218),          // Cyan
            Color.rgb(76, 175, 80),           // Green
            Color.rgb(255, 214, 0)            // Vivid Gold
        )
        val startPoints = floatArrayOf(0.0f, 0.35f, 0.70f, 1.0f)
        Gradient(colors, startPoints)
    }

    /**
     * Builds a collection of WeightedLatLng points representing real-time parking density
     * across the Termini–Esquilino micro-areas and adjacent street segments.
     */
    fun buildRealtimeDensityPoints(
        microAreas: List<MicroArea>,
        activeSignals: List<DepartureSignal> = emptyList(),
        metric: HeatmapMetric = HeatmapMetric.SATURATION_DENSITY
    ): List<WeightedLatLng> {
        if (microAreas.isEmpty()) return emptyList()

        val points = mutableListOf<WeightedLatLng>()

        microAreas.forEach { area ->
            val center = LatLng(area.latitude, area.longitude)

            // Calculate base intensity: 0.05 to 1.0
            val baseAvailability = area.baselineAvailability.coerceIn(0, 100)
            val hasActiveSignal = activeSignals.any { it.microAreaId == area.id }

            val intensity = when (metric) {
                HeatmapMetric.SATURATION_DENSITY -> {
                    // Saturation: 100 - availability. If someone is departing, saturation drops.
                    var sat = (100 - baseAvailability).toDouble()
                    if (hasActiveSignal) {
                        sat -= 25.0
                    }
                    if (area.activeSignalsCount > 0) {
                        sat -= (area.activeSignalsCount * 12.0)
                    }
                    (sat / 100.0).coerceIn(0.08, 1.0)
                }
                HeatmapMetric.VACANCY_AVAILABILITY -> {
                    // Vacancy: direct availability + departure boost
                    var vac = baseAvailability.toDouble()
                    if (hasActiveSignal) {
                        vac += 30.0
                    }
                    (vac / 100.0).coerceIn(0.08, 1.0)
                }
            }

            // Primary micro-area centroid weight
            points.add(WeightedLatLng(center, intensity))

            // Sub-points along the street segment to create realistic continuous density corridors
            val offsets = listOf(
                Pair(0.00030, 0.00025),
                Pair(-0.00030, -0.00025),
                Pair(0.00015, -0.00020),
                Pair(-0.00015, 0.00020)
            )
            offsets.forEach { (dLat, dLng) ->
                val subPos = LatLng(area.latitude + dLat, area.longitude + dLng)
                points.add(WeightedLatLng(subPos, intensity * 0.75))
            }
        }

        // Additional Termini arterial axis reference density points (Piazza dei Cinquecento, Giolitti, Marsala)
        val terminiPoints = listOf(
            WeightedLatLng(LatLng(41.9015, 12.5015), 0.90), // Cinquecento bus hub (very high saturation)
            WeightedLatLng(LatLng(41.9000, 12.5020), 0.85), // Central entrance
            WeightedLatLng(LatLng(41.8975, 12.5045), 0.70)  // Turati / Giolitti crossover
        )
        if (metric == HeatmapMetric.SATURATION_DENSITY) {
            points.addAll(terminiPoints)
        }

        return points
    }

    /**
     * Builds and configures a Google Maps HeatmapTileProvider with the desired parameters.
     */
    fun createHeatmapProvider(
        points: List<WeightedLatLng>,
        metric: HeatmapMetric = HeatmapMetric.SATURATION_DENSITY,
        radius: Int = 42,
        opacity: Double = 0.78
    ): HeatmapTileProvider? {
        if (points.isEmpty()) return null

        return HeatmapTileProvider.Builder()
            .weightedData(points)
            .radius(radius)
            .opacity(opacity)
            .gradient(if (metric == HeatmapMetric.SATURATION_DENSITY) SATURATION_GRADIENT else VACANCY_GRADIENT)
            .build()
    }
}
