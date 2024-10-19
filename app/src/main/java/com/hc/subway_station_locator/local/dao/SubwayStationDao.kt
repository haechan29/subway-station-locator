package com.hc.subway_station_locator.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hc.subway_station_locator.local.entity.SubwayStationEntity

@Dao
interface SubwayStationDao {
    @Query("SELECT * FROM subway_station ORDER BY searchedAt DESC LIMIT 10")
    fun getAllSubwayStations(): List<SubwayStationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubwayStation(subwayStation: SubwayStationEntity)

    @Delete
    fun deleteSubwayStation(subwayStation: SubwayStationEntity)
}