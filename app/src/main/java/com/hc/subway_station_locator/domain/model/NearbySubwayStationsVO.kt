package com.hc.subway_station_locator.domain.model

data class NearbySubwayStationsVO(
    val previousSubwayStation: SubwayStationVO?,
    val nextSubwayStation: SubwayStationVO?,
)