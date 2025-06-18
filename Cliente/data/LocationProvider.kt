package com.example.recado.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 *  Gets device location
 */
class LocationProvider(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var lastValidLocation: Location? = null
    private val smoother = LocationSmoother()

    @SuppressLint("MissingPermission")
    suspend fun getLocation(): Location? = suspendCancellableCoroutine { cont ->
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location ->
                val valid = location?.takeIf { isValid(it) }

                if (valid != null) {
                    smoother.add(valid)
                    val smoothed = smoother.getSmoothedLocation()
                    val result = smoothed?.takeIf { isValid(it) } ?: valid
                    lastValidLocation = result
                    cont.resume(result)
                } else {
                    cont.resume(lastValidLocation)
                }
            }
            .addOnFailureListener {
                cont.resume(lastValidLocation)
            }
    }

    private fun isValid(location: Location?): Boolean {
        return location != null &&
                location.accuracy <= 50f &&
                location.latitude != 0.0 &&
                location.longitude != 0.0
    }

    private class LocationSmoother(private val maxSize: Int = 5) {
        private val locations = ArrayDeque<Location>()

        fun add(location: Location) {
            locations.addLast(location)
            if (locations.size > maxSize) {
                locations.removeFirst()
            }
        }

        fun getSmoothedLocation(): Location? {
            if (locations.isEmpty()) return null
            val avgLat = locations.map { it.latitude }.average()
            val avgLon = locations.map { it.longitude }.average()
            val base = locations.last()

            return Location(base.provider).apply {
                latitude = avgLat
                longitude = avgLon
                accuracy = base.accuracy
                time = System.currentTimeMillis()
            }
        }
    }
}

