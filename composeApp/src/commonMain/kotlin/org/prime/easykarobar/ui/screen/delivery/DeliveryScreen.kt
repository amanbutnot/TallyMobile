package org.prime.easykarobar.ui.screen.delivery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
import org.maplibre.spatialk.geojson.Position
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.model.ORDERSTATUS
import org.prime.easykarobar.data.model.Order
import org.prime.easykarobar.data.model.UpdateOrderStatusRequest
import org.prime.easykarobar.ui.screen.distributor.order.OrderCard
import org.prime.easykarobar.ui.shared.composables.*
import kotlin.random.Random

object DeliveryScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewModel: OrderViewModel = viewModel { OrderViewModel() }
        val listState by viewModel.listOrderState
        val updateState by viewModel.updateStatusState
        val scope = rememberCoroutineScope()

        var currentWorkingOrder by remember { mutableStateOf<Order?>(null) }
        var userLocation by remember { mutableStateOf<Position?>(null) }
        var destination by remember { mutableStateOf<Position?>(null) }
        var showMap by remember { mutableStateOf(false) }
        var isLoadingLocation by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.listOrders()
        }

        TallyScaffold(
            title = if (showMap) "Delivery Route" else "Delivery Orders",
            onBack = {
                if (showMap) showMap = false else nav.pop()
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (showMap && userLocation != null && destination != null) {
                    DeliveryMapView(userLocation!!, destination!!)
                    
                    // Overlay buttons on map
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    currentWorkingOrder?.let { order ->
                                        viewModel.updateOrderStatus(
                                            UpdateOrderStatusRequest(
                                                order_id = order.id,
                                                status = ORDERSTATUS.Delivered.name,
                                                remarks = "Delivered by driver"
                                            )
                                        )
                                    }
                                    showMap = false
                                    currentWorkingOrder = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mark as Delivered")
                        }
                        
                        Button(
                            onClick = { showMap = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Back to List")
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (listState.isLoading || isLoadingLocation) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                TallyCircularLoader()
                            }
                        }

                        listState.data?.let { orders ->
                            if (orders.isEmpty()) {
                                EmptyListPlaceholder(
                                    icon = Icons.Default.ShoppingCart,
                                    title = "No Orders for Delivery",
                                    onAddClick = { }
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(orders.filter { it.OrderStatus != ORDERSTATUS.Delivered && it.OrderStatus != ORDERSTATUS.Cancelled }) { order ->
                                        Column {
                                            OrderCard(
                                                order = order,
                                                onHistoryClick = { },
                                                onCancelOrder = { }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isLoadingLocation = true
                                                        try {
                                                            val geolocator = Geolocator(Locator.mobile())
                                                            val result = withTimeoutOrNull(10000) {
                                                                geolocator.current(Priority.HighAccuracy)
                                                            }
                                                            
                                                            if (result is GeolocatorResult.Success) {
                                                                val pos = Position(
                                                                    longitude = result.data.coordinates.longitude,
                                                                    latitude = result.data.coordinates.latitude
                                                                )
                                                                userLocation = pos
                                                                destination = generateRandomDestination(pos)
                                                                
                                                                currentWorkingOrder = order
                                                                viewModel.updateOrderStatus(
                                                                    UpdateOrderStatusRequest(
                                                                        order_id = order.id,
                                                                        status = ORDERSTATUS.InDispatched.name,
                                                                        remarks = "Starting delivery"
                                                                    )
                                                                )
                                                                showMap = true
                                                            } else {
                                                                // Handle location failure
                                                            }
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        } finally {
                                                            isLoadingLocation = false
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (order.OrderStatus == ORDERSTATUS.InDispatched) 
                                                        MaterialTheme.colorScheme.secondary 
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (order.OrderStatus == ORDERSTATUS.InDispatched) 
                                                        Icons.Default.Map else Icons.Default.LocalShipping,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (order.OrderStatus == ORDERSTATUS.InDispatched) "Continue Delivery" else "Start Delivery")
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (updateState.message != null) {
            TallyResultDialog(
                message = updateState.message ?: "",
                onDone = { 
                    viewModel.listOrders()
                },
                isSuccess = updateState.success
            )
        }
    }

    private fun generateRandomDestination(current: Position): Position {
        val random = Random.Default
        // ~5km radius roughly
        val latOffset = (random.nextDouble() - 0.5) * 0.04
        val lonOffset = (random.nextDouble() - 0.5) * 0.04
        return Position(
            longitude = current.longitude + lonOffset,
            latitude = current.latitude + latOffset
        )
    }

    @Composable
    fun DeliveryMapView(userPos: Position, destPos: Position) {
        val cameraState = rememberCameraState(
            CameraPosition(
                target = userPos,
                zoom = 12.0
            )
        )

        MaplibreMap(
            cameraState = cameraState,
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty")
        ) {
            val routeSource = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(
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
                              [${userPos.longitude}, ${userPos.latitude}],
                              [${destPos.longitude}, ${destPos.latitude}]
                            ]
                          }
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )

            LineLayer(
                id = "route-line",
                source = routeSource,
                color = const(Color(0xFF2196F3)),
                width = const(5.dp),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round)
            )
        }
    }
}
