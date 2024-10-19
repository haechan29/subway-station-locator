package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils
import kotlinx.serialization.Serializable

@Serializable
data class SubwayStationVO(
    val name: String,
    val lineNumber: String,
    val latitude: Double,
    val longitude: Double,
    val frCode: String,
) {

    val lineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(lineNumber)
}