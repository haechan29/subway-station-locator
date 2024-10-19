package com.hc.subway_station_locator.domain.model

import com.hc.subway_station_locator.app.utils.SubwayStationUtils

data class SubwayStationRouteVO(
    val subwayStationRoute: List<SubwayStationVO>,
    val subwayStationIndex: Int,
) {
    val isFirstSubwayStationOnRoute get() = subwayStationIndex == 0
    val isLastSubwayStationOnRoute get() = subwayStationIndex == subwayStationRoute.lastIndex

    val currentSubwayStation get() = subwayStationRoute[subwayStationIndex]
    val previousSubwayStation get() = if (isFirstSubwayStationOnRoute) { null } else { subwayStationRoute[subwayStationIndex - 1] }
    val nextSubwayStation get() = if (isLastSubwayStationOnRoute) { null } else { subwayStationRoute[subwayStationIndex + 1] }

    val currentSubwayStationLineNumberColor get() = SubwayStationUtils.getSubwayStationLineNumberColor(currentSubwayStation.lineNumber)

    fun decreaseSubwayStationIndex() = copy(subwayStationIndex = (subwayStationIndex - 1).coerceAtLeast(0))
    fun increaseSubwayStationIndex() = copy(subwayStationIndex = (subwayStationIndex + 1).coerceAtMost(subwayStationRoute.lastIndex))

    fun toIntervals(): List<SubwayStationInterval> {
        val indicesOfSubwayStationTransfer = subwayStationRoute.indices
            .filter { i -> i != subwayStationRoute.lastIndex && subwayStationRoute[i].lineNumber != subwayStationRoute[i + 1].lineNumber }
            .let { it + listOf(0, subwayStationRoute.size) }

        return (0 .. subwayStationRoute.lastIndex).map { i ->
            if (i in indicesOfSubwayStationTransfer) {
                SubwayStationTransferVO(
                    subwayStationRoute[i],
                    subwayStationRoute[i + 1].name,
                    subwayStationRoute[i + 1].lineNumber,
                    (i + 1 until indicesOfSubwayStationTransfer.first { it > i }).map { subwayStationRoute[it] }
                )
            } else {
                SubwayStationMiddleVO(subwayStationRoute[i])
            }
        }
    }
}