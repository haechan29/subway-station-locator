package com.hc.subway_station_locator.data.source.local

import com.hc.subway_station_locator.data.dto.SubwayStationDto

interface SubwayStationLocalDataSource {
    suspend fun getAllSubwayStations(): Result<List<SubwayStationDto>>

    suspend fun insertSubwayStation(subwayStationDto: SubwayStationDto): Result<Unit>

    suspend fun deleteSubwayStation(subwayStationDto: SubwayStationDto): Result<Unit>
}