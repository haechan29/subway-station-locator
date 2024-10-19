package com.hc.subway_station_locator.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.hc.subway_station_locator.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PermissionUtils {
    private val _requestedPermissions = MutableSharedFlow<Array<String>>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val requestedPermissions get() = _requestedPermissions.asSharedFlow()

    fun getPermissionNotGrantedMessage(context: Context, permission: Permission): String {
        return context.getString(R.string.permission_not_granted, context.getString(permission.nameInKorean))
    }

    enum class Permission(@StringRes val nameInKorean: Int, private val manifestName: String, private val minSdkVersion: Int = Int.MIN_VALUE) {
        ACCESS_FINE_LOCATION(R.string.permission_name_in_korean_fine_location, Manifest.permission.ACCESS_FINE_LOCATION),
        ACCESS_COARSE_LOCATION(R.string.permission_name_in_korean_coarse_location, Manifest.permission.ACCESS_COARSE_LOCATION),
        POST_NOTIFICATIONS(R.string.permission_name_in_korean_post_notification, Manifest.permission.POST_NOTIFICATIONS, Build.VERSION_CODES.TIRAMISU);

        companion object {
            fun findPermissionByManifestName(manifestName: String): Permission {
                return Permission.entries.first { it.manifestName == manifestName }
            }

            fun checkLocationPermission(context: Context): Result<Unit> {
                return runCatching {
                    val permissionManifestNames = listOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
                        .filterNot { permission -> permission.isPermissionGranted(context) }
                        .ifEmpty { return@runCatching }
                        .map { permission -> permission.manifestName }

                    _requestedPermissions.tryEmit(permissionManifestNames.toTypedArray())

                    throw Exception(getPermissionNotGrantedMessage(context, Permission.ACCESS_FINE_LOCATION))
                }
            }

            fun checkNotificationPermission(context: Context): Result<Unit> {
                return runCatching {
                    if (!Permission.POST_NOTIFICATIONS.isPermissionGranted(context)) {
                        _requestedPermissions.tryEmit(arrayOf(Permission.POST_NOTIFICATIONS.manifestName))

                        throw Exception(getPermissionNotGrantedMessage(context,
                            Permission.POST_NOTIFICATIONS
                        ))
                    }
                }
            }
        }

        private fun isPermissionGranted(context: Context): Boolean {
            return Build.VERSION.SDK_INT < minSdkVersion ||
                    ContextCompat.checkSelfPermission(context, manifestName) == PackageManager.PERMISSION_GRANTED
        }
    }
}