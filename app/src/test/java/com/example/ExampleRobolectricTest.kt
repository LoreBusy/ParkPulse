package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DurationNeededFilter
import com.example.data.model.FeedbackType
import com.example.data.model.ParkingSearchFilter
import com.example.data.model.ParkingTypeFilter
import com.example.data.model.StayDuration
import com.example.data.model.TimeOfDayFilter
import com.example.data.repository.ParkingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @org.junit.Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId("com.aistudio.parcheggiroma.epwqyt")
                .setProjectId("parcheggiroma-epwqyt")
                .setApiKey("AIzaSyFakeKeyForRobolectricTesting123456")
                .build()
            com.google.firebase.FirebaseApp.initializeApp(context, options)
        }
    }

    @Test
    fun `read string from context matches ParkPulse`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ParkPulse", appName)
    }

    @Test
    fun `global city provider searches cities and returns preset regions`() {
        val allCities = com.example.data.provider.GlobalCityProvider.GLOBAL_PRESET_CITIES
        assertTrue(allCities.size >= 10)

        val parisResults = com.example.data.provider.GlobalCityProvider.searchCities("Paris")
        assertEquals(1, parisResults.size)
        assertEquals("Paris", parisResults.first().name)

        val italyCities = com.example.data.provider.GlobalCityProvider.searchCities("Italia")
        assertTrue(italyCities.size >= 3)
    }

    @Test
    fun `global city provider findNearestCity accurately detects proximity`() {
        // Paris coordinates (48.8566, 2.3522)
        val nearParis = com.google.android.gms.maps.model.LatLng(48.8600, 2.3400)
        val city = com.example.data.provider.GlobalCityProvider.findNearestCity(nearParis)
        assertEquals("paris", city.id)
    }

    @Test
    fun `traffic ETA calculation computes valid arrival time and duration`() {
        val origin = com.google.android.gms.maps.model.LatLng(41.8902, 12.4922) // Colosseo
        val dest = com.google.android.gms.maps.model.LatLng(41.8992, 12.5035) // Termini

        val eta = com.example.data.model.TrafficEtaResult.calculate(
            origin = origin,
            destination = dest,
            destinationId = "test_dest",
            destinationName = "Stazione Termini"
        )

        assertTrue(eta.trafficDurationMinutes > 0)
        assertTrue(eta.distanceMeters > 0)
        assertNotNull(eta.estimatedArrivalTimeFormatted)
        assertTrue(eta.estimatedArrivalTimeFormatted.contains(":"))
    }

    @Test
    fun `repository switches city and updates regional parking spots`() {
        val repo = ParkingRepository()
        val milan = com.example.data.provider.GlobalCityProvider.GLOBAL_PRESET_CITIES.first { it.id == "milan" }
        repo.selectCity(milan)

        assertEquals("milan", repo.selectedCity.value.id)
        val spots = repo.microAreas.value
        assertTrue(spots.isNotEmpty())
        assertTrue(spots.any { it.name.contains("Centrale") || it.name.contains("Duomo") })
    }

    @Test
    fun `repository updates language preference`() {
        val repo = ParkingRepository()
        assertEquals("it", repo.languagePreference.value)
        repo.setLanguagePreference("en")
        assertEquals("en", repo.languagePreference.value)
    }

    @Test
    fun `repository initializes with clean startup and populates spots on search`() {
        val repo = ParkingRepository()
        // Phase 2: startup must be completely clean with zero fake spots
        assertTrue(repo.microAreas.value.isEmpty())

        // When searching parking around a location, parking spots are dynamically populated
        val spots = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))
        assertTrue(spots.isNotEmpty())
        assertTrue(repo.microAreas.value.isNotEmpty())
        val giolitti = spots.first()
        assertTrue(giolitti.latitude > 41.0)
        assertTrue(giolitti.longitude > 12.0)
    }

    @Test
    fun `submitting departure signal adds signal and awards 50 gamification points`() {
        val repo = ParkingRepository()
        val initialPoints = repo.userProfile.value.points

        val signal = repo.submitDepartureSignal("pza_vittorio_nord")
        assertNotNull(signal)
        assertEquals("pza_vittorio_nord", signal.microAreaId)
        assertFalse(signal.isExpired)

        val updatedPoints = repo.userProfile.value.points
        assertEquals(initialPoints + 50, updatedPoints)
    }

    @Test
    fun `registering parked stay calculates correct departure time and awards points`() {
        val repo = ParkingRepository()
        val initialPoints = repo.userProfile.value.points

        val stay = repo.registerParkedStay("via_giolitti_stazione", StayDuration.MIN_15)
        assertEquals(StayDuration.MIN_15, stay.duration)
        assertTrue(stay.expectedDepartureEpochMs > stay.parkedAtEpochMs)
        assertEquals(initialPoints + 25, repo.userProfile.value.points)
        assertNotNull(repo.userProfile.value.activeStay)
    }

    @Test
    fun `feedback found increases karma and awards 30 points`() {
        val repo = ParkingRepository()
        val signal = repo.submitDepartureSignal("pza_vittorio_nord")
        val pointsBefore = repo.userProfile.value.points

        repo.submitFeedback(signal.id, FeedbackType.FOUND)
        val pointsAfter = repo.userProfile.value.points
        assertEquals(pointsBefore + 30, pointsAfter)
    }

    @Test
    fun `advanced search filters match EV charging and free white spots correctly`() {
        val repo = ParkingRepository()
        val areas = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))

        val evFilter = ParkingSearchFilter(parkingType = ParkingTypeFilter.EV_CHARGING)
        val evAreas = areas.filter { it.matchesFilter(evFilter) }
        assertTrue(evAreas.all { it.hasEvCharging })

        val whiteFilter = ParkingSearchFilter(parkingType = ParkingTypeFilter.FREE_WHITE)
        val whiteAreas = areas.filter { it.matchesFilter(whiteFilter) }
        assertTrue(whiteAreas.all { it.whiteLineSpots > 0 })
    }

    @Test
    fun `ztl monitoring detects unauthorized entrance in Centro Storico zone`() {
        // Coordinate right inside Centro Storico near Piazza Venezia / Via Nazionale
        val insideZtlLocation = com.google.android.gms.maps.model.LatLng(41.8955, 12.4855)

        // Day calendar set to 10:00 AM on a Wednesday (active ZTL)
        val wednesdayDayTime = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Rome")).apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.WEDNESDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 10)
            set(java.util.Calendar.MINUTE, 30)
        }

        val breach = com.example.util.ZtlLiveMonitorService.evaluateZtlBreach(
            userLocation = insideZtlLocation,
            vehiclePermit = com.example.data.model.VehiclePermitType.NONE,
            isPermitActive = false,
            context = null,
            calendar = wednesdayDayTime
        )

        assertNotNull(breach)
        assertEquals("centro_storico", breach?.zone?.id)
        assertTrue(breach!!.isInside)

        // Electric vehicle should be exempt
        val electricBreach = com.example.util.ZtlLiveMonitorService.evaluateZtlBreach(
            userLocation = insideZtlLocation,
            vehiclePermit = com.example.data.model.VehiclePermitType.ELECTRIC_VEHICLE,
            isPermitActive = true,
            context = null,
            calendar = wednesdayDayTime
        )
        org.junit.Assert.assertNull(electricBreach)
    }

    @Test
    fun `historical parking trends generate hours of availability curve for Termini`() = kotlinx.coroutines.runBlocking {
        val repo = ParkingRepository()
        val spots = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))
        val area = spots.first()
        val predictor = com.example.data.ai.GeminiParkingMlPredictor()
        val prediction = predictor.predictAvailabilityTrends(area, forceLocal = true)

        assertNotNull(prediction)
        assertTrue(prediction.hourlyForecast.isNotEmpty())
        assertTrue(prediction.confidenceScore in 50..100)
        assertNotNull(prediction.peakDifficultyWindow)
        assertNotNull(prediction.optimalParkingWindow)

        // 24 Hours Historical Occupancy validation for Recharts Bar Chart
        assertEquals(24, prediction.past24HoursOccupancy.size)
        assertTrue(prediction.past24HoursOccupancy.all { it.occupancyPercent in 0..100 })
        assertTrue(prediction.past24HoursOccupancy.all { it.occupancyPercent + it.availabilityPercent == 100 })
        val latestHour = prediction.past24HoursOccupancy.last()
        assertEquals(0, latestHour.hoursAgo)
        val oldestHour = prediction.past24HoursOccupancy.first()
        assertEquals(23, oldestHour.hoursAgo)
    }

    @Test
    fun `navigation intent to Google Maps contains destination coordinates`() {
        val lat = 41.8992
        val lng = 12.5028
        val intent = com.example.util.NavigationHelper.createGoogleMapsIntent(lat, lng, "Stazione Termini")
        assertEquals(android.content.Intent.ACTION_VIEW, intent.action)
        assertEquals("com.google.android.apps.maps", intent.`package`)
        assertEquals("google.navigation:q=41.8992,12.5028", intent.dataString)

        val fallbackIntent = com.example.util.NavigationHelper.createExternalMapsIntent(lat, lng, "Stazione Termini")
        assertEquals(android.content.Intent.ACTION_VIEW, fallbackIntent.action)
        assertTrue(fallbackIntent.dataString?.startsWith("geo:41.8992,12.5028") == true)
    }

    @Test
    fun `parking spots filter system correctly categorizes free, paid, and EV`() {
        val repo = ParkingRepository()
        val areas = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))

        val freeFilter = ParkingSearchFilter(parkingType = ParkingTypeFilter.FREE_WHITE)
        val freeAreas = areas.filter { it.matchesFilter(freeFilter) }
        assertTrue(freeAreas.isNotEmpty())
        assertTrue(freeAreas.all { it.whiteLineSpots > 0 })

        val paidFilter = ParkingSearchFilter(parkingType = ParkingTypeFilter.METERED_BLUE)
        val paidAreas = areas.filter { it.matchesFilter(paidFilter) }
        assertTrue(paidAreas.isNotEmpty())
        assertTrue(paidAreas.all { it.blueLineTariff.contains("€") })

        val evFilter = ParkingSearchFilter(parkingType = ParkingTypeFilter.EV_CHARGING)
        val evAreas = areas.filter { it.matchesFilter(evFilter) }
        assertTrue(evAreas.isNotEmpty())
        assertTrue(evAreas.all { it.hasEvCharging })
    }

    @Test
    fun `favorites toggling updates profile and Room storage`() {
        val repo = ParkingRepository()
        val areaId = "via_giolitti_stazione"
        val initiallyFavorite = repo.userProfile.value.favoriteAreaIds.contains(areaId)

        // Toggle favorite
        repo.toggleFavorite(areaId)
        val toggledState = repo.userProfile.value.favoriteAreaIds.contains(areaId)
        assertEquals(!initiallyFavorite, toggledState)

        // Toggle back
        repo.toggleFavorite(areaId)
        val revertedState = repo.userProfile.value.favoriteAreaIds.contains(areaId)
        assertEquals(initiallyFavorite, revertedState)
    }

    @Test
    fun `workmanager traffic alert updates active incident flow and can be dismissed`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.util.TerminiTrafficAlertWorker.triggerImmediateEmergencyAlertCheck(
            context = context,
            title = "🚨 Chiusura Straordinaria: Via Giolitti FS",
            street = "Via Giolitti (Termini)",
            delayMinutes = 20,
            isClosure = true
        )

        // Verify active incident flow can be cleared
        com.example.util.TerminiTrafficAlertWorker.clearActiveIncidents()
        assertTrue(com.example.util.TerminiTrafficAlertWorker.activeIncidentsFlow.value.isEmpty())
    }

    @Test
    fun `ztl route breach check warns when route penetrates active ZTL zone`() {
        // Destination inside Centro Storico near Piazza Navona
        val destinationInsideZtl = com.google.android.gms.maps.model.LatLng(41.8986, 12.4731)
        val originTermini = com.google.android.gms.maps.model.LatLng(41.8992, 12.5028)

        val warning = com.example.util.SmartNavigatorEngine.checkRouteZtlBreach(
            origin = originTermini,
            destinationName = "Parcheggio Piazza Navona",
            destinationLocation = destinationInsideZtl,
            avoidZtl = false,
            permitType = com.example.data.model.VehiclePermitType.NONE,
            licensePlate = "AB 123 CD",
            vehicleModel = "Fiat Panda",
            isPermitActive = false
        )

        assertNotNull(warning)
        assertEquals("AB 123 CD", warning?.licensePlate)
        assertTrue(warning?.zoneName?.isNotEmpty() == true)
        assertTrue(warning?.fineInfo?.contains("83") == true)
    }

    @Test
    fun `parking heatmap helper builds real-time weighted points and tile provider`() {
        val repo = ParkingRepository()
        val areas = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))
        assertTrue(areas.isNotEmpty())

        val densityPoints = com.example.util.ParkingHeatmapHelper.buildRealtimeDensityPoints(
            microAreas = areas,
            metric = com.example.util.HeatmapMetric.SATURATION_DENSITY
        )
        assertTrue(densityPoints.isNotEmpty())
        assertTrue(densityPoints.all { it.intensity in 0.0..1.0 })

        // Tile provider generation
        val provider = com.example.util.ParkingHeatmapHelper.createHeatmapProvider(
            points = densityPoints,
            metric = com.example.util.HeatmapMetric.SATURATION_DENSITY
        )
        assertNotNull(provider)

        val vacancyProvider = com.example.util.ParkingHeatmapHelper.createHeatmapProvider(
            points = densityPoints,
            metric = com.example.util.HeatmapMetric.VACANCY_AVAILABILITY
        )
        assertNotNull(vacancyProvider)
    }

    @Test
    fun `distance unit formatting converts meters and feet correctly`() {
        val metricSettings = com.example.data.model.UserSettings(distanceUnit = com.example.data.model.DistanceUnit.METRIC)
        val imperialSettings = com.example.data.model.UserSettings(distanceUnit = com.example.data.model.DistanceUnit.IMPERIAL)

        assertEquals("250 m", metricSettings.formatDistance(250))
        assertEquals("1.5 km", metricSettings.formatDistance(1500))

        val feetShort = imperialSettings.formatDistance(100)
        assertTrue(feetShort.contains("ft"))
        val milesLong = imperialSettings.formatDistance(2000)
        assertTrue(milesLong.contains("mi"))
    }

    @Test
    fun `currency formatting dynamically changes tariff symbol across regions`() {
        val eur = com.example.data.model.UserSettings(currencyFormat = com.example.data.model.CurrencyFormat.EUR)
        val usd = com.example.data.model.UserSettings(currencyFormat = com.example.data.model.CurrencyFormat.USD)
        val gbp = com.example.data.model.UserSettings(currencyFormat = com.example.data.model.CurrencyFormat.GBP)
        val jpy = com.example.data.model.UserSettings(currencyFormat = com.example.data.model.CurrencyFormat.JPY)

        assertTrue(eur.formatTariff("€1,50/h").contains("€"))
        assertTrue(usd.formatTariff("€1,50/h").contains("$"))
        assertTrue(gbp.formatTariff("€1,50/h").contains("£"))
        assertTrue(jpy.formatTariff("€1,50/h").contains("¥"))
    }

    @Test
    fun `parking category filter accurately matches Public, Private, EV, and Accessible spots`() {
        val publicArea = com.example.data.model.MicroArea(
            id = "test_public",
            name = "Stalli Pubblici Termini",
            streetName = "Via Giolitti",
            latitude = 41.899,
            longitude = 12.503,
            distanceMeters = 100,
            radiusMeters = 50,
            baselineAvailability = 50,
            blueLineTariff = "€1,50/h",
            whiteLineSpots = 2,
            ztlZoneName = "Fuori ZTL",
            isZtlBoundary = false,
            cleaningSchedule = "Lun",
            normX = 0.5f,
            normY = 0.5f,
            isPrivateGarage = false,
            hasEvCharging = false,
            isAccessibleDisabled = false
        )

        val privateGarage = publicArea.copy(
            id = "test_private",
            name = "Autorimessa Esquilino Garage",
            isPrivateGarage = true
        )

        val evChargingSpot = publicArea.copy(
            id = "test_ev",
            name = "Colonnina Enel X Fast Charge",
            hasEvCharging = true
        )

        val disabledSpot = publicArea.copy(
            id = "test_disabled",
            name = "Stallo Riservato Disabili",
            isAccessibleDisabled = true
        )

        assertTrue(publicArea.matchesCategory(com.example.data.model.ParkingTypeCategory.ALL))
        assertTrue(publicArea.matchesCategory(com.example.data.model.ParkingTypeCategory.PUBLIC))
        assertFalse(publicArea.matchesCategory(com.example.data.model.ParkingTypeCategory.PRIVATE))

        assertTrue(privateGarage.matchesCategory(com.example.data.model.ParkingTypeCategory.PRIVATE))
        assertFalse(privateGarage.matchesCategory(com.example.data.model.ParkingTypeCategory.PUBLIC))

        assertTrue(evChargingSpot.matchesCategory(com.example.data.model.ParkingTypeCategory.EV_CHARGING))
        assertTrue(disabledSpot.matchesCategory(com.example.data.model.ParkingTypeCategory.ACCESSIBLE))
    }

    @Test
    fun `user settings can toggle theme mode and complete onboarding flow`() {
        val repo = ParkingRepository()
        assertFalse(repo.userSettings.value.hasCompletedOnboarding)

        repo.completeOnboarding()
        assertTrue(repo.userSettings.value.hasCompletedOnboarding)

        repo.updateUserSettings(
            repo.userSettings.value.copy(
                themeMode = com.example.data.model.ThemeMode.DARK,
                distanceUnit = com.example.data.model.DistanceUnit.IMPERIAL,
                currencyFormat = com.example.data.model.CurrencyFormat.USD
            )
        )

        assertEquals(com.example.data.model.ThemeMode.DARK, repo.userSettings.value.themeMode)
        assertEquals(com.example.data.model.DistanceUnit.IMPERIAL, repo.userSettings.value.distanceUnit)
        assertEquals(com.example.data.model.CurrencyFormat.USD, repo.userSettings.value.currencyFormat)
    }

    @Test
    fun `places autocomplete service returns predictions for world queries`() = kotlinx.coroutines.runBlocking {
        val service = com.example.util.PlacesAutocompleteService()
        val results = service.searchPlaces("Termini")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.description.contains("Termini", ignoreCase = true) })
    }

    @Test
    fun `community leaderboard contains ranked drivers with badges and karma score`() {
        val repo = ParkingRepository()
        val board = repo.leaderboard.value
        assertTrue(board.isNotEmpty())
        assertTrue(board.all { it.points >= 0 })
        assertTrue(board.all { it.karmaScore in 0..100 })
        assertTrue(board.first().points >= board.last().points)
    }

    @Test
    fun `saved car location stores spot, calculates meter countdown, and allows time extension`() {
        val repo = ParkingRepository()
        val now = System.currentTimeMillis()
        val location = com.example.data.model.SavedCarLocation(
            latitude = 41.8992,
            longitude = 12.5028,
            address = "Via Giovanni Giolitti 34",
            parkedAtEpochMs = now,
            notes = "Piano -1, Stallo 14B",
            meterDurationMinutes = 60,
            meterExpiryEpochMs = now + (60 * 60 * 1000L),
            floorOrZone = "Settore Blu"
        )

        repo.saveCarLocation(location)
        val saved = repo.savedCarLocation.value
        assertNotNull(saved)
        assertEquals("Via Giovanni Giolitti 34", saved?.address)
        assertTrue(saved!!.isMeterActive)
        assertTrue(saved.remainingMeterMinutes in 58..60)

        // Extend timer by 30 min
        repo.updateMeterTimer(30)
        val extended = repo.savedCarLocation.value
        assertTrue(extended!!.remainingMeterMinutes in 88..90)

        // Clear car location
        repo.clearSavedCarLocation()
        org.junit.Assert.assertNull(repo.savedCarLocation.value)
    }

    @Test
    fun `speed camera provider returns official MIT registry cameras and proximity warning`() {
        val provider = com.example.data.provider.OfficialMitSpeedCameraProvider()
        val termini = com.google.android.gms.maps.model.LatLng(41.8992, 12.5028)

        // Fetch cameras around Rome
        val cameras = provider.getSpeedCamerasAround(termini, 15000.0)
        assertTrue(cameras.isNotEmpty())
        assertTrue(cameras.all { it.reliability == com.example.data.model.DataSourceReliability.OFFICIAL_REGISTRY || it.reliability == com.example.data.model.DataSourceReliability.COMMUNITY_REPORTED })

        // Check warning when close to a speed camera (Tangenziale Est 41.9215, 12.5280)
        val nearCam = com.google.android.gms.maps.model.LatLng(41.9210, 12.5275)
        val overSpeedWarning = provider.getSpeedCameraWarning(nearCam, currentSpeedKmh = 85)
        assertNotNull(overSpeedWarning)
        assertTrue(overSpeedWarning!!.isOverSpeedLimit)
        assertTrue(overSpeedWarning.distanceMeters <= 500)
        assertTrue(overSpeedWarning.warningMessage.contains("Rallenta"))
    }

    @Test
    fun `multi-modal trip calculator combines driving detour, walking time, and tariffs`() {
        val provider = com.example.data.provider.DynamicParkingDataProvider()
        val origin = com.google.android.gms.maps.model.LatLng(41.8902, 12.4922) // Colosseo
        val destination = com.google.android.gms.maps.model.LatLng(41.8992, 12.5028) // Termini

        val parkingSpots = provider.searchParkingAround(destination, 1200.0)
        assertTrue(parkingSpots.isNotEmpty())

        val option = provider.calculateMultiModalTrip(parkingSpots.first(), origin, destination)
        assertTrue(option.drivingTimeMinutes > 0)
        assertTrue(option.walkingTimeMinutes > 0)
        assertTrue(option.totalTripMinutes == option.drivingTimeMinutes + option.walkingTimeMinutes)
        assertTrue(option.recommendationScore in 10..98)
        assertTrue(option.recommendationReason.isNotEmpty())
    }

    @Test
    fun `firebase auth service rejects invalid password reset email strings`() = kotlinx.coroutines.runBlocking {
        val authService = com.example.data.auth.FirebaseAuthService()
        val emptyResult = authService.sendPasswordResetEmail("")
        assertTrue(emptyResult.isFailure)
        assertTrue(emptyResult.exceptionOrNull()?.message?.contains("valido") == true || emptyResult.exceptionOrNull()?.message?.contains("valid") == true)

        val invalidFormatResult = authService.sendPasswordResetEmail("notanemail")
        assertTrue(invalidFormatResult.isFailure)
    }

    @Test
    fun `parking viewmodel rejects guest access to prevent unauthenticated reporting`() {
        val vm = com.example.ui.viewmodel.ParkingViewModel()
        vm.continueAsGuest()
        // Guest mode must NOT be granted
        assertFalse(vm.currentUiState.isGuestMode)
        assertTrue(vm.currentUiState.showLoginScreen)
        assertTrue(vm.currentUiState.toastMessage?.contains("non consentito") == true || vm.currentUiState.toastMessage?.contains("richiesto") == true)
    }

    @Test
    fun `submitting signal without authenticated user prompts login and blocks submission`() {
        val vm = com.example.ui.viewmodel.ParkingViewModel()
        // No current user
        vm.submitDepartureSignal(5)
        assertTrue(vm.currentUiState.showLoginScreen)
        assertTrue(vm.currentUiState.toastMessage?.contains("Accesso richiesto") == true)
    }

    @Test
    fun `roman ZTL public data provider correctly identifies active daytime Centro Storico on Monday noon`() {
        val provider = com.example.data.provider.RomanZtlPublicDataProvider()
        val romeTz = java.util.TimeZone.getTimeZone("Europe/Rome")
        val mondayNoon = java.util.Calendar.getInstance(romeTz).apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        val feed = provider.computeLiveFeed(mondayNoon)
        assertTrue(feed.zones.isNotEmpty())
        assertTrue(feed.totalActiveZones > 0)

        val centro = feed.zones.first { it.zoneId == "centro_storico" }
        assertTrue(centro.isActive)
        assertEquals(com.example.data.provider.GateOperationalStatus.ACTIVE_RESTRICTED, centro.operationalStatus)
        assertTrue(centro.activeGatesCount > 0)
        assertTrue(centro.gates.all { it.isGateActive && it.isCameraActive })
        assertTrue(centro.nextChangeText.contains("18:00"))
        assertTrue(centro.publicDataSource.contains("Roma Servizi per la Mobilità"))
    }

    @Test
    fun `roman ZTL public data provider correctly identifies inactive Centro Storico on Sunday midday`() {
        val provider = com.example.data.provider.RomanZtlPublicDataProvider()
        val romeTz = java.util.TimeZone.getTimeZone("Europe/Rome")
        val sundayNoon = java.util.Calendar.getInstance(romeTz).apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }

        val feed = provider.computeLiveFeed(sundayNoon)
        val centro = feed.zones.first { it.zoneId == "centro_storico" }
        assertFalse(centro.isActive)
        assertEquals(com.example.data.provider.GateOperationalStatus.INACTIVE_FREE, centro.operationalStatus)
        assertEquals(0, centro.activeGatesCount)
        assertTrue(centro.gates.all { !it.isGateActive && !it.isCameraActive })
        assertTrue(centro.nextChangeText.contains("Libero transito") || centro.nextChangeText.contains("Disattiva"))
    }

    @Test
    fun `parking repository exposes live Roman ZTL feed and refreshes correctly`() {
        val repo = com.example.data.repository.ParkingRepository()
        val initialFeed = repo.ztlPublicStatusFeed.value
        assertNotNull(initialFeed)
        assertTrue(initialFeed.zones.size >= 5)
        assertTrue(initialFeed.isPublicFeedOnline)

        val refreshedFeed = repo.refreshZtlPublicData()
        assertNotNull(refreshedFeed)
        assertEquals(refreshedFeed, repo.ztlPublicStatusFeed.value)
    }

    @Test
    fun `parking viewmodel toggles ZTL layer and refreshes public data feed`() {
        val vm = com.example.ui.viewmodel.ParkingViewModel()
        assertTrue(vm.currentUiState.isZtlLayerVisible)

        vm.toggleZtlLayer()
        assertFalse(vm.currentUiState.isZtlLayerVisible)

        vm.toggleZtlLayer()
        assertTrue(vm.currentUiState.isZtlLayerVisible)

        vm.refreshZtlPublicData()
        assertNotNull(vm.currentUiState.ztlLiveFeed)
        assertTrue(vm.currentUiState.ztlLiveFeed!!.zones.isNotEmpty())
    }
}


