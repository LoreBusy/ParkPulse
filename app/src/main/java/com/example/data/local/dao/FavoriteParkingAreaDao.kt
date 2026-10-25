package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteParkingAreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteParkingAreaDao {
    @Query("SELECT * FROM favorite_parking_areas ORDER BY savedAtEpochMs DESC")
    fun getAllFavorites(): Flow<List<FavoriteParkingAreaEntity>>

    @Query("SELECT areaId FROM favorite_parking_areas")
    fun getAllFavoriteAreaIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_parking_areas WHERE areaId = :areaId)")
    fun isFavorite(areaId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteParkingAreaEntity)

    @Query("DELETE FROM favorite_parking_areas WHERE areaId = :areaId")
    suspend fun deleteFavoriteById(areaId: String)

    @Query("DELETE FROM favorite_parking_areas")
    suspend fun clearAll()
}
