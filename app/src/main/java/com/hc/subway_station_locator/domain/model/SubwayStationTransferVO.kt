package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils

data class SubwayStationTransferVO(
    override val subwayStation: SubwayStationVO,
    override val isCurrentLocation: Boolean,
    val orientation: String,
    val lineNumber: String,
    val indicesOfSubwayStationMiddle: IntRange,
    var isFolded: Boolean = false,
): SubwayStationInterval(subwayStation, isCurrentLocation) {

    val lineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(lineNumber)

    fun toggleFoldState() {
        isFolded = !isFolded
    }
}