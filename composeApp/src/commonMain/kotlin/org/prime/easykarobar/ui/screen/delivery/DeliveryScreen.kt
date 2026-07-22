package org.prime.easykarobar.ui.screen.delivery

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position

object DeliveryScreen : Screen {

    @Composable
    override fun Content() {

        val cameraState = rememberCameraState(
            CameraPosition(
                target = Position(
                    longitude = 76.8188,
                    latitude = 30.3655
                ),
                zoom = 11.0
            )
        )

        LaunchedEffect(Unit) {
            cameraState.jumpTo(
                boundingBox = BoundingBox(
                    west = 76.760955,
                    south = 30.334462,
                    east = 76.876642,
                    north = 30.396556
                ),
                padding = PaddingValues(48.dp)
            )
        }

        MaplibreMap(
            cameraState = cameraState,
            baseStyle = BaseStyle.Uri(
                "https://tiles.openfreemap.org/styles/liberty"
            )
        ) {

            val routeSource = rememberGeoJsonSource(
                GeoJsonData.JsonString(
                    """
                    {
                      "type":"FeatureCollection",
                      "features":[
                        {
                          "type":"Feature",
                          "properties":{},
                          "geometry":{
                            "type":"LineString",
                            "coordinates":[
                              [76.760955,30.396556],
                              [76.762859,30.395960],
                              [76.763773,30.395683],
                              [76.763918,30.395650],
                              [76.763910,30.395560],
                              [76.764397,30.395399],
                              [76.768109,30.394271],
                              [76.771211,30.393515],
                              [76.775075,30.392813],
                              [76.777009,30.391801],
                              [76.779879,30.389685],
                              [76.784523,30.386264],
                              [76.788477,30.383338],
                              [76.793700,30.377972],
                              [76.798712,30.368245],
                              [76.802988,30.356014],
                              [76.807224,30.352493],
                              [76.816208,30.347144],
                              [76.821816,30.343844],
                              [76.825771,30.341615],
                              [76.828165,30.340090],
                              [76.830562,30.338657],
                              [76.831046,30.339257],
                              [76.833681,30.339268],
                              [76.838296,30.339491],
                              [76.840432,30.338593],
                              [76.842158,30.340711],
                              [76.843944,30.342917],
                              [76.845336,30.342153],
                              [76.850666,30.340533],
                              [76.856279,30.338818],
                              [76.860138,30.338036],
                              [76.865866,30.336869],
                              [76.871211,30.335758],
                              [76.874548,30.335007],
                              [76.876439,30.334491],
                              [76.876642,30.334666],
                              [76.876506,30.336039]
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )

            LineLayer(
                id = "route",
                source = routeSource,
                color = const(Color(0xFF2979FF)),
                width = const(6.dp),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
        }
    }
}