package com.hc.subway_station_locator.domain.model


data class SubwayStationRouteVO(
    val subwayStationRoute: List<SubwayStationVO>,
) {

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