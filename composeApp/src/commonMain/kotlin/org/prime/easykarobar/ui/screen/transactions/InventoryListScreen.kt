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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.distributor.OrderViewModel
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.ORDERSTATUS
import org.prime.easykarobar.data.model.UpdateOrderStatusRequest
import org.prime.easykarobar.data.model.transactions.InventoryListRequest
import org.prime.easykarobar.data.model.transactions.InventoryListResponse
import org.prime.easykarobar.data.model.transactions.TransportDetails
import org.prime.easykarobar.ui.printing.salesHtml
import org.prime.easykarobar.ui.printing.salesSlipHtml
import org.prime.easykarobar.ui.screen.distributor.order.MyOrdersScreen
import org.prime.easykarobar.ui.screen.transactions.sale.InvoiceItem
import org.prime.easykarobar.ui.screen.transactions.sale.SaleScreen
import org.prime.easykarobar.ui.shared.composables.DownloadResultDialog
import org.prime.easykarobar.ui.shared.composables.EmptyListPlaceholder
import org.prime.easykarobar.ui.shared.composables.OrderCard
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getNameFromGUID
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction

data class InventoryListScreen(
    val startDate: String,
    val endDate: String,
    val vchType: Int,
    val name: String,
    val showStatusChange: Boolean = false
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
        androidx.compose.foundation.layout.ExperimentalLayoutApi::class
    )
    @Composable
    override fun Content() {
        val viewModel: InventoryVoucherViewModel = viewModel { InventoryVoucherViewModel() }
        val orderViewModel: OrderViewModel = viewModel { OrderViewModel() }
        val nav = LocalNavigator.currentOrThrow
        val state by viewModel.listState
        val updateState by orderViewModel.updateStatusState
        val oneState by viewModel.oneState

        var showStatusSheet by remember { mutableStateOf(false) }
        var showConfirmDialog by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf<InventoryListResponse?>(null) }
        var selectedStatus by remember { mutableStateOf<ORDERSTATUS?>(null) }
        val sheetState = rememberModalBottomSheetState()

        var showDetailsSheet by remember { mutableStateOf(false) }
        var selectedItemForDetails by remember { mutableStateOf<InventoryListResponse?>(null) }
        var shareLoading by remember { mutableStateOf(false) }
        var showDownloadDialog by remember { mutableStateOf(false) }
        var a4Html by remember { mutableStateOf("") }
        var slipHtml by remember { mutableStateOf("") }
        var downloadFileName by remember { mutableStateOf("") }

        val filterOptions = listOf("All") + ORDERSTATUS.entries.take(6).map { it.displayName() }
        var selectedFilterIndex by remember { mutableStateOf(0) }


        LaunchedEffect(Unit) {
            viewModel.listInventoryVch(
                InventoryListRequest(
                    VchType = vchType,
                    StartDate = startDate,
                    EndDate = endDate,isCustomer = showStatusChange
                )
            )
        }

        LaunchedEffect(oneState) {
            if (oneState.success && oneState.data != null) {
                val data = oneState.data!!
                val items = data.items.map { itm ->
                    InvoiceItem(
                        name = itm.product_name,
                        price = itm.price.toDoubleOrNull() ?: 0.0,
                        listPrice = itm.list_price.toDoubleOrNull() ?: 0.0,
                        qty = itm.quantity,
                        discountPercentage = itm.discount_percent.toDoubleOrNull() ?: 0.0,
                        taxable = itm.item_amount.toDoubleOrNull() ?: 0.0,
                        gstAmt = itm.taxamt1.toDoubleOrNull() ?: 0.0,
                        net = itm.total_amt.toDoubleOrNull() ?: 0.0,
                        gstPercentage = itm.tax_rate1.toDoubleOrNull() ?: 0.0,
                        taxCategoryCode = 0,
                        CD = itm.CD
                    )
                }

                a4Html = salesHtml(
                    name = name,
                    partyName = data.billing_name,
                    partyGuid = data.billing_guid,
                    invoiceNo = data.order_no,
                    date = Tdate(data.created_at.take(10)),
                    items = items,
                    sundries = data.sundries,
                    grandTotal = data.total_amount.toDoubleOrNull() ?: 0.0,
                    transportDetails = mapTransportDetails(data.other_info),
                    showTax = true
                )

                slipHtml = salesSlipHtml(
                    name = name,
                    partyName = data.billing_name,
                    partyGuid = data.billing_guid,
                    invoiceNo = data.order_no,
                    date = Tdate(data.created_at.take(10)),
                    items = items,
                    sundries = data.sundries,
                    grandTotal = data.total_amount.toDoubleOrNull() ?: 0.0,
                    transportDetails = mapTransportDetails(data.other_info),
                    showTax = true
                )

                downloadFileName = data.order_no.replace("/", "_")
            }
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
                            EndDate = endDate,isCustomer = showStatusChange
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
                        val filteredData = remember(state.data, selectedFilterIndex) {
                            if (selectedFilterIndex == 0) {
                                state.data
                            } else {
                                val statusToMatch = ORDERSTATUS.entries[selectedFilterIndex - 1].name
                                state.data?.filter { it.OrderStatus == statusToMatch }
                            }
                        }

                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filterOptions.forEachIndexed { index, label ->
                                    ToggleButton(
                                        checked = selectedFilterIndex == index,
                                        onCheckedChange = { selectedFilterIndex = index },
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
                                            filterOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        },
                                        modifier = Modifier.semantics { role = Role.RadioButton },
                                    ) {
                                        Text(label)
                                    }
                                }
                            }

                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                filteredData?.let {
                                    itemsIndexed(it) { index, item ->
                                        if (showStatusChange) {
                                            Box(
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                            ) {
                                                OrderCard(
                                                    orderNo = item.order_no,
                                                    orderStatus = item.OrderStatus,
                                                    orderDate = item.created_at,
                                                    billingName = getNameFromGUID(item.billing_guid),
                                                    totalAmount = item.total_amount,
                                                    showStatusChange = true,
                                                    onStatusChangeClick = {
                                                        selectedItem = item
                                                        showStatusSheet = true
                                                    },
                                                    onDownloadClick = {
                                                        selectedItemForDetails = item
                                                        viewModel.getOneInventoryVoucher(item.id)
                                                        showDownloadDialog = true
                                                    },
                                                    onShareClick = {
                                                        selectedItemForDetails = item
                                                        viewModel.getOneInventoryVoucher(item.id)
                                                        showDownloadDialog = true
                                                    },
                                                    onClick = {
                                                        selectedItemForDetails = item
                                                        viewModel.getOneInventoryVoucher(item.id)
                                                        showDetailsSheet = true
                                                    }
                                                )
                                            }
                                        } else {
                                            ListItem(
                                                listState = item,
                                                showStatusChange = false,
                                                onStatusChangeClick = { }
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

                if (showDetailsSheet) {
                    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { showDetailsSheet = false },
                        sheetState = detailSheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Bill Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            when {
                                oneState.isLoading -> {
                                    TallyCircularLoader()
                                }
                                oneState.error != null -> {
                                    Text("Error: ${oneState.error}", color = MaterialTheme.colorScheme.error)
                                }
                                oneState.data != null -> {
                                    val data = oneState.data!!

                                    if (data.sundries.isNotEmpty()) {
                                        Text(
                                            "Sundries",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        data.sundries.forEach { sundry ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(sundry.name)
                                                Text(sundry.amount.formatToAmtDec())
                                            }
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                showDownloadDialog = true
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Export Bill")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                nav.push(
                                                    MyOrdersScreen(
                                                        order_id = data.id.toString(),
                                                        isStatusChangeMode = true
                                                    )
                                                )
                                                showDetailsSheet = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("View Full Order")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                if (showDownloadDialog) {
                    if (oneState.isLoading) {
                        TallyLoadingDialog(
                            text = "Preparing Bill..."
                        )
                    } else if (oneState.data != null) {
                        DownloadResultDialog(
                            message = "Bill Prepared Successfully",
                            onDone = { showDownloadDialog = false },
                            isSuccess = true,
                            fileName = downloadFileName,
                            htmlContent = a4Html,
                            secondFileName = downloadFileName + "_Slip",
                            secondHtmlContent = slipHtml,
                            onLoadingChange = { shareLoading = it }
                        )
                    } else if (oneState.error != null) {
                        TallyResultDialog(
                            message = oneState.error ?: "Error preparing bill",
                            onDone = { showDownloadDialog = false },
                            isSuccess = false
                        )
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

    private fun mapTransportDetails(details: TransportDetails?): org.prime.easykarobar.ui.printing.TransportDetails {
        return org.prime.easykarobar.ui.printing.TransportDetails(
            transportName = details?.transportName ?: "",
            gstRrNo = details?.gstNum ?: "",
            vehicleNo = details?.vehicleNum ?: "",
            station = details?.station ?: "",
            pincode = details?.pincode ?: "",
            gstRrDate = details?.grDate ?: "",
            SpartyName = details?.SpartyName,
            Saddress1 = details?.Saddress1,
            Saddress2 = details?.Saddress2,
            Saddress3 = details?.Saddress3,
            Saddress4 = details?.Saddress4,
            SshipState = details?.SshipState,
            SgstIn = details?.SgstIn
        )
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
