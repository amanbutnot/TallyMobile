package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusDenied
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun requestLocationPermission(host: Any?): Boolean {
    val locationManager = CLLocationManager()
    return when (locationManager.authorizationStatus()) {
        kCLAuthorizationStatusAuthorizedWhenInUse -> true
        kCLAuthorizationStatusNotDetermined -> {
            suspendCancellableCoroutine { continuation ->
                locationManager.requestWhenInUseAuthorization()
                continuation.resume(true) // Should ideally wait for user action
            }
        }
        kCLAuthorizationStatusDenied -> false
        else -> false
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getCurrentLocation(host: Any?, timeoutMs: Long): LocationInfo? {
    val locationManager = CLLocationManager()
    if (locationManager.authorizationStatus() != kCLAuthorizationStatusAuthorizedWhenInUse) {
        return null
    }

    return suspendCancellableCoroutine { continuation ->
        val location = locationManager.location
        if (location != null) {
            val geoCoder = CLGeocoder()
            geoCoder.reverseGeocodeLocation(location) { placemarks, error ->
                val placemark = placemarks?.firstOrNull()
                // Get name, latitude, longitude
                val address = placemark?.toString() // Address or place name
                val latitude = location.coordinate.align.toDouble()
                val longitude = location.coordinate.align.toDouble()

                continuation.resume(LocationInfo(latitude, longitude, address))
            }
        } else {
            continuation.resume(null)
        }
    }
}