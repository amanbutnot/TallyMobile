package org.prime.easykarobar.ui.screen.distributor.order

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.distributor.CartViewModel
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.model.CancelOrderRequest
import org.prime.easykarobar.data.model.ORDERSTATUS
import org.prime.easykarobar.data.model.Order
import org.prime.easykarobar.data.model.StatusHistory
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.OrderCard
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.tally.GetProductsForDis

data class MyOrdersScreen(
    val order_id: String? = null,
    val isStatusChangeMode: Boolean = false
) : Screen {
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewModel: OrderViewModel = viewModel { OrderViewModel() }
        val listState by viewModel.listOrderState


        LaunchedEffect(Unit) {
            viewModel.listOrders(order_id)
        }

        if (listState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TallyCircularLoader()
            }
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
                } else MyOrderContent(
                    list = it,
                    paddingValues = paddingValues,
                    viewModel = viewModel,
                    order_id = order_id,
                    isStatusChangeMode = isStatusChangeMode
                )
            }
        }

    }
}


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)
@Composable
fun MyOrderContent(
    list: List<Order>,
    paddingValues: PaddingValues,
    viewModel: OrderViewModel,
    order_id: String? = null,
    isStatusChangeMode: Boolean = false
) {

    val nav = LocalNavigator.currentOrThrow
    val cartViewModel = nav.rememberNavigatorScreenModel { CartViewModel() }

    val cancelState by viewModel.cancelOrderState
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancellationReason by remember { mutableStateOf("") }
    var reloadData by remember { mutableStateOf(false) }
    var showEmptyMessage by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf("") }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val options = listOf(
        "All", "Cancelled"
    )
    val unCheckedIcons = listOf(
        Icons.Default.AllInbox,
        Icons.Default.Cancel,
    )
    val checkedIcons = listOf(
        Icons.Default.AllInbox,
        Icons.Default.Cancel,
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var showOrderHistory by remember { mutableStateOf(false) }
    var selectedStatusHistory by remember { mutableStateOf<List<StatusHistory>?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).navigationBarsPadding()
    ) {
        if (!isStatusChangeMode) {
            androidx.compose.foundation.layout.FlowRow(
                Modifier
                    .fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.Center,
                maxItemsInEachRow = 1
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
        }


        val filteredList = when (selectedIndex) {
            0 -> {
                list
            }

            1 -> {

                list.filter { it.OrderStatus == ORDERSTATUS.Cancelled }
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
                    orderNo = order.order_no,
                    orderStatus = order.OrderStatus.displayName(),
                    remarks = order.Remarks,
                    orderDate = order.created_at,
                    billingName = order.billing_name,
                    totalAmount = order.total_amount,
                    items = order.items,
                    sundries = order.sundries,
                    cancellationDate = order.cancellation_date,
                    cancelledBy = order.cancelled_by,
                    cancellationRemarks = order.cancellation_remarks,
                    onHistoryClick = {
                        selectedStatusHistory = order.status_history
                        showOrderHistory = true
                    },
                    onCancelOrder = if (isStatusChangeMode) null else {
                        {
                            selectedId = order.id.toString()
                            showCancelDialog = true
                        }
                    },
                    onRepeatOrder = if (isStatusChangeMode) null else {
                        {
                            cartViewModel.emptyList()
                            val ids = order.items.map { it.product_id }
                            val products = DatabaseHolder.instance.productsQueries
                                .getProductsByGuidsForDis(
                                    guids = ids,
                                    changePrice = SharedPrefs.ChangePrice.get()
                                ) { product_id, hospital_id, product_name, category_id, unit_id, sales_price, MRP, purchase_price, discount, gst_tax_percentage, product_description, created_at, updated_at, discounted_price, main_unit, alt_unit, con_factor, con_type ->
                                    GetProductsForDis(
                                        product_id,
                                        hospital_id,
                                        product_name,
                                        category_id,
                                        unit_id,
                                        sales_price,
                                        MRP,
                                        purchase_price,
                                        discount,
                                        gst_tax_percentage,
                                        product_description,
                                        created_at,
                                        updated_at,
                                        discounted_price,
                                        main_unit,
                                        alt_unit,
                                        con_factor,
                                        con_type
                                    )
                                }.executeAsList()

                            products.forEach { product ->
                                val orderItem = order.items.find { it.product_id == product.product_id }
                                val quantity = orderItem?.quantity ?: 1
                                cartViewModel.updateQuantity(product, quantity.toDouble())
                            }
                            nav.push(CartScreen)
                        }
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
                showCancelDialog = false

            }, isSuccess = cancelState.success, confirmText = "Ok"
        )
    }

    if (reloadData) {
        LaunchedEffect(Unit) {
            viewModel.listOrders(order_id)
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
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            ?.replace("_", " ")
                            ?.lowercase()
                            ?.split(" ")
                            ?.joinToString(" ") { word ->
                                word.replaceFirstChar { it.uppercase() }
                            } ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = historyItem.created_at.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (historyItem.remarks.toString().isNotEmpty()) {
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
