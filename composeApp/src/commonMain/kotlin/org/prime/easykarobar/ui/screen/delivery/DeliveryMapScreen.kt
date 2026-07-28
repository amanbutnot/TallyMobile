//package org.prime.easykarobar.ui.screen.delivery
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
//import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Phone
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import cafe.adriel.voyager.core.screen.Screen
//import cafe.adriel.voyager.navigator.LocalNavigator
//import cafe.adriel.voyager.navigator.currentOrThrow
//import org.maplibre.compose.camera.CameraPosition
//import org.maplibre.compose.camera.rememberCameraState
//import org.maplibre.compose.expressions.dsl.const
//import org.maplibre.compose.expressions.value.LineCap
//import org.maplibre.compose.expressions.value.LineJoin
//import org.maplibre.compose.layers.CircleLayer
//import org.maplibre.compose.layers.LineLayer
//import org.maplibre.compose.map.MaplibreMap
//import org.maplibre.compose.sources.GeoJsonData
//import org.maplibre.compose.sources.rememberGeoJsonSource
//import org.maplibre.compose.style.BaseStyle
//import org.maplibre.spatialk.geojson.Position
//import org.prime.easykarobar.data.model.Order
//import org.prime.easykarobar.ui.screen.home.ROLE
//import org.prime.easykarobar.ui.screen.home.SettingScreen
//import org.prime.easykarobar.ui.screen.home.userRole
//
//// Zomato-ish palette
//private val RouteRed = Color(0xFFCB202D)
//private val RouteCasing = Color(0xFFFFFFFF)
//private val DestinationPin = Color(0xFFCB202D)
//private val OriginDot = Color(0xFF1976D2)
//
//data class DeliveryMapScreen(
//    val order: Order,
//    val userLocation: Position,
//    val destination: Position,
//    val pois: List<Position> = emptyList()
//) : Screen {
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    override fun Content() {
//        val nav = LocalNavigator.currentOrThrow
//
//        Box(modifier = Modifier.fillMaxSize()) {
//
//            // Full-bleed map — no Scaffold app bar eating vertical space
//            DeliveryMapView(userLocation, destination, pois)
//
//            // Floating top chrome
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .statusBarsPadding()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                FloatingCircleButton(onClick = { nav.pop() }) {
//                    Icon(Icons.AutoMirrored.Default.ArrowBackIos, contentDescription = "Back")
//                }
//                FloatingCircleButton(onClick = { nav.push(SettingScreen) }) {
//                    Icon(
//                        if (userRole() == ROLE.DISTRIBUTOR) Icons.Default.Person else Icons.Default.Settings,
//                        contentDescription = "Settings"
//                    )
//                }
//            }
//
//            // Bottom sheet card
//            DeliveryBottomCard(
//                order = order,
//                modifier = Modifier.align(Alignment.BottomCenter),
//                onMarkDelivered = {
//                    println("DeliveryMapScreen: Mark as Delivered clicked for Order ID: ${order.id}")
//                    nav.pop()
//                },
//                onBack = { nav.pop() }
//            )
//        }
//    }
//
//    @Composable
//    private fun FloatingCircleButton(onClick: () -> Unit, icon: @Composable () -> Unit) {
//        Surface(
//            shape = CircleShape,
//            color = MaterialTheme.colorScheme.surface,
//            shadowElevation = 4.dp,
//            modifier = Modifier.size(44.dp)
//        ) {
//            IconButton(onClick = onClick) { icon() }
//        }
//    }
//
//    @Composable
//    private fun DeliveryBottomCard(
//        order: Order,
//        modifier: Modifier = Modifier,
//        onMarkDelivered: () -> Unit,
//        onBack: () -> Unit
//    ) {
//        Card(
//            modifier = modifier
//                .fillMaxWidth()
//                .navigationBarsPadding(),
//            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
//        ) {
//            Column(modifier = Modifier.padding(20.dp)) {
//
//                // ETA pill + order id row
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        "Order #${order.id}",
//                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Destination row
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        Icons.Default.LocationOn,
//                        contentDescription = null,
//                        tint = RouteRed,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(modifier = Modifier.width(6.dp))
//                    Text(
//                        "Delivering to customer location", // TODO: bind to order.address
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    OutlinedButton(
//                        onClick = onBack,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(50)
//                    ) {
//                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
//                        Spacer(modifier = Modifier.width(6.dp))
//                        Text("Contact")
//                    }
//                    Button(
//                        onClick = onMarkDelivered,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(50),
//                        colors = ButtonDefaults.buttonColors(containerColor = RouteRed)
//                    ) {
//                        Text("Mark Delivered")
//                    }
//                }
//            }
//        }
//    }
//
//    @Composable
//    private fun DeliveryMapView(userPos: Position, destPos: Position, pois: List<Position>) {
//        val cameraState = rememberCameraState(
//            CameraPosition(
//                target = userPos,
//                zoom = 12.0
//            )
//        )
//
//        val routeGeoJson = """
//            {
//              "type":"FeatureCollection",
//              "features":[
//                {
//                  "type":"Feature",
//                  "properties":{},
//                  "geometry":{
//                    "type":"LineString",
//                    "coordinates":[
//                      [${userPos.longitude}, ${userPos.latitude}],
//                      [${destPos.longitude}, ${destPos.latitude}]
//                    ]
//                  }
//                }
//              ]
//            }
//        """.trimIndent()
//
//        val originGeoJson = """
//            {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[${userPos.longitude}, ${userPos.latitude}]}}
//        """.trimIndent()
//
//        val destGeoJson = """
//            {"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[${destPos.longitude}, ${destPos.latitude}]}}
//        """.trimIndent()
//
//        val poisGeoJson = """
//            {
//              "type":"FeatureCollection",
//              "features":[
//                ${pois.joinToString(",") {
//                    """{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[${it.longitude}, ${it.latitude}]}}"""
//                }}
//              ]
//            }
//        """.trimIndent()
//
//        MaplibreMap(
//            cameraState = cameraState,
//            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/positron")
//        ) {
//            val routeSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(routeGeoJson))
//            val originSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(originGeoJson))
//            val destSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(destGeoJson))
//            val poisSource = rememberGeoJsonSource(data = GeoJsonData.JsonString(poisGeoJson))
//
//            // Route casing and line
//            LineLayer(
//                id = "route-casing",
//                source = routeSource,
//                color = const(RouteCasing),
//                width = const(9.dp),
//                cap = const(LineCap.Round),
//                join = const(LineJoin.Round)
//            )
//            LineLayer(
//                id = "route-line",
//                source = routeSource,
//                color = const(RouteRed),
//                width = const(5.dp),
//                cap = const(LineCap.Round),
//                join = const(LineJoin.Round)
//            )
//
//            // Random locations (POIs) from HeiGIT
//            CircleLayer(
//                id = "pois-dots",
//                source = poisSource,
//                radius = const(6.dp),
//                color = const(Color(0xFF4CAF50)), // Green for POIs
//                strokeColor = const(Color.White),
//                strokeWidth = const(1.5.dp)
//            )
//
//            // Origin marker: small blue dot with white halo (rider/current location)
//            CircleLayer(
//                id = "origin-halo",
//                source = originSource,
//                radius = const(12.dp),
//                color = const(Color.White)
//            )
//            CircleLayer(
//                id = "origin-dot",
//                source = originSource,
//                radius = const(7.dp),
//                color = const(OriginDot),
//                strokeColor = const(Color.White),
//                strokeWidth = const(2.dp)
//            )
//
//            // Destination marker: larger red pin-dot with white center
//            CircleLayer(
//                id = "dest-halo",
//                source = destSource,
//                radius = const(14.dp),
//                color = const(DestinationPin).let { const(DestinationPin.copy(alpha = 0.25f)) }
//            )
//            CircleLayer(
//                id = "dest-dot",
//                source = destSource,
//                radius = const(8.dp),
//                color = const(DestinationPin),
//                strokeColor = const(Color.White),
//                strokeWidth = const(3.dp)
//            )
//        }
//    }
//}