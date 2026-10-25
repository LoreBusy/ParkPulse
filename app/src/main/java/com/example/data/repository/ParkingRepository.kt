package com.example.data.repository

import com.example.data.model.Badge
import com.example.data.model.DepartureSignal
import com.example.data.model.DistrictSector
import com.example.data.model.FeedbackType
import com.example.data.model.HazardAlert
import com.example.data.model.HazardType
import com.example.data.model.MicroArea
import com.example.data.model.ParkedStay
import com.example.data.model.StayDuration
import com.example.data.model.UserProfile
import com.example.data.model.ZtlGate
import com.example.data.model.ZtlZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.data.local.ParkingDatabase
import com.example.data.local.entity.DepartureSignalEntity
import com.example.data.local.entity.FavoriteParkingAreaEntity
import com.example.data.local.entity.MicroAreaEntity
import com.example.data.local.entity.UserActivityEntity
import com.example.data.local.entity.UserParkingReportEntity
import com.example.data.model.VehiclePermitType
import com.example.data.model.RadarSubBlockDensity
import com.example.data.model.UserActivityRecord
import com.example.data.model.UserActivityType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.TrafficEtaResult
import com.example.data.provider.CityRegion
import com.example.data.provider.GlobalCityProvider
import com.example.util.TrafficEtaService
import com.example.util.PlacesAutocompleteService
import com.example.data.model.UserSettings
import com.example.data.model.PlacePrediction
import com.example.data.model.LeaderboardEntry
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.example.data.model.SavedCarLocation
import com.example.data.model.SpeedCamera
import com.example.data.model.MultiModalParkingOption
import com.example.data.provider.SpeedCameraDataProvider
import com.example.data.provider.OfficialMitSpeedCameraProvider
import com.example.data.provider.SpeedCameraWarning
import com.example.data.provider.ParkingDataProvider
import com.example.data.provider.DynamicParkingDataProvider
import com.example.data.provider.RomanZtlPublicDataProvider
import com.example.data.provider.ZtlLiveStatusFeed
import com.example.data.provider.ZtlZoneLiveStatus

class ParkingRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val initialMicroAreas = listOf(
        MicroArea(
            id = "via_giolitti_stazione",
            name = "Stazione Termini (Ala Binari Giolitti)",
            streetName = "Via Giovanni Giolitti",
            latitude = 41.8992,
            longitude = 12.5035,
            distanceMeters = 80,
            radiusMeters = 50,
            baselineAvailability = 24,
            blueLineTariff = "€1,50/h (08:00 - 20:00)",
            whiteLineSpots = 0,
            ztlZoneName = "Fuori ZTL (Attenzione corsia preferenziale Taxi/Bus)",
            isZtlBoundary = false,
            cleaningSchedule = "Lunedì 00:00 - 06:00",
            normX = 0.35f,
            normY = 0.25f,
            activeSignalsCount = 1,
            upcomingDeparturesCount = 1,
            hasEvCharging = true,
            allowsLongStay = false,
            isEveningFree = true,
            availabilityNextHour = 20,
            availabilityAfter6Pm = 60
        ),
        MicroArea(
            id = "via_marsala_nord",
            name = "Termini Nord (Ingresso Marsala)",
            streetName = "Via Marsala",
            latitude = 41.9028,
            longitude = 12.5028,
            distanceMeters = 150,
            radiusMeters = 45,
            baselineAvailability = 35,
            blueLineTariff = "€1,50/h (08:00 - 20:00)",
            whiteLineSpots = 2,
            ztlZoneName = "Fuori ZTL",
            isZtlBoundary = false,
            cleaningSchedule = "Mercoledì 00:00 - 06:00",
            normX = 0.55f,
            normY = 0.15f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 0,
            hasEvCharging = false,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 32,
            availabilityAfter6Pm = 50
        ),
        MicroArea(
            id = "via_filippo_turati",
            name = "Via Filippo Turati (Adiacente Termini)",
            streetName = "Via Filippo Turati",
            latitude = 41.8988,
            longitude = 12.5052,
            distanceMeters = 220,
            radiusMeters = 40,
            baselineAvailability = 32,
            blueLineTariff = "€1,50/h (08:00 - 20:00)",
            whiteLineSpots = 1,
            ztlZoneName = "Fuori ZTL (Strada a senso unico stretto)",
            isZtlBoundary = false,
            cleaningSchedule = "Martedì 00:00 - 06:00",
            normX = 0.45f,
            normY = 0.32f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 1,
            hasEvCharging = false,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 28,
            availabilityAfter6Pm = 65
        ),
        MicroArea(
            id = "via_principe_amedeo",
            name = "Via Principe Amedeo (Centro Esquilino)",
            streetName = "Via Principe Amedeo",
            latitude = 41.8975,
            longitude = 12.5040,
            distanceMeters = 340,
            radiusMeters = 50,
            baselineAvailability = 52,
            blueLineTariff = "€1,00/h (08:00 - 20:00)",
            whiteLineSpots = 5,
            ztlZoneName = "Fuori ZTL",
            isZtlBoundary = false,
            cleaningSchedule = "Giovedì 00:00 - 06:00 (RIMOZIONE FORZATA)",
            normX = 0.42f,
            normY = 0.42f,
            activeSignalsCount = 1,
            upcomingDeparturesCount = 2,
            hasEvCharging = true,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 48,
            availabilityAfter6Pm = 72
        ),
        MicroArea(
            id = "via_cavour_santa_maria",
            name = "Via Cavour (Basilica S. Maria Maggiore)",
            streetName = "Via Cavour",
            latitude = 41.8978,
            longitude = 12.4984,
            distanceMeters = 380,
            radiusMeters = 55,
            baselineAvailability = 18,
            blueLineTariff = "€1,50/h (08:00 - 20:00)",
            whiteLineSpots = 0,
            ztlZoneName = "LIMITE ZTL Centro Storico (Telecamera Varco)",
            isZtlBoundary = true,
            cleaningSchedule = "Venerdì 00:00 - 06:00",
            normX = 0.22f,
            normY = 0.45f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 0,
            hasEvCharging = false,
            allowsLongStay = false,
            isEveningFree = false,
            availabilityNextHour = 15,
            availabilityAfter6Pm = 30
        ),
        MicroArea(
            id = "pza_vittorio_nord",
            name = "Piazza Vittorio (Portici Nord)",
            streetName = "Piazza Vittorio Emanuele II",
            latitude = 41.8962,
            longitude = 12.5048,
            distanceMeters = 450,
            radiusMeters = 65,
            baselineAvailability = 74,
            blueLineTariff = "€1,00/h (08:00 - 20:00)",
            whiteLineSpots = 7,
            ztlZoneName = "Fuori ZTL",
            isZtlBoundary = false,
            cleaningSchedule = "Sabato 00:00 - 06:00",
            normX = 0.60f,
            normY = 0.55f,
            activeSignalsCount = 1,
            upcomingDeparturesCount = 3,
            hasEvCharging = true,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 68,
            availabilityAfter6Pm = 85
        ),
        MicroArea(
            id = "pza_vittorio_sud",
            name = "Piazza Vittorio (Lato Carlo Alberto / Giardini)",
            streetName = "Via Carlo Alberto",
            latitude = 41.8950,
            longitude = 12.5038,
            distanceMeters = 520,
            radiusMeters = 60,
            baselineAvailability = 61,
            blueLineTariff = "€1,00/h (08:00 - 20:00)",
            whiteLineSpots = 4,
            ztlZoneName = "Fuori ZTL",
            isZtlBoundary = false,
            cleaningSchedule = "Lunedì 00:00 - 06:00",
            normX = 0.68f,
            normY = 0.68f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 1,
            hasEvCharging = false,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 55,
            availabilityAfter6Pm = 80
        ),
        MicroArea(
            id = "via_merulana_dante",
            name = "Via Merulana (Tratto Piazza Dante)",
            streetName = "Via Merulana",
            latitude = 41.8918,
            longitude = 12.5034,
            distanceMeters = 610,
            radiusMeters = 70,
            baselineAvailability = 66,
            blueLineTariff = "€1,00/h (08:00 - 20:00)",
            whiteLineSpots = 4,
            ztlZoneName = "Fuori ZTL (Stalli carico merci fino ore 10:00)",
            isZtlBoundary = false,
            cleaningSchedule = "Martedì 00:00 - 06:00",
            normX = 0.48f,
            normY = 0.80f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 2,
            hasEvCharging = false,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 62,
            availabilityAfter6Pm = 78
        ),
        MicroArea(
            id = "viale_manzoni",
            name = "Viale Manzoni (Asse Tramviario)",
            streetName = "Viale Manzoni",
            latitude = 41.8906,
            longitude = 12.5072,
            distanceMeters = 750,
            radiusMeters = 75,
            baselineAvailability = 68,
            blueLineTariff = "€1,00/h (08:00 - 20:00)",
            whiteLineSpots = 9,
            ztlZoneName = "Fuori ZTL (Divieto assoluto su sede rotaie)",
            isZtlBoundary = false,
            cleaningSchedule = "Mercoledì 00:00 - 06:00",
            normX = 0.78f,
            normY = 0.85f,
            activeSignalsCount = 0,
            upcomingDeparturesCount = 1,
            hasEvCharging = true,
            allowsLongStay = true,
            isEveningFree = true,
            availabilityNextHour = 65,
            availabilityAfter6Pm = 82
        )
    )

    val catalogMicroAreas: List<MicroArea> get() = initialMicroAreas

    private val _microAreas = MutableStateFlow<List<MicroArea>>(emptyList())
    val microAreas: StateFlow<List<MicroArea>> = _microAreas.asStateFlow()


    private val _savedCarLocation = MutableStateFlow<SavedCarLocation?>(null)
    val savedCarLocation: StateFlow<SavedCarLocation?> = _savedCarLocation.asStateFlow()

    val speedCameraProvider: SpeedCameraDataProvider = OfficialMitSpeedCameraProvider()
    val parkingDataProvider: ParkingDataProvider = DynamicParkingDataProvider()

    private val _activeSignals = MutableStateFlow<List<DepartureSignal>>(emptyList())
    val activeSignals: StateFlow<List<DepartureSignal>> = _activeSignals.asStateFlow()

    private val _activeParkedStays = MutableStateFlow<List<ParkedStay>>(emptyList())
    val activeParkedStays: StateFlow<List<ParkedStay>> = _activeParkedStays.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _userActivities = MutableStateFlow<List<UserActivityRecord>>(emptyList())
    val userActivities: StateFlow<List<UserActivityRecord>> = _userActivities.asStateFlow()

    val ztlZones: List<ZtlZone> = ZtlZone.ROMA_ZTL_ZONES

    val ztlGates = listOf(
        ZtlGate("Varco Via Cavour", "Via Cavour alt. Via Farini", 41.8978, 12.4984, "06:30 - 18:00 (Lun-Ven), 14:00 - 18:00 (Sab)", true),
        ZtlGate("Varco Via Giolitti", "Via Giolitti alt. Via di Santa Bibiana", 41.8960, 12.5090, "Vigile / Telecamera notturna", true),
        ZtlGate("Varco San Lorenzo (Tiburtina)", "Via Tiburtina alt. Piazzale Verano", 41.9008, 12.5180, "21:00 - 03:00 (Ven-Sab)", true),
        ZtlGate("Varco Trastevere", "Piazza Belli / Ponte Garibaldi", 41.8890, 12.4740, "06:30 - 10:00 (Tutti i giorni) / Notturna", true)
    )

    val romanHazards = listOf(
        HazardAlert(
            type = HazardType.ZTL,
            title = "Attenzione Varco ZTL Centro Storico a 350m",
            description = "Il varco di Via Cavour è attivo. L'accesso non autorizzato comporta multa automatica da 83€ a 332€.",
            location = "Via Cavour / S. Maria Maggiore",
            isCritical = true
        ),
        HazardAlert(
            type = HazardType.LAVAGGIO_STRADE,
            title = "Lavaggio Strade Notturno in arrivo",
            description = "Su Via Principe Amedeo e Via Turati è programmato il passaggio spazzatrici AMA con rimozione forzata dalle 00:00 alle 06:00.",
            location = "Via Principe Amedeo & Turati",
            isCritical = false
        ),
        HazardAlert(
            type = HazardType.TARIFFA_BLU,
            title = "Tariffa Strisce Blu differenziata",
            description = "Zona Stazione Termini €1,50/ora (tariffa ridotta 4 ore a €4,00). Zona interna Esquilino €1,00/ora.",
            location = "Termini vs Esquilino",
            isCritical = false
        ),
        HazardAlert(
            type = HazardType.STALLO_MERCI,
            title = "Trappola stalli carico/scarico",
            description = "Gli stalli gialli merci su Via Merulana sono riservati fino alle 10:00. Multa per divieto di sosta e decurtazione punti.",
            location = "Via Merulana",
            isCritical = true
        )
    )

    val sectors = listOf(
        DistrictSector("termini_stazione", "Termini - Asse Ferroviario", "Elevata pressione, rotazione rapida, posti taxi e carico viaggiatori.", 26, 12, 3, "Basso (Fuori ZTL)"),
        DistrictSector("esquilino_nord", "Esquilino Nord - Turati/Amedeo", "Densità residenziale mista, strade a senso unico, media disponibilità.", 48, 28, 2, "Basso"),
        DistrictSector("piazza_vittorio", "Piazza Vittorio & Portici", "Ampio perimetro con strisce blu e bianche, ottima probabilità.", 68, 54, 2, "Basso"),
        DistrictSector("merulana_manzoni", "Merulana - Manzoni - Dante", "Arterie larghe, collegamento San Giovanni, buona disponibilità.", 67, 49, 2, "Medio (Confine Rione Monti)")
    )

    val radarSubBlocks: List<RadarSubBlockDensity> = listOf(
        RadarSubBlockDensity(
            id = "termini_ala_giolitti",
            name = "Stazione Termini (Ala Binari Giolitti)",
            shortName = "Termini Giolitti",
            description = "Pressione massima per partenze/arrivi treni. Stalli a rotazione velocissima.",
            congestionPercent = 92,
            estimatedSearchTimeMinutes = 22,
            availableSpotsEstimate = 4,
            normX = 0.35f,
            normY = 0.22f,
            latitude = 41.8992,
            longitude = 12.5035,
            heatRadiusDp = 65f,
            ztlStatusNote = "Fuori ZTL (Corsia bus vigilata)",
            recommendation = "Sconsigliata sosta lunga. Usare sosta rapida carico/scarico."
        ),
        RadarSubBlockDensity(
            id = "termini_marsala_nord",
            name = "Termini Nord (Piazza Cinquecento / Marsala)",
            shortName = "Marsala Nord",
            description = "Fronte hotel e collegamenti navette aeroportuali. Elevato traffico taxi.",
            congestionPercent = 85,
            estimatedSearchTimeMinutes = 18,
            availableSpotsEstimate = 7,
            normX = 0.55f,
            normY = 0.14f,
            latitude = 41.9028,
            longitude = 12.5028,
            heatRadiusDp = 60f,
            ztlStatusNote = "Fuori ZTL",
            recommendation = "Ricerca difficile dopo le 09:00. Pochi stalli bianchi."
        ),
        RadarSubBlockDensity(
            id = "esquilino_turati_amedeo",
            name = "Esquilino Centro (Via Turati & Amedeo)",
            shortName = "Turati / Amedeo",
            description = "Strade rettilinee a senso unico ad alta densità commerciale.",
            congestionPercent = 74,
            estimatedSearchTimeMinutes = 14,
            availableSpotsEstimate = 14,
            normX = 0.46f,
            normY = 0.34f,
            latitude = 41.8988,
            longitude = 12.5052,
            heatRadiusDp = 58f,
            ztlStatusNote = "Fuori ZTL (Lavaggio strade Lun/Mar)",
            recommendation = "Controllare sempre i cartelli del lavaggio notturno."
        ),
        RadarSubBlockDensity(
            id = "piazza_vittorio_nord",
            name = "Piazza Vittorio Emanuele II (Portici Nord)",
            shortName = "P.zza Vittorio Nord",
            description = "Perimetro monumentale ampio con stalli blu e bianche a spina di pesce.",
            congestionPercent = 42,
            estimatedSearchTimeMinutes = 6,
            availableSpotsEstimate = 46,
            normX = 0.62f,
            normY = 0.50f,
            latitude = 41.8962,
            longitude = 12.5048,
            heatRadiusDp = 75f,
            ztlStatusNote = "Fuori ZTL (Ideale per raggiungere centro)",
            recommendation = "Zona consigliata: turnover costante e stalli bianchi gratuiti."
        ),
        RadarSubBlockDensity(
            id = "piazza_vittorio_sud",
            name = "Piazza Vittorio Sud (Machiavelli & Ferruccio)",
            shortName = "P.zza Vittorio Sud",
            description = "Area residenziale alberata, vicina a fermata Metro A e mercato rionale.",
            congestionPercent = 48,
            estimatedSearchTimeMinutes = 7,
            availableSpotsEstimate = 32,
            normX = 0.68f,
            normY = 0.66f,
            latitude = 41.8940,
            longitude = 12.5065,
            heatRadiusDp = 62f,
            ztlStatusNote = "Fuori ZTL (Presenza colonnine EV)",
            recommendation = "Ottima per soste superiori alle 2 ore con tariffa standard."
        ),
        RadarSubBlockDensity(
            id = "merulana_maggiore",
            name = "Asse Merulana (S. Maria Maggiore / S. Prassede)",
            shortName = "Merulana / Basilica",
            description = "Arteria di collegamento verso Rione Monti e Colosseo.",
            congestionPercent = 56,
            estimatedSearchTimeMinutes = 9,
            availableSpotsEstimate = 22,
            normX = 0.32f,
            normY = 0.58f,
            latitude = 41.8965,
            longitude = 12.5005,
            heatRadiusDp = 55f,
            ztlStatusNote = "Confine ZTL Centro Storico a 250m",
            recommendation = "Attenzione ai varchi ZTL attivi verso Via Cavour."
        ),
        RadarSubBlockDensity(
            id = "piazza_dante_manzoni",
            name = "Piazza Dante & Viale Manzoni",
            shortName = "P.zza Dante / Manzoni",
            description = "Zona silenziosa con strade ampie e parcheggio agevole anche in orari di punta.",
            congestionPercent = 32,
            estimatedSearchTimeMinutes = 3,
            availableSpotsEstimate = 42,
            normX = 0.52f,
            normY = 0.78f,
            latitude = 41.8912,
            longitude = 12.5058,
            heatRadiusDp = 70f,
            ztlStatusNote = "Fuori ZTL",
            recommendation = "Punto strategico: probabilità di trovare posto >80%."
        ),
        RadarSubBlockDensity(
            id = "san_lorenzo_bibiana",
            name = "Santa Bibiana / Varco San Lorenzo Est",
            shortName = "Varco S. Bibiana",
            description = "Strettoia sotto i binari ferroviari verso il quartiere universitario.",
            congestionPercent = 89,
            estimatedSearchTimeMinutes = 20,
            availableSpotsEstimate = 5,
            normX = 0.82f,
            normY = 0.38f,
            latitude = 41.8960,
            longitude = 12.5090,
            heatRadiusDp = 55f,
            ztlStatusNote = "ZTL San Lorenzo Notturna Ven-Sab",
            recommendation = "Rischio multa elevato la sera nel weekend."
        )
    )

    private var database: ParkingDatabase? = null

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _lastCacheSyncEpochMs = MutableStateFlow(System.currentTimeMillis())
    val lastCacheSyncEpochMs: StateFlow<Long> = _lastCacheSyncEpochMs.asStateFlow()

    // Global Region & City Provider state
    private val _selectedCity = MutableStateFlow(GlobalCityProvider.GLOBAL_PRESET_CITIES.first())
    val selectedCity: StateFlow<CityRegion> = _selectedCity.asStateFlow()

    // Real-Time Traffic ETA Service
    private val trafficEtaService = TrafficEtaService()
    private val _selectedAreaTrafficEta = MutableStateFlow<TrafficEtaResult?>(null)
    val selectedAreaTrafficEta: StateFlow<TrafficEtaResult?> = _selectedAreaTrafficEta.asStateFlow()

    // Language Preference state (en / it)
    private val _languagePreference = MutableStateFlow("it")
    val languagePreference: StateFlow<String> = _languagePreference.asStateFlow()

    // Places Autocomplete Service
    private val placesAutocompleteService = PlacesAutocompleteService()
    private val _placePredictions = MutableStateFlow<List<PlacePrediction>>(emptyList())
    val placePredictions: StateFlow<List<PlacePrediction>> = _placePredictions.asStateFlow()
    private val _isSearchingPlaces = MutableStateFlow(false)
    val isSearchingPlaces: StateFlow<Boolean> = _isSearchingPlaces.asStateFlow()

    // Dynamic User Preferences (Theme, Language, Units, Currency, Onboarding)
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    // Live Community Leaderboard
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(LeaderboardEntry.DEFAULT_COMMUNITY_LEADERBOARD)
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    var firestoreSyncManager: FirestoreSyncManager? = null
        private set
    var currentUserId: String? = null
        private set

    // Roma Servizi per la Mobilità Public Open Data Feed
    private val ztlPublicDataProvider = RomanZtlPublicDataProvider()
    val ztlPublicStatusFeed: StateFlow<ZtlLiveStatusFeed> = ztlPublicDataProvider.liveStatusFeed

    fun refreshZtlPublicData(): ZtlLiveStatusFeed {
        return ztlPublicDataProvider.refreshLiveStatus()
    }


    fun attachFirestoreSync(manager: FirestoreSyncManager, userId: String? = null) {
        this.firestoreSyncManager = manager
        this.currentUserId = userId
        scope.launch {
            manager.observeLeaderboard(userId).collect { entries ->
                if (entries.isNotEmpty()) {
                    _leaderboard.value = entries
                }
            }
        }
        if (userId != null) {
            scope.launch {
                manager.observeFavorites(userId).collect { cloudFavIds ->
                    if (cloudFavIds.isNotEmpty()) {
                        _userProfile.update { it.copy(favoriteAreaIds = it.favoriteAreaIds + cloudFavIds) }
                    }
                }
            }
        }
    }

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _placePredictions.value = emptyList()
            _isSearchingPlaces.value = false
            return
        }
        _isSearchingPlaces.value = true
        scope.launch {
            val results = placesAutocompleteService.searchPlaces(query)
            _placePredictions.value = results
            _isSearchingPlaces.value = false
        }
    }

    fun clearPlacePredictions() {
        _placePredictions.value = emptyList()
    }

    fun updateUserSettings(settings: UserSettings) {
        _userSettings.value = settings
        _languagePreference.value = settings.languageCode
    }

    fun completeOnboarding() {
        _userSettings.update { it.copy(hasCompletedOnboarding = true) }
    }

    private fun syncUserToLeaderboard(profile: UserProfile, userId: String?) {
        val uid = userId ?: currentUserId ?: return
        val entry = LeaderboardEntry(
            userId = uid,
            username = profile.username,
            points = profile.points,
            karmaScore = profile.karmaScore,
            rank = 1,
            badgeTitle = profile.badges.firstOrNull { it.isUnlocked }?.title ?: "Driver Globale",
            signalsShared = profile.signalsSharedCount,
            isCurrentUser = true
        )
        scope.launch {
            firestoreSyncManager?.syncLeaderboardEntry(entry)
        }
    }

    fun setLanguagePreference(lang: String) {
        _languagePreference.value = lang
        _userSettings.update { it.copy(language = lang) }
    }

    fun selectCity(city: CityRegion) {
        _selectedCity.value = city
        val regionalSpots = GlobalCityProvider.generateRegionalParkingData(city)
        _microAreas.value = regionalSpots
        scope.launch {
            try {
                database?.microAreaDao()?.clearAll()
                database?.microAreaDao()?.insertAll(regionalSpots.map { MicroAreaEntity.fromModel(it) })
            } catch (e: Exception) {}
        }
    }

    fun setNearestCityFromLocation(location: LatLng) {
        val nearest = GlobalCityProvider.findNearestCity(location)
        selectCity(nearest)
    }

    fun calculateTrafficEta(origin: LatLng, targetArea: MicroArea) {
        scope.launch {
            val eta = trafficEtaService.fetchRealtimeTrafficEta(
                origin = origin,
                destination = LatLng(targetArea.latitude, targetArea.longitude),
                destinationId = targetArea.id,
                destinationName = targetArea.name
            )
            _selectedAreaTrafficEta.value = eta
        }
    }

    fun clearTrafficEta() {
        _selectedAreaTrafficEta.value = null
    }

    fun saveCarLocation(location: SavedCarLocation) {
        _savedCarLocation.value = location
    }

    fun clearSavedCarLocation() {
        _savedCarLocation.value = null
    }

    fun updateMeterTimer(additionalMinutes: Int) {
        _savedCarLocation.update { current ->
            current?.let {
                val base = it.meterExpiryEpochMs ?: System.currentTimeMillis()
                val newExpiry = base + (additionalMinutes * 60 * 1000L)
                val duration = ((newExpiry - it.parkedAtEpochMs) / (60 * 1000L)).toInt().coerceAtLeast(additionalMinutes)
                it.copy(
                    meterDurationMinutes = duration,
                    meterExpiryEpochMs = newExpiry
                )
            }
        }
    }

    fun searchParkingAroundLocation(center: LatLng, radiusMeters: Double = 1500.0): List<MicroArea> {
        val found = parkingDataProvider.searchParkingAround(center, radiusMeters)
        _microAreas.value = found
        scope.launch {
            try {
                database?.microAreaDao()?.clearAll()
                database?.microAreaDao()?.insertAll(found.map { MicroAreaEntity.fromModel(it) })
            } catch (e: Exception) {}
        }
        return found
    }

    fun calculateMultiModalTrip(parking: MicroArea, origin: LatLng, destination: LatLng): MultiModalParkingOption {
        return parkingDataProvider.calculateMultiModalTrip(parking, origin, destination)
    }

    fun getSpeedCamerasAround(center: LatLng, radiusMeters: Double = 5000.0): List<SpeedCamera> {
        return speedCameraProvider.getSpeedCamerasAround(center, radiusMeters)
    }

    fun checkSpeedCameraWarning(currentLocation: LatLng, currentSpeedKmh: Int): SpeedCameraWarning? {
        return speedCameraProvider.getSpeedCameraWarning(currentLocation, currentSpeedKmh)
    }


    fun updateUserFromAuth(user: FirebaseUser) {
        this.currentUserId = user.uid
        _userProfile.update { current ->
            current.copy(
                email = user.email ?: current.email,
                username = user.displayName ?: current.username
            )
        }
        scope.launch {
            firestoreSyncManager?.saveUserProfile(_userProfile.value, user.uid)
            syncUserToLeaderboard(_userProfile.value, user.uid)
        }
    }

    fun attachDatabase(db: ParkingDatabase) {
        this.database = db
        scope.launch {
            try {
                val cachedCount = db.microAreaDao().count()
                if (cachedCount > 0) {
                    // Read user-searched cached data from Room
                    val cachedEntities = db.microAreaDao().getAllCachedAreasSnapshot()
                    if (cachedEntities.isNotEmpty()) {
                        _microAreas.value = cachedEntities.map { it.toModel() }
                    }
                    val cachedSignals = db.departureSignalDao().getActiveCachedSignalsSnapshot(System.currentTimeMillis())
                    if (cachedSignals.isNotEmpty()) {
                        _activeSignals.value = cachedSignals.map { it.toModel() }
                    }
                }


                // Caching layer for User Activities in Room
                val activityCount = db.userActivityDao().getActivityCount()
                if (activityCount == 0) {
                    val currentActs = _userActivities.value
                    if (currentActs.isNotEmpty()) {
                        db.userActivityDao().insertAll(currentActs.map { UserActivityEntity.fromModel(it) })
                    }
                } else {
                    val cachedActs = db.userActivityDao().getAllActivities()
                    if (cachedActs.isNotEmpty()) {
                        _userActivities.value = cachedActs.map { it.toModel() }
                    }
                }

                // Favorite areas in Room
                launch {
                    db.favoriteParkingAreaDao().getAllFavoriteAreaIds().collect { favIds ->
                        if (favIds.isNotEmpty()) {
                            _userProfile.update { it.copy(favoriteAreaIds = favIds.toSet()) }
                        } else {
                            // Seed default favorites into Room
                            _userProfile.value.favoriteAreaIds.forEach { fid ->
                                val a = initialMicroAreas.find { it.id == fid }
                                if (a != null) {
                                    db.favoriteParkingAreaDao().insertFavorite(
                                        FavoriteParkingAreaEntity(
                                            areaId = a.id,
                                            name = a.name,
                                            streetName = a.streetName,
                                            latitude = a.latitude,
                                            longitude = a.longitude,
                                            baselineAvailability = a.baselineAvailability,
                                            blueLineTariff = a.blueLineTariff,
                                            hasEvCharging = a.hasEvCharging,
                                            isZtlBoundary = a.isZtlBoundary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                _lastCacheSyncEpochMs.value = System.currentTimeMillis()
                updateMicroAreaSignalCounts()
            } catch (e: Exception) {
                // Room fallback
            }
        }
    }

    fun toggleOfflineMode() {
        _isOfflineMode.update { !it }
    }

    fun recordUserActivity(
        type: UserActivityType,
        title: String,
        microAreaId: String,
        microAreaName: String,
        streetAddress: String,
        latitude: Double,
        longitude: Double,
        rewardPoints: Int,
        statusNotes: String,
        impactMetric: String
    ) {
        val record = UserActivityRecord(
            id = UUID.randomUUID().toString(),
            type = type,
            title = title,
            microAreaId = microAreaId,
            microAreaName = microAreaName,
            streetAddress = streetAddress,
            latitude = latitude,
            longitude = longitude,
            timestampEpochMs = System.currentTimeMillis(),
            rewardPoints = rewardPoints,
            statusNotes = statusNotes,
            impactMetric = impactMetric
        )
        _userActivities.update { listOf(record) + it }
        database?.let { db ->
            scope.launch {
                try {
                    db.userActivityDao().insertActivity(UserActivityEntity.fromModel(record))
                    _lastCacheSyncEpochMs.value = System.currentTimeMillis()
                } catch (e: Exception) {}
            }
        }
    }

    init {
        val now = System.currentTimeMillis()
        val initialSignal = DepartureSignal(
            id = "seed-signal-1",
            microAreaId = "pza_vittorio_nord",
            microAreaName = "Piazza Vittorio (Portici Nord)",
            streetName = "Piazza Vittorio Emanuele II",
            latitude = 41.8962,
            longitude = 12.5048,
            createdEpochMs = now - 60_000L,
            expiryEpochMs = now + 240_000L,
            userReliabilityPercent = 95,
            feedbackFoundCount = 2,
            feedbackNotFoundCount = 0,
            isUserSubmitted = false
        )
        _activeSignals.value = listOf(initialSignal)

        // Seed an upcoming departure in Via Giolitti (someone parked who leaves in 15 minutes)
        val seedStay = ParkedStay(
            id = "seed-stay-1",
            microAreaId = "via_giolitti_stazione",
            microAreaName = "Stazione Termini (Ala Binari Giolitti)",
            streetName = "Via Giovanni Giolitti",
            latitude = 41.8992,
            longitude = 12.5035,
            parkedAtEpochMs = now - (15 * 60 * 1000L),
            duration = StayDuration.MIN_30,
            expectedDepartureEpochMs = now + (15 * 60 * 1000L),
            isUserVehicle = false
        )
        _activeParkedStays.value = listOf(seedStay)

        // Pre-populate realistic past user activity & community reports history
        val seedActivities = listOf(
            UserActivityRecord(
                id = "act-1",
                type = UserActivityType.REPORTED_DEPARTURE,
                title = "Segnalato posto libero",
                microAreaId = "via_giolitti_stazione",
                microAreaName = "Stazione Termini (Giolitti)",
                streetAddress = "Via Giovanni Giolitti 44",
                latitude = 41.8992,
                longitude = 12.5035,
                timestampEpochMs = now - (35 * 60 * 1000L),
                rewardPoints = 50,
                statusNotes = "3 automobilisti hanno ringraziato • Posto occupato dopo 4 min",
                impactMetric = "Risparmiati 15 min di traffico • CO₂: 280g"
            ),
            UserActivityRecord(
                id = "act-2",
                type = UserActivityType.REGISTERED_STAY,
                title = "Sosta registrata (30 min)",
                microAreaId = "pza_vittorio_nord",
                microAreaName = "Piazza Vittorio Emanuele II",
                streetAddress = "Piazza Vittorio Emanuele II 80",
                latitude = 41.8962,
                longitude = 12.5048,
                timestampEpochMs = now - (140 * 60 * 1000L),
                rewardPoints = 25,
                statusNotes = "Sosta completata in orario • Orario stimato rispettato",
                impactMetric = "Aiutati altri automobilisti in arrivo"
            ),
            UserActivityRecord(
                id = "act-3",
                type = UserActivityType.FOUND_SPOT,
                title = "Posto libero confermato trovato",
                microAreaId = "esquilino_turati",
                microAreaName = "Esquilino Centro",
                streetAddress = "Via Filippo Turati 38",
                latitude = 41.8988,
                longitude = 12.5052,
                timestampEpochMs = now - (25 * 3600 * 1000L),
                rewardPoints = 20,
                statusNotes = "Confermato stallo strisce blu valido",
                impactMetric = "Parcheggio trovato in 3 min invece di 18 min"
            ),
            UserActivityRecord(
                id = "act-4",
                type = UserActivityType.VERIFIED_SPOT,
                title = "Verifica accuratezza segnalazione",
                microAreaId = "esquilino_amedeo",
                microAreaName = "Esquilino / Principe Amedeo",
                streetAddress = "Via Principe Amedeo 12",
                latitude = 41.8995,
                longitude = 12.5020,
                timestampEpochMs = now - (48 * 3600 * 1000L),
                rewardPoints = 30,
                statusNotes = "Feedback positivo inviato • Affidabilità comunitaria +2%",
                impactMetric = "Affidabilità mappa incrementata"
            ),
            UserActivityRecord(
                id = "act-5",
                type = UserActivityType.REPORTED_DEPARTURE,
                title = "Segnalato posto libero",
                microAreaId = "pza_dante_manzoni",
                microAreaName = "Piazza Dante / Manzoni",
                streetAddress = "Viale Manzoni 24",
                latitude = 41.8912,
                longitude = 12.5058,
                timestampEpochMs = now - (72 * 3600 * 1000L),
                rewardPoints = 50,
                statusNotes = "Badge 'Pioniere Esquilino' avanzato • 4 voti utili",
                impactMetric = "Risparmiati 20 min di giri a vuoto"
            )
        )
        _userActivities.value = seedActivities

        updateMicroAreaSignalCounts()
        startSignalDecayTicker()
    }

    private fun startSignalDecayTicker() {
        scope.launch {
            while (isActive) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val currentSignals = _activeSignals.value

                // Expired signal penalty check
                val newlyExpired = currentSignals.filter {
                    it.isUserSubmitted && it.expiryEpochMs <= now && !it.hasExpiredPenaltyApplied
                }
                if (newlyExpired.isNotEmpty()) {
                    _userProfile.update { profile ->
                        val deduction = newlyExpired.size * 5
                        val newPoints = (profile.points - deduction).coerceAtLeast(0)
                        profile.copy(points = newPoints)
                    }
                }

                val updatedSignals = currentSignals.filter { it.expiryEpochMs > now }
                if (updatedSignals.size != currentSignals.size) {
                    _activeSignals.value = updatedSignals
                    updateMicroAreaSignalCounts()
                }

                // Check expired parked stays
                val currentStays = _activeParkedStays.value
                val updatedStays = currentStays.filter { it.expectedDepartureEpochMs > now }
                if (updatedStays.size != currentStays.size) {
                    _activeParkedStays.value = updatedStays
                    updateMicroAreaSignalCounts()
                }
            }
        }
    }

    private fun updateMicroAreaSignalCounts() {
        val signals = _activeSignals.value
        val stays = _activeParkedStays.value
        _microAreas.update { list ->
            list.map { area ->
                val signalCount = signals.count { it.microAreaId == area.id }
                val upcomingCount = stays.count { it.microAreaId == area.id }
                area.copy(
                    activeSignalsCount = signalCount,
                    upcomingDeparturesCount = upcomingCount
                )
            }
        }
    }

    fun submitDepartureSignal(microAreaId: String, durationMinutes: Int = 5): DepartureSignal {
        val area = _microAreas.value.find { it.id == microAreaId }
            ?: initialMicroAreas.find { it.id == microAreaId }
            ?: _microAreas.value.firstOrNull()
            ?: initialMicroAreas.first()
        val now = System.currentTimeMillis()
        val durationMs = durationMinutes.coerceIn(1, 60) * 60 * 1000L
        val newSignal = DepartureSignal(
            id = UUID.randomUUID().toString(),
            microAreaId = area.id,
            microAreaName = area.name,
            streetName = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            createdEpochMs = now,
            expiryEpochMs = now + durationMs,
            userReliabilityPercent = _userProfile.value.karmaScore,
            feedbackFoundCount = 0,
            feedbackNotFoundCount = 0,
            isUserSubmitted = true
        )

        _activeSignals.update { listOf(newSignal) + it }

        _userProfile.update { current ->
            val updatedShared = current.signalsSharedCount + 1
            val updatedPoints = current.points + 50
            val updatedKarma = (current.karmaScore + 2).coerceAtMost(100)
            val updatedProfile = current.copy(
                signalsSharedCount = updatedShared,
                points = updatedPoints,
                karmaScore = updatedKarma,
                levelRank = computeLevelRank(updatedPoints)
            )
            evaluateBadges(updatedProfile)
        }

        updateMicroAreaSignalCounts()

        recordUserActivity(
            type = UserActivityType.REPORTED_DEPARTURE,
            title = "Segnalato posto libero",
            microAreaId = area.id,
            microAreaName = area.name,
            streetAddress = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            rewardPoints = 50,
            statusNotes = "Segnalazione attiva (~$durationMinutes min) • Notificati guidatori vicini",
            impactMetric = "Risparmiati 15 min di traffico • Riduzione CO₂"
        )

        database?.let { db ->
            scope.launch {
                try {
                    db.departureSignalDao().insertSignal(DepartureSignalEntity.fromModel(newSignal))
                    db.userParkingReportDao().insertReport(
                        UserParkingReportEntity(
                            id = newSignal.id,
                            microAreaId = area.id,
                            microAreaName = area.name,
                            streetName = area.streetName,
                            latitude = area.latitude,
                            longitude = area.longitude,
                            status = "LIBERO_TRA_${durationMinutes}_MIN",
                            parkingType = if (area.whiteLineSpots > 0) "STRISCE_BIANCHE" else "STRISCE_BLU",
                            countdownMinutes = durationMinutes,
                            userReliabilityPercent = newSignal.userReliabilityPercent,
                            timestampEpochMs = now,
                            expiryEpochMs = newSignal.expiryEpochMs,
                            notes = "Posto in fase di liberazione segnalato dall'utente",
                            karmaEarned = 50
                        )
                    )
                    _lastCacheSyncEpochMs.value = System.currentTimeMillis()
                } catch (e: Exception) {}
            }
        }

        return newSignal
    }

    /**
     * User specifies how long they will occupy the spot before freeing it (scheduled departure).
     */
    fun registerUpcomingDeparture(microAreaId: String, durationMinutes: Int): ParkedStay {
        val area = _microAreas.value.find { it.id == microAreaId }
            ?: initialMicroAreas.find { it.id == microAreaId }
            ?: _microAreas.value.firstOrNull()
            ?: initialMicroAreas.first()
        val now = System.currentTimeMillis()
        val matchedDuration = when {
            durationMinutes <= 5 -> StayDuration.MIN_5
            durationMinutes <= 10 -> StayDuration.MIN_10
            durationMinutes <= 15 -> StayDuration.MIN_15
            durationMinutes <= 30 -> StayDuration.MIN_30
            durationMinutes <= 60 -> StayDuration.HOUR_1
            durationMinutes <= 120 -> StayDuration.HOUR_2
            durationMinutes <= 180 -> StayDuration.HOUR_3
            else -> StayDuration.HOUR_4_PLUS
        }

        val stay = ParkedStay(
            id = UUID.randomUUID().toString(),
            microAreaId = area.id,
            microAreaName = area.name,
            streetName = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            parkedAtEpochMs = now,
            duration = matchedDuration,
            expectedDepartureEpochMs = now + (durationMinutes * 60 * 1000L),
            isUserVehicle = true
        )

        _activeParkedStays.update { listOf(stay) + it }

        _userProfile.update { current ->
            val updatedPoints = current.points + 50
            val updatedProfile = current.copy(
                parkingsRegisteredCount = current.parkingsRegisteredCount + 1,
                points = updatedPoints,
                activeStay = stay,
                levelRank = computeLevelRank(updatedPoints)
            )
            evaluateBadges(updatedProfile)
        }

        updateMicroAreaSignalCounts()

        recordUserActivity(
            type = UserActivityType.REPORTED_DEPARTURE,
            title = "Programmata partenza ($durationMinutes min)",
            microAreaId = area.id,
            microAreaName = area.name,
            streetAddress = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            rewardPoints = 50,
            statusNotes = "Notifica programmata alla community • Stallo in occupazione temporanea",
            impactMetric = "Pianificazione preventiva della rotazione stalli"
        )

        return stay
    }

    // NEW: Register stay duration when user parks their vehicle!
    fun registerParkedStay(microAreaId: String, duration: StayDuration): ParkedStay {
        val area = _microAreas.value.find { it.id == microAreaId }
            ?: initialMicroAreas.find { it.id == microAreaId }
            ?: _microAreas.value.firstOrNull()
            ?: initialMicroAreas.first()
        val now = System.currentTimeMillis()
        val stayDurationMs = duration.minutes * 60 * 1000L
        val stay = ParkedStay(
            id = UUID.randomUUID().toString(),
            microAreaId = area.id,
            microAreaName = area.name,
            streetName = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            parkedAtEpochMs = now,
            duration = duration,
            expectedDepartureEpochMs = now + stayDurationMs,
            isUserVehicle = true
        )

        _activeParkedStays.update { listOf(stay) + it }

        // Reward user with +25 points for helping community forecast parking availability!
        _userProfile.update { current ->
            val updatedPoints = current.points + 25
            val updatedProfile = current.copy(
                parkingsRegisteredCount = current.parkingsRegisteredCount + 1,
                points = updatedPoints,
                activeStay = stay,
                levelRank = computeLevelRank(updatedPoints)
            )
            evaluateBadges(updatedProfile)
        }

        updateMicroAreaSignalCounts()

        recordUserActivity(
            type = UserActivityType.REGISTERED_STAY,
            title = "Registrata sosta (${duration.label})",
            microAreaId = area.id,
            microAreaName = area.name,
            streetAddress = area.streetName,
            latitude = area.latitude,
            longitude = area.longitude,
            rewardPoints = 25,
            statusNotes = "Stallo registrato in ${area.name} • Partenza prevista calcolata",
            impactMetric = "Contributo alla trasparenza disponibilità a Termini"
        )

        return stay
    }

    // User is leaving early from their parked spot -> turn it into a 5-min departure signal
    fun convertStayToDepartureSignal(stay: ParkedStay): DepartureSignal {
        _activeParkedStays.update { list -> list.filterNot { it.id == stay.id } }
        _userProfile.update { it.copy(activeStay = null) }
        return submitDepartureSignal(stay.microAreaId)
    }

    fun clearActiveStay() {
        val stay = _userProfile.value.activeStay
        if (stay != null) {
            _activeParkedStays.update { list -> list.filterNot { it.id == stay.id } }
            _userProfile.update { it.copy(activeStay = null) }
            updateMicroAreaSignalCounts()
        }
    }

    fun submitFeedback(signalId: String, feedbackType: FeedbackType) {
        val targetSignal = _activeSignals.value.find { it.id == signalId }
        var isTargetUserSubmitted = false
        _activeSignals.update { list ->
            list.mapNotNull { signal ->
                if (signal.id == signalId) {
                    isTargetUserSubmitted = signal.isUserSubmitted
                    when (feedbackType) {
                        FeedbackType.FOUND -> signal.copy(
                            feedbackFoundCount = signal.feedbackFoundCount + 1,
                            userReliabilityPercent = (signal.userReliabilityPercent + 1).coerceAtMost(100)
                        )
                        FeedbackType.NOT_FOUND -> signal.copy(
                            feedbackNotFoundCount = signal.feedbackNotFoundCount + 1,
                            userReliabilityPercent = (signal.userReliabilityPercent - 4).coerceAtLeast(40)
                        )
                        FeedbackType.WRONG_DATA -> {
                            val abuses = signal.reportedAbuseCount + 1
                            if (abuses >= 2) null
                            else signal.copy(
                                reportedAbuseCount = abuses,
                                userReliabilityPercent = (signal.userReliabilityPercent - 15).coerceAtLeast(20)
                            )
                        }
                    }
                } else signal
            }
        }

        _userProfile.update { current ->
            val deltaPoints = when (feedbackType) {
                FeedbackType.FOUND -> 30
                FeedbackType.NOT_FOUND -> if (isTargetUserSubmitted) -20 else 5
                FeedbackType.WRONG_DATA -> if (isTargetUserSubmitted) -40 else 5
            }
            val deltaKarma = when (feedbackType) {
                FeedbackType.FOUND -> 2
                FeedbackType.NOT_FOUND -> if (isTargetUserSubmitted) -4 else 1
                FeedbackType.WRONG_DATA -> if (isTargetUserSubmitted) -10 else 1
            }

            val newPoints = (current.points + deltaPoints).coerceAtLeast(0)
            val newKarma = (current.karmaScore + deltaKarma).coerceIn(20, 100)
            val updatedProfile = current.copy(
                signalsVerifiedCount = current.signalsVerifiedCount + 1,
                points = newPoints,
                karmaScore = newKarma,
                levelRank = computeLevelRank(newPoints)
            )
            evaluateBadges(updatedProfile)
        }
        updateMicroAreaSignalCounts()

        val feedTitle = when (feedbackType) {
            FeedbackType.FOUND -> "Posto confermato trovato"
            FeedbackType.NOT_FOUND -> "Segnalato posto già occupato"
            FeedbackType.WRONG_DATA -> "Segnalata discrepanza dati"
        }
        val feedPoints = when (feedbackType) {
            FeedbackType.FOUND -> 30
            FeedbackType.NOT_FOUND -> 5
            FeedbackType.WRONG_DATA -> 5
        }
        recordUserActivity(
            type = if (feedbackType == FeedbackType.FOUND) UserActivityType.FOUND_SPOT else UserActivityType.VERIFIED_SPOT,
            title = feedTitle,
            microAreaId = targetSignal?.microAreaId ?: "termini_area",
            microAreaName = targetSignal?.microAreaName ?: "Area Termini",
            streetAddress = targetSignal?.streetName ?: "Via Roma",
            latitude = targetSignal?.latitude ?: 41.8992,
            longitude = targetSignal?.longitude ?: 12.5028,
            rewardPoints = feedPoints,
            statusNotes = "Feedback community verificato • Karma aggiornato",
            impactMetric = "Qualità dati e affidabilità migliorata"
        )
    }

    private fun computeLevelRank(points: Int): String {
        return when {
            points >= 800 -> "Senatore della Mobilità (Liv. 5)"
            points >= 500 -> "Tribuno di Roma (Liv. 4)"
            points >= 300 -> "Centurione della Sosta (Liv. 3)"
            points >= 150 -> "Legionario Urbano (Liv. 2)"
            else -> "Esploratore Esquilino (Liv. 1)"
        }
    }

    private fun evaluateBadges(profile: UserProfile): UserProfile {
        val now = System.currentTimeMillis()
        val updatedBadges = profile.badges.map { badge ->
            when (badge.id) {
                "parking_scout" -> {
                    val unlocked = profile.signalsSharedCount >= 3
                    badge.copy(
                        currentProgress = profile.signalsSharedCount,
                        targetProgress = 3,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "community_hero" -> {
                    val unlocked = profile.karmaScore >= 95 && profile.signalsVerifiedCount >= 5
                    badge.copy(
                        currentProgress = profile.karmaScore,
                        targetProgress = 95,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "street_sentinel" -> {
                    val unlocked = profile.signalsSharedCount >= 10
                    badge.copy(
                        currentProgress = profile.signalsSharedCount,
                        targetProgress = 10,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "accurate_reporter" -> {
                    val unlocked = profile.karmaScore >= 90
                    badge.copy(
                        currentProgress = profile.karmaScore,
                        targetProgress = 90,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "ztl_guardian" -> {
                    val unlocked = profile.signalsVerifiedCount >= 3
                    badge.copy(
                        currentProgress = profile.signalsVerifiedCount.coerceAtMost(3),
                        targetProgress = 3,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "esquilino_pioneer" -> {
                    val unlocked = profile.signalsSharedCount >= 1
                    badge.copy(
                        currentProgress = profile.signalsSharedCount.coerceAtMost(1),
                        targetProgress = 1,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "gold_verifier" -> {
                    val unlocked = profile.signalsVerifiedCount >= 15
                    badge.copy(
                        currentProgress = profile.signalsVerifiedCount,
                        targetProgress = 15,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "centurione_500" -> {
                    val unlocked = profile.points >= 500
                    badge.copy(
                        currentProgress = profile.points,
                        targetProgress = 500,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                "stay_planner" -> {
                    val unlocked = profile.parkingsRegisteredCount >= 2
                    badge.copy(
                        currentProgress = profile.parkingsRegisteredCount,
                        targetProgress = 2,
                        isUnlocked = unlocked,
                        unlockedAtEpochMs = if (unlocked && badge.unlockedAtEpochMs == null) now else badge.unlockedAtEpochMs
                    )
                }
                else -> badge
            }
        }
        return profile.copy(badges = updatedBadges)
    }

    fun updateVehicleData(
        licensePlate: String,
        vehicleModel: String,
        vehiclePermit: VehiclePermitType,
        permitNumber: String,
        permitExpiryDate: String,
        isPermitActive: Boolean
    ) {
        _userProfile.update { current ->
            current.copy(
                licensePlate = licensePlate.trim().uppercase(),
                vehicleModel = vehicleModel.trim(),
                vehiclePermit = vehiclePermit,
                permitNumber = permitNumber.trim(),
                permitExpiryDate = permitExpiryDate.trim(),
                isPermitActive = isPermitActive
            )
        }
    }

    fun toggleFavorite(microAreaId: String, userId: String? = null) {
        val willBeFav = !_userProfile.value.favoriteAreaIds.contains(microAreaId)
        _userProfile.update { profile ->
            val favs = profile.favoriteAreaIds.toMutableSet()
            if (favs.contains(microAreaId)) favs.remove(microAreaId)
            else favs.add(microAreaId)
            profile.copy(favoriteAreaIds = favs)
        }
        val targetUid = userId ?: currentUserId
        val area = _microAreas.value.find { it.id == microAreaId }
        if (targetUid != null && area != null) {
            scope.launch {
                firestoreSyncManager?.syncFavorite(targetUid, area, willBeFav)
            }
        }
        database?.let { db ->
            scope.launch {
                try {
                    if (willBeFav && area != null) {
                        db.favoriteParkingAreaDao().insertFavorite(
                            FavoriteParkingAreaEntity(
                                areaId = area.id,
                                name = area.name,
                                streetName = area.streetName,
                                latitude = area.latitude,
                                longitude = area.longitude,
                                baselineAvailability = area.baselineAvailability,
                                blueLineTariff = area.blueLineTariff,
                                hasEvCharging = area.hasEvCharging,
                                isZtlBoundary = area.isZtlBoundary
                            )
                        )
                    } else {
                        db.favoriteParkingAreaDao().deleteFavoriteById(microAreaId)
                    }
                } catch (e: Exception) {}
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _userProfile.update { it.copy(notificationsEnabled = enabled) }
    }

    fun deleteAccountData() {
        _userProfile.value = UserProfile(
            email = "ospite.anonimo@roma.it",
            points = 0,
            levelRank = "Nuovo Utente",
            karmaScore = 50,
            signalsSharedCount = 0,
            signalsVerifiedCount = 0,
            parkingsRegisteredCount = 0,
            notificationsEnabled = false,
            favoriteAreaIds = emptySet(),
            activeStay = null,
            badges = listOf(
                Badge("top_contributor", "Top Contributor", "5+ segnalazioni di posti condivise", isUnlocked = false),
                Badge("accurate_reporter", "Accurate Reporter", "Oltre 90% di affidabilità confermata", isUnlocked = false),
                Badge("esquilino_pioneer", "Pioniere Esquilino", "Prima segnalazione nell'area", isUnlocked = false),
                Badge("centurione_300", "Centurione 500+", "Raggiungi 500 punti sosta", isUnlocked = false),
                Badge("stay_planner", "Programmatore Sosta", "Registra la durata della sosta", isUnlocked = false)
            )
        )
        _activeSignals.update { list -> list.filterNot { it.isUserSubmitted } }
        _activeParkedStays.update { list -> list.filterNot { it.isUserVehicle } }
        updateMicroAreaSignalCounts()
    }
}
