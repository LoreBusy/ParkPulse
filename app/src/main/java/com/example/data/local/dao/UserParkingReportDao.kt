package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserParkingReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserParkingReportDao {
    @Query("SELECT * FROM user_parking_reports ORDER BY timestampEpochMs DESC")
    fun getAllReports(): Flow<List<UserParkingReportEntity>>

    @Query("SELECT * FROM user_parking_reports WHERE microAreaId = :areaId ORDER BY timestampEpochMs DESC")
    fun getReportsForArea(areaId: String): Flow<List<UserParkingReportEntity>>

    @Query("SELECT * FROM user_parking_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): UserParkingReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: UserParkingReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<UserParkingReportEntity>)

    @Query("DELETE FROM user_parking_reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("DELETE FROM user_parking_reports")
    suspend fun clearAll()
}
