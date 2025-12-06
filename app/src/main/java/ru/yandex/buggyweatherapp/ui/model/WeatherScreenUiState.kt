package ru.yandex.buggyweatherapp.ui.model

import ru.yandex.buggyweatherapp.model.WeatherData

data class WeatherScreenUiState(
    val searchText: String,
    val weatherData: WeatherData?,
    val isLoading: Boolean,
    val error: String?,
    val cityName: String
) {

    companion object {
        fun default() = WeatherScreenUiState(
            searchText = "",
            weatherData = null,
            isLoading = false,
            error = null,
            cityName = ""
        )
    }
}
