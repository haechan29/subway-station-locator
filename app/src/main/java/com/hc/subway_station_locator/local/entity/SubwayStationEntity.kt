package com.hc.subway_station_locator.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "subway_station", primaryKeys = ["name", "lineNumber", "latitude", "longitude", "frCode"])
@Serializable
data class SubwayStationEntity(
    val name: String,
    val lineNumber: String,
    val latitude: Double,
    val longitude: Double,
    val frCode: String,
    val searchedAt: Long,
)