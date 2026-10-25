package com.example.data.model

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TrafficCondition(val labelEn: String, val labelIt: String) {
    SMOOTH("Smooth Traffic", "Traffico Scorrevole"),
    MODERATE("Moderate Delays", "Traffico Rallentato"),
    HEAVY("Heavy Congestion", "Forte Congestione")
}

data class TrafficEtaResult(
    val destinationId: String,
    val destinationName: String,
    val origin: LatLng,
    val destination: LatLng,
    val distanceMeters: Int,
    val distanceFormatted: String,
    val normalDurationMinutes: Int,
    val trafficDurationMinutes: Int,
    val trafficDelayMinutes: Int,
    val condition: TrafficCondition,
    val estimatedArrivalEpochMs: Long,
    val estimatedArrivalTimeFormatted: String,
    val routeSummary: String
) {
    companion object {
        fun calculate(
            origin: LatLng,
            destination: LatLng,
            destinationId: String,
            destinationName: String,
            simulatedTrafficMultiplier: Double = 1.0,
            baseSpeedKmH: Double = 32.0
        ): TrafficEtaResult {
            val distMeters = calculateHaversineDistance(origin, destination)
            val distKm = distMeters / 1000.0

            // Distance formatted
            val distFormatted = if (distMeters < 1000) {
                "${distMeters} m"
            } else {
                String.format(Locale.US, "%.1f km", distKm)
            }

            // Normal driving minutes (average urban speed 32 km/h)
            val normalMinutes = ((distKm / baseSpeedKmH) * 60).toInt().coerceAtLeast(2)

            // Factor in real-world time-of-day traffic congestion
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val isRushHour = (hour in 8..10) || (hour in 17..20)
            val trafficFactor = when {
                isRushHour -> 1.45 * simulatedTrafficMultiplier
                hour in 11..16 -> 1.15 * simulatedTrafficMultiplier
                else -> 1.02 * simulatedTrafficMultiplier
            }

            val trafficMinutes = (normalMinutes * trafficFactor).toInt().coerceAtLeast(normalMinutes)
            val delayMinutes = (trafficMinutes - normalMinutes).coerceAtLeast(0)

            val condition = when {
                delayMinutes >= 8 -> TrafficCondition.HEAVY
                delayMinutes >= 3 -> TrafficCondition.MODERATE
                else -> TrafficCondition.SMOOTH
            }

            val arrivalEpochMs = System.currentTimeMillis() + (trafficMinutes * 60 * 1000L)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val arrivalFormatted = sdf.format(Date(arrivalEpochMs))

            val summary = when (condition) {
                TrafficCondition.SMOOTH -> "Scorrevole (+0 min)"
                TrafficCondition.MODERATE -> "Rallentamenti urbani (+${delayMinutes} min)"
                TrafficCondition.HEAVY -> "Coda e forte traffico (+${delayMinutes} min)"
            }

            return TrafficEtaResult(
                destinationId = destinationId,
                destinationName = destinationName,
                origin = origin,
                destination = destination,
                distanceMeters = distMeters,
                distanceFormatted = distFormatted,
                normalDurationMinutes = normalMinutes,
                trafficDurationMinutes = trafficMinutes,
                trafficDelayMinutes = delayMinutes,
                condition = condition,
                estimatedArrivalEpochMs = arrivalEpochMs,
                estimatedArrivalTimeFormatted = arrivalFormatted,
                routeSummary = summary
            )
        }

        private fun calculateHaversineDistance(start: LatLng, end: LatLng): Int {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(end.latitude - start.latitude)
            val dLng = Math.toRadians(end.longitude - start.longitude)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return (earthRadius * c).toInt()
        }
    }
}
