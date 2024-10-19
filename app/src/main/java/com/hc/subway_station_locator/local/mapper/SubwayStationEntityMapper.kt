package com.hc.subway_station_locator.local.mapper

import com.hc.subway_station_locator.data.dto.SubwayStationDto
import com.hc.subway_station_locator.local.entity.SubwayStationEntity

object SubwayStationEntityMapper {
    fun SubwayStationEntity.toDto(): SubwayStationDto {
        return SubwayStationDto(
            name = name,
            lineNumber = lineNumber,
            latitude = latitude,
            longitude = longitude,
            frCode = frCode
        )
    }

    fun SubwayStationDto.toEntity(): SubwayStationEntity {
        return SubwayStationEntity(
            name = name,
            lineNumber = lineNumber,
            latitude = latitude,
            longitude = longitude,
            frCode = frCode,
            searchedAt = System.currentTimeMillis(),
        )
    }
}