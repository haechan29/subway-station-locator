package com.hc.subway_station_locator.data.dto

data class SubwayStationDto(
    val name: String,
    val lineNumber: String,
    val latitude: Double,
    val longitude: Double,
    val frCode: String,
)