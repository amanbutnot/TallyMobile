//package org.prime.easykarobar.ui.screen.delivery
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
//import androidx.compose.material.icons.filled.LocalShipping
//import androidx.compose.material.icons.filled.Map
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material.icons.filled.ShoppingCart
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import cafe.adriel.voyager.core.screen.Screen
//import cafe.adriel.voyager.navigator.LocalNavigator
//import cafe.adriel.voyager.navigator.currentOrThrow
//import dev.jordond.compass.Priority
//import dev.jordond.compass.geolocation.Geolocator
//import dev.jordond.compass.geolocation.GeolocatorResult
//import dev.jordond.compass.geolocation.Locator
//import dev.jordond.compass.geolocation.mobile.mobile
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withTimeoutOrNull
//import org.maplibre.spatialk.geojson.Position
//import org.prime.easykarobar.business.repository.HeiGitRepository
//import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
//import org.prime.easykarobar.data.model.ORDERSTATUS
//import org.prime.easykarobar.ui.screen.distributor.order.OrderCard
//import org.prime.easykarobar.ui.screen.home.ROLE
//import org.prime.easykarobar.ui.screen.home.SettingScreen
//import org.prime.easykarobar.ui.screen.home.userRole
//import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
//import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
//import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
//import org.prime.easykarobar.ui.shared.globalShared.CompanyName
//import kotlin.random.Random
//
//object DeliveryScreen : Screen {
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    override fun Content() {
//        val nav = LocalNavigator.currentOrThrow
//        val viewModel: OrderViewModel = viewModel { OrderViewModel() }
//        var showLoading by remember {mutableStateOf(false)}
//        val listState by viewModel.listOrderState
//        val updateState by viewModel.updateStatusState
//        val colors = MaterialTheme.colorScheme
//        val scope = rememberCoroutineScope()
//        var showLocationPopup by remember { mutableStateOf(false) }
//
//
//        LaunchedEffect(Unit) {
//            viewModel.listOrders()
//        }
//
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = {
//                        Text(
//                            CompanyName(),
//                            color = colors.onBackground,
//                            style = MaterialTheme.typography.titleLarge.copy(
//                                fontWeight = FontWeight.Medium
//                            ),
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = { nav.pop() }) {
//                            Icon(
//                                imageVector = Icons.AutoMirrored.Default.ArrowBackIos,
//                                contentDescription = "Back",
//                                tint = colors.onBackground
//                            )
//                        }
//                    },
//                    actions = {
//                        IconButton(onClick = { nav.push(SettingScreen) }) {
//                            Icon(
//                                if (userRole() == ROLE.DISTRIBUTOR) Icons.Default.Person else Icons.Default.Settings,
//                                contentDescription = "Settings icon",
//                                tint = colors.onBackground
//                            )
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = colors.primary.copy(alpha = 0.2f)
//                    )
//                )
//            },
//            content = { paddingValues ->
//                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
//                    Column(modifier = Modifier.fillMaxSize()) {
//                        if (listState.isLoading) {
//                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                                TallyLoadingDialog("Loading Orders...")
//                            }
//                        }
//
//                        listState.data?.let { orders ->
//                            if (orders.isEmpty()) {
//                                EmptyListPlaceholder(
//                                    icon = Icons.Default.ShoppingCart,
//                                    title = "No Orders for Delivery",
//                                    onAddClick = { }
//                                )
//                            } else {
//                                LazyColumn(
//                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
//                                    verticalArrangement = Arrangement.spacedBy(12.dp),
//                                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
//                                ) {
//                                    items(orders.filter { it.OrderStatus != ORDERSTATUS.Delivered && it.OrderStatus != ORDERSTATUS.Cancelled }) { order ->
//                                        Column {
//                                            OrderCard(
//                                                order = order,
//                                                onHistoryClick = { },
//                                                onCancelOrder = { }
//                                            )
//                                            Spacer(modifier = Modifier.height(8.dp))
//                                            Button(
//                                                onClick = {
//                                                    // Use random locations as requested
//
//
//                                                    scope.launch {
//                                                        showLoading = true
//                                                        try {
//                                                            val geolocator =
//                                                                Geolocator(Locator.mobile())
//
//                                                            runCatching { geolocator.lastLocation() }
//
//                                                            val result = withTimeoutOrNull(20000) {
//                                                                geolocator.current(Priority.HighAccuracy)
//                                                            }
//
//                                                            when (result) {
//                                                                is GeolocatorResult.Success -> {
//                                                                    val c = result.data.coordinates
//                                                                    val userPos = Position(longitude = c.longitude, latitude = c.latitude)
//                                                                    val destPos = generateRandomDestination(userPos)
//
//                                                                    // Fetch random locations from HeiGIT
//                                                                    val poiResponse = HeiGitRepository.fetchNearbyPois(c.longitude, c.latitude)
//                                                                    val randomPois = poiResponse?.features?.map {
//                                                                        Position(it.geometry.coordinates[0], it.geometry.coordinates[1])
//                                                                    } ?: emptyList()
//
//                                                                    println("DeliveryScreen: HeiGIT POIs fetched. Navigating to Map.")
//                                                                    nav.push(DeliveryMapScreen(order, userPos, destPos, randomPois))
//                                                                }
//
//                                                                else -> showLocationPopup = true
//                                                            }
//
//                                                        } catch (e: Exception) {
//                                                            showLocationPopup = true
//                                                        } finally {
//                                                            showLoading = false
//                                                        }
//                                                    }
//                                                },
//                                                modifier = Modifier.fillMaxWidth(),
//                                                shape = RoundedCornerShape(12.dp),
//                                                colors = ButtonDefaults.buttonColors(
//                                                    containerColor = if (order.OrderStatus == ORDERSTATUS.InDispatched)
//                                                        MaterialTheme.colorScheme.secondary
//                                                    else MaterialTheme.colorScheme.primary
//                                                )
//                                            ) {
//                                                Icon(
//                                                    imageVector = if (order.OrderStatus == ORDERSTATUS.InDispatched)
//                                                        Icons.Default.Map else Icons.Default.LocalShipping,
//                                                    contentDescription = null
//                                                )
//                                                Spacer(modifier = Modifier.width(8.dp))
//                                                Text(if (order.OrderStatus == ORDERSTATUS.InDispatched) "Continue Delivery" else "Start Delivery")
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        )
//        if (showLocationPopup) {
//            AlertDialog(
//                onDismissRequest = { showLocationPopup = false },
//                title = { Text("Location is Off") },
//                text = { Text("Please enable location to continue.") },
//                confirmButton = {
//                    Button(onClick = { showLocationPopup = false }) {
//                        Text("OK")
//                    }
//                })
//        }
//
//        if (showLoading) {
//            TallyLoadingDialog("Getting Location")
//        }
//        if (updateState.message != null) {
//            TallyResultDialog(
//                message = updateState.message ?: "",
//                onDone = {
//                    viewModel.listOrders()
//                },
//                isSuccess = updateState.success
//            )
//        }
//    }
//
//    private fun generateRandomDestination(current: Position): Position {
//        val random = Random.Default
//        // ~5km radius roughly
//        val latOffset = (random.nextDouble() - 0.5) * 0.04
//        val lonOffset = (random.nextDouble() - 0.5) * 0.04
//        return Position(
//            longitude = current.longitude + lonOffset,
//            latitude = current.latitude + latOffset
//        )
//    }
//}
