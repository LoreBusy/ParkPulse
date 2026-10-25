package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.CopilotAnalysisResult
import com.example.data.ai.CopilotModelChoice
import com.example.data.ai.GeminiCopilotService
import com.example.data.local.ParkingDatabase
import com.example.data.model.RadarSubBlockDensity
import com.example.data.model.DepartureSignal
import com.example.data.model.DepartureSliderMode
import com.example.data.model.DistrictSector
import com.example.data.model.FeedbackType
import com.example.data.model.HazardAlert
import com.example.data.model.MapRendererType
import com.example.data.model.MicroArea
import com.example.data.model.ParkedStay
import com.example.data.model.ParkingSearchFilter
import com.example.data.model.ParkingTypeFilter
import com.example.data.model.StayDuration
import com.example.data.model.UserProfile
import com.example.data.model.ViewMode
import com.example.data.model.ZtlGate
import com.example.data.model.ZtlWarningAlert
import com.example.data.model.ZtlZone
import com.example.data.model.UserActivityRecord
import com.example.data.model.UserActivityType
import com.example.data.model.ActiveNavigationSession
import com.example.data.repository.ParkingRepository
import com.example.util.NavigationHelper
import com.example.util.ParkingNotificationService
import com.example.util.SmartNavigatorEngine
import com.example.util.ZtlNotificationHelper
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import com.example.data.ai.GeminiParkingMlPredictor
import com.example.data.model.EmergencyTrafficIncident
import com.example.data.model.MlParkingPrediction
import com.example.data.model.VehiclePermitType
import com.example.data.model.ZtlRouteBreachWarning
import com.example.util.TerminiTrafficAlertWorker
import com.example.util.ZtlLiveMonitorService
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.TrafficEtaResult
import com.example.data.provider.CityRegion
import com.example.data.provider.GlobalCityProvider
import com.example.data.model.UserSettings
import com.example.data.model.PlacePrediction
import com.example.data.model.ParkingTypeCategory
import com.example.data.model.LeaderboardEntry
import com.example.data.model.ThemeMode
import com.google.firebase.auth.FirebaseUser
import com.example.data.model.SavedCarLocation
import com.example.data.model.SpeedCamera
import com.example.data.model.MultiModalParkingOption
import com.example.data.provider.SpeedCameraWarning
import com.example.data.provider.ZtlLiveStatusFeed
import com.example.data.provider.ZtlZoneLiveStatus
import com.example.data.provider.GateOperationalStatus

data class ParkingUiState(
    val viewMode: ViewMode = ViewMode.MAP,
    val mapRendererType: MapRendererType = MapRendererType.GOOGLE_MAPS,
    val userLocation: LatLng? = null, // Real GPS only, clean on startup
    val hasLocationPermission: Boolean = false,
    val microAreas: List<MicroArea> = emptyList(),
    val filteredMicroAreas: List<MicroArea> = emptyList(),
    val savedMicroAreas: List<MicroArea> = emptyList(),
    val allFavorites: List<MicroArea> = emptyList(),
    val activeTrafficIncidents: List<EmergencyTrafficIncident> = emptyList(),
    val activeSignals: List<DepartureSignal> = emptyList(),
    val activeParkedStays: List<ParkedStay> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val selectedMicroArea: MicroArea? = null,
    val searchQuery: String = "",
    val searchFilter: ParkingSearchFilter = ParkingSearchFilter(),
    val showFilterSheet: Boolean = false,
    val showDepartureModal: Boolean = false,
    val departureAreaId: String = "pza_vittorio_nord",
    val departureSliderDuration: Int = 5,
    val departureSliderMode: DepartureSliderMode = DepartureSliderMode.FREE_SPOT_WINDOW,
    val showRegisterStayModal: Boolean = false,
    val areaForStayRegistration: MicroArea? = null,
    val signalForFeedback: DepartureSignal? = null,
    val showZtlSheet: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val showVehiclePermitDialog: Boolean = false,
    val showMlTrendsSheet: Boolean = false,
    val mlPrediction: MlParkingPrediction? = null,
    val mlPredictionIsLoading: Boolean = false,
    val mlTargetArea: MicroArea? = null,
    val pendingZtlRouteBreach: ZtlRouteBreachWarning? = null,
    val copilotQuery: String = "",
    val copilotTargetStreet: String = "Via Cavour",
    val copilotModel: CopilotModelChoice = CopilotModelChoice.MAPS_GROUNDED_FLASH,
    val copilotIsLoading: Boolean = false,
    val copilotResult: CopilotAnalysisResult? = null,
    val isZtlLayerVisible: Boolean = true,
    val showZtlVisibilitySheet: Boolean = false,
    val visibleZtlZoneIds: Set<String> = emptySet(),
    val ztlZones: List<ZtlZone> = emptyList(),
    val selectedZtlZone: ZtlZone? = null,
    val activeZtlWarning: ZtlWarningAlert? = null,
    val activeCongestionAlertSubBlock: RadarSubBlockDensity? = null,
    val activeSpotAvailableAlertSignal: DepartureSignal? = null,
    val ztlGates: List<ZtlGate> = emptyList(),
    val hazards: List<HazardAlert> = emptyList(),
    val sectors: List<DistrictSector> = emptyList(),
    val radarSubBlocks: List<RadarSubBlockDensity> = emptyList(),
    val selectedSubBlock: RadarSubBlockDensity? = null,
    val userActivities: List<UserActivityRecord> = emptyList(),
    val filterActivityType: UserActivityType? = null,
    val isOfflineMode: Boolean = false,
    val lastCacheSyncEpochMs: Long = System.currentTimeMillis(),
    val activeNavigation: ActiveNavigationSession? = null,
    val toastMessage: String? = null,
    // Global & Auth additions
    val selectedCity: CityRegion = GlobalCityProvider.GLOBAL_PRESET_CITIES.first(),
    val selectedAreaTrafficEta: TrafficEtaResult? = null,
    val languagePreference: String = "it",
    val currentUser: FirebaseUser? = null,
    val isGuestMode: Boolean = false,
    val showLoginScreen: Boolean = false,
    val showCityPickerSheet: Boolean = false,
    // User Settings & Onboarding
    val userSettings: UserSettings = UserSettings(),
    val searchPredictions: List<PlacePrediction> = emptyList(),
    val isSearchingPlaces: Boolean = false,
    val selectedCategory: ParkingTypeCategory = ParkingTypeCategory.ALL,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val showSettingsDialog: Boolean = false,
    val showLeaderboardSheet: Boolean = false,
    // "Dove ho parcheggiato?" & Speed Camera additions
    val savedCarLocation: SavedCarLocation? = null,
    val showSavedCarSheet: Boolean = false,
    val isSpeedCameraLayerVisible: Boolean = true,
    val speedCameras: List<SpeedCamera> = emptyList(),
    val activeSpeedCameraWarning: SpeedCameraWarning? = null,
    val multiModalParkingOption: MultiModalParkingOption? = null,
    val isMultiModalCalculating: Boolean = false,
    val ztlLiveFeed: ZtlLiveStatusFeed? = null,
    val isRefreshingZtlPublicData: Boolean = false
)

class ParkingViewModel(
    val repository: ParkingRepository = ParkingRepository(),
    val authService: FirebaseAuthService = FirebaseAuthService(),
    private val copilotService: GeminiCopilotService = GeminiCopilotService(),
    private val mlPredictor: GeminiParkingMlPredictor = GeminiParkingMlPredictor()
) : ViewModel() {

    private fun isMapsKeyConfigured(): Boolean {
        return try {
            val key = com.example.BuildConfig.MAPS_API_KEY
            key.isNotBlank() && !key.contains("Placeholder", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    private val _uiState = MutableStateFlow(
        ParkingUiState(
            mapRendererType = MapRendererType.GOOGLE_MAPS,
            ztlGates = repository.ztlGates,
            ztlZones = repository.ztlZones,
            hazards = repository.romanHazards,
            sectors = repository.sectors,
            radarSubBlocks = repository.radarSubBlocks,
            currentUser = authService.currentUser
        )
    )

    val uiState: StateFlow<ParkingUiState> = combine(
        _uiState,
        repository.microAreas,
        repository.activeSignals,
        repository.activeParkedStays,
        repository.userProfile,
        repository.isOfflineMode,
        repository.lastCacheSyncEpochMs,
        repository.userActivities
    ) { args: Array<Any> ->
        val state = args[0] as ParkingUiState
        @Suppress("UNCHECKED_CAST")
        val areas = args[1] as List<MicroArea>
        @Suppress("UNCHECKED_CAST")
        val signals = args[2] as List<DepartureSignal>
        @Suppress("UNCHECKED_CAST")
        val stays = args[3] as List<ParkedStay>
        val profile = args[4] as UserProfile
        val offline = args[5] as Boolean
        val lastSync = args[6] as Long
        @Suppress("UNCHECKED_CAST")
        val activities = args[7] as List<UserActivityRecord>

        val filtered = areas.filter { area ->
            val matchesText = state.searchQuery.isBlank() ||
                area.name.contains(state.searchQuery, ignoreCase = true) ||
                area.streetName.contains(state.searchQuery, ignoreCase = true)

            matchesText && area.matchesFilter(state.searchFilter) && area.matchesCategory(state.selectedCategory)
        }

        val allFavs = areas.filter { profile.favoriteAreaIds.contains(it.id) }
        val saved = allFavs.filter { it.matchesFilter(state.searchFilter) && it.matchesCategory(state.selectedCategory) }

        val currentSelected = state.selectedMicroArea?.let { sel ->
            areas.find { it.id == sel.id }
        }

        state.copy(
            microAreas = areas,
            filteredMicroAreas = filtered,
            savedMicroAreas = saved,
            allFavorites = allFavs,
            activeSignals = signals,
            activeParkedStays = stays,
            userProfile = profile,
            selectedMicroArea = currentSelected,
            isOfflineMode = offline,
            lastCacheSyncEpochMs = lastSync,
            radarSubBlocks = repository.radarSubBlocks,
            userActivities = activities
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _uiState.value
    )

    val currentUiState: ParkingUiState
        get() = _uiState.value

    init {
        viewModelScope.launch {
            repository.savedCarLocation.collect { car ->
                _uiState.update { it.copy(savedCarLocation = car) }
            }
        }
        val defaultCameras = repository.getSpeedCamerasAround(LatLng(41.9028, 12.4964), 30000.0)
        _uiState.update { it.copy(speedCameras = defaultCameras) }

        viewModelScope.launch {
            TerminiTrafficAlertWorker.activeIncidentsFlow.collect { incidents ->
                _uiState.update { it.copy(activeTrafficIncidents = incidents) }
            }
        }
        viewModelScope.launch {
            repository.selectedCity.collect { city ->
                _uiState.update { it.copy(selectedCity = city) }
            }
        }
        viewModelScope.launch {
            repository.selectedAreaTrafficEta.collect { eta ->
                _uiState.update { it.copy(selectedAreaTrafficEta = eta) }
            }
        }
        viewModelScope.launch {
            repository.languagePreference.collect { lang ->
                _uiState.update { it.copy(languagePreference = lang) }
            }
        }
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                _uiState.update { it.copy(userSettings = settings) }
            }
        }
        viewModelScope.launch {
            repository.placePredictions.collect { predictions ->
                _uiState.update { it.copy(searchPredictions = predictions) }
            }
        }
        viewModelScope.launch {
            repository.isSearchingPlaces.collect { isSearching ->
                _uiState.update { it.copy(isSearchingPlaces = isSearching) }
            }
        }
        viewModelScope.launch {
            repository.leaderboard.collect { board ->
                _uiState.update { it.copy(leaderboard = board) }
            }
        }
        viewModelScope.launch {
            authService.authStateFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
        viewModelScope.launch {
            repository.ztlPublicStatusFeed.collect { feed ->
                _uiState.update { it.copy(ztlLiveFeed = feed) }
            }
        }
    }

    fun refreshZtlPublicData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingZtlPublicData = true) }
            val feed = repository.refreshZtlPublicData()
            _uiState.update {
                it.copy(
                    ztlLiveFeed = feed,
                    isRefreshingZtlPublicData = false,
                    toastMessage = "ZTL Roma aggiornata: ${feed.totalActiveZones} zone attive (${feed.totalActiveGates} varchi) • RSM Open Data"
                )
            }
        }
    }

    fun triggerWorkManagerTrafficAlert(context: Context) {
        TerminiTrafficAlertWorker.triggerImmediateEmergencyAlertCheck(
            context = context,
            title = "🚨 Chiusura Straordinaria: Via Giolitti FS",
            street = "Via Giolitti (Termini)",
            delayMinutes = 25,
            isClosure = true
        )
        _uiState.update {
            it.copy(toastMessage = "Allerta WorkManager inviata! Notifica in arrivo...")
        }
    }

    fun dismissTrafficIncident(incidentId: String) {
        _uiState.update { current ->
            current.copy(activeTrafficIncidents = current.activeTrafficIncidents.filterNot { it.id == incidentId })
        }
        TerminiTrafficAlertWorker.clearActiveIncidents()
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleMapRenderer() {
        _uiState.update {
            val next = if (it.mapRendererType == MapRendererType.GOOGLE_MAPS) {
                MapRendererType.TACTICAL_VECTOR
            } else {
                MapRendererType.GOOGLE_MAPS
            }
            it.copy(mapRendererType = next)
        }
    }

    fun updateLocationPermission(granted: Boolean, loc: LatLng? = null, context: Context? = null) {
        val wasNull = _uiState.value.userLocation == null
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                userLocation = loc ?: it.userLocation
            )
        }
        if (loc != null) {
            if (wasNull) {
                // Automatically discover nearby parking spots around user's real GPS position
                searchParkingNearLocation(loc)
            }
            checkZtlProximity(loc, context)
            checkCongestionProximity(loc, context)
            checkSpeedCameraAlert(loc, 45)
        }
    }

    fun searchParkingNearLocation(latLng: LatLng) {
        val found = repository.searchParkingAroundLocation(latLng)
        val cams = repository.getSpeedCamerasAround(latLng, 15000.0)
        _uiState.update { current ->
            current.copy(
                selectedMicroArea = found.firstOrNull(),
                speedCameras = cams,
                toastMessage = "Trovati ${found.size} parcheggi nell'area"
            )
        }
    }

    fun searchParkingForCurrentOrMapCenter(fallbackCenter: LatLng = LatLng(41.8992, 12.5028)) {
        val center = _uiState.value.userLocation ?: fallbackCenter
        searchParkingNearLocation(center)
    }

    fun saveCarParkedLocation(
        currentGps: LatLng? = null,
        address: String = "Posizione attuale veicolo",
        notes: String = "",
        meterMinutes: Int? = null,
        floor: String = ""
    ) {
        val targetLoc = currentGps ?: _uiState.value.userLocation ?: LatLng(41.8992, 12.5028)
        val expiry = if (meterMinutes != null && meterMinutes > 0) {
            System.currentTimeMillis() + (meterMinutes * 60 * 1000L)
        } else null

        val saved = SavedCarLocation(
            latitude = targetLoc.latitude,
            longitude = targetLoc.longitude,
            address = address,
            parkedAtEpochMs = System.currentTimeMillis(),
            notes = notes,
            meterDurationMinutes = meterMinutes,
            meterExpiryEpochMs = expiry,
            floorOrZone = floor
        )
        repository.saveCarLocation(saved)
        _uiState.update {
            it.copy(
                showSavedCarSheet = true,
                toastMessage = "Posizione auto salvata con successo!"
            )
        }
    }

    fun clearSavedCarLocation() {
        repository.clearSavedCarLocation()
        _uiState.update {
            it.copy(
                showSavedCarSheet = false,
                toastMessage = "Posizione auto rimossa."
            )
        }
    }

    fun updateMeterTimer(additionalMinutes: Int) {
        repository.updateMeterTimer(additionalMinutes)
    }

    fun toggleSavedCarSheet(show: Boolean) {
        _uiState.update { it.copy(showSavedCarSheet = show) }
    }

    fun toggleSpeedCameraLayer() {
        _uiState.update { it.copy(isSpeedCameraLayerVisible = !it.isSpeedCameraLayerVisible) }
    }

    fun checkSpeedCameraAlert(currentLocation: LatLng, speedKmh: Int) {
        val alert = repository.checkSpeedCameraWarning(currentLocation, speedKmh)
        _uiState.update { it.copy(activeSpeedCameraWarning = alert) }
    }

    fun dismissSpeedCameraAlert() {
        _uiState.update { it.copy(activeSpeedCameraWarning = null) }
    }

    fun calculateMultiModalForArea(area: MicroArea, origin: LatLng, destination: LatLng) {
        val opt = repository.calculateMultiModalTrip(area, origin, destination)
        _uiState.update { it.copy(multiModalParkingOption = opt) }
    }

    fun clearSearchResults() {
        _uiState.update { it.copy(selectedMicroArea = null, searchQuery = "") }
    }


    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateSearchFilter(filter: ParkingSearchFilter) {
        _uiState.update { it.copy(searchFilter = filter) }
    }

    fun resetSearchFilter() {
        _uiState.update { it.copy(searchFilter = ParkingSearchFilter()) }
    }

    fun toggleFilterSheet(show: Boolean) {
        _uiState.update { it.copy(showFilterSheet = show) }
    }

    fun selectMicroArea(area: MicroArea?) {
        _uiState.update { it.copy(selectedMicroArea = area) }
        if (area != null) {
            val origin = _uiState.value.userLocation ?: _uiState.value.selectedCity.center
            repository.calculateTrafficEta(origin, area)
        } else {
            repository.clearTrafficEta()
        }
    }

    fun selectCity(city: CityRegion) {
        repository.selectCity(city)
        _uiState.update {
            it.copy(
                selectedCity = city,
                selectedMicroArea = null,
                showCityPickerSheet = false,
                toastMessage = if (it.languagePreference == "it") "Città impostata: ${city.name} (${city.country})" else "City updated: ${city.name} (${city.country})"
            )
        }
    }

    fun setNearestCityFromLocation(location: LatLng) {
        val nearest = GlobalCityProvider.findNearestCity(location)
        selectCity(nearest)
    }

    fun toggleCityPicker(show: Boolean) {
        _uiState.update { it.copy(showCityPickerSheet = show) }
    }

    fun setLanguagePreference(lang: String) {
        repository.setLanguagePreference(lang)
        _uiState.update { it.copy(languagePreference = lang) }
    }

    fun onUserSignedIn(user: FirebaseUser) {
        _uiState.update {
            it.copy(
                currentUser = user,
                isGuestMode = false,
                showLoginScreen = false,
                userProfile = it.userProfile.copy(
                    email = user.email ?: it.userProfile.email,
                    username = user.displayName ?: it.userProfile.username
                ),
                toastMessage = "Accesso effettuato con successo: ${user.displayName ?: user.email ?: "Utente"}"
            )
        }
    }

    fun continueAsGuest() {
        _uiState.update {
            it.copy(
                isGuestMode = false,
                showLoginScreen = true,
                toastMessage = "Accesso ospite non consentito: per garantire l'affidabilità delle segnalazioni è richiesto il login verificato."
            )
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            authService.signOut(context)
            _uiState.update {
                it.copy(
                    currentUser = null,
                    isGuestMode = false,
                    showLoginScreen = true,
                    toastMessage = "Disconnessione completata. Accedi nuovamente per continuare."
                )
            }
        }
    }

    fun openLoginScreen() {
        _uiState.update { it.copy(showLoginScreen = true) }
    }

    fun closeLoginScreen() {
        _uiState.update { it.copy(showLoginScreen = false) }
    }

    fun openDepartureModal(defaultAreaId: String? = null) {
        val targetId = defaultAreaId ?: _uiState.value.selectedMicroArea?.id ?: "pza_vittorio_nord"
        _uiState.update {
            it.copy(
                showDepartureModal = true,
                departureAreaId = targetId
            )
        }
    }

    fun closeDepartureModal() {
        _uiState.update { it.copy(showDepartureModal = false) }
    }

    fun setDepartureAreaId(id: String) {
        _uiState.update { it.copy(departureAreaId = id) }
    }

    fun toggleZtlLayer() {
        _uiState.update { current ->
            val next = !current.isZtlLayerVisible
            current.copy(
                isZtlLayerVisible = next,
                activeZtlWarning = if (!next) null else current.activeZtlWarning
            )
        }
    }

    fun toggleZtlVisibilitySheet(show: Boolean) {
        _uiState.update { it.copy(showZtlVisibilitySheet = show) }
    }

    fun toggleZtlZoneVisibility(zoneId: String) {
        _uiState.update { current ->
            val allIds = current.ztlZones.map { it.id }.toSet()
            val currentSet = if (current.visibleZtlZoneIds.isEmpty()) allIds else current.visibleZtlZoneIds
            val nextSet = if (currentSet.contains(zoneId)) {
                currentSet - zoneId
            } else {
                currentSet + zoneId
            }
            current.copy(
                visibleZtlZoneIds = nextSet,
                isZtlLayerVisible = nextSet.isNotEmpty()
            )
        }
    }

    fun setAllZtlZonesVisible(visible: Boolean) {
        _uiState.update { current ->
            current.copy(
                isZtlLayerVisible = visible,
                visibleZtlZoneIds = if (visible) current.ztlZones.map { it.id }.toSet() else emptySet()
            )
        }
    }

    fun setOnlyActiveZtlZonesVisible() {
        _uiState.update { current ->
            val activeIds = current.ztlZones.filter { it.isCurrentlyActive() }.map { it.id }.toSet()
            current.copy(
                isZtlLayerVisible = activeIds.isNotEmpty(),
                visibleZtlZoneIds = activeIds
            )
        }
    }

    fun setParkingTypeFilter(type: ParkingTypeFilter) {
        _uiState.update { it.copy(searchFilter = it.searchFilter.copy(parkingType = type)) }
    }

    fun selectZtlZone(zone: ZtlZone?) {
        _uiState.update { it.copy(selectedZtlZone = zone) }
    }

    fun dismissZtlWarning(context: Context? = null) {
        _uiState.update { it.copy(activeZtlWarning = null) }
        context?.let { ZtlNotificationHelper.cancelZtlNotification(it) }
    }

    fun setDepartureSliderDuration(minutes: Int) {
        _uiState.update { it.copy(departureSliderDuration = minutes) }
    }

    fun setDepartureSliderMode(mode: DepartureSliderMode) {
        _uiState.update { it.copy(departureSliderMode = mode) }
    }

    fun submitDepartureSignal(
        durationMinutes: Int = _uiState.value.departureSliderDuration,
        mode: DepartureSliderMode = _uiState.value.departureSliderMode,
        context: Context? = null
    ) {
        val user = _uiState.value.currentUser
        if (user == null) {
            _uiState.update {
                it.copy(
                    showLoginScreen = true,
                    toastMessage = "Accesso richiesto: devi effettuare il login con email verificata per segnalare un posto."
                )
            }
            return
        }
        if (!user.isEmailVerified) {
            _uiState.update {
                it.copy(
                    showLoginScreen = true,
                    toastMessage = "Verifica email richiesta: controlla la tua casella di posta prima di pubblicare segnalazioni."
                )
            }
            return
        }

        val areaId = _uiState.value.departureAreaId
        val areaName = _uiState.value.microAreas.find { it.id == areaId }?.name ?: areaId
        val userLoc = _uiState.value.userLocation

        // Secure Audit Record to Firestore
        viewModelScope.launch {
            repository.firestoreSyncManager?.recordParkingReport(
                userId = user.uid,
                userEmail = user.email ?: "user@parkpulse.verified",
                areaId = areaId,
                areaName = areaName,
                actionType = if (mode == DepartureSliderMode.FREE_SPOT_WINDOW) "DEPARTURE_SIGNAL" else "UPCOMING_DEPARTURE",
                departureMinutes = durationMinutes,
                latitude = userLoc?.latitude ?: 0.0,
                longitude = userLoc?.longitude ?: 0.0,
                userName = user.displayName ?: "Driver"
            )
        }

        if (mode == DepartureSliderMode.FREE_SPOT_WINDOW) {
            repository.submitDepartureSignal(areaId, durationMinutes)
            _uiState.update {
                it.copy(
                    showDepartureModal = false,
                    toastMessage = "+50 Punti Sosta! Segnale verificato pubblicato e registrato nei log di sicurezza."
                )
            }
        } else {
            repository.registerUpcomingDeparture(areaId, durationMinutes)
            _uiState.update {
                it.copy(
                    showDepartureModal = false,
                    toastMessage = "+50 Punti Sosta! Partenza programmata verificata e archiviata con successo."
                )
            }
        }
    }

    fun checkZtlProximity(position: LatLng, context: Context? = null) {
        val userProfile = _uiState.value.userProfile
        val breachAlert = ZtlLiveMonitorService.evaluateZtlBreach(
            userLocation = position,
            vehiclePermit = userProfile.vehiclePermit,
            isPermitActive = userProfile.isPermitActive,
            context = context
        )
        if (breachAlert != null) {
            _uiState.update { it.copy(activeZtlWarning = breachAlert) }
        }
    }

    fun triggerTestZtlWarning(context: Context? = null) {
        val userProfile = _uiState.value.userProfile
        val userLoc = _uiState.value.userLocation ?: LatLng(41.8992, 12.5028)
        val alert = ZtlLiveMonitorService.evaluateZtlBreach(
            userLocation = userLoc,
            vehiclePermit = userProfile.vehiclePermit,
            isPermitActive = userProfile.isPermitActive,
            context = context,
            forceAlert = true
        )
        if (alert != null) {
            _uiState.update { it.copy(activeZtlWarning = alert) }
        } else {
            val zone = _uiState.value.ztlZones.firstOrNull() ?: return
            val fallbackAlert = ZtlWarningAlert(
                zone = zone,
                distanceMeters = 240,
                isInside = false,
                activeHours = zone.dayActiveSchedule,
                permitUsed = userProfile.vehiclePermit,
                isPermitAuthorized = false
            )
            _uiState.update { it.copy(activeZtlWarning = fallbackAlert) }
            context?.let {
                ZtlNotificationHelper.sendZtlProximityNotification(it, zone, 240, fallbackAlert.activeHours)
            }
        }
    }

    // NEW: Open stay registration modal
    fun openRegisterStayModal(area: MicroArea? = null) {
        val target = area ?: _uiState.value.selectedMicroArea ?: _uiState.value.microAreas.firstOrNull()
        _uiState.update {
            it.copy(
                showRegisterStayModal = true,
                areaForStayRegistration = target
            )
        }
    }

    fun closeRegisterStayModal() {
        _uiState.update { it.copy(showRegisterStayModal = false) }
    }

    fun confirmParkedStay(duration: StayDuration) {
        val area = _uiState.value.areaForStayRegistration ?: return
        val user = _uiState.value.currentUser
        if (user == null) {
            _uiState.update {
                it.copy(
                    showLoginScreen = true,
                    toastMessage = "Accesso richiesto: accedi per registrare la sosta del tuo veicolo."
                )
            }
            return
        }
        viewModelScope.launch {
            repository.firestoreSyncManager?.recordParkingReport(
                userId = user.uid,
                userEmail = user.email ?: "user@parkpulse.verified",
                areaId = area.id,
                areaName = area.name,
                actionType = "REGISTER_PARKED_STAY",
                departureMinutes = duration.minutes,
                latitude = area.latitude,
                longitude = area.longitude,
                userName = user.displayName ?: "Driver"
            )
        }
        repository.registerParkedStay(area.id, duration)
        _uiState.update {
            it.copy(
                showRegisterStayModal = false,
                toastMessage = "+25 Punti Sosta! Sosta verificata registrata fino a ${duration.label}."
            )
        }
    }

    fun convertMyStayToDeparture() {
        val stay = _uiState.value.userProfile.activeStay ?: return
        val user = _uiState.value.currentUser
        if (user != null) {
            val stayArea = _uiState.value.microAreas.find { it.id == stay.microAreaId }
            viewModelScope.launch {
                repository.firestoreSyncManager?.recordParkingReport(
                    userId = user.uid,
                    userEmail = user.email ?: "user@parkpulse.verified",
                    areaId = stay.microAreaId,
                    areaName = stayArea?.name ?: stay.microAreaName,
                    actionType = "DEPARTURE_FROM_STAY",
                    departureMinutes = 5,
                    latitude = stayArea?.latitude ?: stay.latitude,
                    longitude = stayArea?.longitude ?: stay.longitude,
                    userName = user.displayName ?: "Driver"
                )
            }
        }
        repository.convertStayToDepartureSignal(stay)
        _uiState.update {
            it.copy(
                toastMessage = "Posto liberato! Pubblicato segnale di 5 minuti per la community (+50 pt)."
            )
        }
    }

    fun cancelMyParkedStay() {
        repository.clearActiveStay()
        _uiState.update {
            it.copy(toastMessage = "Sosta archiviata.")
        }
    }

    fun openFeedbackDialog(signal: DepartureSignal) {
        _uiState.update { it.copy(signalForFeedback = signal) }
    }

    fun closeFeedbackDialog() {
        _uiState.update { it.copy(signalForFeedback = null) }
    }

    fun submitSignalFeedback(feedbackType: FeedbackType) {
        val signal = _uiState.value.signalForFeedback ?: return
        val user = _uiState.value.currentUser
        if (user == null) {
            _uiState.update {
                it.copy(
                    showLoginScreen = true,
                    toastMessage = "Accesso richiesto per confermare o smentire una segnalazione."
                )
            }
            return
        }
        if (!user.isEmailVerified) {
            _uiState.update {
                it.copy(
                    showLoginScreen = true,
                    toastMessage = "Verifica la tua email per poter inviare feedback sulle segnalazioni."
                )
            }
            return
        }

        // Record anti-abuse audit log to Firestore
        viewModelScope.launch {
            repository.firestoreSyncManager?.recordFeedbackAudit(
                userId = user.uid,
                userEmail = user.email ?: "user@parkpulse.verified",
                signalId = signal.id,
                feedbackType = feedbackType.name
            )
        }

        repository.submitFeedback(signal.id, feedbackType)
        val msg = when (feedbackType) {
            FeedbackType.FOUND -> "Fantastico! +30 Punti Sosta per aver confermato il posto (feedback autenticato)."
            FeedbackType.NOT_FOUND -> "Grazie del feedback. Segnalazione inesatta registrata nel log di sicurezza anti-abuso (-20 pt)."
            FeedbackType.WRONG_DATA -> "Segnalazione errata registrata nel log per revisione della community."
        }
        _uiState.update {
            it.copy(
                signalForFeedback = null,
                toastMessage = msg
            )
        }
    }

    fun setParkingCategory(category: ParkingTypeCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun searchPlaces(query: String) {
        repository.searchPlaces(query)
    }

    fun clearPlacePredictions() {
        repository.clearPlacePredictions()
    }

    fun selectPlacePrediction(prediction: PlacePrediction) {
        repository.clearPlacePredictions()
        _uiState.update {
            it.copy(
                searchQuery = prediction.primaryText,
                userLocation = LatLng(prediction.latitude, prediction.longitude)
            )
        }
        repository.setNearestCityFromLocation(LatLng(prediction.latitude, prediction.longitude))
    }

    fun updateUserSettings(settings: UserSettings) {
        repository.updateUserSettings(settings)
        _uiState.update { it.copy(userSettings = settings) }
    }

    fun completeOnboarding() {
        repository.completeOnboarding()
        _uiState.update { it.copy(userSettings = it.userSettings.copy(hasCompletedOnboarding = true)) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun toggleLeaderboardSheet(show: Boolean) {
        _uiState.update { it.copy(showLeaderboardSheet = show) }
    }

    fun initFirestoreSync(manager: FirestoreSyncManager) {
        repository.attachFirestoreSync(manager, _uiState.value.currentUser?.uid)
    }

    fun toggleFavorite(areaId: String) {
        val uid = _uiState.value.currentUser?.uid
        repository.toggleFavorite(areaId, uid)
    }

    fun toggleNotifications(enabled: Boolean) {
        repository.toggleNotifications(enabled)
        _uiState.update {
            it.copy(
                toastMessage = if (enabled) "Notifiche avvisi Termini–Esquilino attivate" else "Notifiche disattivate"
            )
        }
    }

    fun toggleZtlSheet(show: Boolean) {
        _uiState.update { it.copy(showZtlSheet = show) }
    }

    fun togglePrivacyDialog(show: Boolean) {
        _uiState.update { it.copy(showPrivacyDialog = show) }
    }

    fun deleteAccountAndData() {
        repository.deleteAccountData()
        _uiState.update {
            it.copy(
                showPrivacyDialog = false,
                toastMessage = "Account e tutti i dati personali eliminati con successo."
            )
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun initDatabase(context: Context) {
        val db = ParkingDatabase.getInstance(context)
        repository.attachDatabase(db)
    }

    fun toggleOfflineMode() {
        repository.toggleOfflineMode()
        val willBeOffline = !repository.isOfflineMode.value
        _uiState.update {
            it.copy(
                toastMessage = if (willBeOffline) "Modalità Offline simulata: dati caricati dalla cache locale Room" else "Tornato Online: sincronizzazione attiva"
            )
        }
    }

    fun selectSubBlock(block: RadarSubBlockDensity?) {
        _uiState.update { it.copy(selectedSubBlock = block) }
    }

    fun setCopilotModel(model: CopilotModelChoice) {
        _uiState.update { it.copy(copilotModel = model) }
        runCopilotAssessment(_uiState.value.copilotTargetStreet, model)
    }

    fun runCopilotAssessment(
        street: String,
        model: CopilotModelChoice = _uiState.value.copilotModel
    ) {
        _uiState.update {
            it.copy(
                copilotTargetStreet = street,
                copilotModel = model,
                copilotIsLoading = true
            )
        }
        viewModelScope.launch {
            val result = copilotService.analyzeRomanParkingQuery(
                query = "Verifica ZTL, orari lavaggio strade, tariffe strisce e alternative per $street",
                targetStreet = street,
                modelChoice = model
            )
            _uiState.update {
                it.copy(
                    copilotIsLoading = false,
                    copilotResult = result
                )
            }
        }
    }

    fun setFilterActivityType(type: UserActivityType?) {
        _uiState.update { it.copy(filterActivityType = type) }
    }

    fun dismissCongestionAlert() {
        _uiState.update { it.copy(activeCongestionAlertSubBlock = null) }
    }

    fun dismissSpotAlert() {
        _uiState.update { it.copy(activeSpotAvailableAlertSignal = null) }
    }

    fun checkCongestionProximity(position: LatLng, context: Context? = null) {
        val userProf = _uiState.value.userProfile
        if (!userProf.notificationsEnabled || !userProf.congestionAlertsEnabled) return
        context?.let { ctx ->
            val alertedBlock = ParkingNotificationService.checkCongestionProximity(
                userLat = position.latitude,
                userLng = position.longitude,
                subBlocks = _uiState.value.radarSubBlocks,
                context = ctx
            )
            if (alertedBlock != null) {
                _uiState.update { it.copy(activeCongestionAlertSubBlock = alertedBlock) }
            }
        }
    }

    fun triggerTestCongestionAlert(context: Context) {
        val block = _uiState.value.radarSubBlocks.find { it.id == "termini_ala_giolitti" }
            ?: _uiState.value.radarSubBlocks.firstOrNull() ?: return
        _uiState.update { it.copy(activeCongestionAlertSubBlock = block) }
        ParkingNotificationService.sendHighCongestionAlert(
            context = context,
            subBlock = block,
            distanceMeters = 180,
            alternativeRecommendation = block.recommendation
        )
    }

    fun triggerTestSpotAvailableAlert(context: Context) {
        val signal = _uiState.value.activeSignals.firstOrNull() ?: DepartureSignal(
            id = "test-alert-sig",
            microAreaId = "pza_vittorio_nord",
            microAreaName = "Piazza Vittorio (Portici Nord)",
            streetName = "Piazza Vittorio Emanuele II",
            latitude = 41.8962,
            longitude = 12.5048,
            createdEpochMs = System.currentTimeMillis(),
            expiryEpochMs = System.currentTimeMillis() + 300_000L,
            userReliabilityPercent = 96,
            feedbackFoundCount = 3,
            feedbackNotFoundCount = 0,
            isUserSubmitted = false
        )
        _uiState.update { it.copy(activeSpotAvailableAlertSignal = signal) }
        ParkingNotificationService.sendNearbySpotAvailableAlert(
            context = context,
            signal = signal,
            matchReason = "Via salvata nei tuoi preferiti (Piazza Vittorio)"
        )
    }

    fun updateVehicleData(
        licensePlate: String,
        vehicleModel: String,
        vehiclePermit: VehiclePermitType,
        permitNumber: String,
        permitExpiryDate: String,
        isPermitActive: Boolean
    ) {
        repository.updateVehicleData(
            licensePlate = licensePlate,
            vehicleModel = vehicleModel,
            vehiclePermit = vehiclePermit,
            permitNumber = permitNumber,
            permitExpiryDate = permitExpiryDate,
            isPermitActive = isPermitActive
        )
        _uiState.update { it.copy(showVehiclePermitDialog = false) }
    }

    fun toggleVehiclePermitDialog(show: Boolean) {
        _uiState.update { it.copy(showVehiclePermitDialog = show) }
    }

    fun dismissZtlRouteBreachWarning() {
        _uiState.update { it.copy(pendingZtlRouteBreach = null) }
    }

    fun proceedAnywayWithZtlBreach() {
        val breach = _uiState.value.pendingZtlRouteBreach ?: return
        val origin = _uiState.value.userLocation ?: LatLng(41.8992, 12.5028)
        val profile = _uiState.value.userProfile
        val session = SmartNavigatorEngine.createRoute(
            origin = origin,
            destinationId = "breach_dest",
            destinationName = breach.destinationName,
            destinationAddress = breach.destinationName,
            destinationLocation = breach.breachLocation,
            parkingProbability = 80,
            avoidZtl = false,
            permitType = profile.vehiclePermit,
            licensePlate = profile.licensePlate,
            vehicleModel = profile.vehicleModel,
            isPermitActive = profile.isPermitActive,
            activeSignals = _uiState.value.activeSignals
        )
        _uiState.update {
            it.copy(
                activeNavigation = session,
                pendingZtlRouteBreach = null,
                viewMode = ViewMode.MAP
            )
        }
    }

    fun reroutePlannedRouteAvoidingZtl() {
        val breach = _uiState.value.pendingZtlRouteBreach ?: return
        val origin = _uiState.value.userLocation ?: LatLng(41.8992, 12.5028)
        val profile = _uiState.value.userProfile
        val session = SmartNavigatorEngine.createRoute(
            origin = origin,
            destinationId = "breach_dest_rerouted",
            destinationName = breach.destinationName,
            destinationAddress = "${breach.destinationName} (Percorso Anti-ZTL)",
            destinationLocation = breach.breachLocation,
            parkingProbability = 85,
            avoidZtl = true,
            permitType = profile.vehiclePermit,
            licensePlate = profile.licensePlate,
            vehicleModel = profile.vehicleModel,
            isPermitActive = profile.isPermitActive,
            activeSignals = _uiState.value.activeSignals
        )
        _uiState.update {
            it.copy(
                activeNavigation = session,
                pendingZtlRouteBreach = null,
                viewMode = ViewMode.MAP
            )
        }
    }

    fun startNavigation(
        destinationId: String,
        destinationName: String,
        destinationAddress: String,
        destinationLocation: LatLng,
        parkingProbability: Int = 85,
        bypassZtlWarning: Boolean = false
    ) {
        val origin = _uiState.value.userLocation ?: LatLng(41.8992, 12.5028)
        val profile = _uiState.value.userProfile

        if (!bypassZtlWarning) {
            val breach = SmartNavigatorEngine.checkRouteZtlBreach(
                origin = origin,
                destinationName = destinationName,
                destinationLocation = destinationLocation,
                avoidZtl = false,
                permitType = profile.vehiclePermit,
                licensePlate = profile.licensePlate,
                vehicleModel = profile.vehicleModel,
                isPermitActive = profile.isPermitActive
            )
            if (breach != null) {
                // Warn the user before driving into an unauthorized ZTL zone
                _uiState.update { it.copy(pendingZtlRouteBreach = breach) }
                return
            }
        }

        val session = SmartNavigatorEngine.createRoute(
            origin = origin,
            destinationId = destinationId,
            destinationName = destinationName,
            destinationAddress = destinationAddress,
            destinationLocation = destinationLocation,
            parkingProbability = parkingProbability,
            avoidZtl = true,
            permitType = profile.vehiclePermit,
            licensePlate = profile.licensePlate,
            vehicleModel = profile.vehicleModel,
            isPermitActive = profile.isPermitActive,
            activeSignals = _uiState.value.activeSignals
        )
        _uiState.update {
            it.copy(
                activeNavigation = session,
                pendingZtlRouteBreach = null,
                viewMode = ViewMode.MAP
            )
        }
    }

    fun stopNavigation() {
        _uiState.update { it.copy(activeNavigation = null) }
    }

    fun advanceNavigationStep() {
        val current = _uiState.value.activeNavigation ?: return
        val updated = SmartNavigatorEngine.advanceStep(current, _uiState.value.activeSignals)
        _uiState.update { it.copy(activeNavigation = updated) }
    }

    fun toggleNavigationVoice() {
        _uiState.update { state ->
            val nav = state.activeNavigation ?: return@update state
            state.copy(activeNavigation = nav.copy(isVoiceEnabled = !nav.isVoiceEnabled))
        }
    }

    fun toggleNavigationAvoidZtl() {
        _uiState.update { state ->
            val nav = state.activeNavigation ?: return@update state
            val updated = SmartNavigatorEngine.rerouteAvoidZtl(nav, !nav.avoidZtlEnabled, state.activeSignals)
            state.copy(activeNavigation = updated)
        }
    }

    fun divertNavigationToFreeSpot(signal: DepartureSignal) {
        _uiState.update { state ->
            val nav = state.activeNavigation ?: return@update state
            val updated = SmartNavigatorEngine.rerouteToFreeSpot(nav, signal)
            state.copy(activeNavigation = updated)
        }
    }

    fun dismissNavigationSpotAlert() {
        _uiState.update { state ->
            val nav = state.activeNavigation ?: return@update state
            state.copy(activeNavigation = nav.copy(liveFreeSpotNearbyAlert = null))
        }
    }

    fun toggleMlTrendsSheet(show: Boolean, microArea: MicroArea? = null) {
        val area = microArea ?: _uiState.value.selectedMicroArea ?: _uiState.value.microAreas.firstOrNull()
        _uiState.update {
            it.copy(
                showMlTrendsSheet = show,
                mlTargetArea = area
            )
        }
        if (show && area != null) {
            requestMlAvailabilityTrend(area)
        }
    }

    fun requestMlAvailabilityTrend(microArea: MicroArea, forceLocal: Boolean = false) {
        _uiState.update { it.copy(mlPredictionIsLoading = true, mlTargetArea = microArea) }
        viewModelScope.launch {
            val prediction = mlPredictor.predictAvailabilityTrends(microArea, forceLocal = forceLocal)
            _uiState.update {
                it.copy(
                    mlPrediction = prediction,
                    mlPredictionIsLoading = false
                )
            }
        }
    }
}
