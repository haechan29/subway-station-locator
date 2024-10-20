package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils

data class SubwayStationMiddleVO(
    override val subwayStation: SubwayStationVO,
    override val isCurrentLocation: Boolean = false,
): SubwayStationInterval(subwayStation, isCurrentLocation) {

    val lineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(subwayStation.lineNumber)
}