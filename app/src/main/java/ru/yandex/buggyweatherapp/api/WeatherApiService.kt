package ru.yandex.buggyweatherapp.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.yandex.buggyweatherapp.BuildConfig

interface WeatherApiService {
    
    
    companion object {
        val API_KEY = BuildConfig.WEATHER_API_KEY
        const val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    }
    
    
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<JsonObject>
    
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<JsonObject>
}