package com.hc.subway_station_locator.data.mapper

import com.hc.subway_station_locator.data.dto.SubwayStationDto
import com.hc.subway_station_locator.domain.model.SubwayStationVO

object SubwayStationDtoMapper {
    fun SubwayStationDto.toDomain(): SubwayStationVO {
        return SubwayStationVO(
            name = name,
            lineNumber = lineNumber,
            latitude = latitude,
            longitude = longitude,
            frCode = frCode
        )
    }

    fun SubwayStationVO.toDto(): SubwayStationDto {
        return SubwayStationDto(
            name = name,
            lineNumber = lineNumber,
            latitude = latitude,
            longitude = longitude,
            frCode = frCode,
        )
    }
}