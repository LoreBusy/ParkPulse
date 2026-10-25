package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_parking_areas")
data class FavoriteParkingAreaEntity(
    @PrimaryKey
    val areaId: String,
    val name: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val baselineAvailability: Int,
    val blueLineTariff: String,
    val hasEvCharging: Boolean = false,
    val isZtlBoundary: Boolean = false,
    val savedAtEpochMs: Long = System.currentTimeMillis()
)
