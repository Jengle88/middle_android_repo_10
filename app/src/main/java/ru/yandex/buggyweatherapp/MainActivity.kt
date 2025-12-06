package ru.yandex.buggyweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.yandex.buggyweatherapp.ui.screens.WeatherScreen
import ru.yandex.buggyweatherapp.ui.theme.BuggyWeatherAppTheme
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModel()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                viewModel.fetchCurrentLocationWeather()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                viewModel.fetchCurrentLocationWeather()
            }
            else -> {
                Toast.makeText(this, "Для определения погоды нужно разрешение", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation) {
            viewModel.fetchCurrentLocationWeather()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }


        enableEdgeToEdge()
        
        setContent {
            BuggyWeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val state by viewModel.state.collectAsStateWithLifecycle()

                    WeatherScreen(
                        modifier = Modifier.padding(innerPadding),
                        state = state,
                        onSearchTextChange = viewModel::onSearchTextChange,
                        onSearchWeatherByCity = viewModel::searchWeatherByCity,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onFetchCurrentLocationWeather = viewModel::fetchCurrentLocationWeather,
                    )

                    LaunchedEffect(Unit) {
                        viewModel.initialize()
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
    }
}
