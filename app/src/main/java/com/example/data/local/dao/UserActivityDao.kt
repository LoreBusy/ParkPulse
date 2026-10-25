package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.UserActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityDao {

    @Query("SELECT * FROM user_activity_records ORDER BY timestampEpochMs DESC")
    fun getAllActivitiesFlow(): Flow<List<UserActivityEntity>>

    @Query("SELECT * FROM user_activity_records ORDER BY timestampEpochMs DESC")
    suspend fun getAllActivities(): List<UserActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<UserActivityEntity>)

    @Query("SELECT COUNT(*) FROM user_activity_records")
    suspend fun getActivityCount(): Int

    @Query("DELETE FROM user_activity_records")
    suspend fun clearAll()
}
