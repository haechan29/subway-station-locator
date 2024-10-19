package com.hc.subway_station_locator.data.repository

import com.hc.subway_station_locator.data.mapper.SubwayStationDtoMapper.toDomain
import com.hc.subway_station_locator.data.mapper.SubwayStationDtoMapper.toDto
import com.hc.subway_station_locator.data.source.local.SubwayStationLocalDataSource
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import com.hc.subway_station_locator.domain.repository.SubwayStationRepository
import com.hc.subway_station_locator.local.source.SubwayStationLocalDataSourceImpl

class SubwayStationRepositoryImpl: SubwayStationRepository {
    private val subwayStationLocalDataSource: SubwayStationLocalDataSource by lazy { SubwayStationLocalDataSourceImpl() }

    override suspend fun getAllSubwayStations(): Result<List<SubwayStationVO>> {
        return subwayStationLocalDataSource.getAllSubwayStations()
            .mapCatching { subwayStationDtos ->
                subwayStationDtos.map { it.toDomain() }
            }
    }

    override suspend fun insertSubwayStation(subwayStation: SubwayStationVO): Result<Unit> {
        return subwayStationLocalDataSource.insertSubwayStation(subwayStation.toDto())
    }

    override suspend fun deleteSubwayStation(subwayStation: SubwayStationVO): Result<Unit> {
        return subwayStationLocalDataSource.deleteSubwayStation(subwayStation.toDto())
    }
}