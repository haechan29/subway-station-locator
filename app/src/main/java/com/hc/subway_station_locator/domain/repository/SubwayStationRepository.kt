package com.hc.subway_station_locator.domain.repository

import com.hc.subway_station_locator.domain.model.SubwayStationVO

interface SubwayStationRepository {
    suspend fun getAllSubwayStations(): Result<List<SubwayStationVO>>
    suspend fun insertSubwayStation(subwayStation: SubwayStationVO): Result<Unit>
    suspend fun deleteSubwayStation(subwayStation: SubwayStationVO): Result<Unit>
}