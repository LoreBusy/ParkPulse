package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserActivityRecord
import com.example.data.model.UserActivityType

@Entity(tableName = "user_activity_records")
data class UserActivityEntity(
    @PrimaryKey
    val id: String,
    val typeName: String,
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
) {
    fun toModel(): UserActivityRecord {
        val resolvedType = try {
            UserActivityType.valueOf(typeName)
        } catch (e: Exception) {
            UserActivityType.REPORTED_DEPARTURE
        }
        return UserActivityRecord(
            id = id,
            type = resolvedType,
            title = title,
            microAreaId = microAreaId,
            microAreaName = microAreaName,
            streetAddress = streetAddress,
            latitude = latitude,
            longitude = longitude,
            timestampEpochMs = timestampEpochMs,
            rewardPoints = rewardPoints,
            statusNotes = statusNotes,
            impactMetric = impactMetric
        )
    }

    companion object {
        fun fromModel(model: UserActivityRecord): UserActivityEntity {
            return UserActivityEntity(
                id = model.id,
                typeName = model.type.name,
                title = model.title,
                microAreaId = model.microAreaId,
                microAreaName = model.microAreaName,
                streetAddress = model.streetAddress,
                latitude = model.latitude,
                longitude = model.longitude,
                timestampEpochMs = model.timestampEpochMs,
                rewardPoints = model.rewardPoints,
                statusNotes = model.statusNotes,
                impactMetric = model.impactMetric
            )
        }
    }
}
