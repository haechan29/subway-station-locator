package com.hc.subway_station_locator.presentation.view_model

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.hc.subway_station_locator.domain.model.LocationVO
import com.hc.subway_station_locator.domain.model.SubwayStationRouteVO
import com.hc.subway_station_locator.domain.model.SubwayStationVO
import com.hc.subway_station_locator.domain.usecase.GetAllSubwayStationUseCase
import com.hc.subway_station_locator.domain.usecase.InsertSubwayStationUseCase
import com.hc.subway_station_locator.app.utils.LocationUtils
import com.hc.subway_station_locator.app.utils.SubwayStationUtils
import com.hc.subway_station_locator.domain.model.NearbySubwayStationsVO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class MainViewModel: BaseViewModel() {

    private val getAllSubwayStationUseCase = GetAllSubwayStationUseCase()
    private val insertSubwayStationUseCase = InsertSubwayStationUseCase()

    val currentLocation = LocationUtils.currentLocation.map { getCurrentLocation(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)

    private val currentNearbySubwayStations = currentLocation.map { getCurrentNearbySubwayStations(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)
    val distanceFromNextSubwayStation = currentNearbySubwayStations.map { getDistanceFromNextSubwayStation(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)
    val remainingSubwayStations = currentNearbySubwayStations.map { getRemainingSubwayStations(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)

    val departureSubwayStation = MutableStateFlow<BaseState<SubwayStationVO>>(BaseState.Unset)
    val arrivalSubwayStation = MutableStateFlow<BaseState<SubwayStationVO>>(BaseState.Unset)

    private val _subwayStationRoute = MutableStateFlow<BaseState<SubwayStationRouteVO>>(BaseState.Unset)
    val subwayStationRoute get() = _subwayStationRoute.asStateFlow()

    val subwayStationSearchText = MutableStateFlow<BaseState<String>>(BaseState.Unset)

    val searchSubwaySearchResult = subwayStationSearchText.map { getSubwayStationSearchResult(it.valueOnSet) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)

    init {
        fetchSubwayStationRouteState()
    }

    fun startForegroundService() {
        // TODO(추후 구현)
        setEffect { MainEffect.StartSubwayStationForegroundService(0) }
    }

    fun seePreviousSubwayStation() {
        if (!subwayStationRoute.value.isSet) return

        setState (_subwayStationRoute) {
            BaseState.Set(valueOnSet!!.decreaseSubwayStationIndex())
        }
    }

    fun seeNextSubwayStation() {
        if (!subwayStationRoute.value.isSet) return

        setState (_subwayStationRoute) {
            BaseState.Set(valueOnSet!!.increaseSubwayStationIndex())
        }
    }

    fun moveToSetRouteFragment() {
        setState(departureSubwayStation) { BaseState.Unset }
        setState(arrivalSubwayStation) { BaseState.Unset }

        setEffect { MainEffect.MoveToSetRouteFragment }
    }

    fun startSetDepartureSubwayStation() {
        setState(departureSubwayStation) { SubwayStationState.Waiting }

        setEffect { MainEffect.MoveToSearchSubwayStationFragment }
    }

    fun startSetArrivalSubwayStation() {
        setState(arrivalSubwayStation) { SubwayStationState.Waiting }

        setEffect { MainEffect.MoveToSearchSubwayStationFragment }
    }

    fun finishSetSubwayStation(subwayStation: SubwayStationVO) {
        listOf(departureSubwayStation, arrivalSubwayStation)
            .first { it.value is SubwayStationState.Waiting }
            .let { setState(it) { BaseState.Set(subwayStation) } }

        saveSubwayStation(subwayStation)

        finishSearchSubwayStation()
    }

    fun finishSetRoute() {
        setEffect { MainEffect.OnBackPressed }
    }

    fun setSubwayStationSearchText(searchText: CharSequence) {
        setState(subwayStationSearchText) {
            BaseState.Set(searchText.toString())
        }
    }

    fun fetchCurrentLocation() {
        setEffect { MainEffect.FetchCurrentLocation }
    }

    private fun getCurrentNearbySubwayStations(currentLocation: BaseState<LocationVO>): BaseState<NearbySubwayStationsVO> {
        val currentLocation = currentLocation.valueOnSet?.location ?: return BaseState.Unset
        val subwayStationRoute = subwayStationRoute.value.valueOnSet?.subwayStationRoute ?: return BaseState.Unset

        val currentNearbySubwayStations = SubwayStationUtils.getNearbySubwayStation(currentLocation, subwayStationRoute)

        if (currentNearbySubwayStations[0] == subwayStationRoute[0]) {
            val distanceFromNextSubwayStation = LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, subwayStationRoute[1].latitude, subwayStationRoute[1].longitude)
            val distanceBetweenDepartureSubwayStationAndNextSubwayStation = LocationUtils.getDistanceBetween(subwayStationRoute[0].latitude, subwayStationRoute[0].longitude, subwayStationRoute[1].latitude, subwayStationRoute[1].longitude)

            if (distanceFromNextSubwayStation > distanceBetweenDepartureSubwayStationAndNextSubwayStation) {
                return BaseState.Set(NearbySubwayStationsVO(null, currentNearbySubwayStations[0]))
            }
        } else if (currentNearbySubwayStations[0] == subwayStationRoute.last()) {
            val distanceFromPreviousSubwayStation = LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].latitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].longitude)
            val distanceBetweenArrivalSubwayStationAndPreviousSubwayStation = LocationUtils.getDistanceBetween(subwayStationRoute.last().latitude, subwayStationRoute.last().longitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].latitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].longitude)

            if (distanceFromPreviousSubwayStation > distanceBetweenArrivalSubwayStationAndPreviousSubwayStation) {
                return BaseState.Set(NearbySubwayStationsVO(currentNearbySubwayStations.last(), null))
            }
        }

        return BaseState.Set(NearbySubwayStationsVO(currentNearbySubwayStations[0], currentNearbySubwayStations[1]))
    }

    private fun getDistanceFromNextSubwayStation(currentNearbySubwayStations: BaseState<NearbySubwayStationsVO>): BaseState<Int> {

        val currentLocation = currentLocation.value.valueOnSet?.location ?: return BaseState.Unset
        val currentNearbySubwayStations = currentNearbySubwayStations.valueOnSet ?: return BaseState.Unset

        val distanceFromNextSubwayStation = currentNearbySubwayStations.nextSubwayStation?.let { nextSubwayStation ->
            LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, nextSubwayStation.latitude, nextSubwayStation.longitude)
        }

        return BaseState.Set((distanceFromNextSubwayStation ?: 0F).toInt())
    }
    
    private fun getRemainingSubwayStations(currentNearbySubwayStations: BaseState<NearbySubwayStationsVO>): BaseState<Int> {

        val subwayStationRoute = subwayStationRoute.value.valueOnSet?.subwayStationRoute ?: return BaseState.Unset
        val currentNearbySubwayStations = currentNearbySubwayStations.valueOnSet ?: return BaseState.Unset

        if (currentNearbySubwayStations.previousSubwayStation == null) {
            return BaseState.Set(subwayStationRoute.size)
        }

        if (currentNearbySubwayStations.nextSubwayStation == null) {
            return BaseState.Set(0)
        }

        val indexOfPreviousSubwayStation = currentNearbySubwayStations.previousSubwayStation?.let { subwayStationRoute.indexOf(it) } ?: 0
        return BaseState.Set(subwayStationRoute.lastIndex - indexOfPreviousSubwayStation)
    }

    private suspend fun getSubwayStationSearchResult(search: String?): BaseState<List<SubwayStationVO>> {
        return if (search.isNullOrEmpty()) {
            getAllSubwayStationUseCase()
                .getOrElse { return BaseState.Unset }
        } else {
            SubwayStationUtils.subwayStations.filter { subwayStation ->
                subwayStation.name.startsWith(search)
            }
        }.let {
            BaseState.Set(it)
        }
    }

    private fun getCurrentLocation(location: Location?): BaseState<LocationVO> {
        location ?: return BaseState.Unset

        return BaseState.Set(LocationVO(location))
    }

    private fun saveSubwayStation(subwayStation: SubwayStationVO) {
        viewModelScope.launch {
            insertSubwayStationUseCase(subwayStation)
        }
    }

    private fun finishSearchSubwayStation() {
        setState(subwayStationSearchText) { BaseState.Unset }

        setEffect { MainEffect.OnBackPressed }
    }

    private fun fetchSubwayStationRouteState() {
        viewModelScope.launch {
            val subwayStationRoute = departureSubwayStation.filter { it.isSet }.zip(arrivalSubwayStation.filter { it.isSet }) { departureSubwayStation, arrivalSubwayStation ->
                SubwayStationUtils.getShortestSubwayStationRoute(departureSubwayStation.valueOnSet!!, arrivalSubwayStation.valueOnSet!!)
            }

            subwayStationRoute.collect {
                setState(_subwayStationRoute) { getSubwayStationRouteState(it) }
            }
        }

    }

    private fun getSubwayStationRouteState(subwayStationRoute: List<SubwayStationVO>): BaseState<SubwayStationRouteVO> {
        val currentLocation = LocationUtils.currentLocation.value ?: return SubwayStationRouteState.LocationNotAvailableFail

        val nextSubwayStationIndex = SubwayStationUtils.getNearbySubwayStation(currentLocation, subwayStationRoute)
            .ifEmpty { return SubwayStationRouteState.NoSubwayStationRouteNearby }
            .let { nearbySubwayStations -> nearbySubwayStations[1] }
            .let { closestSubwayStation -> subwayStationRoute.indexOf(closestSubwayStation) }

        return BaseState.Set(SubwayStationRouteVO(subwayStationRoute, nextSubwayStationIndex))
    }
}