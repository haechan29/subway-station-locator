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
import com.hc.subway_station_locator.domain.model.SubwayStationArrivalVO
import com.hc.subway_station_locator.domain.model.SubwayStationInterval
import com.hc.subway_station_locator.domain.model.SubwayStationMiddleVO
import com.hc.subway_station_locator.domain.model.SubwayStationTransferVO
import com.hc.subway_station_locator.presentation.view_model.BaseState.Unset.toState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val subwayStationRoute = departureSubwayStation.mapNotNull { it.valueOnSet }.combine(arrivalSubwayStation.mapNotNull { it.valueOnSet }) { departureSubwayStation, arrivalSubwayStation -> BaseState.Set(SubwayStationRouteVO(SubwayStationUtils.getShortestSubwayStationRoute(departureSubwayStation, arrivalSubwayStation))) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)

    val subwayStationIntervals = combine(subwayStationRoute.mapNotNull { it.valueOnSet }, currentNearbySubwayStations.mapNotNull { it.valueOnSet }) { subwayStationRoute, currentNearbySubwayStations -> getSubwayStationIntervals(subwayStationRoute, currentNearbySubwayStations) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val _subwayStationIndex = MutableStateFlow<BaseState<Int>>(BaseState.Unset)
    val subwayStationIndex get() = _subwayStationIndex.asStateFlow()

    val currentSubwayStation = subwayStationRoute.mapNotNull { it.valueOnSet }.combine(subwayStationIndex.mapNotNull { it.valueOnSet }) { subwayStationRoute, subwayStationIndex -> if (subwayStationIndex in subwayStationRoute.subwayStationRoute.indices) subwayStationRoute.subwayStationRoute[subwayStationIndex] else null }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val previousSubwayStation = subwayStationRoute.mapNotNull { it.valueOnSet }.combine(subwayStationIndex.mapNotNull { it.valueOnSet }) { subwayStationRoute, subwayStationIndex -> if (subwayStationIndex == 0) { null } else { if (subwayStationIndex in subwayStationRoute.subwayStationRoute.indices) subwayStationRoute.subwayStationRoute[subwayStationIndex - 1] else null } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
    val nextSubwayStation = subwayStationRoute.mapNotNull { it.valueOnSet }.combine(subwayStationIndex.mapNotNull { it.valueOnSet }) { subwayStationRoute, subwayStationIndex -> if (subwayStationIndex == subwayStationRoute.subwayStationRoute.lastIndex) { null } else { if (subwayStationIndex in subwayStationRoute.subwayStationRoute.indices) subwayStationRoute.subwayStationRoute[subwayStationIndex + 1] else null } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val subwayStationSearchText = MutableStateFlow<BaseState<String>>(BaseState.Unset)

    val searchSubwaySearchResult = subwayStationSearchText.map { getSubwayStationSearchResult(it.valueOnSet) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BaseState.Unset)

    init {
        collectSubwayStationRoute()
    }

    fun startForegroundService() {
        // TODO(추후 구현)
        setEffect { MainEffect.StartSubwayStationForegroundService(0) }
    }

    fun seePreviousSubwayStation() {
        setState (_subwayStationIndex) {
            BaseState.Set((valueOnSet!! - 1).coerceAtLeast(0))
        }
    }

    fun seeNextSubwayStation() {

        val subwayStationRoute = subwayStationRoute.value.valueOnSet?.subwayStationRoute ?: return

        setState (_subwayStationIndex) {
            BaseState.Set((valueOnSet!! + 1).coerceAtMost(subwayStationRoute.lastIndex))
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

    private fun collectSubwayStationRoute() {

        viewModelScope.launch {
            subwayStationRoute.mapNotNull { it.valueOnSet }.collect {
                setState(_subwayStationIndex) { getIndexOfNextSubwayStation(it.subwayStationRoute).toState() }
            }
        }
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

    private fun getSubwayStationIntervals(subwayStationRouteVO: SubwayStationRouteVO, currentNearbySubwayStations: NearbySubwayStationsVO): List<SubwayStationInterval> {

        val subwayStationRoute = subwayStationRouteVO.subwayStationRoute

        val indicesOfSubwayStationTransfer = subwayStationRoute.indices
            .filter { i -> i ==  0 || i == subwayStationRoute.lastIndex || subwayStationRoute[i].lineNumber != subwayStationRoute[i + 1].lineNumber }

        return (0 .. subwayStationRoute.lastIndex).map { i ->
            when (i) {
                subwayStationRoute.lastIndex -> SubwayStationArrivalVO(subwayStationRoute[i], currentNearbySubwayStations.previousSubwayStation == subwayStationRoute[i])
                in indicesOfSubwayStationTransfer -> SubwayStationTransferVO(
                    subwayStationRoute[i],
                    currentNearbySubwayStations.previousSubwayStation == subwayStationRoute[i],
                    subwayStationRoute[i + 1].name,
                    subwayStationRoute[i + 1].lineNumber,
                    (i + 1 until indicesOfSubwayStationTransfer.first { it > i })
                )
                else -> SubwayStationMiddleVO(
                    subwayStationRoute[i],
                    currentNearbySubwayStations.previousSubwayStation == subwayStationRoute[i]
                )
            }
        }
    }

    private fun getIndexOfNextSubwayStation(subwayStationRoute: List<SubwayStationVO>): Result<Int> {

        return runCatching {

            val currentLocation = LocationUtils.currentLocation.value ?: throw Exception("위치가 설정되지 않았습니다.")

            val currentNearbySubwayStations = SubwayStationUtils.getNearbySubwayStation(currentLocation, subwayStationRoute)

            if (currentNearbySubwayStations[0] == subwayStationRoute[0]) {
                val distanceFromNextSubwayStation = LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, subwayStationRoute[1].latitude, subwayStationRoute[1].longitude)
                val distanceBetweenDepartureSubwayStationAndNextSubwayStation = LocationUtils.getDistanceBetween(subwayStationRoute[0].latitude, subwayStationRoute[0].longitude, subwayStationRoute[1].latitude, subwayStationRoute[1].longitude)

                if (distanceFromNextSubwayStation > distanceBetweenDepartureSubwayStationAndNextSubwayStation) {
                    return@runCatching -1
                }
            } else if (currentNearbySubwayStations[1] == subwayStationRoute.last()) {
                val distanceFromPreviousSubwayStation = LocationUtils.getDistanceBetween(currentLocation.latitude, currentLocation.longitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].latitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].longitude)
                val distanceBetweenArrivalSubwayStationAndPreviousSubwayStation = LocationUtils.getDistanceBetween(subwayStationRoute.last().latitude, subwayStationRoute.last().longitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].latitude, subwayStationRoute[subwayStationRoute.lastIndex - 1].longitude)

                if (distanceFromPreviousSubwayStation > distanceBetweenArrivalSubwayStationAndPreviousSubwayStation) {
                    return@runCatching -2
                }
            }

            currentNearbySubwayStations
                .let { nearbySubwayStations -> nearbySubwayStations[1] }
                .let { closestSubwayStation -> subwayStationRoute.indexOf(closestSubwayStation) }
        }
    }
}