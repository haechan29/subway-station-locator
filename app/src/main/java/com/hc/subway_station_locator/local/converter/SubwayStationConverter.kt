package com.hc.subway_station_locator.local.converter

import androidx.room.TypeConverter
import com.hc.subway_station_locator.local.entity.SubwayStationEntity
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SubwayStationConverter {

    @TypeConverter
    fun toSubwayStationEntity(string: String): SubwayStationEntity {
        return Json.decodeFromString<SubwayStationEntity>(string)
    }

    @TypeConverter
    fun toString(subwayStationEntity: SubwayStationEntity): String {
        return Json.encodeToString(subwayStationEntity)
    }
}