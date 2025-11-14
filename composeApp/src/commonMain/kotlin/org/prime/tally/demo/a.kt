package org.prime.tally.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import dev.jordond.compass.Place
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitCameraType
import io.github.vinceglb.filekit.dialogs.openCameraPicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.launch
import org.prime.tally.ui.shared.composables.TallyScaffold

object Demo : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        val locator: Locator = Locator.mobile()
        val geolocator = Geolocator(locator)

        var capturedFile by remember { mutableStateOf<PlatformFile?>(null) }
        var lat by remember { mutableStateOf<String?>(null) }
        var lon by remember { mutableStateOf<String?>(null) }
        var address by remember { mutableStateOf<String?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        TallyScaffold(
            title = "Demo",
            onBack = { nav.pop() },
            showEditIcon = false,
            onEditClick = {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {

                // LOADING OVERLAY
                if (isLoading) {
                    CircularProgressIndicator()
                    return@TallyScaffold
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
                ) {

                    // IMAGE PREVIEW
                    capturedFile?.let { file ->
                        AsyncImage(
                            model = file.path,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }

                    // CAMERA BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                val file = FileKit.openCameraPicker(
                                    type = FileKitCameraType.Photo,
                                    cameraFacing = FileKitCameraFacing.Back
                                )
                                capturedFile = file
                            }
                        }
                    ) {
                        Text("Capture Image")
                    }

                    // LOCATION BUTTON
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                address = null
                                lat = null
                                lon = null

                                when (val result = geolocator.current(Priority.HighAccuracy)) {
                                    is GeolocatorResult.Success -> {
                                        val c = result.data.coordinates
                                        lat = c.latitude.toString()
                                        lon = c.longitude.toString()

                                        val place = getPlaceFromCoordinates(c.latitude, c.longitude)
                                        address =
                                            place?.let { formatAddress(it) } ?: "Address not found"
                                    }

                                    is GeolocatorResult.Error -> {
                                        error = "Turn on your location"
                                    }
                                }

                                isLoading = false
                            }
                        }
                    ) {
                        Text("Get Location + Address")
                    }

                    // OUTPUT
                    lat?.let { Text("Latitude: $it") }
                    lon?.let { Text("Longitude: $it") }
                    address?.let { Text("Address:\n$it") }
                    error?.let { Text("Error: $it") }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// HELPERS
// ---------------------------------------------------------------------

suspend fun getPlaceFromCoordinates(lat: Double, lon: Double): Place? {
    return Geocoder.mobile().placeOrNull(lat, lon)
}

fun formatAddress(a: Place): String {
    fun clean(v: String?) = v?.takeIf { it.isNotBlank() }

    val parts = listOfNotNull(
        clean(a.name),
        clean("${a.subThoroughfare.orEmpty()} ${a.thoroughfare.orEmpty()}".trim()),
        clean("${a.subLocality.orEmpty()} ${a.locality.orEmpty()}".trim()),
        clean("${a.subAdministrativeArea.orEmpty()} ${a.administrativeArea.orEmpty()}".trim()),
        clean(a.postalCode),
        clean(a.country),
        clean(a.isoCountryCode?.let { "($it)" })
    )

    return parts.joinToString("\n")
}
