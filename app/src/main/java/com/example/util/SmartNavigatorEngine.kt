package com.example.util

import com.example.data.model.ActiveNavigationSession
import com.example.data.model.DepartureSignal
import com.example.data.model.ManeuverType
import com.example.data.model.NavigationStep
import com.example.data.model.VehiclePermitType
import com.example.data.model.ZtlGate
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

import com.example.data.model.ZtlRouteBreachWarning
import com.example.data.model.ZtlZone

object SmartNavigatorEngine {

    // Rome Termini Central Reference
    val TERMINI_CENTER = LatLng(41.8992, 12.5028)

    // Known Roman ZTL Gates
    private val ZTL_GATES = listOf(
        ZtlGate("Varco Via Cavour", "Via Cavour alt. Via Farini", 41.8978, 12.4984, "06:30 - 18:00 (Lun-Ven)", true),
        ZtlGate("Varco Via Giolitti", "Via Giolitti alt. Via di Santa Bibiana", 41.8960, 12.5090, "Vigile / Telecamera notturna", true),
        ZtlGate("Varco San Lorenzo (Tiburtina)", "Via Tiburtina alt. Piazzale Verano", 41.9008, 12.5180, "21:00 - 03:00 (Ven-Sab)", true)
    )

    fun checkRouteZtlBreach(
        origin: LatLng?,
        destinationName: String,
        destinationLocation: LatLng,
        avoidZtl: Boolean,
        permitType: VehiclePermitType,
        licensePlate: String,
        vehicleModel: String = "Fiat 500 Hybrid",
        isPermitActive: Boolean = true,
        calendar: java.util.Calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Rome"))
    ): ZtlRouteBreachWarning? {
        val start = origin ?: TERMINI_CENTER
        val waypoints = generateRomanWaypoints(start, destinationLocation, avoidZtl)

        // Check each official Rome ZTL zone
        for (zone in ZtlZone.ROMA_ZTL_ZONES) {
            val isRestricted = !isPermitActive || zone.isAccessRestrictedFor(permitType, calendar)
            if (!isRestricted) continue

            // Check if any waypoint on this planned path penetrates the restricted zone polygon or gate
            val breachPt = waypoints.firstOrNull { pt ->
                zone.isPointInside(pt) || zone.gates.any { gate ->
                    calculateDistanceMeters(pt, LatLng(gate.latitude, gate.longitude)) < 110
                }
            } ?: if (zone.isPointInside(destinationLocation)) destinationLocation else null

            if (breachPt != null) {
                val nearestGate = zone.gates.minByOrNull { gate ->
                    calculateDistanceMeters(breachPt, LatLng(gate.latitude, gate.longitude))
                }
                val gateName = nearestGate?.name ?: "Varco ZTL ${zone.name}"
                return ZtlRouteBreachWarning(
                    zoneId = zone.id,
                    zoneName = zone.name,
                    gateName = gateName,
                    licensePlate = licensePlate,
                    vehicleModel = vehicleModel,
                    permitUsed = permitType,
                    isPermitActive = isPermitActive,
                    activeSchedule = zone.dayActiveSchedule,
                    fineInfo = zone.fineInfo,
                    destinationName = destinationName,
                    breachLocation = breachPt,
                    isRerouteAvailable = true
                )
            }
        }
        return null
    }

    fun createRoute(
        origin: LatLng?,
        destinationId: String,
        destinationName: String,
        destinationAddress: String,
        destinationLocation: LatLng,
        parkingProbability: Int = 85,
        avoidZtl: Boolean = true,
        permitType: VehiclePermitType = VehiclePermitType.NONE,
        licensePlate: String = "RM 782 ED",
        vehicleModel: String = "Fiat 500 Hybrid",
        isPermitActive: Boolean = true,
        activeSignals: List<DepartureSignal> = emptyList()
    ): ActiveNavigationSession {
        val start = origin ?: TERMINI_CENTER

        // Compute simulated intermediate waypoints respecting Roman urban grid
        val waypoints = generateRomanWaypoints(start, destinationLocation, avoidZtl)
        val steps = generateNavigationSteps(waypoints, destinationName, destinationAddress, avoidZtl)

        val totalDist = steps.sumOf { it.distanceMeters }
        val totalSec = steps.sumOf { it.durationSeconds }

        // Find upcoming ZTL gate warning if within 500m
        val upcomingGate = if (avoidZtl) {
            ZTL_GATES.firstOrNull { gate ->
                calculateDistanceMeters(start, LatLng(gate.latitude, gate.longitude)) < 500
            }
        } else null

        // Check if route breaches unauthorized ZTL
        val breachWarning = checkRouteZtlBreach(
            origin = start,
            destinationName = destinationName,
            destinationLocation = destinationLocation,
            avoidZtl = avoidZtl,
            permitType = permitType,
            licensePlate = licensePlate,
            vehicleModel = vehicleModel,
            isPermitActive = isPermitActive
        )

        // Check if a community free spot alert exists near the route
        val nearbyFreeSpot = activeSignals.firstOrNull { sig ->
            waypoints.any { pt ->
                calculateDistanceMeters(pt, LatLng(sig.latitude, sig.longitude)) < 280
            }
        }

        return ActiveNavigationSession(
            destinationId = destinationId,
            destinationName = destinationName,
            destinationAddress = destinationAddress,
            destinationLocation = destinationLocation,
            totalDistanceMeters = totalDist,
            initialDurationSeconds = totalSec,
            routePolyline = waypoints,
            steps = steps,
            currentStepIndex = 0,
            remainingDistanceMeters = totalDist,
            remainingDurationSeconds = totalSec,
            currentSpeedKmh = 38,
            currentVehicleLocation = start,
            isVoiceEnabled = true,
            avoidZtlEnabled = avoidZtl,
            permitType = permitType,
            licensePlate = licensePlate,
            isUnauthorizedZtlRouteBreach = breachWarning != null,
            unauthorizedZtlBreachZoneName = breachWarning?.zoneName,
            upcomingZtlGateWarning = upcomingGate,
            ztlCameraCountdownMeters = if (upcomingGate != null) 240 else null,
            destinationParkingProbability = parkingProbability,
            liveFreeSpotNearbyAlert = nearbyFreeSpot,
            isArrived = false
        )
    }

    private fun generateRomanWaypoints(
        start: LatLng,
        dest: LatLng,
        avoidZtl: Boolean
    ): List<LatLng> {
        val points = mutableListOf<LatLng>()
        points.add(start)

        // Mid-point calculations around Termini / Esquilino grid
        val dLat = dest.latitude - start.latitude
        val dLng = dest.longitude - start.longitude

        if (avoidZtl) {
            // Anti-ZTL bypass corridors (staying outside Via Cavour / Santa Maria Maggiore restricted perimeter)
            // Corridor along Via Marsala / Castro Pretorio or Via Giolitti esterno
            val corridorPt1 = LatLng(start.latitude + (dLat * 0.25), start.longitude + (dLng * 0.15))
            val safeDetourPt = LatLng(start.latitude + (dLat * 0.55), start.longitude + (dLng * 0.45) + 0.0008)
            val corridorPt2 = LatLng(start.latitude + (dLat * 0.85), start.longitude + (dLng * 0.80))
            points.add(corridorPt1)
            points.add(safeDetourPt)
            points.add(corridorPt2)
        } else {
            val mid1 = LatLng(start.latitude + (dLat * 0.33), start.longitude + (dLng * 0.25))
            val mid2 = LatLng(start.latitude + (dLat * 0.66), start.longitude + (dLng * 0.75))
            points.add(mid1)
            points.add(mid2)
        }

        points.add(dest)
        return points
    }

    private fun generateNavigationSteps(
        waypoints: List<LatLng>,
        destName: String,
        destAddress: String,
        avoidZtl: Boolean
    ): List<NavigationStep> {
        val steps = mutableListOf<NavigationStep>()

        // Step 1: Depart
        steps.add(
            NavigationStep(
                stepIndex = 0,
                maneuver = ManeuverType.DEPART,
                instruction = "Parti da Piazza dei Cinquecento verso Via Giovanni Giolitti",
                streetName = "Piazza dei Cinquecento",
                distanceMeters = 140,
                durationSeconds = 25,
                waypoint = waypoints.first(),
                voiceInstruction = "Parti verso Via Giovanni Giolitti. Limite di velocità cinquanta chilometri orari.",
                roadSpeedLimitKmh = 50,
                roadHazardNote = "Attenzione pedoni nei pressi della Stazione Termini"
            )
        )

        // Step 2: Turn Right
        steps.add(
            NavigationStep(
                stepIndex = 1,
                maneuver = ManeuverType.TURN_RIGHT,
                instruction = "Tra 120m gira a destra in Via Giovanni Giolitti",
                streetName = "Via Giovanni Giolitti",
                distanceMeters = 380,
                durationSeconds = 60,
                waypoint = waypoints.getOrElse(1) { waypoints.first() },
                voiceInstruction = "Svolta a destra in Via Giovanni Giolitti, poi prosegui per trecentottanta metri.",
                roadSpeedLimitKmh = 50,
                roadHazardNote = "Corsia preferenziale tram attiva sulla sinistra"
            )
        )

        // Step 3: Anti-ZTL Detour or Continue
        if (avoidZtl) {
            steps.add(
                NavigationStep(
                    stepIndex = 2,
                    maneuver = ManeuverType.AVOID_ZTL_DETOUR,
                    instruction = "Devia a sinistra: percorso ZigZag anti-varco ZTL Via Volturno",
                    streetName = "Via di Santa Bibiana / Esquilino",
                    distanceMeters = 290,
                    durationSeconds = 45,
                    waypoint = waypoints.getOrElse(2) { waypoints.last() },
                    voiceInstruction = "Attenzione: deviazione a sinistra. Varco ZTL attivo evitato con successo. Nessun rischio multa.",
                    isAntiZtlSafeManeuver = true,
                    nearbyZtlGateWarning = ZTL_GATES.firstOrNull(),
                    roadSpeedLimitKmh = 30,
                    roadHazardNote = "🛡️ Varco ZTL evitato: risparmiati 83€ di sanzione"
                )
            )
        } else {
            steps.add(
                NavigationStep(
                    stepIndex = 2,
                    maneuver = ManeuverType.CONTINUE_STRAIGHT,
                    instruction = "Prosegui dritto su Via Principe Amedeo",
                    streetName = "Via Principe Amedeo",
                    distanceMeters = 320,
                    durationSeconds = 50,
                    waypoint = waypoints.getOrElse(2) { waypoints.last() },
                    voiceInstruction = "Prosegui dritto su Via Principe Amedeo per trecento metri.",
                    roadSpeedLimitKmh = 50,
                    roadHazardNote = "Buca sampietrini segnalata a bordo carreggiata"
                )
            )
        }

        // Step 4: Turn towards final street
        steps.add(
            NavigationStep(
                stepIndex = steps.size,
                maneuver = ManeuverType.TURN_LEFT,
                instruction = "Svolta a sinistra verso $destAddress",
                streetName = destAddress,
                distanceMeters = 180,
                durationSeconds = 35,
                waypoint = waypoints.getOrElse(waypoints.size - 2) { waypoints.last() },
                voiceInstruction = "Svolta a sinistra verso $destAddress in avvicinamento al parcheggio.",
                roadSpeedLimitKmh = 30,
                roadHazardNote = "Stalli strisce blu e bianche con rotazione rapida"
            )
        )

        // Step 5: Arrival Destination
        steps.add(
            NavigationStep(
                stepIndex = steps.size,
                maneuver = ManeuverType.ARRIVE_DESTINATION,
                instruction = "Sei arrivato a destinazione: $destName",
                streetName = destAddress,
                distanceMeters = 60,
                durationSeconds = 15,
                waypoint = waypoints.last(),
                voiceInstruction = "Sei arrivato a destinazione in $destName. Verifica lo stallo libero e conferma la sosta.",
                roadSpeedLimitKmh = 30,
                roadHazardNote = "Stallo di sosta disponibile rilevato dalla community"
            )
        )

        return steps
    }

    fun advanceStep(
        session: ActiveNavigationSession,
        activeSignals: List<DepartureSignal>
    ): ActiveNavigationSession {
        val nextIndex = session.currentStepIndex + 1
        if (nextIndex >= session.steps.size) {
            // Arrived at destination!
            return session.copy(
                currentStepIndex = session.steps.lastIndex,
                remainingDistanceMeters = 0,
                remainingDurationSeconds = 0,
                currentSpeedKmh = 0,
                isArrived = true,
                currentVehicleLocation = session.destinationLocation
            )
        }

        val remainingSteps = session.steps.drop(nextIndex)
        val remDist = remainingSteps.sumOf { it.distanceMeters }
        val remSec = remainingSteps.sumOf { it.durationSeconds }
        val nextStep = session.steps[nextIndex]

        // Check if there is an active departure signal near this step
        val nearbySpot = activeSignals.firstOrNull { sig ->
            calculateDistanceMeters(nextStep.waypoint, LatLng(sig.latitude, sig.longitude)) < 260
        }

        return session.copy(
            currentStepIndex = nextIndex,
            remainingDistanceMeters = remDist,
            remainingDurationSeconds = remSec,
            currentVehicleLocation = nextStep.waypoint,
            currentSpeedKmh = if (nextStep.maneuver == ManeuverType.ARRIVE_DESTINATION) 12 else (30..48).random(),
            liveFreeSpotNearbyAlert = nearbySpot,
            isArrived = false
        )
    }

    fun rerouteAvoidZtl(
        session: ActiveNavigationSession,
        avoidZtl: Boolean,
        activeSignals: List<DepartureSignal>
    ): ActiveNavigationSession {
        return createRoute(
            origin = session.currentVehicleLocation,
            destinationId = session.destinationId,
            destinationName = session.destinationName,
            destinationAddress = session.destinationAddress,
            destinationLocation = session.destinationLocation,
            parkingProbability = session.destinationParkingProbability,
            avoidZtl = avoidZtl,
            permitType = session.permitType,
            licensePlate = session.licensePlate,
            activeSignals = activeSignals
        )
    }

    fun rerouteToFreeSpot(
        session: ActiveNavigationSession,
        spotSignal: DepartureSignal
    ): ActiveNavigationSession {
        return createRoute(
            origin = session.currentVehicleLocation,
            destinationId = spotSignal.id,
            destinationName = spotSignal.microAreaName,
            destinationAddress = spotSignal.streetName,
            destinationLocation = LatLng(spotSignal.latitude, spotSignal.longitude),
            parkingProbability = 96,
            avoidZtl = session.avoidZtlEnabled,
            permitType = session.permitType,
            licensePlate = session.licensePlate,
            activeSignals = emptyList()
        )
    }

    fun calculateDistanceMeters(pos1: LatLng, pos2: LatLng): Int {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(pos2.latitude - pos1.latitude)
        val dLng = Math.toRadians(pos2.longitude - pos1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(pos1.latitude)) * cos(Math.toRadians(pos2.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toInt()
    }
}
