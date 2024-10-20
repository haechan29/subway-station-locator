package com.hc.subway_station_locator.domain.model

sealed class SubwayStationInterval(
    open val subwayStation: SubwayStationVO,
    open val isCurrentLocation: Boolean,
)