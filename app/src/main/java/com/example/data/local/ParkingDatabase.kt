package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DepartureSignalDao
import com.example.data.local.dao.FavoriteParkingAreaDao
import com.example.data.local.dao.MicroAreaDao
import com.example.data.local.dao.UserActivityDao
import com.example.data.local.dao.UserParkingReportDao
import com.example.data.local.entity.DepartureSignalEntity
import com.example.data.local.entity.FavoriteParkingAreaEntity
import com.example.data.local.entity.MicroAreaEntity
import com.example.data.local.entity.UserActivityEntity
import com.example.data.local.entity.UserParkingReportEntity

@Database(
    entities = [
        MicroAreaEntity::class,
        DepartureSignalEntity::class,
        UserActivityEntity::class,
        UserParkingReportEntity::class,
        FavoriteParkingAreaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun microAreaDao(): MicroAreaDao
    abstract fun departureSignalDao(): DepartureSignalDao
    abstract fun userActivityDao(): UserActivityDao
    abstract fun userParkingReportDao(): UserParkingReportDao
    abstract fun favoriteParkingAreaDao(): FavoriteParkingAreaDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingDatabase? = null

        fun getInstance(context: Context): ParkingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingDatabase::class.java,
                    "roma_parcheggi_cache.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
