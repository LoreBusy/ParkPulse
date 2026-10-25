package com.example.data.model

import com.google.android.gms.maps.model.LatLng

enum class ManeuverType(val label: String) {
    DEPART("Parti"),
    CONTINUE_STRAIGHT("Prosegui dritto"),
    TURN_RIGHT("Svolta a destra"),
    TURN_LEFT("Svolta a sinistra"),
    SLIGHT_RIGHT("Tieni la destra"),
    SLIGHT_LEFT("Tieni la sinistra"),
    ROUNDABOUT("Entra nella rotatoria"),
    AVOID_ZTL_DETOUR("Deviazione Anti-Varco ZTL"),
    ARRIVE_DESTINATION("Sei arrivato a destinazione")
}

data class NavigationStep(
    val stepIndex: Int,
    val maneuver: ManeuverType,
    val instruction: String,
    val streetName: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val waypoint: LatLng,
    val voiceInstruction: String,
    val nearbyZtlGateWarning: ZtlGate? = null,
    val isAntiZtlSafeManeuver: Boolean = false,
    val roadSpeedLimitKmh: Int = 50,
    val roadHazardNote: String? = null
)

data class ActiveNavigationSession(
    val destinationId: String,
    val destinationName: String,
    val destinationAddress: String,
    val destinationLocation: LatLng,
    val totalDistanceMeters: Int,
    val initialDurationSeconds: Int,
    val routePolyline: List<LatLng>,
    val steps: List<NavigationStep>,
    val currentStepIndex: Int = 0,
    val remainingDistanceMeters: Int,
    val remainingDurationSeconds: Int,
    val currentSpeedKmh: Int = 34,
    val currentVehicleLocation: LatLng,
    val isVoiceEnabled: Boolean = true,
    val avoidZtlEnabled: Boolean = true,
    val permitType: VehiclePermitType = VehiclePermitType.NONE,
    val licensePlate: String = "RM 782 ED",
    val isUnauthorizedZtlRouteBreach: Boolean = false,
    val unauthorizedZtlBreachZoneName: String? = null,
    val upcomingZtlGateWarning: ZtlGate? = null,
    val ztlCameraCountdownMeters: Int? = null,
    val destinationParkingProbability: Int = 85,
    val liveFreeSpotNearbyAlert: DepartureSignal? = null,
    val isArrived: Boolean = false
) {
    val currentStep: NavigationStep?
        get() = steps.getOrNull(currentStepIndex)

    val nextStep: NavigationStep?
        get() = steps.getOrNull(currentStepIndex + 1)

    val formattedRemainingDuration: String
        get() {
            val mins = (remainingDurationSeconds / 60).coerceAtLeast(1)
            return if (mins >= 60) "${mins / 60} h ${mins % 60} min" else "$mins min"
        }

    val formattedRemainingDistance: String
        get() = if (remainingDistanceMeters >= 1000) {
            String.format(java.util.Locale.ITALY, "%.1f km", remainingDistanceMeters / 1000f)
        } else {
            "$remainingDistanceMeters m"
        }
}
