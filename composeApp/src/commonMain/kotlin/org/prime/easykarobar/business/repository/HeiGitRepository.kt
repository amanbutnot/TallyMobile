package org.prime.easykarobar.business.repository

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.utils.KtorClient

@Serializable
data class PoiRequest(
    val request: String = "pois",
    val geometry: GeometryRequest,
    val limit: Int = 10
)

@Serializable
data class GeometryRequest(
    val geojson: GeoJsonPoint,
    val buffer: Int
)

@Serializable
data class GeoJsonPoint(
    val type: String = "Point",
    val coordinates: List<Double>
)

@Serializable
data class HeiGitPoiResponse(
    val type: String? = null,
    val features: List<PoiFeature> = emptyList()
)

@Serializable
data class PoiFeature(
    val type: String,
    val geometry: PoiGeometry,
    val properties: PoiProperties? = null
)

@Serializable
data class PoiGeometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class PoiProperties(
    val osm_tags: OsmTags? = null
)

@Serializable
data class OsmTags(
    val name: String? = null
)

object HeiGitRepository {
    private const val API_KEY = "11c9b9972a894ae8bd0bbc1976a7c49e"
    private const val BASE_URL = "https://api.heigit.org/openpoiservice/v0/pois"

    suspend fun fetchNearbyPois(lon: Double, lat: Double): HeiGitPoiResponse? {
        return try {
            val res = KtorClient.client.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                header("Authorization", API_KEY)
                setBody(PoiRequest(
                    geometry = GeometryRequest(
                        geojson = GeoJsonPoint(coordinates = listOf(lon, lat)),
                        buffer = 5000 // 5km buffer for more "random" points
                    )
                ))
            }
            res.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
