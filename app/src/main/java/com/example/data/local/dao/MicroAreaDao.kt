package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MicroAreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MicroAreaDao {
    @Query("SELECT * FROM cached_micro_areas ORDER BY name ASC")
    fun getAllCachedAreas(): Flow<List<MicroAreaEntity>>

    @Query("SELECT * FROM cached_micro_areas ORDER BY name ASC")
    suspend fun getAllCachedAreasSnapshot(): List<MicroAreaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(areas: List<MicroAreaEntity>)

    @Query("DELETE FROM cached_micro_areas")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cached_micro_areas")
    suspend fun count(): Int
}
