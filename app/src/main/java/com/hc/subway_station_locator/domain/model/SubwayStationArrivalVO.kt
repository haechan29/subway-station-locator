package com.hc.subway_station_locator.domain.model

data class SubwayStationArrivalVO(
    override val subwayStation: SubwayStationVO,
): SubwayStationInterval(subwayStation)