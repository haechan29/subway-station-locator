package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils

data class SubwayStationTransferVO(
    override val subwayStation: SubwayStationVO,
    val orientation: String,
    val lineNumber: String,
    val subwayStationMiddles: List<SubwayStationVO>,
    var isFolded: Boolean = false

): SubwayStationInterval(subwayStation) {

    val lineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(lineNumber)
}