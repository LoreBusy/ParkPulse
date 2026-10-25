package com.example

import com.example.data.model.DurationNeededFilter
import com.example.data.model.FeedbackType
import com.example.data.model.ParkingSearchFilter
import com.example.data.model.ParkingTypeFilter
import com.example.data.model.StayDuration
import com.example.data.repository.ParkingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun repository_initializes_micro_areas_with_coordinates() {
        val repo = ParkingRepository()
        assertTrue(repo.microAreas.value.isEmpty())
        val areas = repo.searchParkingAroundLocation(com.google.android.gms.maps.model.LatLng(41.8992, 12.5028))
        assertTrue(areas.isNotEmpty())
        val giolitti = areas.first()
        assertTrue(giolitti.latitude > 41.0)
        assertTrue(giolitti.longitude > 12.0)
    }

    @Test
    fun departure_signal_awards_50_points() {
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
    fun registering_parked_stay_awards_points_and_sets_active_stay() {
        val repo = ParkingRepository()
        val initialPoints = repo.userProfile.value.points

        val stay = repo.registerParkedStay("via_giolitti_stazione", StayDuration.MIN_15)
        assertEquals(StayDuration.MIN_15, stay.duration)
        assertTrue(stay.expectedDepartureEpochMs > stay.parkedAtEpochMs)
        assertEquals(initialPoints + 25, repo.userProfile.value.points)
        assertNotNull(repo.userProfile.value.activeStay)
    }

    @Test
    fun feedback_found_awards_30_points() {
        val repo = ParkingRepository()
        val signal = repo.submitDepartureSignal("pza_vittorio_nord")
        val pointsBefore = repo.userProfile.value.points

        repo.submitFeedback(signal.id, FeedbackType.FOUND)
        val pointsAfter = repo.userProfile.value.points
        assertEquals(pointsBefore + 30, pointsAfter)
    }

    @Test
    fun advanced_filters_work_correctly() {
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
    fun user_activity_history_records_actions_and_points() {
        val repo = ParkingRepository()
        val initialCount = repo.userActivities.value.size
        assertTrue(initialCount >= 5)

        repo.submitDepartureSignal("via_giolitti_stazione", 10)
        val afterCount = repo.userActivities.value.size
        assertEquals(initialCount + 1, afterCount)

        val latest = repo.userActivities.value.first()
        assertEquals(com.example.data.model.UserActivityType.REPORTED_DEPARTURE, latest.type)
        assertEquals(50, latest.rewardPoints)
    }

    @Test
    fun radar_sub_blocks_cover_termini_esquilino() {
        val repo = ParkingRepository()
        val blocks = repo.radarSubBlocks
        assertTrue(blocks.size >= 6)
        assertTrue(blocks.any { it.id.contains("termini") })
        assertTrue(blocks.any { it.id.contains("vittorio") })
        assertTrue(blocks.all { it.congestionPercent in 0..100 })
        assertTrue(blocks.all { it.estimatedSearchTimeMinutes > 0 })
    }

    @Test
    fun distance_calculation_haversine_works() {
        // Termini to Piazza Vittorio is approximately 500-600 meters
        val dist = com.example.util.ParkingNotificationService.calculateDistanceMeters(
            41.8992, 12.5028,
            41.8962, 12.5048
        )
        assertTrue("Distance should be around 350-500 meters but was $dist", dist in 250..650)
    }
}
