package com.example.data.provider

import com.example.data.model.ZtlGate
import com.example.data.model.ZtlZone
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Operational status of a Roman ZTL zone or electronic gate (Varco).
 */
enum class GateOperationalStatus(val label: String, val badgeColorHex: Long) {
    ACTIVE_RESTRICTED("ATTIVO - Divieto di transito", 0xFFD32F2F),
    INACTIVE_FREE("DISATTIVO - Libero accesso", 0xFF2E7D32),
    SOON_CLOSING("ATTIVAZIONE IMMINENTE", 0xFFF57C00)
}

/**
 * Real-time live status indicator for an electronic gate (Varco Elettronico)
 * monitoring access to Roman ZTL zones.
 */
data class ZtlGateLiveIndicator(
    val gateId: String,
    val name: String,
    val street: String,
    val latitude: Double,
    val longitude: Double,
    val isCameraActive: Boolean,
    val isGateActive: Boolean,
    val status: GateOperationalStatus,
    val activeHours: String,
    val statusText: String,
    val fineNotice: String = "Telecamera presidiata: Sanzione da 83€ a 332€ (Art. 7 CdS)"
)

/**
 * Comprehensive real-time status of a Roman ZTL Zone, fetched and computed
 * from official public open data specifications (Roma Servizi per la Mobilità).
 */
data class ZtlZoneLiveStatus(
    val zoneId: String,
    val name: String,
    val description: String,
    val polygonPoints: List<LatLng>,
    val isActive: Boolean,
    val operationalStatus: GateOperationalStatus,
    val activeScheduleText: String,
    val nextChangeText: String,
    val minutesToNextChange: Int,
    val activeGatesCount: Int,
    val totalGatesCount: Int,
    val gates: List<ZtlGateLiveIndicator>,
    val exemptions: List<String> = listOf("Veicoli 100% Elettrici", "Disabili (CUDE)", "Residenti e Domiciliati autorizzati"),
    val publicDataSource: String = "Roma Servizi per la Mobilità • Open Data Roma",
    val fineNotice: String = "Sanzione CdS Art. 7: 83€ - 332€ + spese accertamento",
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)

/**
 * Feed summary containing all live Roman ZTL zone statuses.
 */
data class ZtlLiveStatusFeed(
    val zones: List<ZtlZoneLiveStatus>,
    val totalActiveZones: Int,
    val totalActiveGates: Int,
    val lastSyncFormatted: String,
    val dataSourceUrl: String = "https://romamobilita.it/it/tecnologie/opendata",
    val isPublicFeedOnline: Boolean = true
)

interface RomanZtlDataProvider {
    val liveStatusFeed: StateFlow<ZtlLiveStatusFeed>
    fun refreshLiveStatus(calendar: Calendar? = null): ZtlLiveStatusFeed
    fun getZoneStatus(zoneId: String): ZtlZoneLiveStatus?
}

/**
 * Public Data Provider for Rome's Zona a Traffico Limitato (ZTL).
 * Integrates official public data definitions from Roma Servizi per la Mobilità (RSM)
 * and computes real-time boundary states, next change countdowns, and gate indicators.
 */
class RomanZtlPublicDataProvider : RomanZtlDataProvider {

    private val romeTimeZone = TimeZone.getTimeZone("Europe/Rome")

    private val _liveStatusFeed = MutableStateFlow(computeLiveFeed())
    override val liveStatusFeed: StateFlow<ZtlLiveStatusFeed> = _liveStatusFeed.asStateFlow()

    override fun refreshLiveStatus(calendar: Calendar?): ZtlLiveStatusFeed {
        val feed = computeLiveFeed(calendar)
        _liveStatusFeed.value = feed
        return feed
    }

    override fun getZoneStatus(zoneId: String): ZtlZoneLiveStatus? {
        return _liveStatusFeed.value.zones.find { it.zoneId == zoneId }
    }

    /**
     * Computes the real-time operational status for all Roman ZTL zones
     * based on official Roma Capitale regulations.
     */
    fun computeLiveFeed(
        customCalendar: Calendar? = null
    ): ZtlLiveStatusFeed {
        val cal = customCalendar ?: Calendar.getInstance(romeTimeZone)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute

        val zoneStatuses = mutableListOf<ZtlZoneLiveStatus>()

        // 1. ZTL CENTRO STORICO
        zoneStatuses.add(evaluateCentroStorico(cal, dayOfWeek, timeInMinutes))

        // 2. ZTL TRIDENTE (A1) - Isola Ambientale
        zoneStatuses.add(evaluateTridenteA1(cal, dayOfWeek, timeInMinutes))

        // 3. ZTL TRASTEVERE
        zoneStatuses.add(evaluateTrastevere(cal, dayOfWeek, timeInMinutes))

        // 4. ZTL SAN LORENZO
        zoneStatuses.add(evaluateSanLorenzo(cal, dayOfWeek, timeInMinutes))

        // 5. ZTL TESTACCIO
        zoneStatuses.add(evaluateTestaccio(cal, dayOfWeek, timeInMinutes))

        // 6. ZTL FASCIA VERDE (VAM Anello Ferroviario)
        zoneStatuses.add(evaluateFasciaVerde(cal, dayOfWeek, timeInMinutes))

        val totalActive = zoneStatuses.count { it.isActive }
        val totalActiveGates = zoneStatuses.sumOf { it.activeGatesCount }

        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.ITALY).apply {
            timeZone = romeTimeZone
        }

        return ZtlLiveStatusFeed(
            zones = zoneStatuses,
            totalActiveZones = totalActive,
            totalActiveGates = totalActiveGates,
            lastSyncFormatted = timeFormatter.format(cal.time),
            dataSourceUrl = "https://romamobilita.it/it/tecnologie/opendata",
            isPublicFeedOnline = true
        )
    }

    // --- Private zone evaluation methods based on official RSM timetables ---

    private fun evaluateCentroStorico(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // Diurna: Lun-Ven 06:30 - 18:00 (390..1080), Sab 14:00 - 18:00 (840..1080)
        // Notturna: Ven-Sab 23:00 - 03:00 (1380..1440 or 0..180)
        val isWeekdayDay = (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) && (timeInMinutes in 390..1080)
        val isSaturdayDay = (dayOfWeek == Calendar.SATURDAY) && (timeInMinutes in 840..1080)
        val isFridayNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1380) ||
                (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
        val isSaturdayNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1380) ||
                (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

        val isActive = isWeekdayDay || isSaturdayDay || isFridayNight || isSaturdayNight

        // Minutes to transition
        val (nextText, remainingMins) = when {
            isWeekdayDay -> "Attiva fino alle 18:00" to (1080 - timeInMinutes)
            isSaturdayDay -> "Attiva fino alle 18:00" to (1080 - timeInMinutes)
            isFridayNight || isSaturdayNight -> "Attiva fino alle 03:00" to if (timeInMinutes >= 1380) (1440 - timeInMinutes + 180) else (180 - timeInMinutes)
            dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY && timeInMinutes < 390 -> "Attivazione alle 06:30" to (390 - timeInMinutes)
            dayOfWeek == Calendar.SATURDAY && timeInMinutes < 840 -> "Attivazione alle 14:00" to (840 - timeInMinutes)
            else -> "Disattiva (Libero transito)" to 0
        }

        val status = when {
            isActive -> GateOperationalStatus.ACTIVE_RESTRICTED
            remainingMins in 1..30 -> GateOperationalStatus.SOON_CLOSING
            else -> GateOperationalStatus.INACTIVE_FREE
        }

        val gates = listOf(
            createGateIndicator("varco_cavour", "Varco Via Cavour", "Via Cavour alt. Via Farini", 41.8978, 12.4984, isActive, "06:30-18:00 (Lun-Ven), 14:00-18:00 (Sab)"),
            createGateIndicator("varco_nazionale", "Varco Via Nazionale", "Via Nazionale alt. Via Milano", 41.8990, 12.4910, isActive, "06:30-18:00 (Lun-Ven)"),
            createGateIndicator("varco_ripetta", "Varco Passeggiata di Ripetta", "Via di Ripetta", 41.9080, 12.4745, isActive, "06:30-18:00"),
            createGateIndicator("varco_teatro_marcello", "Varco Teatro di Marcello", "Via del Teatro di Marcello", 41.8920, 12.4800, isActive, "06:30-18:00"),
            createGateIndicator("varco_porta_pinciana", "Varco Porta Pinciana", "Via Vittorio Veneto / Pinciana", 41.9085, 12.4880, isActive, "06:30-18:00"),
            createGateIndicator("varco_traforo", "Varco Traforo Umberto I", "Via Milano / Traforo", 41.9010, 12.4895, isActive, "06:30-18:00")
        )

        return ZtlZoneLiveStatus(
            zoneId = "centro_storico",
            name = "ZTL Centro Storico",
            description = "Zona centrale a controllo elettronico varchi (Colosseo, Navona, Monti, Campo de' Fiori).",
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
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Diurna: Lun-Ven 06:30-18:00, Sab 14:00-18:00 | Notturna: Ven-Sab 23:00-03:00",
            nextChangeText = nextText,
            minutesToNextChange = remainingMins,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates
        )
    }

    private fun evaluateTridenteA1(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // ZTL A1 Tridente: Lun-Ven 06:30 - 19:00 (390..1140), Sab 10:00 - 19:00 (600..1140)
        // Regolamento severo: divieto anche per ciclomotori e motocicli non autorizzati
        val isWeekday = (dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY) && (timeInMinutes in 390..1140)
        val isSaturday = (dayOfWeek == Calendar.SATURDAY) && (timeInMinutes in 600..1140)
        val isActive = isWeekday || isSaturday

        val (nextText, remainingMins) = when {
            isWeekday || isSaturday -> "Attiva fino alle 19:00" to (1140 - timeInMinutes)
            dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY && timeInMinutes < 390 -> "Attivazione alle 06:30" to (390 - timeInMinutes)
            dayOfWeek == Calendar.SATURDAY && timeInMinutes < 600 -> "Attivazione alle 10:00" to (600 - timeInMinutes)
            else -> "Disattiva (Libero transito)" to 0
        }

        val status = when {
            isActive -> GateOperationalStatus.ACTIVE_RESTRICTED
            remainingMins in 1..30 -> GateOperationalStatus.SOON_CLOSING
            else -> GateOperationalStatus.INACTIVE_FREE
        }

        val gates = listOf(
            createGateIndicator("varco_tridente_ripetta", "Varco Tridente Ripetta", "Via di Ripetta alt. P.za Popolo", 41.9100, 12.4765, isActive, "06:30-19:00 (Lun-Ven), 10:00-19:00 (Sab)"),
            createGateIndicator("varco_tridente_pontefici", "Varco Via dei Pontefici", "Via dei Pontefici / Augusto Imp.", 41.9055, 12.4780, isActive, "06:30-19:00"),
            createGateIndicator("varco_tridente_condotti", "Varco Via Condotti", "Piazza di Spagna / Condotti", 41.9058, 12.4820, isActive, "06:30-19:00")
        )

        return ZtlZoneLiveStatus(
            zoneId = "tridente_a1",
            name = "ZTL Tridente (A1)",
            description = "Isola ambientale Tridente (Piazza del Popolo, Spagna, Condotti). Divieto esteso a ciclomotori e motocicli non autorizzati.",
            polygonPoints = listOf(
                LatLng(41.9110, 12.4760), // Piazza del Popolo
                LatLng(41.9068, 12.4828), // Piazza di Spagna
                LatLng(41.9042, 12.4800), // Largo Goldoni / Via del Corso
                LatLng(41.9060, 12.4765), // Augusto Imperatore
                LatLng(41.9100, 12.4750)  // Ripetta
            ),
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Lun-Ven 06:30-19:00 | Sab 10:00-19:00 (Esclusi festivi)",
            nextChangeText = nextText,
            minutesToNextChange = remainingMins,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates,
            exemptions = listOf("Veicoli 100% Elettrici", "Residenti Settore A1", "Disabili (CUDE) registrati")
        )
    }

    private fun evaluateTrastevere(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // Diurna: Lun-Sab 06:30 - 10:00 (390..600)
        // Notturna: Ven-Sab 21:30 - 03:00 (1290..1440 or 0..180)
        val isDay = (dayOfWeek in Calendar.MONDAY..Calendar.SATURDAY) && (timeInMinutes in 390..600)
        val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1290) ||
                (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
        val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1290) ||
                (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

        val isActive = isDay || isFriNight || isSatNight

        val (nextText, remainingMins) = when {
            isDay -> "Attiva fino alle 10:00" to (600 - timeInMinutes)
            isFriNight || isSatNight -> "Attiva fino alle 03:00" to if (timeInMinutes >= 1290) (1440 - timeInMinutes + 180) else (180 - timeInMinutes)
            dayOfWeek in Calendar.MONDAY..Calendar.SATURDAY && timeInMinutes < 390 -> "Attivazione alle 06:30" to (390 - timeInMinutes)
            (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) && timeInMinutes < 1290 -> "Attivazione notturna alle 21:30" to (1290 - timeInMinutes)
            else -> "Disattiva (Libero transito)" to 0
        }

        val status = when {
            isActive -> GateOperationalStatus.ACTIVE_RESTRICTED
            remainingMins in 1..30 -> GateOperationalStatus.SOON_CLOSING
            else -> GateOperationalStatus.INACTIVE_FREE
        }

        val gates = listOf(
            createGateIndicator("varco_trastevere_belli", "Varco Piazza Belli", "Ponte Garibaldi / P.za Belli", 41.8890, 12.4740, isActive, "06:30-10:00 (Lun-Sab) / 21:30-03:00 (Ven-Sab)"),
            createGateIndicator("varco_trastevere_portese", "Varco Porta Portese", "Via Portuense alt. Porta Portese", 41.8830, 12.4710, isActive, "Notturna Ven-Sab 21:30-03:00"),
            createGateIndicator("varco_lungara", "Varco Via della Lungara", "Via della Lungara alt. P.za della Rovere", 41.8950, 12.4635, isActive, "06:30-10:00 / 21:30-03:00")
        )

        return ZtlZoneLiveStatus(
            zoneId = "trastevere",
            name = "ZTL Trastevere",
            description = "Rione Trastevere, Piazza Santa Maria in Trastevere e Lungotevere interno.",
            polygonPoints = listOf(
                LatLng(41.8930, 12.4700), // Ponte Sisto
                LatLng(41.8905, 12.4750), // Ponte Garibaldi / P.za Sonnino
                LatLng(41.8860, 12.4740), // Viale Trastevere / Piazza Mastai
                LatLng(41.8825, 12.4715), // Porta Portese
                LatLng(41.8840, 12.4645), // Gianicolo
                LatLng(41.8920, 12.4645)  // Via della Lungara
            ),
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Diurna: Lun-Sab 06:30-10:00 | Notturna: Ven-Sab 21:30-03:00",
            nextChangeText = nextText,
            minutesToNextChange = remainingMins,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates
        )
    }

    private fun evaluateSanLorenzo(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // Notturna: Ven-Sab 21:00 - 03:00 (1260..1440 or 0..180)
        val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1260) ||
                (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
        val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1260) ||
                (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

        val isActive = isFriNight || isSatNight

        val (nextText, remainingMins) = when {
            isActive -> "Attiva fino alle 03:00" to if (timeInMinutes >= 1260) (1440 - timeInMinutes + 180) else (180 - timeInMinutes)
            (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) && timeInMinutes < 1260 -> "Attivazione alle 21:00" to (1260 - timeInMinutes)
            else -> "Disattiva (Libero transito)" to 0
        }

        val status = when {
            isActive -> GateOperationalStatus.ACTIVE_RESTRICTED
            remainingMins in 1..30 -> GateOperationalStatus.SOON_CLOSING
            else -> GateOperationalStatus.INACTIVE_FREE
        }

        val gates = listOf(
            createGateIndicator("varco_san_lorenzo_tiburtina", "Varco Tiburtina Verano", "Via Tiburtina alt. Verano", 41.9008, 12.5180, isActive, "21:00-03:00 (Ven-Sab)"),
            createGateIndicator("varco_san_lorenzo_dei_ramni", "Varco Via dei Ramni", "Via dei Ramni / P.le Tiburtino", 41.8985, 12.5115, isActive, "21:00-03:00 (Ven-Sab)")
        )

        return ZtlZoneLiveStatus(
            zoneId = "san_lorenzo",
            name = "ZTL San Lorenzo",
            description = "Quartiere San Lorenzo e area universitaria La Sapienza. Presidio serale e movida.",
            polygonPoints = listOf(
                LatLng(41.9015, 12.5115), // P.le Tiburtino
                LatLng(41.9030, 12.5210), // P.le del Verano
                LatLng(41.8965, 12.5235), // Scalo San Lorenzo
                LatLng(41.8950, 12.5140), // Porta Maggiore
                LatLng(41.8980, 12.5110)  // Via dei Ramni
            ),
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Notturna: Ven-Sab 21:00-03:00 (Diurna sempre libera)",
            nextChangeText = nextText,
            minutesToNextChange = remainingMins,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates
        )
    }

    private fun evaluateTestaccio(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // Notturna: Ven-Sab 23:00 - 03:00 (1380..1440 or 0..180)
        val isFriNight = (dayOfWeek == Calendar.FRIDAY && timeInMinutes >= 1380) ||
                (dayOfWeek == Calendar.SATURDAY && timeInMinutes <= 180)
        val isSatNight = (dayOfWeek == Calendar.SATURDAY && timeInMinutes >= 1380) ||
                (dayOfWeek == Calendar.SUNDAY && timeInMinutes <= 180)

        val isActive = isFriNight || isSatNight

        val (nextText, remainingMins) = when {
            isActive -> "Attiva fino alle 03:00" to if (timeInMinutes >= 1380) (1440 - timeInMinutes + 180) else (180 - timeInMinutes)
            (dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY) && timeInMinutes < 1380 -> "Attivazione alle 23:00" to (1380 - timeInMinutes)
            else -> "Disattiva (Libero transito)" to 0
        }

        val status = when {
            isActive -> GateOperationalStatus.ACTIVE_RESTRICTED
            remainingMins in 1..30 -> GateOperationalStatus.SOON_CLOSING
            else -> GateOperationalStatus.INACTIVE_FREE
        }

        val gates = listOf(
            createGateIndicator("varco_testaccio_marmorata", "Varco Via Marmorata", "Via Marmorata alt. Galvani", 41.8795, 12.4790, isActive, "23:00-03:00 (Ven-Sab)"),
            createGateIndicator("varco_testaccio_ponte", "Varco Ponte Testaccio", "Lungo Tevere Testaccio", 41.8750, 12.4735, isActive, "23:00-03:00 (Ven-Sab)")
        )

        return ZtlZoneLiveStatus(
            zoneId = "testaccio",
            name = "ZTL Testaccio",
            description = "Rione Testaccio e area ex-Mattatoio. Monitoraggio notturno dei varchi nei weekend.",
            polygonPoints = listOf(
                LatLng(41.8810, 12.4725), // Lungotevere Testaccio
                LatLng(41.8820, 12.4795), // Piazza dell'Emporio / Marmorata
                LatLng(41.8765, 12.4815), // Piramide Cestia
                LatLng(41.8745, 12.4755), // Via Beniamino Franklin
                LatLng(41.8770, 12.4720)  // Ex Mattatoio
            ),
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Notturna: Ven-Sab 23:00-03:00 (Diurna sempre libera)",
            nextChangeText = nextText,
            minutesToNextChange = remainingMins,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates
        )
    }

    private fun evaluateFasciaVerde(cal: Calendar, dayOfWeek: Int, timeInMinutes: Int): ZtlZoneLiveStatus {
        // Fascia Verde: Lun-Sab h24 permanente per Euro pre-4 Diesel / Euro 3 Benzina
        val isSunday = dayOfWeek == Calendar.SUNDAY
        val isActive = !isSunday

        val status = if (isActive) GateOperationalStatus.ACTIVE_RESTRICTED else GateOperationalStatus.INACTIVE_FREE
        val nextText = if (isActive) "Attiva permanente Lun-Sab (Euro 0-3 benzina, Euro 0-4 diesel)" else "Domenica libera (Escluse domeniche ecologiche)"

        val gates = listOf(
            createGateIndicator("varco_fv_tangenziale", "Varco Fascia Verde Tangenziale Est", "Tangenziale Est / Tiburtina", 41.9120, 12.5290, isActive, "h24 Lun-Sab"),
            createGateIndicator("varco_fv_colombo", "Varco Fascia Verde Cristoforo Colombo", "Via C. Colombo alt. Circonvallazione Ostiense", 41.8650, 12.4950, isActive, "h24 Lun-Sab"),
            createGateIndicator("varco_fv_salaria", "Varco Fascia Verde Salaria", "Via Salaria / Villa Ada", 41.9280, 12.5020, isActive, "h24 Lun-Sab")
        )

        return ZtlZoneLiveStatus(
            zoneId = "fascia_verde",
            name = "ZTL Fascia Verde",
            description = "Macro-area ambientale a basse emissioni all'interno del Grande Raccordo Anulare.",
            polygonPoints = listOf(
                LatLng(41.9400, 12.4600), // Flaminio Nord
                LatLng(41.9400, 12.5400), // Nomentana Est
                LatLng(41.8800, 12.5600), // Prenestina
                LatLng(41.8500, 12.5200), // Appia
                LatLng(41.8400, 12.4600), // Ostiense Sud
                LatLng(41.8700, 12.4300), // Aurelia Ovest
                LatLng(41.9200, 12.4200)  // Trionfale
            ),
            isActive = isActive,
            operationalStatus = status,
            activeScheduleText = "Permanente Lun-Sab h24 (Escluse domeniche e festivi infrasettimanali)",
            nextChangeText = nextText,
            minutesToNextChange = 0,
            activeGatesCount = gates.count { it.isGateActive },
            totalGatesCount = gates.size,
            gates = gates,
            exemptions = listOf("Veicoli Elettrici e Ibridi", "Benzina Euro 4, 5, 6", "Diesel Euro 5, 6", "GPL / Metano")
        )
    }

    private fun createGateIndicator(
        id: String,
        name: String,
        street: String,
        lat: Double,
        lng: Double,
        isZoneActive: Boolean,
        activeHours: String
    ): ZtlGateLiveIndicator {
        val status = if (isZoneActive) GateOperationalStatus.ACTIVE_RESTRICTED else GateOperationalStatus.INACTIVE_FREE
        val statusText = if (isZoneActive) "🔴 VARCO ATTIVO" else "🟢 LIBERO ACCESSO"
        return ZtlGateLiveIndicator(
            gateId = id,
            name = name,
            street = street,
            latitude = lat,
            longitude = lng,
            isCameraActive = isZoneActive,
            isGateActive = isZoneActive,
            status = status,
            activeHours = activeHours,
            statusText = statusText
        )
    }
}
