package org.prime.tally.data.expect

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLAuthorizationStatus
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
                val address = placemarks?.firstOrNull()?.let { it.name }
                continuation.resume(LocationInfo(location.coordinate.latitude, location.coordinate.longitude, address))
            }
        } else {
            continuation.resume(null)
        }
    }
}
