package ru.yandex.buggyweatherapp.di

import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.yandex.buggyweatherapp.api.WeatherApiService
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

val appModule = module {
    single<GsonConverterFactory> { GsonConverterFactory.create(Gson()) }
    single<Retrofit> { Retrofit.Builder()
        .baseUrl(WeatherApiService.BASE_URL)
        .addConverterFactory(get<GsonConverterFactory>())
        .build()
    }
    single<WeatherApiService> { get<Retrofit>().create(WeatherApiService::class.java) }

    factory<WeatherRepository> { WeatherRepository(weatherApi = get()) }
    factory<LocationRepository> { LocationRepository(context = androidContext()) }
    viewModel<WeatherViewModel> { WeatherViewModel(weatherRepository = get(), locationRepository = get()) }
}