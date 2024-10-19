package com.hc.subway_station_locator.domain.usecase

import com.hc.subway_station_locator.data.repository.SubwayStationRepositoryImpl
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import com.hc.subway_station_locator.domain.repository.SubwayStationRepository

class InsertSubwayStationUseCase {
    private val subwayStationRepository: SubwayStationRepository by lazy { SubwayStationRepositoryImpl() }

    suspend operator fun invoke(subwayStation: SubwayStationVO): Result<Unit> {
        return subwayStationRepository.insertSubwayStation(subwayStation)
    }
}