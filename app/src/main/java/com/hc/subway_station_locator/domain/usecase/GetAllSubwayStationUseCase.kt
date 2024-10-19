package com.hc.subway_station_locator.domain.usecase

import com.hc.subway_station_locator.data.repository.SubwayStationRepositoryImpl
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import com.hc.subway_station_locator.domain.repository.SubwayStationRepository

class GetAllSubwayStationUseCase {
    private val subwayStationRepository: SubwayStationRepository by lazy { SubwayStationRepositoryImpl() }

    suspend operator fun invoke(): Result<List<SubwayStationVO>> {
        return subwayStationRepository.getAllSubwayStations()
    }
}