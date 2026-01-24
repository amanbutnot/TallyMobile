package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.model.CancelOrderRequest
import org.prime.easykarobar.data.model.Order
import org.prime.easykarobar.data.model.OrderItem
import org.prime.easykarobar.data.model.OrderStatus
import org.prime.easykarobar.data.model.StatusHistory
import org.prime.easykarobar.data.utils.BASE_URL
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField

object MyOrdersScreen : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewModel: OrderViewModel = viewModel { OrderViewModel() }
        val listState by viewModel.listOrderState


        LaunchedEffect(Unit) {
            viewModel.listOrders()
        }

        TallyScaffold(
            title = "My Orders",
            onBack = { nav.pop() },
            showEditIcon = false,
            onEditClick = {},
        ) { paddingValues ->

            listState.data?.let {
                if (it.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        EmptyListPlaceholder(
                            icon = Icons.Default.ShoppingCart,
                            title = "No Orders",
                            onAddClick = { })
                    }
                } else if (listState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else MyOrderContent(
                    list = it, paddingValues = paddingValues, viewModel
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MyOrderContent(
    list: List<Order>, paddingValues: PaddingValues, viewModel: OrderViewModel
) {

    val cancelState by viewModel.cancelOrderState
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }
    var reloadData by remember { mutableStateOf(false) }
    var showEmptyMessage by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf("") }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val options = listOf("All", "Cancelled", "Is Dispatched", "Delivered")
    val unCheckedIcons = listOf(
        Icons.Default.AllInbox,
        Icons.Default.Cancel,
        Icons.Default.DoneAll,
        Icons.Default.DoneAll,
        Icons.Default.DoneAll
    )
    val checkedIcons = listOf(
        Icons.Default.AllInbox,
        Icons.Default.Cancel,
        Icons.Default.DoneAll,
        Icons.Default.DoneAll,
        Icons.Default.DoneAll
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var showOrderHistory by remember { mutableStateOf(false) }
    var selectedStatusHistory by remember { mutableStateOf<List<StatusHistory>?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues)
    ) {
        FlowRow(
            Modifier
                .fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.Center,
            maxLines = 1
        )
        {
            options.forEachIndexed { index, string ->
                ToggleButton(
                    checked = selectedIndex == index,
                    onCheckedChange = { selectedIndex = index },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    modifier = Modifier.semantics { role = Role.RadioButton },
                ) {
                    Icon(
                        if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                        contentDescription = "Filter Icons",
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(string)
                }
            }
        }


        val filteredList = when (selectedIndex) {
            0 -> {
                list
            }

            1 -> {

                list.filter { it.order_status == OrderStatus.Cancelled }
            }

            2 -> {
                list.filter { it.order_status == OrderStatus.InDispatched }
            }

            3 -> {
                list.filter { it.order_status == OrderStatus.Delivered }
            }


            else -> {
                list.filter { it.cancellation_date == null }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = { reloadData = true }
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { order ->
                OrderCard(
                    onHistoryClick = {
                        selectedStatusHistory = order.status_history
                        showOrderHistory = true
                    },
                    order = order,
                    onCancelOrder = {
                        selectedId = order.ID.toString()
                        showCancelDialog = true
                    }
                )
            }
        }
    }

    if (showOrderHistory) {
        selectedStatusHistory?.let { history ->
            OrderHistoryBottomSheet(
                statusHistory = history,
                onDismiss = { showOrderHistory = false }
            )
        }
    }



    if (showCancelDialog) {
        TallyAlertBox(
            title = "Cancel your order",
            message = "Are you sure you want to cancel your order??",
            confirmButtonText = "Yes",
            cancelButtonText = "No",
            onConfirm = {
                if (cancellationReason.isEmpty()) {
                    showEmptyMessage = true
                } else {
                    viewModel.cancelOrder(
                        CancelOrderRequest(
                            order_id = selectedId.toInt(), cancellation_remarks = cancellationReason
                        )
                    )
                }
            },
            onCancel = { showCancelDialog = false },
            onDismiss = { showCancelDialog = false },
            content = {
                Column {
                    TallyTextField(
                        value = cancellationReason,
                        onValueChange = { cancellationReason = it },
                        placeholder = "Cancellation Reason",
                        isPassword = false,
                        isNumber = false,
                        label = "Reason "
                    )
                    if (showEmptyMessage) {
                        Text(
                            "Cancellation Reason cannot be empty",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                }

            })
    }
    if (cancelState.message != null) {
        TallyResultDialog(
            message = cancelState.message ?: "Error Occurred", onDone = {
                reloadData = true
                viewModel.clearCancelMessage()

            }, isSuccess = cancelState.success, confirmText = "Ok"
        )
    }

    if (reloadData) {
        LaunchedEffect(Unit) {
            viewModel.listOrders()
            reloadData = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderHistoryBottomSheet(
    statusHistory: List<StatusHistory>,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Order History",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            StatusHistoryCard(historyItem = statusHistory)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusHistoryCard(historyItem: List<StatusHistory>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            items(historyItem) { historyItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = historyItem.status
                            .replace("_", " ")
                            .lowercase()
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { it.uppercase() }
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = historyItem.created_at,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (historyItem.remarks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Remarks: ${historyItem.remarks}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

            }
        }
    }
}


@Composable
fun OrderCard(
    order: Order, onCancelOrder: () -> Unit, onHistoryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    )
                    {
                        Text(
                            text = "Order Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp, fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Order #${order.OrderID}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${order.order_status}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    OrderField(
                        icon = Icons.Default.Edit, label = "Remarks", value = order.remarks
                    )
                    OrderField(
                        icon = Icons.Default.CalendarMonth,
                        label = "Order Date",
                        value = order.order_date
                    )
                    OrderField(
                        icon = Icons.Default.Person,
                        label = "Billing Name",
                        value = order.billing_name
                    )
                    OrderField(
                        icon = Icons.Default.Home,
                        label = "Address",
                        value = order.billing_address
                    )
                    OrderField(
                        icon = Icons.Default.Phone,
                        label = "Mobile",
                        value = order.billing_mobile
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order Items", style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                    ), color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${order.items.size} items",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    order.items.forEachIndexed { index, item ->
                        OrderItemRow(
                            item = item, serialNumber = index + 1
                        )
                        if (index != order.items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(
                    onClick = onHistoryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "History",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "History",
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }



                if (order.cancellation_date == null) {
                    Button(
                        onClick = onCancelOrder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel Order",
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        border = BorderStroke(
                            1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancelled",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Order Cancelled",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                            ) {
                                CancellationField(
                                    label = "Cancelled on",
                                    value = order.cancellation_date,
                                    icon = Icons.Default.CalendarMonth
                                )

                                if (order.cancelled_by.isNotEmpty()) {
                                    CancellationField(
                                        label = "Cancelled by",
                                        value = order.cancelled_by,
                                        icon = Icons.Default.Person
                                    )
                                }

                                if (order.cancellation_remarks.isNotEmpty()) {
                                    CancellationField(
                                        label = "Reason",
                                        value = order.cancellation_remarks,
                                        icon = Icons.Default.Info
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }


    }
}

@Composable
fun OrderField(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                ), color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItem, serialNumber: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = serialNumber.toString(), style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    ), color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp).border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
        ) {
            AsyncImage(
                model = "$BASE_URL${item.profile_picture}",
                contentDescription = "Item Image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "ID: ${item.item_id}",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ItemDetailChip(
                    label = "Qty",
                    value = item.quantity,
                    color = MaterialTheme.colorScheme.secondary
                )

                ItemDetailChip(
                    label = "Amount",
                    value = "₹${item.net_amount}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            ItemDetailChip(
                label = "Packing",
                value = "₹${item.packing_charges}",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ItemDetailChip(
    label: String, value: String, color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
            Text(
                text = value, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = color
            )
        }
    }
}

@Composable
fun CancellationField(
    label: String, value: String, icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = label, style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ), color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}