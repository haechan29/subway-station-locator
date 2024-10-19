package com.hc.subway_station_locator.presentation.view_model

import com.hc.subway_station_locator.R
import com.hc.subway_station_locator.domain.model.SubwayStationRouteVO
import com.hc.subway_station_locator.domain.model.SubwayStationVO

sealed interface State<out T> {
    val isSet: Boolean

    interface Fail { val string: Int }
}

sealed class BaseState<out T>: State<T> {
    override val isSet get() = this is Set<*>
    val valueOnSet get() = if (isSet) (this as Set<T>).value else null

    class Set<T>(val value: T): BaseState<T>()
    data object Unset: BaseState<Nothing>()
}

sealed class SubwayStationRouteState: BaseState<SubwayStationRouteVO>() {
    data object LocationNotAvailableFail: SubwayStationRouteState(), State.Fail {
        override val string = R.string.location_not_available
    }
    data object NoSubwayStationRouteNearby: SubwayStationRouteState(), State.Fail {
        override val string = R.string.no_subway_station_nearby_1000
    }
}

sealed class SubwayStationState: BaseState<SubwayStationVO>() {
    data object Waiting: SubwayStationState()
}