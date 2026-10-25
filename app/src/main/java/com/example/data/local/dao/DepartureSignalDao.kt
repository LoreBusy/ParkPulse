package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DepartureSignalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartureSignalDao {
    @Query("SELECT * FROM cached_departure_signals WHERE expiryEpochMs > :now ORDER BY createdEpochMs DESC")
    fun getActiveCachedSignals(now: Long): Flow<List<DepartureSignalEntity>>

    @Query("SELECT * FROM cached_departure_signals ORDER BY createdEpochMs DESC")
    fun getAllCachedSignals(): Flow<List<DepartureSignalEntity>>

    @Query("SELECT * FROM cached_departure_signals WHERE expiryEpochMs > :now ORDER BY createdEpochMs DESC")
    suspend fun getActiveCachedSignalsSnapshot(now: Long): List<DepartureSignalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: DepartureSignalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<DepartureSignalEntity>)

    @Query("DELETE FROM cached_departure_signals WHERE id = :id")
    suspend fun deleteSignalById(id: String)

    @Query("DELETE FROM cached_departure_signals WHERE expiryEpochMs <= :now")
    suspend fun purgeExpired(now: Long)

    @Query("DELETE FROM cached_departure_signals")
    suspend fun clearAll()
}
