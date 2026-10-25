package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MicroArea

@Entity(tableName = "cached_micro_areas")
data class MicroAreaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int,
    val radiusMeters: Int,
    val baselineAvailability: Int,
    val blueLineTariff: String,
    val whiteLineSpots: Int,
    val ztlZoneName: String,
    val isZtlBoundary: Boolean,
    val cleaningSchedule: String,
    val normX: Float,
    val normY: Float,
    val hasEvCharging: Boolean,
    val allowsLongStay: Boolean,
    val isEveningFree: Boolean,
    val availabilityNextHour: Int,
    val availabilityAfter6Pm: Int,
    val cachedAtEpochMs: Long = System.currentTimeMillis()
) {
    fun toModel(activeSignalsCount: Int = 0, upcomingDeparturesCount: Int = 0): MicroArea {
        return MicroArea(
            id = id,
            name = name,
            streetName = streetName,
            latitude = latitude,
            longitude = longitude,
            distanceMeters = distanceMeters,
            radiusMeters = radiusMeters,
            baselineAvailability = baselineAvailability,
            blueLineTariff = blueLineTariff,
            whiteLineSpots = whiteLineSpots,
            ztlZoneName = ztlZoneName,
            isZtlBoundary = isZtlBoundary,
            cleaningSchedule = cleaningSchedule,
            normX = normX,
            normY = normY,
            activeSignalsCount = activeSignalsCount,
            upcomingDeparturesCount = upcomingDeparturesCount,
            hasEvCharging = hasEvCharging,
            allowsLongStay = allowsLongStay,
            isEveningFree = isEveningFree,
            availabilityNextHour = availabilityNextHour,
            availabilityAfter6Pm = availabilityAfter6Pm
        )
    }

    companion object {
        fun fromModel(model: MicroArea, now: Long = System.currentTimeMillis()): MicroAreaEntity {
            return MicroAreaEntity(
                id = model.id,
                name = model.name,
                streetName = model.streetName,
                latitude = model.latitude,
                longitude = model.longitude,
                distanceMeters = model.distanceMeters,
                radiusMeters = model.radiusMeters,
                baselineAvailability = model.baselineAvailability,
                blueLineTariff = model.blueLineTariff,
                whiteLineSpots = model.whiteLineSpots,
                ztlZoneName = model.ztlZoneName,
                isZtlBoundary = model.isZtlBoundary,
                cleaningSchedule = model.cleaningSchedule,
                normX = model.normX,
                normY = model.normY,
                hasEvCharging = model.hasEvCharging,
                allowsLongStay = model.allowsLongStay,
                isEveningFree = model.isEveningFree,
                availabilityNextHour = model.availabilityNextHour,
                availabilityAfter6Pm = model.availabilityAfter6Pm,
                cachedAtEpochMs = now
            )
        }
    }
}
