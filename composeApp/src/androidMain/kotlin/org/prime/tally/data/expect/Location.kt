package org.prime.tally.data.expect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

actual suspend fun requestLocationPermission(host: Any?): Boolean = suspendCancellableCoroutine { continuation ->
    val activity = host as? Activity ?: return@suspendCancellableCoroutine

    if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        continuation.resume(true)
        return@suspendCancellableCoroutine
    }

    val requestCode = 1001
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        requestCode
    )
}

@Suppress("DEPRECATION")
actual suspend fun getCurrentLocation(host: Any?, timeoutMs: Long): LocationInfo? {
    val activity = host as? Activity ?: return null
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    if (ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    return withContext(Dispatchers.IO) {
        withTimeoutOrNull(timeoutMs) {
            val location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }

            location?.let {
                val address = getAddressFromLocation(activity, it.latitude, it.longitude)
                LocationInfo(it.latitude, it.longitude, address)
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    } else {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
}
