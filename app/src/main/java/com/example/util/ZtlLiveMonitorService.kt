package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.VehiclePermitType
import com.example.data.model.ZtlGate
import com.example.data.model.ZtlWarningAlert
import com.example.data.model.ZtlZone
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import java.util.TimeZone

/**
 * Service that continuously checks the current Rome local time and user GPS location
 * against ZTL (Zona a Traffico Limitato) schedules in Rome and triggers high-priority alerts
 * if the user is entering or approaching an active ZTL zone without permit authorization.
 */
object ZtlLiveMonitorService {
    private const val TAG = "ZtlLiveMonitorService"

    // Throttle alert intervals (e.g. alert once every 45 seconds per zone)
    private var lastAlertEpochMs: Long = 0
    private var lastAlertZoneId: String? = null
    private const val ALERT_THROTTLE_MS = 45_000L

    /**
     * Evaluates current user GPS coordinates and Rome time against active ZTL boundaries and gates.
     * Returns a high-priority ZtlWarningAlert if entering an unauthorized active zone, or null if safe.
     */
    fun evaluateZtlBreach(
        userLocation: LatLng,
        vehiclePermit: VehiclePermitType,
        isPermitActive: Boolean,
        context: Context?,
        forceAlert: Boolean = false,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
    ): ZtlWarningAlert? {
        val now = System.currentTimeMillis()

        for (zone in ZtlZone.ROMA_ZTL_ZONES) {
            val isRestricted = zone.isAccessRestrictedFor(vehiclePermit, calendar) ||
                (!isPermitActive && zone.isCurrentlyActive(calendar))

            if (!isRestricted) continue

            // 1. Check if user GPS location is inside the ZTL boundary polygon
            val isInside = zone.isPointInside(userLocation)

            // 2. Find nearest electronic camera gate
            var nearestGate: ZtlGate? = null
            var minGateDistance = Int.MAX_VALUE

            zone.gates.forEach { gate ->
                val dist = calculateDistanceMeters(
                    userLocation.latitude,
                    userLocation.longitude,
                    gate.latitude,
                    gate.longitude
                )
                if (dist < minGateDistance) {
                    minGateDistance = dist
                    nearestGate = gate
                }
            }

            // 3. Trigger condition: User is inside the polygon OR approaching within 250m of an active camera gate
            val isEntering = isInside || minGateDistance <= 250

            if (isEntering || forceAlert) {
                val distanceMeters = if (isInside) 0 else minGateDistance
                val activeSchedule = if (zone.isCurrentlyActive(calendar)) {
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    if (hour in 6..19) zone.dayActiveSchedule else zone.nightActiveSchedule
                } else {
                    zone.dayActiveSchedule
                }

                val warningAlert = ZtlWarningAlert(
                    zone = zone,
                    distanceMeters = distanceMeters,
                    isInside = isInside,
                    activeHours = activeSchedule,
                    permitUsed = vehiclePermit,
                    isPermitAuthorized = false,
                    timestampMs = now
                )

                // Dispatch high-priority system notification if not throttled
                val shouldNotify = forceAlert || (now - lastAlertEpochMs > ALERT_THROTTLE_MS) || (lastAlertZoneId != zone.id)
                if (shouldNotify) {
                    lastAlertEpochMs = now
                    lastAlertZoneId = zone.id
                    if (context != null) {
                        try {
                            ZtlNotificationHelper.sendZtlProximityNotification(
                                context = context,
                                zone = zone,
                                distanceMeters = distanceMeters,
                                activeHours = activeSchedule
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to send notification: ${e.message}")
                        }
                    }
                }

                return warningAlert
            }
        }

        return null
    }

    /**
     * Resets alert throttle state.
     */
    fun resetCooldown() {
        lastAlertEpochMs = 0
        lastAlertZoneId = null
    }

    /**
     * Distance in meters between coordinates.
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return (r * c).toInt()
    }
}
