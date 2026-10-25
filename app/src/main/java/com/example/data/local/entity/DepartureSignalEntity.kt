package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DepartureSignal

@Entity(tableName = "cached_departure_signals")
data class DepartureSignalEntity(
    @PrimaryKey
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
    val hasExpiredPenaltyApplied: Boolean = false,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
) {
    fun toModel(): DepartureSignal {
        return DepartureSignal(
            id = id,
            microAreaId = microAreaId,
            microAreaName = microAreaName,
            streetName = streetName,
            latitude = latitude,
            longitude = longitude,
            createdEpochMs = createdEpochMs,
            expiryEpochMs = expiryEpochMs,
            userReliabilityPercent = userReliabilityPercent,
            feedbackFoundCount = feedbackFoundCount,
            feedbackNotFoundCount = feedbackNotFoundCount,
            reportedAbuseCount = reportedAbuseCount,
            isUserSubmitted = isUserSubmitted,
            hasExpiredPenaltyApplied = hasExpiredPenaltyApplied
        )
    }

    companion object {
        fun fromModel(model: DepartureSignal, now: Long = System.currentTimeMillis()): DepartureSignalEntity {
            return DepartureSignalEntity(
                id = model.id,
                microAreaId = model.microAreaId,
                microAreaName = model.microAreaName,
                streetName = model.streetName,
                latitude = model.latitude,
                longitude = model.longitude,
                createdEpochMs = model.createdEpochMs,
                expiryEpochMs = model.expiryEpochMs,
                userReliabilityPercent = model.userReliabilityPercent,
                feedbackFoundCount = model.feedbackFoundCount,
                feedbackNotFoundCount = model.feedbackNotFoundCount,
                reportedAbuseCount = model.reportedAbuseCount,
                isUserSubmitted = model.isUserSubmitted,
                hasExpiredPenaltyApplied = model.hasExpiredPenaltyApplied,
                cachedAtEpochMs = now
            )
        }
    }
}
