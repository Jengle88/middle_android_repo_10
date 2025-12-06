package ru.yandex.buggyweatherapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.ui.model.WeatherScreenUiState
import ru.yandex.buggyweatherapp.utils.WeatherIconMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    state: WeatherScreenUiState,
    onSearchTextChange: (newText: String) -> Unit,
    onSearchWeatherByCity: (searchText: String) -> Unit,
    onToggleFavorite: () -> Unit,
    onFetchCurrentLocationWeather: () -> Unit,
) {
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = state.searchText,
            onValueChange = onSearchTextChange,
            label = { Text("Search city") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    onSearchWeatherByCity(state.searchText)
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchWeatherByCity(state.searchText)
            })
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (state.isLoading) {
            Text("Loading weather data...")
        }
        
        if (!state.error.isNullOrBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )

        }

        if (state.weatherData != null) {
            WeatherCard(
                weather = state.weatherData,
                cityName = state.cityName,
                onFavoriteClick = onToggleFavorite,
                onRefreshClick = onFetchCurrentLocationWeather
            )
        }
    }
}

@Composable
fun WeatherCard(
    weather: WeatherData,
    cityName: String,
    onFavoriteClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cityName.ifEmpty { weather.cityName },
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Row {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (weather.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            
            Text(
                text = "Temperature: " + weather.temperature.toString() + "°C",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Feels like: " + weather.feelsLike.toString() + "°C",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Description: " + weather.description.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Humidity: " + weather.humidity.toString() + "%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Wind: " + weather.windSpeed.toString() + " m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
                Text(
                    text = "Sunrise: " + WeatherIconMapper.formatTimestamp(weather.sunriseTime),
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "Sunset: " + WeatherIconMapper.formatTimestamp(weather.sunsetTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRefreshClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Refresh Weather")
            }
        }
    }
}