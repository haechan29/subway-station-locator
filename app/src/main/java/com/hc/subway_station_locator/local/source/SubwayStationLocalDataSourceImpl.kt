package com.hc.subway_station_locator.local.source

import com.hc.subway_station_locator.app.Application
import com.hc.subway_station_locator.data.dto.SubwayStationDto
import com.hc.subway_station_locator.data.source.local.SubwayStationLocalDataSource
import com.hc.subway_station_locator.local.database.SubwayStationDatabase
import com.hc.subway_station_locator.local.mapper.SubwayStationEntityMapper.toDto
import com.hc.subway_station_locator.local.mapper.SubwayStationEntityMapper.toEntity
import com.hc.subway_station_locator.presentation.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubwayStationLocalDataSourceImpl: SubwayStationLocalDataSource {
    private val subwayStationDao by lazy { SubwayStationDatabase.getInstance(Application.context).subwayStationDao() }

    override suspend fun getAllSubwayStations(): Result<List<SubwayStationDto>> {
        return runCatching {
            withContext(Dispatchers.IO) {
                subwayStationDao.getAllSubwayStations().map { it.toDto() }
            }
        }
    }

    override suspend fun insertSubwayStation(subwayStationDto: SubwayStationDto): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                subwayStationDao.insertSubwayStation(subwayStationDto.toEntity())
            }
        }
    }

    override suspend fun deleteSubwayStation(subwayStationDto: SubwayStationDto): Result<Unit> {
        return runCatching {
            withContext(Dispatchers.IO) {
                subwayStationDao.deleteSubwayStation(subwayStationDto.toEntity())
            }
        }
    }
}