package com.example.data.model

import com.google.android.gms.maps.model.LatLng

data class ZtlRouteBreachWarning(
    val zoneId: String,
    val zoneName: String,
    val gateName: String,
    val licensePlate: String,
    val vehicleModel: String,
    val permitUsed: VehiclePermitType,
    val isPermitActive: Boolean,
    val activeSchedule: String,
    val fineInfo: String = "Sanzione CdS Art. 7: da 83€ a 332€ + spese notifica",
    val destinationName: String,
    val breachLocation: LatLng,
    val isRerouteAvailable: Boolean = true
)

data class HourlyTrendPoint(
    val hourOfDay: Int,          // 0 to 23
    val hourLabel: String,        // e.g. "08:00", "09:00"
    val availabilityPercent: Int, // 0 to 100%
    val turnoverRate: String,     // e.g. "Alta (25 min)", "Bassa (3+ ore)"
    val congestionLevel: String,  // e.g. "Critica", "Moderata", "Ottimale"
    val confidencePercent: Int,   // e.g. 92%
    val isPeakDifficulty: Boolean = false,
    val isOptimalWindow: Boolean = false,
    val occupancyPercent: Int = (100 - availabilityPercent).coerceIn(0, 100),
    val hoursAgo: Int = 0
)

data class MlParkingPrediction(
    val targetAreaId: String,
    val targetAreaName: String,
    val currentHour: Int,
    val dayOfWeek: String,
    val hourlyForecast: List<HourlyTrendPoint>,
    val peakDifficultyWindow: String,
    val optimalParkingWindow: String,
    val confidenceScore: Int,
    val analysisSummary: String,
    val recommendedAlternativeStreets: List<String>,
    val actionableDriverAdvice: String,
    val modelUsed: String,
    val isFromRemoteGemini: Boolean,
    val past24HoursOccupancy: List<HourlyTrendPoint> = emptyList(),
    val generatedTimestampMs: Long = System.currentTimeMillis()
)
