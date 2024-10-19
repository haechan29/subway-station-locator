package com.hc.subway_station_locator.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

object LocationUtils {

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation get() = _currentLocation.asStateFlow()

    suspend fun fetchCurrentLocation(context: Context): Result<Unit> {
        return getCurrentLocation(context).mapCatching {
            currentLocation -> _currentLocation.update { currentLocation }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(context: Context): Result<Location> {
        return runCatching {
            PermissionUtils.Permission.checkLocationPermission(context).getOrThrow()

            withContext(Dispatchers.IO) {
                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token,
                    ).let {
                        Tasks.await(it)
                    }
            }
        }
    }

    fun getDistanceBetween(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Float {
        val distanceArray = FloatArray(1)
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, distanceArray)
        return distanceArray[0]
    }
}