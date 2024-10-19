package com.hc.subway_station_locator.presentation.view_model

import androidx.annotation.StringRes

sealed class Effect

sealed class MainEffect: Effect() {
    data object OnBackPressed: MainEffect()
    data class StartSubwayStationForegroundService(val remainingSubwayStations: Int): MainEffect()
    data class ShowToast(@StringRes val message: Int): MainEffect()
    data object MoveToSetRouteFragment: MainEffect()
    data object MoveToSearchSubwayStationFragment: MainEffect()
    data object FetchCurrentLocation: MainEffect()
}