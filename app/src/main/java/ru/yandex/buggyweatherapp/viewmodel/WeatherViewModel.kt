package ru.yandex.buggyweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.ui.model.WeatherScreenUiState

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherScreenUiState.default())
    val state = _state.asStateFlow()

    private var currentLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    private var refreshJob: Job? = null

    fun initialize() {
        startAutoRefresh()
    }
    
    fun fetchCurrentLocationWeather() {
        _state.update {
            it.copy(
                isLoading = true,
                error = null,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                val cityNameFromLocation =
                    locationRepository.getCityNameFromLocation(location)
                val weatherData = getWeatherForLocation(location).getOrNull()
                if (weatherData != null) {
                    currentLocation.update { location }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            cityName = cityNameFromLocation ?: "",
                            weatherData = weatherData
                        )
                    }
                    return@launch
                }
            }
            currentLocation.update { null }
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Unable to get current location",

                )
            }
        }
    }

    private suspend fun getWeatherForLocation(location: Location): Result<WeatherData> {
        return weatherRepository.getWeatherData(location)
    }

    fun onSearchTextChange(newText: String) {
        _state.update { it.copy(searchText = newText) }
    }
    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            _state.update { it.copy(error = "City name cannot be empty") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(Dispatchers.IO) {
            val weatherData = weatherRepository.getWeatherByCity(city).getOrNull()
            _state.update {
                if (weatherData != null) {
                    currentLocation.update { Location(0.0, 0.0, weatherData.cityName) }
                    it.copy(
                        isLoading = false,
                        error = null,
                        cityName = weatherData.cityName,
                        weatherData = weatherData,
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        error = "Unable to fetch weather for $city"
                    )
                }
            }
        }
    }
    
    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                _state.update { it.copy(isLoading = true) }
                val weatherData = currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }?.getOrNull()
                if (weatherData != null)
                    _state.update { it.copy(isLoading = false, weatherData = weatherData, error = null) }
                else _state.update { it.copy(isLoading = false) }
                delay(AUTO_REFRESH_DELAY)
            }
        }
    }
    
    fun toggleFavorite() {
        _state.update {
            it.copy(
                weatherData = it.weatherData?.copy(
                    isFavorite = !it.weatherData.isFavorite
                )
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        refreshJob = null
    }

    private companion object {
        private const val AUTO_REFRESH_DELAY = 60_000L
    }
}