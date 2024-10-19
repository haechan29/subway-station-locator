package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils

data class SubwayStationMiddleVO(
    override val subwayStation: SubwayStationVO,
): SubwayStationInterval(subwayStation) {

    val lineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(subwayStation.lineNumber)
}