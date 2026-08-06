package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.model.ORDERSTATUS
import org.prime.easykarobar.data.model.UpdateOrderStatusRequest
import org.prime.easykarobar.data.model.transactions.InventoryListRequest
import org.prime.easykarobar.data.model.transactions.InventoryListResponse
import org.prime.easykarobar.ui.screen.transactions.sale.SaleScreen
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getNameFromGUID

data class InventoryListScreen(
    val startDate: String,
    val endDate: String,
    val vchType: Int,
    val name: String,
    val showStatusChange: Boolean = false
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: InventoryVoucherViewModel = viewModel { InventoryVoucherViewModel() }
        val orderViewModel: OrderViewModel = viewModel { OrderViewModel() }
        val nav = LocalNavigator.currentOrThrow
        val state by viewModel.listState
        val updateState by orderViewModel.updateStatusState

        var showStatusSheet by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<InventoryListResponse?>(null) }
        var selectedStatus by remember { mutableStateOf<ORDERSTATUS?>(null) }
        val sheetState = rememberModalBottomSheetState()


        LaunchedEffect(Unit) {
            viewModel.listInventoryVch(
                InventoryListRequest(
                    VchType = vchType,
                    StartDate = startDate,
                    EndDate = endDate
                )
            )
        }

        if (updateState.success) {
            TallyResultDialog(
                message = updateState.message ?: "Status updated successfully",
                onDone = {
                    orderViewModel.clearUpdateStatusState()
                    viewModel.listInventoryVch(
                        InventoryListRequest(
                            VchType = vchType,
                            StartDate = startDate,
                            EndDate = endDate
                        )
                    )
                },
                isSuccess = true
            )
        }

        TallyScaffold(
            title = "$name List",
            showBottomBar = false,
            bottomBarContent = {},
            content = { paddingValues ->

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            TallyCircularLoader()
                        }
                    }

                    state.error != null -> {
                        EmptyListPlaceholder(
                            icon = Icons.Default.Inbox,
                            title = state.error.toString(),
                            onAddClick = {
                                nav.push(
                                    SaleScreen(
                                        name = name,
                                        vchType = vchType,
                                        isEdit = false
                                    )
                                )
                            },
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            state.data?.let {
                                itemsIndexed(it) { index, item ->
                                    ListItem(
                                        listState = item,
                                        showStatusChange = showStatusChange,
                                        onStatusChangeClick = {
                                            selectedItem = item
                                            showStatusSheet = true
                                        }
                                    ) {
                                        nav.push(
                                            SaleScreen(
                                                name = name,
                                                vchType = vchType,
                                                tranId = item.id,
                                                isEdit = true,
                                                enableUpdateButton = item.OrderStatus == ORDERSTATUS.Pending.name
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showStatusSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showStatusSheet = false },
                        sheetState = sheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Select Status",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ORDERSTATUS.entries.take(6).forEach { status ->
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    onClick = {
                                        selectedStatus = status
                                        showStatusSheet = false
                                        showConfirmDialog = true
                                    }
                                ) {
                                    Text(status.displayName())
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                if (showConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text("Confirm Status Change") },
                        text = {
                            Text("Are you sure you want to change the status of ${selectedItem?.order_no} to ${selectedStatus?.displayName()}?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    selectedItem?.let { item ->
                                        selectedStatus?.let { status ->
                                            orderViewModel.updateOrderStatus(
                                                UpdateOrderStatusRequest(
                                                    order_id = item.id,
                                                    status = status.name,
                                                    remarks = "Status updated from Inventory List"
                                                )
                                            )
                                        }
                                    }
                                    showConfirmDialog = false
                                }
                            ) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            })
    }
}


@Composable
private fun ListItem(
    listState: InventoryListResponse,
    showStatusChange: Boolean = false,
    onStatusChangeClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (listState.OrderStatus == ORDERSTATUS.Pending.name)
                Color(0xffFBC02D).copy(alpha = 0.04f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voucher Number
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Voucher",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = listState.order_no,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Tdate(listState.created_at.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Billing Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Billing Name: ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getNameFromGUID(listState.billing_guid),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = listState.OrderStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = listState.total_amount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showStatusChange &&
                listState.OrderStatus != ORDERSTATUS.Delivered.name &&
                listState.OrderStatus != ORDERSTATUS.Cancelled.name
            ) {
                OutlinedButton(
                    onClick = onStatusChangeClick,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Change Status")
                }
            }

        }
    }
}
