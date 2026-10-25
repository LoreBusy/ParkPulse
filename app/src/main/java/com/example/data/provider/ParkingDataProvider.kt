package com.example.data.provider

import com.example.data.model.MicroArea
import com.example.data.model.MultiModalParkingOption
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

interface ParkingDataProvider {
    fun searchParkingAround(center: LatLng, radiusMeters: Double = 1500.0): List<MicroArea>
    fun calculateMultiModalTrip(
        parking: MicroArea,
        origin: LatLng,
        destination: LatLng
    ): MultiModalParkingOption
}

class DynamicParkingDataProvider : ParkingDataProvider {

    override fun searchParkingAround(center: LatLng, radiusMeters: Double): List<MicroArea> {
        // Generate structured, realistic parking zones around the queried location
        val offsets = listOf(
            Triple(0.0018, 0.0012, "Parcheggio in Struttura Multipiano"),
            Triple(-0.0015, 0.0020, "Strisce Blu Tariffa Ordinaria"),
            Triple(0.0022, -0.0018, "Stalli Sosta Gratuita (Strisce Bianche)"),
            Triple(-0.0025, -0.0015, "Area con Colonnina Ricarica EV e Sosta"),
            Triple(0.0008, 0.0030, "Garage Coperto Custodito H24"),
            Triple(-0.0032, 0.0005, "Parcheggio di Scambio con Trasporto Pubblico")
        )

        return offsets.mapIndexed { idx, (dLat, dLon, desc) ->
            val pLat = center.latitude + dLat
            val pLon = center.longitude + dLon
            val dist = computeDistanceMeters(center.latitude, center.longitude, pLat, pLon).toInt()
            
            val isEv = idx == 3
            val isFree = idx == 2
            val isGarage = idx == 0 || idx == 4
            val baseAvail = when (idx) {
                0 -> 72
                1 -> 45
                2 -> 20
                3 -> 80
                4 -> 65
                else -> 50
            }

            MicroArea(
                id = "dyn_park_${idx}_${(pLat * 1000).toInt()}_${(pLon * 1000).toInt()}",
                name = "${desc} (#${idx + 1})",
                streetName = "Via adiacente a ${String.format("%.4f, %.4f", center.latitude, center.longitude)}",
                latitude = pLat,
                longitude = pLon,
                distanceMeters = dist,
                radiusMeters = 50,
                baselineAvailability = baseAvail,
                blueLineTariff = if (isFree) "Gratuito (Disco orario 2h)" else if (isGarage) "€2,50/h (Custodito)" else "€1,50/h (08:00 - 20:00)",
                whiteLineSpots = if (isFree) 12 else 2,
                ztlZoneName = "Verifica varchi perimetrali",
                isZtlBoundary = false,
                cleaningSchedule = "Giovedì 00:00 - 06:00",
                normX = 0.5f + (dLon * 50).toFloat(),
                normY = 0.5f + (dLat * 50).toFloat(),
                activeSignalsCount = if (baseAvail > 60) 1 else 0,
                upcomingDeparturesCount = if (baseAvail < 30) 2 else 0,
                hasEvCharging = isEv,
                allowsLongStay = isGarage || !isFree,
                isEveningFree = !isGarage,
                availabilityNextHour = (baseAvail * 0.9).toInt(),
                availabilityAfter6Pm = (baseAvail * 1.2).coerceAtMost(95.0).toInt(),
                isPrivateGarage = isGarage,
                isAccessibleDisabled = true
            )
        }.filter { it.distanceMeters <= radiusMeters }
    }

    override fun calculateMultiModalTrip(
        parking: MicroArea,
        origin: LatLng,
        destination: LatLng
    ): MultiModalParkingOption {
        val driveDistMeters = computeDistanceMeters(origin.latitude, origin.longitude, parking.latitude, parking.longitude)
        val walkDistMeters = computeDistanceMeters(parking.latitude, parking.longitude, destination.latitude, destination.longitude)
        val directDistMeters = computeDistanceMeters(origin.latitude, origin.longitude, destination.latitude, destination.longitude)

        // Driving speed ~25 km/h in city traffic = 416 m/min
        val driveTimeMin = (driveDistMeters / 416.0).roundToInt().coerceAtLeast(2)
        val directDriveTimeMin = (directDistMeters / 416.0).roundToInt().coerceAtLeast(2)
        val detourMin = (driveTimeMin - directDriveTimeMin).coerceAtLeast(0)

        // Walking speed ~4.5 km/h = 75 m/min
        val walkTimeMin = (walkDistMeters / 75.0).roundToInt().coerceAtLeast(1)
        val totalTripMin = driveTimeMin + walkTimeMin

        // Recommendation score: penalize detour & long walk, favor high availability and free/cheap tariff
        val availScore = parking.baselineAvailability
        val penalty = (detourMin * 5) + (walkTimeMin * 4)
        val score = (availScore - penalty).coerceIn(10, 98)

        val reason = when {
            detourMin <= 1 && walkTimeMin <= 3 -> "Ottimale: minima deviazione (+${detourMin}m) e solo ${walkTimeMin} min a piedi"
            parking.baselineAvailability >= 70 -> "Alta disponibilità posti verificata (${parking.baselineAvailability}%) a ${walkTimeMin} min dalla meta"
            parking.whiteLineSpots > 5 -> "Stalli bianchi gratuiti disponibili (${parking.whiteLineSpots} posti) • ${walkTimeMin} min a piedi"
            parking.hasEvCharging -> "Colonnina ricarica EV disponibile a ${walkDistMeters.toInt()}m dalla destinazione"
            else -> "Comodo parcheggio alternativo a ${walkTimeMin} min a piedi"
        }

        return MultiModalParkingOption(
            parkingArea = parking,
            drivingTimeMinutes = driveTimeMin,
            detourMinutes = detourMin,
            walkingTimeMinutes = walkTimeMin,
            walkingDistanceMeters = walkDistMeters.toInt(),
            totalTripMinutes = totalTripMin,
            estimatedCost = parking.blueLineTariff,
            recommendationScore = score,
            recommendationReason = reason
        )
    }

    private fun computeDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
