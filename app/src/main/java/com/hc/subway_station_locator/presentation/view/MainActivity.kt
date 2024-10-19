package com.hc.subway_station_locator.presentation.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hc.subway_station_locator.app.Constants
import com.hc.subway_station_locator.presentation.service.ForegroundService
import com.hc.subway_station_locator.databinding.ActivityMainBinding
import com.hc.subway_station_locator.app.utils.LocationUtils
import com.hc.subway_station_locator.app.utils.PermissionUtils
import com.hc.subway_station_locator.presentation.view_model.MainEffect
import com.hc.subway_station_locator.presentation.view_model.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val setRouteFragment by lazy { SetRouteFragment() }
    private val searchSubwayStationFragment by lazy { SearchSubwayStationFragment() }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            result.entries.firstOrNull()?.also { (permissionManifestName, isPermissionGranted) ->
                if (!isPermissionGranted) {
                    PermissionUtils.Permission.findPermissionByManifestName(permissionManifestName).let { permission ->
                        showToast(PermissionUtils.getPermissionNotGrantedMessage(this, permission))
                    }
                }
            }
        }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleEffect()
        handlePermissionRequest()

        fetchLocation()
    }

    private fun handleEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        MainEffect.OnBackPressed -> supportFragmentManager.popBackStack()
                        is MainEffect.StartSubwayStationForegroundService -> startForegroundService("지하철 위치 알림이", "남은 정거장 수: ${effect.remainingSubwayStations}")
                        is MainEffect.ShowToast -> showToast(effect.message)
                        MainEffect.MoveToSetRouteFragment -> moveToFragment(setRouteFragment)
                        MainEffect.MoveToSearchSubwayStationFragment -> moveToFragment(searchSubwayStationFragment)
                        MainEffect.FetchCurrentLocation -> LocationUtils.fetchCurrentLocation(this@MainActivity)
                    }
                }
            }
        }
    }

    private fun handlePermissionRequest() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                PermissionUtils.requestedPermissions.collect {
                    requestPermissionLauncher.launch(it)
                }
            }
        }
    }

    private fun fetchLocation() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (true) {
                LocationUtils.fetchCurrentLocation(this@MainActivity)

                delay(10.seconds)
            }
        }
    }

    private fun moveToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerViewMain.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startForegroundService(title: String, content: String): Result<Unit> {
        return runCatching {
            PermissionUtils.Permission.checkNotificationPermission(this).getOrThrow()

            val serviceIntent = Intent(this, ForegroundService::class.java).apply {
                putExtra(Constants.EXTRA_FOREGROUND_SERVICE_TITLE, title)
                putExtra(Constants.EXTRA_FOREGROUND_SERVICE_CONTENT, content)
            }

            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }
}