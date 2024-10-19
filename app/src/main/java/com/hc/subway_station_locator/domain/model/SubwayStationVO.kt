package com.hc.subway_station_locator.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubwayStationVO(
    val name: String,
    val lineNumber: String,
    val latitude: Double,
    val longitude: Double,
    val frCode: String,
)