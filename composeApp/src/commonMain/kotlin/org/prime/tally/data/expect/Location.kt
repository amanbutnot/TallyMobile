package org.prime.tally.data.expect

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * Request runtime location permission from the user.
 *
 * - On Android: pass an Activity (android.app.Activity)
 * - On iOS: pass a UIViewController or any object expected by your iOS bridge (e.g. nil)
 *
 * Return true if permission is already granted or was granted by the user.
 */
expect suspend fun requestLocationPermission(host: Any?): Boolean

/**
 * Get the current location (latitude, longitude) and optional reverse-geocoded address.
 *
 * - host: an Android Activity or iOS UIViewController / nil depending on platform wiring.
 * - timeoutMs: maximum wait for location provider
 *
 * Returns LocationInfo or null if unable to obtain a location.
 */
expect suspend fun getCurrentLocation(host: Any?, timeoutMs: Long = 10_000L): LocationInfo?