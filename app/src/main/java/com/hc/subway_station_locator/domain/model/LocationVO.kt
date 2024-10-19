package com.hc.subway_station_locator.domain.model

import android.location.Location
import java.text.SimpleDateFormat
import java.util.Locale

data class LocationVO(
    val location: Location,
    val lastUpdatedAt: String = format(System.currentTimeMillis()),
) {
    companion object {

        private fun format(epochTime: Long): String {
            return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).run {
                format(epochTime)
            }
        }
    }
}