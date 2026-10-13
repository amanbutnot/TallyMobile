package org.prime.easykarobar.ui.screen.transactions.sale

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.prime.easykarobar.data.model.transactions.StockJournalSaveRequest
import org.prime.easykarobar.data.utils.SaleOrderConfig
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.globalShared.ProductsWithConfig
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getConfigItemMasters
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import org.prime.easykarobar.ui.shared.reportsShared.TallyDatePickerRow

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

        var items1 by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var items2 by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }

        var showLocation1Sheet by remember { mutableStateOf(false) }
        var showLocation2Sheet by remember { mutableStateOf(false) }
        var showItem1Sheet by remember { mutableStateOf(false) }
        var showItem2Sheet by remember { mutableStateOf(false) }

        // Editing / Pending state for Location 1
        var editingItem1 by remember { mutableStateOf<InvoiceItem?>(null) }
        var editingItem1Index by remember { mutableStateOf<Int?>(null) }
        var pendingProduct1 by remember { mutableStateOf<ProductsWithConfig?>(null) }

        // Editing / Pending state for Location 2
        var editingItem2 by remember { mutableStateOf<InvoiceItem?>(null) }
        var editingItem2Index by remember { mutableStateOf<Int?>(null) }
        var pendingProduct2 by remember { mutableStateOf<ProductsWithConfig?>(null) }

        var showResultDialog by remember { mutableStateOf(false) }

        val totalProduce = remember(items1) { items1.sumOf { it.net } }
        val totalConsumed = remember(items2) { items2.sumOf { it.net } }
        val areAmountsEqual = remember(totalProduce, totalConsumed, items1, items2) {
            if (items1.isEmpty() && items2.isEmpty()) false
            else kotlin.math.abs(totalProduce - totalConsumed) < 0.01
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
                        val isDiscAsPer = SaleOrderConfig.discountAsPer(vchType)

                        val billingItems1 = items1.map { itm ->
                            val prod = itemsList.find { it.Name == itm.name }
                            BillingItem(
                                product_id = prod?.ID?.toString() ?: "",
                                product_name = itm.name,
                                CD = itm.CD,
                                quantity = itm.qty,
                                list_price = itm.listPrice,
                                discount_percent = if (isDiscAsPer) itm.discountPercentage else 0.0,
                                discount_amt = if (!isDiscAsPer) itm.discountPercentage else null,
                                tax_rate1 = itm.gstPercentage,
                                tax_rate2 = 0.0,
                                taxable = itm.taxable,
                                gstAmt = itm.gstAmt,
                                net = itm.net,
                                guid = itm.guid,
                                item_serial = itm.item_serial.map { it.SerialNo ?: "" },
                                conFactor = itm.conFactor,
                                conType = itm.conType,
                                selectedUnit = itm.selectedUnit,
                                altQty = itm.altQty,
                                item_parameter = itm.item_parameter.ifEmpty {
                                    itm.item_params.map { it.C2 ?: "" }
                                },
                                additionalinfo = "Produce Location: $location1"
                            )
                        }

                        val billingItems2 = items2.map { itm ->
                            val prod = itemsList.find { it.Name == itm.name }
                            BillingItem(
                                product_id = prod?.ID?.toString() ?: "",
                                product_name = itm.name,
                                CD = itm.CD,
                                quantity = itm.qty,
                                list_price = itm.listPrice,
                                discount_percent = if (isDiscAsPer) itm.discountPercentage else 0.0,
                                discount_amt = if (!isDiscAsPer) itm.discountPercentage else null,
                                tax_rate1 = itm.gstPercentage,
                                tax_rate2 = 0.0,
                                taxable = itm.taxable,
                                gstAmt = itm.gstAmt,
                                net = itm.net,
                                guid = itm.guid,
                                item_serial = itm.item_serial.map { it.SerialNo ?: "" },
                                conFactor = itm.conFactor,
                                conType = itm.conType,
                                selectedUnit = itm.selectedUnit,
                                altQty = itm.altQty,
                                item_parameter = itm.item_parameter.ifEmpty {
                                    itm.item_params.map { it.C2 ?: "" }
                                },
                                additionalinfo = "Consumed Location: $location2"
                            )
                        }

                        val request = StockJournalSaveRequest(
                            vch_type = vchType,
                            date = selectedDate,
                            narration = narration,
                            produceLocation = location1,
                            consumeLocation = location2,
                            items = billingItems1,
                            items_2 = billingItems2,
                            total_amt = totalProduce
                        )

                        viewModel.createEditInventoryResponse(
                            inventoryVoucherRequest = request,
                            onSuccess = {},
                            url = "addInventoryVch"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = location1.isNotBlank() && location2.isNotBlank() && items1.isNotEmpty() && items2.isNotEmpty() && areAmountsEqual && !dataState.isLoading
                ) {
                    Text(if (dataState.isLoading) "Saving..." else "Save Stock Journal", fontWeight = FontWeight.Bold)
                }
            },
            content = { paddingValues ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date Card
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
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

                    // Produce Location Section
                    item {
                        SectionCard(
                            title = "PRODUCE LOCATION",
                            count = items1.size,
                            headerAction = {
                                AssistChip(
                                    onClick = { showItem1Sheet = true },
                                    enabled = editingItem1 == null,
                                    label = { Text("Add Item") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = null
                                )
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { showLocation1Sheet = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (location1.isBlank()) "Select Produce Location" else location1, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Produce Location Expanded Editor if active
                    if (editingItem1 != null && pendingProduct1 != null) {
                        item {
                            ExpandedItemEditor1(
                                name = pendingProduct1!!.Name ?: "",
                                vchType = vchType,
                                defaultListPrice = pendingProduct1!!.PurcPrice ?: 0.0,
                                gstPercentage = 0.0,
                                initialQuantity = editingItem1!!.qty,
                                taxType = TaxType.VOUCHER,
                                initialDiscount = editingItem1!!.CD,
                                existingItem = editingItem1,
                                mainUnit = pendingProduct1!!.UnitName ?: "",
                                altUnit = pendingProduct1!!.AltUnit,
                                conFactor = pendingProduct1!!.ConFactor,
                                conType = pendingProduct1!!.ConType,
                                salesPriceAlt = pendingProduct1!!.SalesPriceAlt,
                                purcPriceAlt = pendingProduct1!!.PurcPriceAlt,
                                isSale = false,
                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, _, _, serialNumbers, cFactor, cType, sUnit, aQty ->
                                    val newItem = editingItem1!!.copy(
                                        name = pendingProduct1!!.Name ?: "",
                                        qty = qty,
                                        price = unitPrice,
                                        listPrice = listPriceText,
                                        discountPercentage = discount,
                                        CD = compoundDiscount ?: "",
                                        taxable = taxable,
                                        gstAmt = gstAmount,
                                        net = net,
                                        gstPercentage = gstPercentage,
                                        guid = pendingProduct1!!.GUID ?: "",
                                        conFactor = cFactor,
                                        conType = cType,
                                        selectedUnit = sUnit,
                                        altQty = aQty,
                                        mainUnit = pendingProduct1!!.UnitName,
                                        altUnit = pendingProduct1!!.AltUnit
                                    )
                                    val mutable = items1.toMutableList()
                                    if (editingItem1Index != null) {
                                        mutable[editingItem1Index!!] = newItem
                                    } else {
                                        mutable.add(newItem)
                                    }
                                    items1 = mutable
                                    editingItem1 = null
                                    editingItem1Index = null
                                    pendingProduct1 = null
                                },
                                onCancel = {
                                    editingItem1 = null
                                    editingItem1Index = null
                                    pendingProduct1 = null
                                },
                                onBack = {
                                    editingItem1 = null
                                    editingItem1Index = null
                                    pendingProduct1 = null
                                }
                            )
                        }
                    }

                    itemsIndexed(items1) { index, item ->
                        if (editingItem1Index != index) {
                            CompactItemCard(
                                index = index,
                                item = item,
                                gstPercentage = item.gstPercentage,
                                taxType = TaxType.VOUCHER,
                                onQuantityChange = { newQty ->
                                    items1 = items1.mapIndexed { idx, itm ->
                                        if (idx == index) {
                                            val t = itm.price * newQty
                                            itm.copy(qty = newQty, taxable = t, net = t)
                                        } else itm
                                    }
                                },
                                onRemove = {
                                    if (editingItem1 == null) items1 = items1 - item
                                },
                                onEdit = {
                                    if (editingItem1 == null) {
                                        editingItem1Index = index
                                        editingItem1 = item
                                        pendingProduct1 = itemsList.find { it.Name == item.name }
                                    }
                                },
                                onSerialNo = {},
                                onParameter = {},
                                showExtraButtons = false
                            )
                        }
                    }

                    if (items1.isNotEmpty()) {
                        item {
                            SubtotalRow("Produce Subtotal", totalProduce)
                        }
                    }

                    // Consumed Location Section
                    item {
                        Spacer(Modifier.height(4.dp))
                        SectionCard(
                            title = "CONSUMED LOCATION",
                            count = items2.size,
                            headerAction = {
                                AssistChip(
                                    onClick = { showItem2Sheet = true },
                                    enabled = editingItem2 == null,
                                    label = { Text("Add Item") },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    border = null
                                )
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { showLocation2Sheet = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (location2.isBlank()) "Select Consumed Location" else location2, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Consumed Location Expanded Editor if active
                    if (editingItem2 != null && pendingProduct2 != null) {
                        item {
                            ExpandedItemEditor1(
                                name = pendingProduct2!!.Name ?: "",
                                vchType = vchType,
                                defaultListPrice = pendingProduct2!!.SalesPrice ?: 0.0,
                                gstPercentage = 0.0,
                                initialQuantity = editingItem2!!.qty,
                                taxType = TaxType.VOUCHER,
                                initialDiscount = editingItem2!!.CD,
                                existingItem = editingItem2,
                                mainUnit = pendingProduct2!!.UnitName ?: "",
                                altUnit = pendingProduct2!!.AltUnit,
                                conFactor = pendingProduct2!!.ConFactor,
                                conType = pendingProduct2!!.ConType,
                                salesPriceAlt = pendingProduct2!!.SalesPriceAlt,
                                purcPriceAlt = pendingProduct2!!.PurcPriceAlt,
                                isSale = true,
                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, _, _, serialNumbers, cFactor, cType, sUnit, aQty ->
                                    val newItem = editingItem2!!.copy(
                                        name = pendingProduct2!!.Name ?: "",
                                        qty = qty,
                                        price = unitPrice,
                                        listPrice = listPriceText,
                                        discountPercentage = discount,
                                        CD = compoundDiscount ?: "",
                                        taxable = taxable,
                                        gstAmt = gstAmount,
                                        net = net,
                                        gstPercentage = gstPercentage,
                                        guid = pendingProduct2!!.GUID ?: "",
                                        conFactor = cFactor,
                                        conType = cType,
                                        selectedUnit = sUnit,
                                        altQty = aQty,
                                        mainUnit = pendingProduct2!!.UnitName,
                                        altUnit = pendingProduct2!!.AltUnit
                                    )
                                    val mutable = items2.toMutableList()
                                    if (editingItem2Index != null) {
                                        mutable[editingItem2Index!!] = newItem
                                    } else {
                                        mutable.add(newItem)
                                    }
                                    items2 = mutable
                                    editingItem2 = null
                                    editingItem2Index = null
                                    pendingProduct2 = null
                                },
                                onCancel = {
                                    editingItem2 = null
                                    editingItem2Index = null
                                    pendingProduct2 = null
                                },
                                onBack = {
                                    editingItem2 = null
                                    editingItem2Index = null
                                    pendingProduct2 = null
                                }
                            )
                        }
                    }

                    itemsIndexed(items2) { index, item ->
                        if (editingItem2Index != index) {
                            CompactItemCard(
                                index = index,
                                item = item,
                                gstPercentage = item.gstPercentage,
                                taxType = TaxType.VOUCHER,
                                onQuantityChange = { newQty ->
                                    items2 = items2.mapIndexed { idx, itm ->
                                        if (idx == index) {
                                            val t = itm.price * newQty
                                            itm.copy(qty = newQty, taxable = t, net = t)
                                        } else itm
                                    }
                                },
                                onRemove = {
                                    if (editingItem2 == null) items2 = items2 - item
                                },
                                onEdit = {
                                    if (editingItem2 == null) {
                                        editingItem2Index = index
                                        editingItem2 = item
                                        pendingProduct2 = itemsList.find { it.Name == item.name }
                                    }
                                },
                                onSerialNo = {},
                                onParameter = {},
                                showExtraButtons = false
                            )
                        }
                    }

                    if (items2.isNotEmpty()) {
                        item {
                            SubtotalRow("Consumed Subtotal", totalConsumed)
                        }
                    }

                    // Validation warning if amounts don't match
                    if (items1.isNotEmpty() && items2.isNotEmpty() && !areAmountsEqual) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Amount Mismatch",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Produce Total (${totalProduce.formatToAmtDec()}) must equal Consumed Total (${totalConsumed.formatToAmtDec()}).",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    // Narration and Grand Total
                    item {
                        Spacer(Modifier.height(4.dp))
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = narration,
                                    onValueChange = { narration = it },
                                    label = { Text("Narration") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(totalProduce.formatToAmtDec(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        )

        // Bottom Sheets for Locations (using SelectionSheet)
        SelectionSheet(
            show = showLocation1Sheet,
            title = "Select Produce Location",
            options = godowns,
            onSelect = { location1 = it },
            onDismiss = { showLocation1Sheet = false }
        )

        SelectionSheet(
            show = showLocation2Sheet,
            title = "Select Consumed Location",
            options = godowns,
            onSelect = { location2 = it },
            onDismiss = { showLocation2Sheet = false }
        )

        // Bottom Sheet for Produce Location Items
        SelectionSheetItem(
            show = showItem1Sheet,
            title = "Select Item (Produce)",
            options = itemsList,
            onSelect = { prod ->
                pendingProduct1 = prod
                editingItem1 = InvoiceItem(
                    name = prod.Name ?: "",
                    price = prod.PurcPrice ?: 0.0,
                    listPrice = prod.PurcPrice ?: 0.0,
                    qty = 1.0,
                    discountPercentage = 0.0,
                    taxCategoryCode = prod.TaxCategoryCode?.toInt() ?: 0,
                    gstPercentage = 0.0,
                    taxable = prod.PurcPrice ?: 0.0,
                    gstAmt = 0.0,
                    net = prod.PurcPrice ?: 0.0,
                    guid = prod.GUID ?: "",
                    CD = "",
                    mainUnit = prod.UnitName,
                    altUnit = prod.AltUnit,
                    conFactor = prod.ConFactor,
                    conType = prod.ConType
                )
                editingItem1Index = null
            },
            onDismiss = { showItem1Sheet = false },
            productGroups = productGroups
        )

        // Bottom Sheet for Consumed Location Items
        SelectionSheetItem(
            show = showItem2Sheet,
            title = "Select Item (Consumed)",
            options = itemsList,
            onSelect = { prod ->
                pendingProduct2 = prod
                editingItem2 = InvoiceItem(
                    name = prod.Name ?: "",
                    price = prod.SalesPrice ?: 0.0,
                    listPrice = prod.SalesPrice ?: 0.0,
                    qty = 1.0,
                    discountPercentage = 0.0,
                    taxCategoryCode = prod.TaxCategoryCode?.toInt() ?: 0,
                    gstPercentage = 0.0,
                    taxable = prod.SalesPrice ?: 0.0,
                    gstAmt = 0.0,
                    net = prod.SalesPrice ?: 0.0,
                    guid = prod.GUID ?: "",
                    CD = "",
                    mainUnit = prod.UnitName,
                    altUnit = prod.AltUnit,
                    conFactor = prod.ConFactor,
                    conType = prod.ConType
                )
                editingItem2Index = null
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
