package com.example.data.model

enum class ViewMode {
    MAP,
    LIST,
    RADAR,
    SAVED,
    ACTIVITY
}

enum class MapRendererType {
    GOOGLE_MAPS,
    TACTICAL_VECTOR
}

enum class FeedbackType {
    FOUND,
    NOT_FOUND,
    WRONG_DATA
}

enum class HazardType {
    ZTL,
    LAVAGGIO_STRADE,
    TARIFFA_BLU,
    STALLO_MERCI,
    DIVIETO_RIMOZIONE
}

enum class StayDuration(val label: String, val minutes: Int) {
    MIN_5("5 min (sosta lampo)", 5),
    MIN_10("10 min", 10),
    MIN_15("15 min (un quarto d'ora)", 15),
    MIN_30("30 min", 30),
    HOUR_1("1 ora", 60),
    HOUR_2("2 ore", 120),
    HOUR_3("3 ore", 180),
    HOUR_4_PLUS("4+ ore (lunga sosta)", 240)
}

enum class TimeOfDayFilter(val label: String) {
    ANY("Qualsiasi orario"),
    NEXT_HOUR("Prossima ora"),
    AFTER_6_PM("Dopo le 18:00")
}

enum class DurationNeededFilter(val label: String) {
    ANY("Qualsiasi durata"),
    SHORT_30_MIN("30 minuti"),
    MEDIUM_1_HOUR("1 ora"),
    LONG_2_PLUS_HOURS("2+ ore")
}

enum class ParkingTypeFilter(val label: String) {
    ALL("Tutti i tipi"),
    METERED_BLUE("Strisce Blu"),
    FREE_WHITE("Strisce Bianche (Gratis)"),
    EV_CHARGING("Colonnina EV")
}

data class ParkingSearchFilter(
    val timeOfDay: TimeOfDayFilter = TimeOfDayFilter.ANY,
    val durationNeeded: DurationNeededFilter = DurationNeededFilter.ANY,
    val parkingType: ParkingTypeFilter = ParkingTypeFilter.ALL
) {
    val isDefault: Boolean
        get() = timeOfDay == TimeOfDayFilter.ANY &&
                durationNeeded == DurationNeededFilter.ANY &&
                parkingType == ParkingTypeFilter.ALL
}

data class MicroArea(
    val id: String,
    val name: String,
    val streetName: String,
    val latitude: Double, // Real Rome coordinates
    val longitude: Double,
    val distanceMeters: Int,
    val radiusMeters: Int, // 30m - 75m anonymous zone
    val baselineAvailability: Int, // 0 - 100%
    val blueLineTariff: String,
    val whiteLineSpots: Int,
    val ztlZoneName: String,
    val isZtlBoundary: Boolean,
    val cleaningSchedule: String,
    val normX: Float, // Normalized for vector canvas
    val normY: Float,
    val activeSignalsCount: Int = 0,
    val upcomingDeparturesCount: Int = 0,
    val hasEvCharging: Boolean = false,
    val allowsLongStay: Boolean = true,
    val isEveningFree: Boolean = true,
    val availabilityNextHour: Int = 40,
    val availabilityAfter6Pm: Int = 75,
    val isPrivateGarage: Boolean = false,
    val isAccessibleDisabled: Boolean = true
) {
    val availabilityLevel: AvailabilityLevel
        get() = when {
            baselineAvailability >= 60 -> AvailabilityLevel.HIGH
            baselineAvailability >= 30 -> AvailabilityLevel.MEDIUM
            else -> AvailabilityLevel.LOW
        }

    fun matchesCategory(category: ParkingTypeCategory): Boolean {
        return when (category) {
            ParkingTypeCategory.ALL -> true
            ParkingTypeCategory.PUBLIC -> !isPrivateGarage
            ParkingTypeCategory.PRIVATE -> isPrivateGarage
            ParkingTypeCategory.EV_CHARGING -> hasEvCharging
            ParkingTypeCategory.ACCESSIBLE -> isAccessibleDisabled
        }
    }

    fun matchesFilter(filter: ParkingSearchFilter): Boolean {
        when (filter.timeOfDay) {
            TimeOfDayFilter.ANY -> Unit
            TimeOfDayFilter.NEXT_HOUR -> {
                if (availabilityNextHour < 30) return false
            }
            TimeOfDayFilter.AFTER_6_PM -> {
                if (availabilityAfter6Pm < 40) return false
            }
        }

        when (filter.durationNeeded) {
            DurationNeededFilter.ANY -> Unit
            DurationNeededFilter.SHORT_30_MIN -> Unit
            DurationNeededFilter.MEDIUM_1_HOUR -> Unit
            DurationNeededFilter.LONG_2_PLUS_HOURS -> {
                if (!allowsLongStay) return false
            }
        }

        when (filter.parkingType) {
            ParkingTypeFilter.ALL -> Unit
            ParkingTypeFilter.METERED_BLUE -> {
                if (!blueLineTariff.contains("€")) return false
            }
            ParkingTypeFilter.FREE_WHITE -> {
                if (whiteLineSpots <= 0) return false
            }
            ParkingTypeFilter.EV_CHARGING -> {
                if (!hasEvCharging) return false
            }
        }

        return true
    }
}

enum class AvailabilityLevel {
    HIGH,
    MEDIUM,
    LOW
}

data class DepartureSignal(
    val id: String,
    val microAreaId: String,
    val microAreaName: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val createdEpochMs: Long,
    val expiryEpochMs: Long,
    val userReliabilityPercent: Int,
    val feedbackFoundCount: Int = 0,
    val feedbackNotFoundCount: Int = 0,
    val reportedAbuseCount: Int = 0,
    val isUserSubmitted: Boolean = false,
    val hasExpiredPenaltyApplied: Boolean = false
) {
    fun remainingSeconds(currentTimeMs: Long): Long {
        val diff = (expiryEpochMs - currentTimeMs) / 1000L
        return if (diff > 0) diff else 0
    }

    val isExpired: Boolean
        get() = remainingSeconds(System.currentTimeMillis()) <= 0
}

data class ParkedStay(
    val id: String,
    val microAreaId: String,
    val microAreaName: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val parkedAtEpochMs: Long,
    val duration: StayDuration,
    val expectedDepartureEpochMs: Long,
    val isUserVehicle: Boolean = false
) {
    fun remainingMinutes(currentTimeMs: Long): Long {
        val diff = (expectedDepartureEpochMs - currentTimeMs) / (60 * 1000L)
        return if (diff > 0) diff else 0
    }

    val isExpired: Boolean
        get() = remainingMinutes(System.currentTimeMillis()) <= 0
}

enum class BadgeCategory(val label: String) {
    FREQUENCY("Frequenza Segnalazioni"),
    ACCURACY("Precisione & Affidabilità"),
    COMMUNITY("Impatto Community & ZTL")
}

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory = BadgeCategory.FREQUENCY,
    val currentProgress: Int = 0,
    val targetProgress: Int = 1,
    val tier: String = "Bronzo", // Bronzo, Argento, Oro, Platino
    val karmaReward: Int = 50,
    val isUnlocked: Boolean = false,
    val unlockedAtEpochMs: Long? = null
) {
    val progressFraction: Float
        get() = (currentProgress.toFloat() / targetProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

data class DepartureCountdownSession(
    val spotId: String,
    val microAreaId: String,
    val microAreaName: String,
    val streetAddress: String,
    val latitude: Double,
    val longitude: Double,
    val startEpochMs: Long,
    val totalSeconds: Int = 300, // 5 min countdown
    val remainingSeconds: Int = 300,
    val isActive: Boolean = true
) {
    val progressFraction: Float
        get() = (remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val formattedCountdown: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format(java.util.Locale.ITALY, "%02d:%02d", mins, secs)
        }
}

data class ZtlGate(
    val name: String,
    val street: String,
    val latitude: Double,
    val longitude: Double,
    val activeHours: String,
    val isCameraActive: Boolean
)

data class HazardAlert(
    val type: HazardType,
    val title: String,
    val description: String,
    val location: String,
    val isCritical: Boolean = false
)

data class DistrictSector(
    val key: String,
    val title: String,
    val description: String,
    val availabilityPercent: Int,
    val availableSpotsEstimate: Int,
    val microAreaCount: Int,
    val ztlRiskLevel: String
)

data class RadarSubBlockDensity(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
    val congestionPercent: Int, // 0 - 100%
    val estimatedSearchTimeMinutes: Int,
    val availableSpotsEstimate: Int,
    val normX: Float, // Normalized X on Termini-Esquilino map
    val normY: Float, // Normalized Y
    val latitude: Double,
    val longitude: Double,
    val heatRadiusDp: Float = 65f,
    val ztlStatusNote: String = "Fuori ZTL",
    val recommendation: String
) {
    val availabilityPercent: Int get() = (100 - congestionPercent).coerceIn(0, 100)

    val congestionLevelLabel: String
        get() = when {
            congestionPercent >= 80 -> "Critica"
            congestionPercent >= 60 -> "Elevata"
            congestionPercent >= 40 -> "Moderata"
            else -> "Fluida / Libera"
        }
}

data class UserProfile(
    val username: String = "Global Driver",
    val email: String = "cittadino.roma@email.it",
    val points: Int = 420,
    val levelRank: String = "Centurione della Sosta (Liv. 3)",
    val karmaScore: Int = 94,
    val signalsSharedCount: Int = 6,
    val signalsVerifiedCount: Int = 14,
    val parkingsRegisteredCount: Int = 2,
    val licensePlate: String = "RM 782 ED",
    val vehicleModel: String = "Fiat 500 Hybrid",
    val vehiclePermit: VehiclePermitType = VehiclePermitType.NONE,
    val permitNumber: String = "RM-2026-98124",
    val permitExpiryDate: String = "31/12/2026",
    val isPermitActive: Boolean = true,
    val activeDepartureCountdown: DepartureCountdownSession? = null,
    val notificationsEnabled: Boolean = true,
    val congestionAlertsEnabled: Boolean = true,
    val preferredSpotsAlertsEnabled: Boolean = true,
    val favoriteAreaIds: Set<String> = setOf("pza_vittorio_nord", "via_giolitti_stazione"),
    val activeStay: ParkedStay? = null,
    val badges: List<Badge> = listOf(
        Badge(
            id = "parking_scout",
            title = "Parking Scout",
            description = "Condividi 3 o più segnalazioni di posti auto liberi con la community",
            category = BadgeCategory.FREQUENCY,
            currentProgress = 6,
            targetProgress = 3,
            tier = "Oro",
            karmaReward = 60,
            isUnlocked = true,
            unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 4
        ),
        Badge(
            id = "community_hero",
            title = "Community Hero",
            description = "Raggiungi almeno il 95% di accuratezza con 5+ conferme da altri utenti",
            category = BadgeCategory.ACCURACY,
            currentProgress = 94,
            targetProgress = 95,
            tier = "Platino",
            karmaReward = 150,
            isUnlocked = false
        ),
        Badge(
            id = "street_sentinel",
            title = "Street Sentinel",
            description = "Segnala 10 posti auto liberati nei vari quadranti Termini-Esquilino",
            category = BadgeCategory.FREQUENCY,
            currentProgress = 6,
            targetProgress = 10,
            tier = "Argento",
            karmaReward = 80,
            isUnlocked = false
        ),
        Badge(
            id = "accurate_reporter",
            title = "Accurate Reporter",
            description = "Oltre il 90% di affidabilità confermata da altri guidatori",
            category = BadgeCategory.ACCURACY,
            currentProgress = 94,
            targetProgress = 90,
            tier = "Oro",
            karmaReward = 75,
            isUnlocked = true,
            unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 2
        ),
        Badge(
            id = "ztl_guardian",
            title = "ZTL Guardian",
            description = "Zero violazioni varchi ZTL e utilizzo dei percorsi anti-multa ZigZag",
            category = BadgeCategory.COMMUNITY,
            currentProgress = 3,
            targetProgress = 3,
            tier = "Platino",
            karmaReward = 100,
            isUnlocked = true,
            unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 1
        ),
        Badge(
            id = "esquilino_pioneer",
            title = "Pioniere Esquilino",
            description = "Segnalazioni attive nell'area di Piazza Vittorio e Mercato Esquilino",
            category = BadgeCategory.COMMUNITY,
            currentProgress = 1,
            targetProgress = 1,
            tier = "Bronzo",
            karmaReward = 40,
            isUnlocked = true,
            unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 7
        ),
        Badge(
            id = "gold_verifier",
            title = "Verificatore d'Oro",
            description = "Verifica lo stato di 15 segnalazioni create da altri guidatori",
            category = BadgeCategory.ACCURACY,
            currentProgress = 14,
            targetProgress = 15,
            tier = "Oro",
            karmaReward = 90,
            isUnlocked = false
        ),
        Badge(
            id = "centurione_500",
            title = "Centurione 500+",
            description = "Accumula 500 punti reputazione nella community di Roma",
            category = BadgeCategory.FREQUENCY,
            currentProgress = 420,
            targetProgress = 500,
            tier = "Oro",
            karmaReward = 120,
            isUnlocked = false
        ),
        Badge(
            id = "stay_planner",
            title = "Programmatore Sosta",
            description = "Registra 2 soste programmate per aiutare chi arriverà in seguito",
            category = BadgeCategory.FREQUENCY,
            currentProgress = 2,
            targetProgress = 2,
            tier = "Bronzo",
            karmaReward = 50,
            isUnlocked = true,
            unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 3
        )
    )
)

enum class UserActivityType(val label: String) {
    REPORTED_DEPARTURE("Segnalazione Posto Libero"),
    REGISTERED_STAY("Sosta Parcheggiata Registrata"),
    VERIFIED_SPOT("Verifica Segnalazione"),
    FOUND_SPOT("Posto Confermato Trovato"),
    COMMUNITY_BONUS("Bonus Community")
}

data class UserActivityRecord(
    val id: String,
    val type: UserActivityType,
    val title: String,
    val microAreaId: String,
    val microAreaName: String,
    val streetAddress: String,
    val latitude: Double,
    val longitude: Double,
    val timestampEpochMs: Long,
    val rewardPoints: Int,
    val statusNotes: String,
    val impactMetric: String
)

data class SavedCarLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val parkedAtEpochMs: Long = System.currentTimeMillis(),
    val notes: String = "",
    val meterDurationMinutes: Int? = null,
    val meterExpiryEpochMs: Long? = null,
    val floorOrZone: String = ""
) {
    val isMeterActive: Boolean
        get() = meterExpiryEpochMs != null && meterExpiryEpochMs > System.currentTimeMillis()

    val remainingMeterMinutes: Int
        get() = if (meterExpiryEpochMs != null) {
            val diff = (meterExpiryEpochMs - System.currentTimeMillis()) / (1000 * 60)
            if (diff > 0) diff.toInt() else 0
        } else 0
}

enum class SpeedCameraType(val label: String, val iconName: String) {
    FIXED("Autovelox Fisso", "camera_fixed"),
    MOBILE("Postazione Mobile Polizia", "camera_mobile"),
    TUTOR_AVERAGE("Controllo Velocità Media (Tutor)", "tutor_avg"),
    TRAFFIC_LIGHT("Telecamera Semaforica (T-Red)", "traffic_light_cam")
}

enum class DataSourceReliability(val label: String, val sourceTag: String) {
    OFFICIAL_REGISTRY("Registro Ufficiale MIT / Prefettura", "MIT"),
    VERIFIED_PROVIDER("Fornitore Cartografico Verificato", "VERIFIED"),
    COMMUNITY_REPORTED("Segnalazione Community (Verificata)", "COMMUNITY")
}

data class SpeedCamera(
    val id: String,
    val name: String,
    val type: SpeedCameraType,
    val latitude: Double,
    val longitude: Double,
    val speedLimitKmh: Int,
    val roadName: String,
    val directionDegrees: Double? = null, // null if omni-directional
    val reliability: DataSourceReliability = DataSourceReliability.OFFICIAL_REGISTRY,
    val lastVerifiedDate: String = "2026-04-01",
    val notes: String = ""
)

data class MultiModalParkingOption(
    val parkingArea: MicroArea,
    val drivingTimeMinutes: Int,
    val detourMinutes: Int,
    val walkingTimeMinutes: Int,
    val walkingDistanceMeters: Int,
    val totalTripMinutes: Int,
    val estimatedCost: String,
    val recommendationScore: Int, // 0 - 100
    val recommendationReason: String
)


