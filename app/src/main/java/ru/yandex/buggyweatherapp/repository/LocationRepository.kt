package ru.yandex.buggyweatherapp.repository

import android.content.Context
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.yandex.buggyweatherapp.model.Location
import java.util.Locale
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationRepository(
    private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    suspend fun getCurrentLocation(): Location? {
        return try {
            val lastLocation = suspendCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val userLocation = Location(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                            continuation.resumeWith(Result.success(userLocation))
                        } else {
                            continuation.resumeWith(Result.success(null))
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("LocationRepository", "Error getting location", e)
                        continuation.resumeWith(Result.success(null))
                    }
            }
            return lastLocation ?: requestLocationUpdates()
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            null
        }
    }
    
    private suspend fun requestLocationUpdates(): Location? {
        return suspendCancellableCoroutine { continuation ->
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { loc ->
                        val userLocation = Location(
                            latitude = loc.latitude,
                            longitude = loc.longitude
                        )
                        continuation.resumeWith(Result.success(userLocation))
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            try {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(5000)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("LocationRepository", "Location permission not granted", e)
                continuation.resumeWith(Result.success(null))
            } finally {
                continuation.invokeOnCancellation { fusedLocationClient.removeLocationUpdates(locationCallback) }
            }
        }
    }

    suspend fun getCityNameFromLocation(location: Location): String? =
        suspendCoroutine { continuation ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)


                if (addresses.isNullOrEmpty()) {
                    continuation.resumeWith(Result.success(null))
                    return@suspendCoroutine
                }

                val foundAddress = addresses[0]?.let { address ->
                    address.locality ?: address.subAdminArea ?: address.adminArea
                }
                continuation.resumeWith(Result.success(foundAddress))
            } catch (e: Exception) {
                Log.e("LocationRepository", "Error getting city name", e)
                continuation.resumeWithException(e)
            }
        }
}