package com.hc.subway_station_locator.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hc.subway_station_locator.local.entity.SubwayStationEntity
import com.hc.subway_station_locator.local.converter.SubwayStationConverter
import com.hc.subway_station_locator.local.dao.SubwayStationDao

@Database(entities = [SubwayStationEntity::class], version = 1)
@TypeConverters(SubwayStationConverter::class)
abstract class SubwayStationDatabase: RoomDatabase() {
    abstract fun subwayStationDao(): SubwayStationDao

    companion object {
        private var instance: SubwayStationDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SubwayStationDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    SubwayStationDatabase::class.java,
                    "subway station database"
                ).build()
            }
            return instance!!
        }
    }
}