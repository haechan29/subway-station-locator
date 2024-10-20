package com.hc.subway_station_locator.domain.model


data class SubwayStationRouteVO(
    val subwayStationRoute: List<SubwayStationVO>,
) {

    fun toIntervals(): List<SubwayStationInterval> {
        val indicesOfSubwayStationTransfer = subwayStationRoute.indices
            .filter { i -> i ==  0 || i == subwayStationRoute.lastIndex || subwayStationRoute[i].lineNumber != subwayStationRoute[i + 1].lineNumber }

        return (0 .. subwayStationRoute.lastIndex).map { i ->
            when (i) {
                subwayStationRoute.lastIndex -> SubwayStationArrivalVO(subwayStationRoute[i])
                in indicesOfSubwayStationTransfer -> SubwayStationTransferVO(
                    subwayStationRoute[i],
                    subwayStationRoute[i + 1].name,
                    subwayStationRoute[i + 1].lineNumber,
                    (i + 1 until indicesOfSubwayStationTransfer.first { it > i }).map { subwayStationRoute[it] }
                )
                else -> SubwayStationMiddleVO(subwayStationRoute[i])
            }
        }
    }
}