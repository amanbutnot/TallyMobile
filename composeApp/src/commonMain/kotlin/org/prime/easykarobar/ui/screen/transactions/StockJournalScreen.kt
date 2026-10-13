package org.prime.easykarobar.ui.screen.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.transactions.BillingItem
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.TransportDetails
import org.prime.easykarobar.ui.screen.transactions.sale.SelectionSheet
import org.prime.easykarobar.ui.screen.transactions.sale.SelectionSheetItem
import org.prime.easykarobar.ui.shared.composables.*
import org.prime.easykarobar.ui.shared.globalShared.getConfigItemMasters
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import org.prime.easykarobar.ui.shared.reportsShared.TallyDatePickerRow

data class StockJournalItem(
    val productId: String,
    val productName: String,
    val guid: String,
    val quantity: Double,
    val price: Double,
    val unit: String?,
    val net: Double
)

data class StockJournalScreen(
    val name: String = "Stock Journal",
    val vchType: Int = 8
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val viewModel: InventoryVoucherViewModel = viewModel { InventoryVoucherViewModel() }
        val db = DatabaseHolder.instance

        val godowns = remember { db.godownMasterQueries.selectAll().executeAsList().map { it.Name.toString() } }
        val itemsList = remember { getConfigItemMasters(db, vchType) }
        val productGroups = remember {
            db.productGroupMasterQueries.selectAll(
                filterGroup = filterItemGroups(),
                groupCodes = if (filterItemGroups() == 1L) itemGroupCodes() else emptyList()
            ).executeAsList()
        }

        var selectedDate by remember { mutableStateOf(CurrentDate()) }
        var location1 by remember { mutableStateOf(godowns.firstOrNull() ?: "") }
        var location2 by remember { mutableStateOf(godowns.getOrNull(1) ?: godowns.firstOrNull() ?: "") }
        var narration by remember { mutableStateOf("") }

        var items1 by remember { mutableStateOf<List<StockJournalItem>>(emptyList()) }
        var items2 by remember { mutableStateOf<List<StockJournalItem>>(emptyList()) }

        var showLocation1Sheet by remember { mutableStateOf(false) }
        var showLocation2Sheet by remember { mutableStateOf(false) }
        var showItem1Sheet by remember { mutableStateOf(false) }
        var showItem2Sheet by remember { mutableStateOf(false) }

        var editingItem1Index by remember { mutableStateOf<Int?>(null) }
        var editingItem2Index by remember { mutableStateOf<Int?>(null) }
        var editQty1 by remember { mutableStateOf("") }
        var editPrice1 by remember { mutableStateOf("") }
        var editQty2 by remember { mutableStateOf("") }
        var editPrice2 by remember { mutableStateOf("") }

        var showResultDialog by remember { mutableStateOf(false) }

        val totalAmount = remember(items1, items2) {
            items1.sumOf { it.net } + items2.sumOf { it.net }
        }

        val dataState by viewModel.dataState

        LaunchedEffect(dataState.success) {
            if (dataState.success) {
                showResultDialog = true
            }
        }

        TallyScaffold(
            title = name,
            showBottomBar = true,
            bottomBarContent = {
                Button(
                    onClick = {
                        val billingItems1 = items1.map { itm ->
                            BillingItem(
                                product_id = itm.productId,
                                product_name = itm.productName,
                                CD = "",
                                quantity = itm.quantity,
                                list_price = itm.price,
                                tax_rate1 = 0.0,
                                tax_rate2 = 0.0,
                                taxable = itm.net,
                                gstAmt = 0.0,
                                net = itm.net,
                                guid = itm.guid,
                                item_serial = emptyList(),
                                item_parameter = emptyList(),
                                additionalinfo = "Location 1: $location1"
                            )
                        }

                        val billingItems2 = items2.map { itm ->
                            BillingItem(
                                product_id = itm.productId,
                                product_name = itm.productName,
                                CD = "",
                                quantity = itm.quantity,
                                list_price = itm.price,
                                tax_rate1 = 0.0,
                                tax_rate2 = 0.0,
                                taxable = itm.net,
                                gstAmt = 0.0,
                                net = itm.net,
                                guid = itm.guid,
                                item_serial = emptyList(),
                                item_parameter = emptyList(),
                                additionalinfo = "Location 2: $location2"
                            )
                        }

                        val request = InventoryVoucherRequest(
                            billing_guid = "",
                            vch_type = vchType,
                            billing_name = "Stock Journal",
                            TranDate = selectedDate,
                            billing_mobile = "",
                            billing_state = "",
                            billing_country = "",
                            billing_address = "",
                            taxType = 2,
                            items = billingItems1 + billingItems2,
                            sundries = emptyList(),
                            Narration = narration,
                            transportDetails = TransportDetails(
                                transportName = "",
                                station = "",
                                gstNum = "",
                                vehicleNum = "",
                                pincode = "",
                                grDate = "",
                                dispatch_name = location1,
                                dispatch_address = location2
                            ),
                            bills_collection = emptyList(),
                            total_amt = totalAmount
                        )

                        viewModel.createEditInventoryResponse(
                            inventoryVoucherRequest = request,
                            onSuccess = {},
                            url = "addInventoryVch"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = location1.isNotBlank() && location2.isNotBlank() && (items1.isNotEmpty() || items2.isNotEmpty()) && !dataState.isLoading
                ) {
                    Text(if (dataState.isLoading) "Saving..." else "Save Stock Journal")
                }
            },
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Card matching SaleScreen style
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TallyDatePickerRow(
                                    label = "Entry Date",
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    defaultDate = CurrentDate()
                                )
                            }
                        }
                    }

                    // Location 1 & Items (Purchase Price)
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Location 1 (Source - Purchase Price)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedButton(
                                    onClick = { showLocation1Sheet = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (location1.isBlank()) "Select Location 1" else location1)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Items (${items1.size})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    AssistChip(
                                        onClick = { showItem1Sheet = true },
                                        label = { Text("Add Item") },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(items1) { index, item ->
                        if (editingItem1Index == index) {
                            // Expanded Item Editor Card for Location 1
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Editing: ${item.productName}", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = editQty1,
                                        onValueChange = { editQty1 = it },
                                        label = { Text("Quantity") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editPrice1,
                                        onValueChange = { editPrice1 = it },
                                        label = { Text("Purchase Price") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { editingItem1Index = null }) {
                                            Text("Cancel")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(onClick = {
                                            val q = editQty1.toDoubleOrNull() ?: item.quantity
                                            val p = editPrice1.toDoubleOrNull() ?: item.price
                                            val updated = item.copy(quantity = q, price = p, net = q * p)
                                            items1 = items1.toMutableList().apply { set(index, updated) }
                                            editingItem1Index = null
                                        }) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Collapsed / Compact Item Card for Location 1
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text("Qty: ${item.quantity} ${item.unit ?: ""} | Price: ${item.price.formatToAmtDec()}")
                                        Text("Total: ${item.net.formatToAmtDec()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            editingItem1Index = index
                                            editQty1 = item.quantity.toString()
                                            editPrice1 = item.price.toString()
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            items1 = items1.toMutableList().apply { removeAt(index) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Location 2 & Items (Sale Price)
                    item {
                        Spacer(Modifier.height(8.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Location 2 (Destination - Sale Price)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                OutlinedButton(
                                    onClick = { showLocation2Sheet = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (location2.isBlank()) "Select Location 2" else location2)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Items (${items2.size})", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    AssistChip(
                                        onClick = { showItem2Sheet = true },
                                        label = { Text("Add Item") },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(items2) { index, item ->
                        if (editingItem2Index == index) {
                            // Expanded Item Editor Card for Location 2
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Editing: ${item.productName}", fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = editQty2,
                                        onValueChange = { editQty2 = it },
                                        label = { Text("Quantity") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editPrice2,
                                        onValueChange = { editPrice2 = it },
                                        label = { Text("Sale Price") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { editingItem2Index = null }) {
                                            Text("Cancel")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(onClick = {
                                            val q = editQty2.toDoubleOrNull() ?: item.quantity
                                            val p = editPrice2.toDoubleOrNull() ?: item.price
                                            val updated = item.copy(quantity = q, price = p, net = q * p)
                                            items2 = items2.toMutableList().apply { set(index, updated) }
                                            editingItem2Index = null
                                        }) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Collapsed / Compact Item Card for Location 2
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold)
                                        Text("Qty: ${item.quantity} ${item.unit ?: ""} | Price: ${item.price.formatToAmtDec()}")
                                        Text("Total: ${item.net.formatToAmtDec()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            editingItem2Index = index
                                            editQty2 = item.quantity.toString()
                                            editPrice2 = item.price.toString()
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            items2 = items2.toMutableList().apply { removeAt(index) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = narration,
                            onValueChange = { narration = it },
                            label = { Text("Narration") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(totalAmount.formatToAmtDec(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        )

        // Bottom Sheets for Locations (using SelectionSheet)
        SelectionSheet(
            show = showLocation1Sheet,
            title = "Select Location 1",
            options = godowns,
            onSelect = { location1 = it },
            onDismiss = { showLocation1Sheet = false }
        )

        SelectionSheet(
            show = showLocation2Sheet,
            title = "Select Location 2",
            options = godowns,
            onSelect = { location2 = it },
            onDismiss = { showLocation2Sheet = false }
        )

        // Bottom Sheet for Location 1 Items (Purchase Price)
        SelectionSheetItem(
            show = showItem1Sheet,
            title = "Select Item (Purchase)",
            options = itemsList,
            onSelect = { prod ->
                val prc = prod.PurcPrice ?: 0.0
                val qty = 1.0
                val newItem = StockJournalItem(
                    productId = prod.ID.toString(),
                    productName = prod.Name.toString(),
                    guid = prod.GUID.toString(),
                    quantity = qty,
                    price = prc,
                    unit = prod.UnitName,
                    net = qty * prc
                )
                items1 = items1 + newItem
            },
            onDismiss = { showItem1Sheet = false },
            productGroups = productGroups
        )

        // Bottom Sheet for Location 2 Items (Sale Price)
        SelectionSheetItem(
            show = showItem2Sheet,
            title = "Select Item (Sale)",
            options = itemsList,
            onSelect = { prod ->
                val prc = prod.SalesPrice ?: 0.0
                val qty = 1.0
                val newItem = StockJournalItem(
                    productId = prod.ID.toString(),
                    productName = prod.Name.toString(),
                    guid = prod.GUID.toString(),
                    quantity = qty,
                    price = prc,
                    unit = prod.UnitName,
                    net = qty * prc
                )
                items2 = items2 + newItem
            },
            onDismiss = { showItem2Sheet = false },
            productGroups = productGroups
        )

        if (showResultDialog) {
            TallyResultDialog(
                message = "Stock Journal saved successfully!",
                onDone = {
                    showResultDialog = false
                    nav.pop()
                },
                isSuccess = true
            )
        }

        if (dataState.error != null) {
            TallyResultDialog(
                message = dataState.error ?: "Error",
                onDone = { viewModel.clearError() },
                isSuccess = false
            )
        }
    }
}
