package com.example.data.model

import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class VehiclePermitType(
    val id: String,
    val label: String,
    val shortName: String,
    val description: String,
    val exemptsFromAllZtl: Boolean = false,
    val allowedZoneIds: Set<String> = emptySet()
) {
    NONE(
        id = "none",
        label = "Nessun Permesso (Standard)",
        shortName = "No Permesso",
        description = "Soggetto a tutti i varchi elettronici e divieti ZTL"
    ),
    ELECTRIC_VEHICLE(
        id = "electric",
        label = "Veicolo 100% Elettrico (Accesso Libero)",
        shortName = "Elettrico (Libero)",
        description = "Transito e sosta libera h24 in tutte le ZTL di Roma Capitale",
        exemptsFromAllZtl = true
    ),
    RESIDENT_ESQUILINO_CENTRO(
        id = "resident_b",
        label = "Permesso Residente Settore B / Centro",
        shortName = "Residente Sett. B",
        description = "Accesso autorizzato in ZTL Centro Storico e quadrante Termini",
        allowedZoneIds = setOf("centro_storico")
    ),
    DISABLED_BADGE(
        id = "cude",
        label = "Contrassegno Disabili (CUDE)",
        shortName = "CUDE / Invalidi",
        description = "Accesso consentito a tutte le ZTL e corsie preferenziali",
        exemptsFromAllZtl = true
    ),
    MOTORCYCLE(
        id = "moto",
        label = "Moto / Scooter (2 Ruote)",
        shortName = "Moto / Scooter",
        description = "Accesso libero a ZTL diurna e notturna (escluso A1 Tridente)",
        allowedZoneIds = setOf("centro_storico", "trastevere", "san_lorenzo", "testaccio")
    )
}

enum class DepartureSliderMode(val label: String, val description: String) {
    FREE_SPOT_WINDOW(
        label = "Posto libero per",
        description = "Indica per quanti minuti stimi che il posto rimanga libero per chi cerca"
    ),
    OCCUPY_UNTIL_DEPARTURE(
        label = "Lo occuperò ancora per",
        description = "Programma la partenza: indica tra quanto tempo libererai il posto"
    )
}

data class ZtlWarningAlert(
    val zone: ZtlZone,
    val distanceMeters: Int,
    val isInside: Boolean,
    val activeHours: String,
    val permitUsed: VehiclePermitType = VehiclePermitType.NONE,
    val isPermitAuthorized: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

data class ZtlZone(
    val id: String,
    val name: String,
    val description: String,
    val polygonPoints: List<LatLng>,
    val dayActiveSchedule: String,
    val nightActiveSchedule: String,
    val fineInfo: String = "Multa da 83€ a 332€ per transito non autorizzato (Art. 7 CdS)",
    val gates: List<ZtlGate> = emptyList()
) {
    /**
     * Determines whether access is restricted for the specific vehicle permit.
     */
    fun isAccessRestrictedFor(
        permit: VehiclePermitType,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
    ): Boolean {
        if (permit.exemptsFromAllZtl) return false
        if (permit.allowedZoneIds.contains(id)) return false
        return isCurrentlyActive(calendar)
    }

    /**
     * Determines whether this ZTL zone is active right now based on Rome local time.
     */
    fun isCurrentlyActive(calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ... 7 = Saturday
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        return when (id) {
            "centro_storico" -> {
                // Diurna: Lun-Ven 06:30 - 18:00 (390..1080), Sab 14:00 - 18:00 (840..1080)
                // Notturna: Ven-Sab 23:00 - 03:00 (1380..1440 or 0..180)
                val isWeekdayDay = (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) && (timeInMinutes in 390..1080)
                val isSaturdayDay = (dayOfWeek == Calendar.SATURDAY) && (timeInMinutes in 840..1080)
                val isFridayNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1380) ||
                    (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
                val isSaturdayNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1380) ||
                    (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

                isWeekdayDay || isSaturdayDay || isFridayNight || isSaturdayNight
            }
            "trastevere" -> {
                // Diurna: Lun-Sab 06:30 - 10:00 (390..600)
                // Notturna: Ven-Sab 21:30 - 03:00 (1290..1440 or 0..180)
                val isDay = (dayOfWeek in Calendar.MONDAY..Calendar.SATURDAY) && (timeInMinutes in 390..600)
                val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1290) ||
                    (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
                val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1290) ||
                    (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

                isDay || isFriNight || isSatNight
            }
            "san_lorenzo" -> {
                // Notturna: Ven-Sab 21:00 - 03:00 (1260..1440 or 0..180)
                val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1260) ||
                    (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
                val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1260) ||
                    (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

                isFriNight || isSatNight
            }
            "testaccio" -> {
                // Notturna: Ven-Sab 23:00 - 03:00 (1380..1440 or 0..180)
                val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1380) ||
                    (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
                val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1380) ||
                    (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

                isFriNight || isSatNight
            }
            else -> false
        }
    }

    /**
     * Ray-casting algorithm to test whether a given LatLng is inside the polygon.
     */
    fun isPointInside(point: LatLng): Boolean {
        var intersectCount = 0
        val n = polygonPoints.size
        for (i in 0 until n) {
            val p1 = polygonPoints[i]
            val p2 = polygonPoints[(i + 1) % n]
            if ((p1.latitude > point.latitude) != (p2.latitude > point.latitude)) {
                val xIntersection = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) /
                    (p2.latitude - p1.latitude) + p1.longitude
                if (point.longitude < xIntersection) {
                    intersectCount++
                }
            }
        }
        return (intersectCount % 2) != 0
    }

    /**
     * Calculates the minimum distance in meters between a LatLng and the polygon boundary/gates.
     */
    fun distanceToZoneMeters(point: LatLng): Double {
        if (isPointInside(point)) return 0.0

        var minDistance = Double.MAX_VALUE
        // Distance to polygon vertices
        for (vertex in polygonPoints) {
            val d = haversineDistanceMeters(point.latitude, point.longitude, vertex.latitude, vertex.longitude)
            if (d < minDistance) minDistance = d
        }
        // Also check distance to gates
        for (gate in gates) {
            val d = haversineDistanceMeters(point.latitude, point.longitude, gate.latitude, gate.longitude)
            if (d < minDistance) minDistance = d
        }
        return minDistance
    }

    companion object {
        fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        val ROMA_ZTL_ZONES: List<ZtlZone> = listOf(
            ZtlZone(
                id = "centro_storico",
                name = "ZTL Centro Storico",
                description = "Zona a Traffico Limitato centrale (Tridente, Navona, Colosseo, Monti). Confine orientale a Via Cavour.",
                polygonPoints = listOf(
                    LatLng(41.9105, 12.4760), // Piazza del Popolo
                    LatLng(41.9065, 12.4835), // Trinità dei Monti
                    LatLng(41.9040, 12.4890), // Via del Tritone
                    LatLng(41.8975, 12.4895), // Via Nazionale / Largo Magnanapoli
                    LatLng(41.8968, 12.4935), // Via Cavour / S. Maria Maggiore boundary
                    LatLng(41.8905, 12.4925), // Colosseo / Celio
                    LatLng(41.8860, 12.4855), // Circo Massimo
                    LatLng(41.8885, 12.4795), // Bocca della Verità / Lungotevere
                    LatLng(41.8935, 12.4715), // Lungotevere dei Tebaldi / Ponte Sisto
                    LatLng(41.9030, 12.4725), // Lungotevere Marzio
                    LatLng(41.9080, 12.4740)  // Lungotevere in Augusta
                ),
                dayActiveSchedule = "Lun-Ven 06:30 - 18:00 | Sab 14:00 - 18:00",
                nightActiveSchedule = "Ven-Sab 23:00 - 03:00",
                gates = listOf(
                    ZtlGate("Varco Via Cavour", "Via Cavour alt. Via Farini", 41.8978, 12.4984, "06:30 - 18:00 (Lun-Ven), 14:00 - 18:00 (Sab)", true),
                    ZtlGate("Varco Via Nazionale", "Via Nazionale alt. Via Milano", 41.8990, 12.4910, "06:30 - 18:00 (Lun-Ven)", true),
                    ZtlGate("Varco Ripetta", "Via di Ripetta alt. Passeggiata di Ripetta", 41.9080, 12.4745, "06:30 - 18:00", true),
                    ZtlGate("Varco Teatro Marcello", "Via del Teatro di Marcello", 41.8920, 12.4800, "06:30 - 18:00", true)
                )
            ),
            ZtlZone(
                id = "trastevere",
                name = "ZTL Trastevere",
                description = "Rione Trastevere e Lungotevere interno. Attiva la mattina presto e notturna nei weekend.",
                polygonPoints = listOf(
                    LatLng(41.8930, 12.4700), // Ponte Sisto
                    LatLng(41.8905, 12.4750), // Ponte Garibaldi / P.za Sonnino
                    LatLng(41.8860, 12.4740), // Viale Trastevere / Piazza Mastai
                    LatLng(41.8825, 12.4715), // Porta Portese
                    LatLng(41.8840, 12.4645), // Gianicolo
                    LatLng(41.8920, 12.4645)  // Via della Lungara
                ),
                dayActiveSchedule = "Lun-Sab 06:30 - 10:00",
                nightActiveSchedule = "Ven-Sab 21:30 - 03:00",
                gates = listOf(
                    ZtlGate("Varco Trastevere (P.za Belli)", "Piazza Belli / Ponte Garibaldi", 41.8890, 12.4740, "06:30 - 10:00 / 21:30 - 03:00", true),
                    ZtlGate("Varco Porta Portese", "Via Portuense alt. Porta Portese", 41.8830, 12.4710, "Notturna Ven-Sab", true)
                )
            ),
            ZtlZone(
                id = "san_lorenzo",
                name = "ZTL San Lorenzo",
                description = "Quartiere San Lorenzo / Università La Sapienza. Attiva nelle ore notturne del fine settimana.",
                polygonPoints = listOf(
                    LatLng(41.9015, 12.5115), // P.le Tiburtino
                    LatLng(41.9030, 12.5210), // P.le del Verano
                    LatLng(41.8965, 12.5235), // Scalo San Lorenzo
                    LatLng(41.8950, 12.5140), // Porta Maggiore
                    LatLng(41.8980, 12.5110)  // Via dei Ramni
                ),
                dayActiveSchedule = "Non attiva diurna",
                nightActiveSchedule = "Ven-Sab 21:00 - 03:00",
                gates = listOf(
                    ZtlGate("Varco Tiburtina", "Via Tiburtina alt. Piazzale Verano", 41.9008, 12.5180, "21:00 - 03:00 (Ven-Sab)", true)
                )
            ),
            ZtlZone(
                id = "testaccio",
                name = "ZTL Testaccio",
                description = "Area Rione Testaccio e movida Monte dei Cocci.",
                polygonPoints = listOf(
                    LatLng(41.8810, 12.4725), // Lungotevere Testaccio
                    LatLng(41.8820, 12.4795), // Piazza dell'Emporio / Marmorata
                    LatLng(41.8765, 12.4815), // Piramide Cestia
                    LatLng(41.8745, 12.4755), // Via Beniamino Franklin
                    LatLng(41.8770, 12.4720)  // Ex Mattatoio
                ),
                dayActiveSchedule = "Non attiva diurna",
                nightActiveSchedule = "Ven-Sab 23:00 - 03:00",
                gates = listOf(
                    ZtlGate("Varco Marmorata", "Via Marmorata alt. Galvani", 41.8795, 12.4790, "23:00 - 03:00 (Ven-Sab)", true)
                )
            )
        )
    }
}
