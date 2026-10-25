package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_parking_reports")
data class UserParkingReportEntity(
    @PrimaryKey
    val id: String,
    val microAreaId: String,
    val microAreaName: String,
    val streetName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String, // e.g. "LIBERO_TRA_5_MIN", "OCCUPATO", "CONFERMATO_LIBERO"
    val parkingType: String = "STRISCE_BLU", // "STRISCE_BIANCHE", "STRISCE_BLU", "EV"
    val countdownMinutes: Int = 5,
    val userReliabilityPercent: Int = 95,
    val timestampEpochMs: Long = System.currentTimeMillis(),
    val expiryEpochMs: Long = System.currentTimeMillis() + 300_000L,
    val notes: String = "",
    val karmaEarned: Int = 50
)
