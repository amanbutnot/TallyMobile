package org.prime.tally.ui.screen.transactions.sale

import CurrentDate
import TallyDatePickerRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.prime.tally.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.model.transactions.BillingItem
import org.prime.tally.data.model.transactions.InventoryVoucherRequest
import org.prime.tally.data.model.transactions.SundryItem
import org.prime.tally.ui.screen.transactions.SelectLedgerRow
import org.prime.tally.ui.screen.transactions.TallyNarrationField
import org.prime.tally.ui.screen.transactions.TransactionBottomSheet
import org.prime.tally.ui.screen.transactions.TransactionBottomSheetThree
import org.prime.tally.ui.screen.transactions.TransactionOneBottomSheet
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyAlertBox
import org.prime.tally.ui.shared.composables.TallyButton
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallyResultDialog
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.globalShared.getItemMasters
import org.prime.tally.ui.shared.globalShared.getLedgerMasters
import org.prime.tally.ui.shared.globalShared.isBusy
import org.tally.Products
import kotlin.math.abs
import kotlin.math.round

fun formatTwo(value: Double): String {
    val cents = round(value * 100).toLong()
    val whole = cents / 100
    val frac = abs((cents % 100).toInt())
    return "$whole.${if (frac < 10) "0$frac" else "$frac"}"
}

@Serializable
data class InvoiceItem(
    var name: String,
    val price: Double,
    val listPrice: Double,
    val qty: Int = 1,
    val priceType: Int = 0,
    val discountPercentage: Double,
    //sdfasdfasdfadsfsadf
    val taxable: Double,
    val gstAmt: Double,
    val net: Double,
    // GUID for the selected product (not shown in UI, sent to backend)
    val guid: String = ""
) {
    val total: Double get() = price * qty
}


enum class TaxType {
    INCLUSIVE,
    EXTRA
}

data class SaleScreen(
    val name: String,
    val vchType: Int,
    val tranId: Int? = null,
    val isEdit: Boolean = false
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow

        var selectedLedger by rememberSaveable { mutableStateOf("") }
        var selectedLedgerGUID by rememberSaveable { mutableStateOf("") }
        var narration by rememberSaveable { mutableStateOf("") }
        var selectedDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var taxType by rememberSaveable { mutableStateOf(TaxType.INCLUSIVE) }

        var selectedItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var selectedSundries by remember { mutableStateOf<List<SundryItem>>(emptyList()) }

        var showLedgerSheet by rememberSaveable { mutableStateOf(false) }
        var showItemSheet by rememberSaveable { mutableStateOf(false) }
        var showSundrySheet by rememberSaveable { mutableStateOf(false) }
        var showResultDialog by rememberSaveable { mutableStateOf(false) }

        var editingItem by remember { mutableStateOf<InvoiceItem?>(null) }

        var pendingSelectedProductName by rememberSaveable { mutableStateOf<String?>(null) }
        var pendingSelectedProductGUID by rememberSaveable { mutableStateOf<String?>(null) }

        val ledgerList = getLedgerMasters(db)
        val busyLedgerList = db.bSMasterQueries.selectAll().executeAsList()
        val itemsList = getItemMasters(db)
        val viewmodel: InventoryVoucherViewModel = viewModel { InventoryVoucherViewModel() }
        val state by viewmodel.dataState
        val oneState by viewmodel.oneState
        val deleteState by viewmodel.deleteState
        val scope = rememberCoroutineScope()

        var showDeleteDialog by remember { mutableStateOf(false) }



        if (deleteState.message != null) {
            TallyResultDialog(
                message = deleteState.message ?: "Error Occurred",
                onDone = { nav.pop() },
                isSuccess = deleteState.success,
                confirmText = "Done"
            )
        }


        if (showDeleteDialog) {
            TallyAlertBox(
                title = "Delete",
                message = "Are you sure you want to delete this voucher",
                onConfirm = {
                    scope.launch {
                        tranId?.let {
                            viewmodel.deleteInventoryVch(
                                tranId = it,
                                vchType = vchType
                            )
                        }
                    }
                },
                onCancel = { showDeleteDialog = false },
                onDismiss = { showDeleteDialog = false },
            )
        }


        val gstPercent = 18.0

        val itemsTotal by derivedStateOf {
            selectedItems.sumOf { item ->
                if (taxType == TaxType.EXTRA) {
                    val taxable = item.price * item.qty
                    taxable + taxable * gstPercent / 100.0
                } else {
                    item.price * item.qty
                }
            }
        }

        LaunchedEffect(tranId) {
            tranId?.let { viewmodel.getOneInventoryVoucher(it) }
        }


        LaunchedEffect(oneState.data) {
            if (!isEdit) return@LaunchedEffect
            oneState.data?.let { data ->
                selectedDate = Tdate(data.created_at.take(10))
                selectedLedger = data.billing_name
                selectedLedgerGUID = data.billing_guid
                taxType = if (data.taxType == 1) TaxType.EXTRA else TaxType.INCLUSIVE
                selectedItems = data.items.map {
                    InvoiceItem(
                        name = it.product_name,
                        price = it.price.toDouble(),
                        listPrice = it.list_price.toDouble(),
                        qty = it.quantity,
                        discountPercentage = it.discount_percent.toDouble(),
                        taxable = it.item_amount.toDouble(),
                        gstAmt = it.taxamt1.toDouble(),
                        net = it.total_amt.toDouble()
                        // guid will default to "" if backend item doesn't include guid in this DTO
                    )
                }
            }
        }


        val sundriesTotal = selectedSundries.sumOf {
            when (it.i1) {
                1 -> it.amount
                0 -> -it.amount
                else -> 0.0
            }
        }

        val grandTotal = itemsTotal + sundriesTotal

        if (state.isLoading) {
            TallyLoadingDialog(if (isEdit) "Editing transaction" else "Creating transaction")
        }

        LaunchedEffect(pendingSelectedProductName) {
            pendingSelectedProductName?.let { name ->
                val prod = itemsList.find { it.Name == name }
                val price = prod?.SalesPrice ?: 0.0
                editingItem =
                    InvoiceItem(
                        name = name, price = price, qty = 1, discountPercentage = 0.0,
                        listPrice = 0.0,
                        taxable = 0.0,
                        gstAmt = 0.0,
                        net = 0.0,
                        guid = pendingSelectedProductGUID ?: ""
                    )
                showItemSheet = false
                pendingSelectedProductName = null
            }
        }

        TallyReportScaffold(
            showBurgerMenu = isEdit,
            menuItems = listOf(
                MenuItemData(Icons.Default.Download, "Download", {}),
                MenuItemData(Icons.Default.Share, "Share", {}),
                MenuItemData(Icons.Default.Delete, "Delete", { showDeleteDialog = true })
            ),
            title = if (isEdit) "Edit $name" else name,
            content = { paddingValues ->

                if (isEdit && oneState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(paddingValues).fillMaxSize()
                    )
                    {
                        Column(
                            modifier = Modifier

                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        )
                        {
                            Spacer(modifier = Modifier.height(4.dp))

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
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

                                    SelectLedgerRow(
                                        selectedAccount = selectedLedger,
                                        onShowBottomSheet = { showLedgerSheet = true },
                                        title = "Party Ledger"
                                    )
                                }
                            }

                            TaxTypeSelector(
                                selectedTaxType = taxType,
                                onTaxTypeSelected = { taxType = it }
                            )

                            SectionCard(
                                title = "ITEMS",
                                count = selectedItems.size,
                                headerAction = {
                                    SmallAddButton(label = "Add Item") { showItemSheet = true }
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val pending = editingItem
                                    if (pending != null) {
                                        val product = itemsList.find { it.Name == pending.name }
                                        if (product != null) {
                                            ExpandedItemEditor(
                                                name = product.Name ?: pending.name,
                                                defaultListPrice = pending.listPrice,
                                                initialQuantity = pending.qty,
                                                initialDiscount = pending.discountPercentage,
                                                taxType = taxType,
                                                onAdd = { qty, unitPrice, discount, listPriceText, taxable, gstAmount, net ->
                                                    selectedItems = selectedItems + InvoiceItem(
                                                        name = product.Name ?: pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        gstAmt = gstAmount,
                                                        net = net,
                                                        guid = product.GUID
                                                            ?: pendingSelectedProductGUID
                                                            ?: ""
                                                    )
                                                    editingItem = null
                                                },
                                                onCancel = {
                                                    selectedItems = selectedItems + pending
                                                    editingItem = null
                                                },
                                                gstPercentage = gstPercent
                                            )
                                        } else {
                                            ExpandedItemEditor(
                                                name = pending.name,
                                                defaultListPrice = pending.listPrice,
                                                initialDiscount = pending.discountPercentage,
                                                initialQuantity = pending.qty,
                                                taxType = taxType,
                                                onAdd = { qty, unitPrice, discount, listPriceText, taxable, gstAmount, net ->
                                                    selectedItems = selectedItems + InvoiceItem(
                                                        name = pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        gstAmt = gstAmount,
                                                        net = net,
                                                        guid = pendingSelectedProductGUID ?: ""
                                                    )
                                                    editingItem = null
                                                },
                                                onCancel = {
                                                    selectedItems = selectedItems + pending
                                                    editingItem = null
                                                },
                                                gstPercentage = gstPercent
                                            )
                                        }
                                    }

                                    selectedItems.forEachIndexed { index, item ->
                                        CompactItemCard(
                                            index = index,
                                            item = item,
                                            gstPercentage = gstPercent,
                                            taxType = taxType,
                                            onQuantityChange = { newQty ->
                                                selectedItems = selectedItems.map {
                                                    if (it.name == item.name) it.copy(qty = newQty) else it
                                                }
                                            },
                                            onRemove = { selectedItems = selectedItems - item },
                                            onEdit = {
                                                if (editingItem == null) {
                                                    selectedItems = selectedItems - item
                                                    editingItem = item
                                                }
                                            }
                                        )
                                    }

                                    if (selectedItems.isNotEmpty()) {
                                        SubtotalRow("Subtotal", itemsTotal)
                                    }
                                }
                            }

                            SectionCard(
                                title = if (isBusy()) "SUNDRIES" else "Ledgers",
                                count = selectedSundries.size,
                                headerAction = {
                                    SmallAddButton(label = if (isBusy()) "Add Sundry" else "Add Ledger") {
                                        showSundrySheet = true
                                    }
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedSundries.forEachIndexed { index, sundry ->
                                        SundryCard(
                                            index = index,
                                            sundry = sundry,
                                            onAmountChange = { newAmount ->
                                                selectedSundries = selectedSundries.map {
                                                    if (it.name == sundry.name) it.copy(amount = newAmount) else it
                                                }
                                            },
                                            onRemove = {
                                                selectedSundries = selectedSundries - sundry
                                            }
                                        )
                                    }

                                    if (selectedSundries.isNotEmpty()) {
                                        SubtotalRow("Subtotal", sundriesTotal)
                                    }
                                }
                            }

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    TallyNarrationField(
                                        value = narration,
                                        onValueChange = { narration = it },
                                        label = "Narration"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(60.dp))
                        }


                    }

                }



                SelectionSheet(
                    show = showLedgerSheet,
                    title = "Select Party Ledger",
                    options = ledgerList.filter { it.L1 == 1.0 || it.L2 == 1.0 || it.L3 == 1.0 }
                        .map { it.Name ?: "" },
                    onSelect = { selected ->
                        selectedLedger = selected
                        selectedLedgerGUID = ledgerList.find { l -> l.Name == selected }?.GUID ?: ""
                    },
                    onDismiss = { showLedgerSheet = false }
                )

                SelectionSheetItem(
                    show = showItemSheet,
                    title = "Select Item",
                    options = itemsList,
                    onSelect = { itemName ->
                        pendingSelectedProductName = itemName.Name
                        pendingSelectedProductGUID = itemName.GUID
                    },
                    onDismiss = { showItemSheet = false }
                )
                if (isBusy()) {
                    SelectionSheetThree(
                        show = showSundrySheet,
                        title = "Select Sundry",
                        options = busyLedgerList.map {
                            Triple(
                                it.Name ?: "",
                                it.GUID ?: "",
                                it.I1?.toInt() ?: 0
                            )
                        },
                        onSelect = { sundryName, GUID, i1 ->
                            if (!selectedSundries.any { it.name == sundryName }) {
                                selectedSundries =
                                    selectedSundries + SundryItem(
                                        sundryName,
                                        0.0,
                                        guid = GUID,
                                        i1 = i1
                                    )
                            }
                        },
                        onDismiss = { showSundrySheet = false }
                    )
                } else {

                    SelectionSheetTwo(
                        show = showSundrySheet,
                        title = "Select Ledger",
                        options = ledgerList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelect = { sundryName, GUID ->
                            if (!selectedSundries.any { it.name == sundryName }) {
                                selectedSundries =
                                    selectedSundries + SundryItem(sundryName, 0.0, guid = GUID)
                            }
                        },
                        onDismiss = { showSundrySheet = false }
                    )
                }
                if (showResultDialog) {
                    TallyResultDialog(
                        message = "${state.message} ${state.data?.VoucherNumber}",
                        onDone = { nav.pop() },
                        isSuccess = state.success,
                        confirmText = "Ok"
                    )
                }
            },
            showBottomBar = true,
            bottomBarContent = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 12.dp,
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                )
                {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatTwo(grandTotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }


                        TallyButton(

                            onClick = {

                                val billingItems = selectedItems.map { item ->
                                    val prod = itemsList.find { it.Name == item.name }
                                    BillingItem(
                                        product_id = prod?.ID?.toString() ?: "",
                                        product_name = item.name,
                                        quantity = item.qty,
                                        list_price = item.listPrice,
                                        discount_percent = item.discountPercentage,
                                        discount_amt = null,
                                        tax_rate1 = gstPercent,
                                        tax_rate2 = 0.0,
                                        taxable = item.taxable,
                                        net = item.net,
                                        gstAmt = item.gstAmt,
                                        // include the pending item GUID so backend receives it
                                        guid = item.guid
                                    )
                                }

                                viewmodel.createEditInventoryResponse(
                                    inventoryVoucherRequest = InventoryVoucherRequest(
                                        billing_guid = selectedLedgerGUID,
                                        vch_type = vchType,
                                        billing_name = selectedLedger,
                                        billing_mobile = "",
                                        billing_state = "",
                                        billing_country = "",
                                        billing_address = "",
                                        taxType = if (taxType == TaxType.EXTRA) 1 else 2,
                                        items = billingItems,
                                        sundries = selectedSundries,
                                        TranDate = selectedDate,
                                        Narration = narration, TransactionID = tranId
                                    ),
                                    onSuccess = {
                                        println(
                                            InventoryVoucherRequest(
                                                billing_guid = selectedLedgerGUID,
                                                vch_type = vchType,
                                                billing_name = selectedLedger,
                                                billing_mobile = "",
                                                billing_state = "",
                                                billing_country = "",
                                                billing_address = "",
                                                taxType = if (taxType == TaxType.EXTRA) 1 else 2,
                                                items = billingItems,
                                                sundries = selectedSundries,
                                                TranDate = selectedDate,
                                                Narration = narration, TransactionID = tranId
                                            )
                                        )
                                        showResultDialog = true
                                    },
                                    url = if (isEdit) "updateInventory" else "addInventoryVch"
                                )
                            },
                            enabled = selectedLedger.isNotEmpty() && selectedItems.isNotEmpty(),
                            label = if (isEdit) "Update" else "Create Invoice",
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )

                    }
                }
            }
        )
    }
}


@Composable
fun SmallAddButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(
    show: Boolean,
    title: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionOneBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetTwo(
    show: Boolean,
    title: String,
    options: List<Pair<String, String>>,
    onSelect: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionBottomSheet(
        showBottomSheet = show,
        list = options,
        onSelected = { it ->
            it.let {
                onSelect(it.first, it.second)

            }
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetThree(
    show: Boolean,
    title: String,
    options: List<Triple<String, String, Int>>,
    onSelect: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionBottomSheetThree(
        showBottomSheet = show,
        list = options,
        onSelected = { it ->
            it.let {
                onSelect(it.first, it.second, it.third)

            }
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheetItem(
    show: Boolean,
    title: String,
    options: List<Products>,
    onSelect: (Products) -> Unit,
    onDismiss: () -> Unit
) {
    TransactionItemBottomList(
        showBottomSheet = show,
        list = options,
        onSelected = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = { onDismiss() },
        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        title = title
    )
}

@Composable
fun SectionCard(
    title: String,
    count: Int,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.6.sp
                    )
                    if (count > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$count",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (headerAction != null) {
                    headerAction()
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CompactItemCard(
    item: InvoiceItem,
    gstPercentage: Double,
    taxType: TaxType,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    index: Int
) {
    val displayedTotal = remember(item, gstPercentage, taxType) {
        if (taxType == TaxType.EXTRA) {
            val taxable = item.price * item.qty
            taxable + taxable * gstPercentage / 100.0
        } else {
            item.price * item.qty
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(index + 1)}. ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        QuantitySelector(
                            qty = item.qty,
                            onDecrease = { onQuantityChange((item.qty - 1).coerceAtLeast(1)) },
                            onIncrease = { onQuantityChange(item.qty + 1) }
                        )
                        Text(
                            text = "x ${formatTwo(item.price)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = " ${formatTwo(displayedTotal)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.widthIn(min = 70.dp),
                            textAlign = TextAlign.End
                        )

                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                }
            }


        }
    }
}

@Composable
fun QuantitySelector(
    qty: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "$qty",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExpandedItemEditor(
    name: String,
    defaultListPrice: Double,
    gstPercentage: Double,
    initialQuantity: Int = 1,
    initialDiscount: Double = 0.0,
    taxType: TaxType,
    onAdd: (qty: Int, unitPrice: Double, discount: Double, listPriceText: Double, taxable: Double, gstAmount: Double, net: Double) -> Unit,
    onCancel: () -> Unit
) {
    var qtyText by rememberSaveable { mutableStateOf(initialQuantity.toString()) }
    var priceText by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var listPriceText by rememberSaveable { mutableStateOf("") }
    var discountText by rememberSaveable { mutableStateOf("") }

    // Track if user has manually entered amount
    var isAmountManuallyEntered by rememberSaveable { mutableStateOf(false) }

    if (defaultListPrice != 0.0 && listPriceText.isEmpty()) {
        listPriceText = defaultListPrice.toString()
    }
    if (initialDiscount != 0.0 && discountText.isEmpty()) {
        discountText = initialDiscount.toString()
    }

    fun parseDoubleSafe(s: String): Double {
        val filtered = s.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        val valid = if (dotCount > 1) {
            filtered.substringBefore('.') + "." + filtered.substringAfter('.').replace(".", "")
        } else filtered
        return valid.toDoubleOrNull() ?: 0.0
    }

    val qty = parseDoubleSafe(qtyText).toInt().coerceAtLeast(0)
    val listPrice = parseDoubleSafe(if (listPriceText == "") "0.0" else listPriceText)
    val discount = parseDoubleSafe(if (discountText == "") "0.0" else discountText)

    val computedUnit = listPrice * (1 - discount / 100)

    val enteredUnit = if (priceText.isBlank()) computedUnit else parseDoubleSafe(priceText)

    val taxableAmount: Double
    val gstAmount: Double
    val netAmount: Double

    if (taxType == TaxType.EXTRA) {
        taxableAmount = enteredUnit * qty
        gstAmount = taxableAmount * gstPercentage / 100.0
        netAmount = taxableAmount + gstAmount
    } else {
        val grossAmount = enteredUnit * qty
        taxableAmount =
            if (gstPercentage == 0.0) grossAmount else (grossAmount * 100.0 / (100.0 + gstPercentage))
        gstAmount = grossAmount - taxableAmount
        netAmount = grossAmount
    }

    val unitPriceToStore = enteredUnit

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    )
    {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            )
            {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Configure item before adding",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Qty",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = qtyText,
                        onValueChange = { new ->
                            qtyText = new.filter { it.isDigit() }.ifEmpty { "" }

                            // Recalculate amount when qty changes and amount was manually entered
                            if (isAmountManuallyEntered && amountText.isNotBlank()) {
                                val newQty = parseDoubleSafe(qtyText).toInt().coerceAtLeast(1)
                                val amount = parseDoubleSafe(amountText)
                                listPriceText = if (newQty > 0) formatTwo(amount / newQty) else ""
                                priceText = listPriceText
                            }
                        },
                        keyboardType = KeyboardType.Number
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "List Price.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = listPriceText,
                        onValueChange = { new ->
                            listPriceText = new.filter { it.isDigit() || it == '.' }
                            // Reset amount manual entry flag when list price is directly edited
                            isAmountManuallyEntered = false
                            priceText = ""
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = discountText,
                        onValueChange = { new ->
                            val raw = new.filter { it.isDigit() || it == '.' }
                            val value = raw.toDoubleOrNull() ?: 0.0
                            val clamped = value.coerceAtMost(100.0)
                            discountText = if (raw.isEmpty()) "" else clamped.toString()

                            // If discount is changed after amount entry, reset the flag
                            if (isAmountManuallyEntered) {
                                isAmountManuallyEntered = false
                                priceText = ""
                            }
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BorderedInput(
                        value = if (isAmountManuallyEntered && amountText.isNotBlank()) {
                            amountText
                        } else {
                            priceText.ifBlank { formatTwo(computedUnit * qty) }
                        },
                        onValueChange = { new ->
                            val filtered = new.filter { it.isDigit() || it == '.' }
                            amountText = filtered

                            // When amount is entered, set discount to 0 and calculate list price
                            if (filtered.isNotBlank()) {
                                isAmountManuallyEntered = true
                                discountText = "0"

                                val amount = parseDoubleSafe(filtered)
                                val currentQty = parseDoubleSafe(qtyText).toInt().coerceAtLeast(1)

                                listPriceText = if (currentQty > 0) {
                                    formatTwo(amount / currentQty)
                                } else {
                                    ""
                                }
                                priceText = listPriceText
                            } else {
                                isAmountManuallyEntered = false
                                priceText = ""
                            }
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Taxable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " ${formatTwo(taxableAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GST %",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatTwo(gstPercentage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GST Amt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " ${formatTwo(gstAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " ${formatTwo(netAmount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val finalQty = qty.coerceAtLeast(0)

                    if (finalQty > 0) {
                        onAdd(
                            finalQty,
                            unitPriceToStore,
                            discount,
                            listPrice,
                            taxableAmount,
                            gstAmount,
                            netAmount
                        )
                    }
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun SundryCard(
    sundry: SundryItem,
    onAmountChange: (Double) -> Unit,
    onRemove: () -> Unit,
    index: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${(index + 1)}. ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = sundry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.width(110.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        BasicTextField(
                            value = if (sundry.amount == 0.0) "" else sundry.amount.toString(),
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    onAmountChange(0.0)
                                } else {
                                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                                    val dotCount = filtered.count { it == '.' }
                                    val validInput = if (dotCount > 1) {
                                        filtered.substringBefore('.') + "." +
                                                filtered.substringAfter('.').replace(".", "")
                                    } else {
                                        filtered
                                    }
                                    onAmountChange(validInput.toDoubleOrNull() ?: 0.0)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            decorationBox = { innerTextField ->
                                if (sundry.amount == 0.0) {
                                    Text(
                                        text = "0.00",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                            ),
                                            textAlign = TextAlign.End
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SubtotalRow(label: String, amount: Double) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatTwo(amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TaxTypeSelector(
    selectedTaxType: TaxType,
    onTaxTypeSelected: (TaxType) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TAX TYPE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TaxTypeOption(
                    label = "Tax Inclusive",
                    selected = selectedTaxType == TaxType.INCLUSIVE,
                    onClick = { onTaxTypeSelected(TaxType.INCLUSIVE) },
                    modifier = Modifier.weight(1f)
                )
                TaxTypeOption(
                    label = "Tax Extra",
                    selected = selectedTaxType == TaxType.EXTRA,
                    onClick = { onTaxTypeSelected(TaxType.EXTRA) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TaxTypeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BorderedInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text, isEnabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItemBottomList(
    showBottomSheet: Boolean,
    list: List<Products>,
    onSelected: (Products) -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState,
    title: String = "Select Account",
    itemContent: @Composable ((String) -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }


    val filteredList = remember(list, query) {
        if (query.isBlank()) {
            list
        } else {
            val startsWith = list.filter { item ->
                item.Name?.startsWith(query, ignoreCase = true) ?: false
            }
            val contains = list.filter { item ->
                !item.Name?.startsWith(query, ignoreCase = true)!! &&
                        item.Name.contains(query, ignoreCase = true)
            }
            startsWith + contains
        }
    }


    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = bottomSheetState,
            dragHandle = null,
            sheetGesturesEnabled = false,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    Modifier.padding(bottom = 12.dp),
                    DividerDefaults.Thickness,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    TallySearchBar(
                        searchQuery = query,
                        onQueryChange = { query = it },
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (query.isNotBlank()) {
                        item {
                            Text(
                                text = "${filteredList.size} result${if (filteredList.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = if (query.isBlank()) "No items available" else "No results found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(filteredList) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelected(item)
                                    onDismiss()
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                if (itemContent != null) {
                                    itemContent(item.Name.toString())
                                } else {
                                    Text(
                                        text = item.Name.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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