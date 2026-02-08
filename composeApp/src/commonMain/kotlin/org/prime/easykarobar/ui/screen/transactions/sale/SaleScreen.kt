package org.prime.easykarobar.ui.screen.transactions.sale

import CurrentDate
import TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.expect.BarcodeScannerLauncher
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.rememberBarcodeScanner
import org.prime.easykarobar.data.model.transactions.BillingItem
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.model.transactions.TransportDetails
import org.prime.easykarobar.ui.printing.salesHtml
import org.prime.easykarobar.ui.screen.transactions.SelectLedgerRow
import org.prime.easykarobar.ui.screen.transactions.TallyNarrationField
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.getItemMasters
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.isBusy
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
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
    val qty: Int = 0,
    val priceType: Int = 0,
    val discountPercentage: Double,
    val taxCategoryCode: Int,
    val gstPercentage: Double,
    //sdfasdfasdfadsfsadf
    val taxable: Double,
    val gstAmt: Double,
    val net: Double,
//    val salePrice:Double,
//    val purchasePrice: Double,
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
    val isEdit: Boolean = false, val enableUpdateButton: Boolean = true
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val isSale = name in listOf("Sale Order", "Sale Invoice", "Sale Return")

        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow

        var selectedLedger by rememberSaveable { mutableStateOf("") }
        var barcodeQty by rememberSaveable { mutableStateOf("") }
        var selectedLedgerGUID by rememberSaveable { mutableStateOf("") }
        var narration by rememberSaveable { mutableStateOf("") }
        var selectedDate by rememberSaveable { mutableStateOf(CurrentDate()) }
        var taxType by rememberSaveable { mutableStateOf(TaxType.INCLUSIVE) }

        var selectedItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var selectedSundries by remember { mutableStateOf<List<SundryItem>>(emptyList()) }

        var showLedgerSheet by rememberSaveable { mutableStateOf(false) }
        var showQtyPopup by rememberSaveable { mutableStateOf(false) }
        var showItemSheet by rememberSaveable { mutableStateOf(false) }
        var showWarningMessage by rememberSaveable { mutableStateOf(false) }
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
        var showEmptyBarcode by remember { mutableStateOf(false) }
        var displayItemName by remember { mutableStateOf("") }
        var showAddMorePopup by remember { mutableStateOf(false) }
        var scannerLauncher by remember {
            mutableStateOf<BarcodeScannerLauncher?>(null)
        }
        var focusedSundryGuid by remember { mutableStateOf<String?>(null) }

        var showTransportDetails by remember { mutableStateOf(isEdit) }
        var transportName by remember { mutableStateOf("") }
        var gstRrNo by remember { mutableStateOf("") }
        var vehicleNo by remember { mutableStateOf("") }
        var station by remember { mutableStateOf("") }
        var pincode by remember { mutableStateOf("") }
        var gstRrDate by remember { mutableStateOf(CurrentDate()) }
        var shareLoading by remember { mutableStateOf(false) }


        scannerLauncher = rememberBarcodeScanner(
            onResult = { text ->
                val product = db.productsQueries
                    .getItemByName(text?.trim())
                    .executeAsOneOrNull()

                if (product == null) {
                    showEmptyBarcode = true
                } else {
                    showQtyPopup = false

                    selectedItems = selectedItems + InvoiceItem(
                        name = product.Name ?: "",
                        price = product.SalesPrice ?: 0.0,
                        qty = barcodeQty.toInt(),
                        discountPercentage = 0.0,
                        listPrice = 0.0,
                        taxable = 0.0,
                        gstAmt = 0.0,
                        net = 0.0,
                        guid = product.GUID ?: pendingSelectedProductGUID.orEmpty(),
                        gstPercentage = 0.0,
                        taxCategoryCode = product.TaxCategoryCode?.toInt() ?: 0
                    )
                    displayItemName = product.Name.toString()
                    showAddMorePopup = true
                }
            }
        )

        if (showAddMorePopup) {
            TallyAlertBox(
                title = "Item Added Successfully (${displayItemName})",
                message = "Do you want to add more?",
                confirmButtonText = "Yes",
                cancelButtonText = "No",
                onConfirm = {
                    showAddMorePopup = false
                    scannerLauncher?.launch()
                },
                onCancel = { showAddMorePopup = false },
                onDismiss = { showAddMorePopup = false },
            )
        }



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

        if (showQtyPopup) {
            TallyAlertBox(
                title = "Enter Quantity",
                confirmButtonText = "OK",
                cancelButtonText = "Cancel",
                onConfirm = {

                    scannerLauncher?.launch()

                },
                onCancel = { showQtyPopup = false },
                onDismiss = { showQtyPopup = false },
                content = {
                    TallyTextField(
                        value = barcodeQty,
                        onValueChange = { barcodeQty = it },
                        placeholder = "Enter Quantity",
                        isPassword = false,
                        isNumber = true,
                        label = "Barcode Qty",
                    )
                }
            )
        }


        val itemsTotal by derivedStateOf {
            selectedItems.sumOf { item ->
                if (taxType == TaxType.EXTRA) {
                    val taxable = item.price * item.qty
                    taxable + taxable * item.gstPercentage / 100.0
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
                        net = it.total_amt.toDouble(), gstPercentage = it.tax_rate1.toDouble()
                        //TODO: FIX IT LATER
                        , taxCategoryCode = 0
                    )
                }
                // PRE-POPULATE SUNDRIES SECTION
                selectedSundries = data.sundries.map { s ->
                    SundryItem(
                        name = s.name,
                        amount = s.amount,
                        guid = s.guid,
                        i1 = s.i1,
                        i2 = s.i2,
                        d2 = s.d2,
                        rate = s.rate,
                        srno = s.srno, percentValue = s.percentValue
                    )
                }

                transportName = data.other_info.transportName
                gstRrNo = data.other_info.gstNum
                vehicleNo = data.other_info.vehicleNum
                station = data.other_info.station
                pincode = data.other_info.pincode
                gstRrDate = data.other_info.grDate
            }
        }
        val sundriesTotal = selectedSundries.fold(0.0) { runningTotal, sundry ->
            val baseAmount = itemsTotal + runningTotal

            val sundryValue = if (sundry.i2 == 1) {
                baseAmount * (sundry.amount / 100.0)
            } else {
                sundry.amount
            }

            when (sundry.i1) {
                0 -> runningTotal - sundryValue
                1 -> runningTotal + sundryValue
                else -> runningTotal + sundryValue
            }
        }

        val grandTotal = itemsTotal + sundriesTotal

        if (state.isLoading) {
            TallyLoadingDialog(if (isEdit) "Editing transaction" else "Creating transaction")
        }

        LaunchedEffect(pendingSelectedProductName) {
            pendingSelectedProductName?.let { name ->
                val prod = itemsList.find { it.Name == name }
                val price = if (isSale) prod?.SalesPrice ?: 0.0 else prod?.PurcPrice ?: 0.0
                val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
//                selectedItems = selectedItems + InvoiceItem(
//                    name = name,
//                    price = price,
//                    qty = 1,
//                    discountPercentage = 0.0,
//                    listPrice = 0.0,
//                    taxable = 0.0,
//                    gstAmt = 0.0,
//                    net = 0.0,
//                    guid = pendingSelectedProductGUID ?: "",
//                    gstPercentage = 0.0,
//                    taxCategoryCode = taxCategoryCode.toInt()
//                )
                editingItem =
                    InvoiceItem(
                        name = name,
                        price = price,
                        qty = 1,
                        discountPercentage = 0.0,
                        listPrice = 0.0,
                        taxable = 0.0,
                        gstAmt = 0.0,
                        net = 0.0,
                        guid = pendingSelectedProductGUID ?: "",
                        gstPercentage = 0.0,
                        taxCategoryCode = taxCategoryCode.toInt()
                    )
                showItemSheet = false
                pendingSelectedProductName = null
            }
        }



        if (showEmptyBarcode) {
            TallyResultDialog(
                message = "Barcode not found",
                onDone = { showEmptyBarcode = false },
                isSuccess = false
            )
        }
        val htmlContent = salesHtml(
            name = name,
            partyName = selectedLedger,
            invoiceNo = oneState.data?.AutoVchNo.toString(),
            date = selectedDate,
            items = selectedItems,
            sundries = selectedSundries,
            grandTotal = grandTotal,
            transportDetails = org.prime.easykarobar.ui.printing.TransportDetails(
                transportName = transportName,
                gstRrNo = gstRrNo,
                vehicleNo = vehicleNo,
                station = station,
                pincode = pincode,
                gstRrDate = gstRrDate
            )
        )
        TallyReportScaffold(
            showBurgerMenu = isEdit,
            showBarcodeIcon = !isEdit,
            onBarcodeClick = {
                showQtyPopup = true

            },
            menuItems = listOf(
                MenuItemData(Icons.Default.Download, "Download", {
                    scope.launch {
                        handlePdfAction(
                            fileName = name,
                            htmlContent = htmlContent,
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }),
                MenuItemData(Icons.Default.Share, "Share", {
                    scope.launch {
                        handlePdfAction(
                            fileName = name,
                            htmlContent = htmlContent,
                            action = PdfAction.Share,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }),
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

                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val pending = editingItem
                                    val product = itemsList.find { it.Name == pending?.name }

                                    if (pending != null) {
                                        val gst = try {
                                            db.taxCategoryMastQueries
                                                .selectTaxRate(
                                                    pending.taxCategoryCode.toString(),
                                                    selectedDate
                                                )
                                                .executeAsOneOrNull()
                                                ?: 18.0
                                        } catch (e: Exception) {
                                            println(e.message)
                                            0.0
                                        }
                                        if (product != null) {
                                            ExpandedItemEditor1(
                                                name = product.Name ?: pending.name,
                                                defaultListPrice = if (pending.listPrice == 0.0) if (isSale) product.SalesPrice
                                                    ?: 0.0 else product.PurcPrice
                                                    ?: 0.0 else pending.listPrice,
                                                initialQuantity = pending.qty,
                                                initialDiscount = pending.discountPercentage,
                                                taxType = taxType,
                                                onAdd = { qty, unitPrice, discount, listPriceText, taxable, gstAmount, net, gstPercentage ->
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
                                                            ?: "",
                                                        gstPercentage = gstPercentage,
                                                        taxCategoryCode = product.TaxCategoryCode?.toInt()
                                                            ?: 0
                                                    )
                                                    editingItem = null
                                                },
                                                onBack = {
                                                    selectedItems = selectedItems + pending
                                                    editingItem = null
                                                },
                                                gstPercentage = gst,
                                                onCancel = {
                                                    selectedItems = selectedItems - pending
                                                    editingItem = null
                                                }
                                            )
                                        } else {
                                            ExpandedItemEditor1(
                                                name = pending.name,
                                                defaultListPrice = pending.listPrice,
                                                initialDiscount = pending.discountPercentage,
                                                initialQuantity = pending.qty,
                                                taxType = taxType,
                                                onAdd = { qty, unitPrice, discount, listPriceText, taxable, gstAmount, net, gstPercentage ->
                                                    selectedItems = selectedItems + InvoiceItem(
                                                        name = pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        gstAmt = gstAmount,
                                                        net = net,
                                                        guid = pendingSelectedProductGUID ?: "",
                                                        gstPercentage = gstPercentage,
                                                        taxCategoryCode = product?.TaxCategoryCode?.toInt()
                                                            ?: 0
                                                    )
                                                    editingItem = null
                                                },
                                                onBack = {
                                                    selectedItems = selectedItems + pending
                                                    editingItem = null
                                                },
                                                gstPercentage = gst,
                                                onCancel = {
                                                    selectedItems = selectedItems - pending
                                                    editingItem = null
                                                }
                                            )
                                        }
                                    }
                                    selectedItems.forEachIndexed { index, item ->
                                        CompactItemCard(
                                            index = index,
                                            item = item,
                                            gstPercentage = item.gstPercentage,
                                            taxType = taxType,
                                            onQuantityChange = { newQty ->
                                                selectedItems =
                                                    selectedItems.mapIndexed { index1, item1 ->
                                                        if (index1 == index) item1.copy(qty = newQty) else item1
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SmallAddButton(
                                            label = "Add More Item",
                                            enabled = editingItem == null
                                        ) {
                                            showItemSheet = true
                                        }
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

                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    var cumulativeTotal = itemsTotal

                                    selectedSundries.forEachIndexed { index, sundry ->
                                        SundryCard(
                                            index = index,
                                            sundry = sundry,
                                            runningTotal = cumulativeTotal,
                                            onAmountChange = { newAmount, rate, srno, valueToBeSent ->
                                                selectedSundries =
                                                    selectedSundries.mapIndexed { i, it ->
                                                        if (i == index) it.copy(
                                                            amount = newAmount,
                                                            rate = rate,
                                                            srno = srno,
                                                            percentValue = valueToBeSent
                                                        ) else it
                                                    }
                                            },
                                            onRemove = {
                                                selectedSundries = selectedSundries - sundry
                                            },
                                            shouldFocus = sundry.guid == focusedSundryGuid,
                                            onFocusConsumed = { focusedSundryGuid = null },
                                        )

                                        // Update cumulative total for next iteration
                                        val sundryValue = if (sundry.i2 == 1) {
                                            // It's a percentage - calculate based on current total
                                            cumulativeTotal * (sundry.amount / 100.0)
                                        } else {
                                            // It's a fixed amount
                                            sundry.amount
                                        }

                                        cumulativeTotal = when (sundry.i1) {
                                            0 -> cumulativeTotal - sundryValue  // Subtract
                                            1 -> cumulativeTotal + sundryValue  // Add
                                            else -> cumulativeTotal + sundryValue
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SmallAddButton(label = if (isBusy()) "Add More Sundry" else "Add More Ledger") {
                                            showSundrySheet = true
                                        }
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

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Transport Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = { showTransportDetails = !showTransportDetails }
                                ) {
                                    Icon(
                                        imageVector = if (showTransportDetails) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showTransportDetails) "Remove" else "Add",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = showTransportDetails,
                                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                                    animationSpec = tween(300)
                                ),
                                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                                    animationSpec = tween(300)
                                )
                            ) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Section Header
                                        Text(
                                            text = "Transportation Information",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 1.dp
                                        )

                                        // Transport Name - Full Width
                                        TallyTextField(
                                            value = transportName,
                                            onValueChange = { transportName = it },
                                            label = "Transport Name",
                                            placeholder = "Enter transport name",
                                            isPassword = false,
                                            isNumber = false,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // Station - Full Width
                                        TallyTextField(
                                            value = station,
                                            onValueChange = { station = it },
                                            label = "Station",
                                            placeholder = "Enter station name",
                                            isPassword = false,
                                            isNumber = false,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        // 2x2 Grid for remaining fields
                                        // First Row - GST/RR No & Vehicle No
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            TallyTextField(
                                                value = gstRrNo,
                                                onValueChange = { gstRrNo = it },
                                                label = "GST/RR No.",
                                                placeholder = "Enter number",
                                                isPassword = false,
                                                isNumber = false,
                                                modifier = Modifier.weight(1f)
                                            )

                                            TallyTextField(
                                                value = vehicleNo,
                                                onValueChange = { vehicleNo = it },
                                                label = "Vehicle No.",
                                                placeholder = "Enter vehicle no.",
                                                isPassword = false,
                                                isNumber = false,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        // Second Row - Pincode & GR/RR Date
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            TallyTextField(
                                                value = pincode,
                                                onValueChange = { pincode = it },
                                                label = "Pincode",
                                                placeholder = "Enter pincode",
                                                isPassword = false,
                                                isNumber = true,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Box(modifier = Modifier.weight(1f)) {
                                                TallyDatePickerRow(
                                                    label = "GR/RR Date",
                                                    selectedDate = gstRrDate,
                                                    onDateSelected = { gstRrDate = it },
                                                    defaultDate = CurrentDate()
                                                )
                                            }
                                        }
                                    }
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
                            SundryItem(
                                name = it.Name.toString(),
                                amount = 0.0,
                                guid = it.GUID.toString(),
                                i1 = it.I1?.toInt() ?: 0,
                                i2 = it.I2?.toInt() ?: 0,
                                d2 = it.D2?.toInt() ?: 0,
                                //TODO: FIX TH IS
                                rate = 0.0,
                                srno = 0, percentValue = 0.0
                            )
                        },
                        onSelect = { item ->
                            if (!selectedSundries.any { it.name == item.name }) {
                                val newItem = SundryItem(
                                    item.name,
                                    0.0,
                                    guid = item.guid,
                                    i1 = item.i1,
                                    i2 = item.i2,
                                    d2 = item.d2,
                                    rate = item.rate,
                                    srno = item.srno,
                                    percentValue = item.percentValue
                                )
                                selectedSundries = selectedSundries + newItem
                                focusedSundryGuid = newItem.guid   // 👈 THIS
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
                                val newItem = SundryItem(
                                    sundryName,
                                    0.0,
                                    guid = GUID,
                                    i1 = 0,
                                    i2 = 0,
                                    d2 = 0,
                                    rate = 0.0,
                                    srno = 0,
                                    percentValue = 0.0
                                )
                                selectedSundries = selectedSundries + newItem
                                focusedSundryGuid = GUID   // 👈 THIS
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
                        fun createO() {
                            val billingItems = selectedItems.map { item ->
                                val prod = itemsList.find { it.Name == item.name }
                                BillingItem(
                                    product_id = prod?.ID?.toString() ?: "",
                                    product_name = item.name,
                                    quantity = item.qty,
                                    list_price = item.listPrice,
                                    discount_percent = item.discountPercentage,
                                    discount_amt = null,
                                    tax_rate1 = item.gstPercentage,
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
                                    Narration = narration,
                                    TransactionID = tranId,
                                    total_amt = grandTotal, transportDetails = TransportDetails(
                                        transportName = transportName,
                                        station = station,
                                        gstNum = gstRrNo,
                                        vehicleNum = vehicleNo,
                                        pincode = pincode,
                                        grDate = gstRrDate
                                    )
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
                                            Narration = narration,
                                            TransactionID = tranId,
                                            total_amt = grandTotal,
                                            transportDetails = TransportDetails(
                                                transportName = transportName,
                                                station = station,
                                                gstNum = gstRrNo,
                                                vehicleNum = vehicleNo,
                                                pincode = pincode,
                                                grDate = gstRrDate
                                            )
                                        )
                                    )
                                    showResultDialog = true
                                },
                                url = if (isEdit) "updateInventory" else "addInventoryVch"
                            )
                        }

                        TallyButton(

                            onClick = {

                                if (grandTotal < 0.0) {
                                    showWarningMessage = true
                                } else {
                                    createO()
                                }

                            },
                            enabled = if (isEdit) enableUpdateButton else selectedLedger.isNotEmpty() && selectedItems.isNotEmpty(),
                            label = if (isEdit) "Update" else "Create Invoice",
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )
                        if (showWarningMessage) {
                            TallyAlertBox(
                                title = "Warning",
                                message = "Do you want to save negative voucher??",
                                confirmButtonText = "Yes",
                                cancelButtonText = "No",
                                onConfirm = {
                                    showWarningMessage = false
                                    createO()
                                },
                                onCancel = { showWarningMessage = false },
                                onDismiss = { showWarningMessage = false },
                            )
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun SmallAddButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = "")
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
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

// SundryCard Component
@Composable
fun SundryCard(
    sundry: SundryItem,
    onAmountChange: (Double, Double, Int, Double) -> Unit, // Now returns (amount, rate, srno)
    onRemove: () -> Unit,
    index: Int,
    runningTotal: Double, shouldFocus: Boolean,
    onFocusConsumed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    val isPercentage = sundry.i2 == 1

    var textValue by remember(sundry.name) {
        mutableStateOf(sundry.d2.takeIf { it.toDouble() != 0.0 }?.toString() ?: "")
    }

    LaunchedEffect(sundry.amount) {
        val amountStr = if (sundry.amount == 0.0) "" else sundry.amount.toString()
        if (amountStr != textValue) {
            textValue = amountStr
        }
    }

    val displayValue = textValue.toDoubleOrNull() ?: 0.0

    val calculatedAmount = if (isPercentage) {
        runningTotal * (displayValue / 100.0)
    } else {
        displayValue
    }

    // Calculate rate: if percentage, return the percentage value, else 0.0
    val rate = if (isPercentage) displayValue else 0.0
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column {
                    Text(
                        text = sundry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (displayValue != 0.0) {
                        Text(
                            text = if (isPercentage) {
                                "${if (sundry.i1 == 0) "-" else "+"}${formatTwo(calculatedAmount)}"
                            } else {
                                "${if (sundry.i1 == 0) "-" else "+"}${formatTwo(displayValue)}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        BasicTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    textValue = ""
                                    onAmountChange(0.0, 0.0, index + 1, 0.0)
                                } else {
                                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                                    val dotCount = filtered.count { it == '.' }
                                    val validInput = if (dotCount > 1) {
                                        filtered.substringBefore('.') + "." +
                                                filtered.substringAfter('.').replace(".", "")
                                    } else {
                                        filtered
                                    }

                                    textValue = validInput
                                    val parsedValue = validInput.toDoubleOrNull() ?: 0.0

                                    val finalValue = if (isPercentage) {
                                        rate
                                    } else {
                                        parsedValue
                                    }


                                    val finalRate = if (isPercentage) finalValue else 0.0
                                    onAmountChange(
                                        finalValue,
                                        finalRate,
                                        index + 1,
                                        calculatedAmount
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (textValue.isEmpty()) {
                                            Text(
                                                text = if (isPercentage) "0%" else "0.00",
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
                                    if (isPercentage && textValue.isNotEmpty()) {
                                        Text(
                                            text = "%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(start = 2.dp)
                                        )
                                    }
                                }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    isEnabled: Boolean = true
) {
    // Maintain internal TextFieldValue to handle selection
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Sync external value changes
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value, selection = TextRange(value.length))
        }
    }

    // Select all text when focused
    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) textFieldValue.text.length else 0
        textFieldValue = textFieldValue.copy(
            selection = TextRange(
                start = 0,
                end = endRange
            )
        )
    }

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
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            enabled = isEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            interactionSource = interactionSource,
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

@Composable
fun ExpandedItemEditor1(
    name: String,
    defaultListPrice: Double,
    gstPercentage: Double,
    initialQuantity: Int? = 0,
    taxType: TaxType,
    initialDiscount: Double,
    onAdd: (
        qty: Int,
        unitPrice: Double,
        discount: Double,
        listPrice: Double,
        taxable: Double,
        gstAmount: Double,
        net: Double,
        gstPercentage: Double
    ) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {

    var qtyN by remember { mutableStateOf(if (initialQuantity == 0) "" else initialQuantity.toString()) }
    var listPriceN by remember { mutableStateOf(defaultListPrice.toString()) }
    var discountN by remember { mutableStateOf(initialDiscount.toString()) }
    var amountN by remember { mutableStateOf("") }

    var editMode by remember { mutableStateOf(PriceEditMode.LIST_PRICE) }
    var isAmountManuallyEdited by remember { mutableStateOf(false) }

    val qtyValue = qtyN.toIntOrNull()?.takeIf { it > 0 } ?: 0

    /* ------------------ CORE PRICE LOGIC ------------------ */

    val unitPrice by remember {
        derivedStateOf {
            when (editMode) {

                PriceEditMode.LIST_PRICE,
                PriceEditMode.DISCOUNT -> {
                    val lp = listPriceN.toDoubleOrNull() ?: 0.0
                    val dis = discountN.toDoubleOrNull() ?: 0.0
                    lp - (lp * dis / 100.0)
                }

                PriceEditMode.AMOUNT -> {
                    if (!isAmountManuallyEdited || qtyValue == 0) return@derivedStateOf 0.0

                    val amt = amountN.toDoubleOrNull() ?: 0.0
                    val price = amt / qtyValue

                    // REQUIRED behavior
                    discountN = "0"
                    listPriceN = price.toString()

                    price
                }
            }
        }
    }

    val amount by remember {
        derivedStateOf {
            if (qtyValue > 0) unitPrice * qtyValue else 0.0
        }
    }

    /* ------------------ TAX CALCULATION ------------------ */

    val taxableAmount: Double
    val gstAmount: Double
    val netAmount: Double

    if (taxType == TaxType.EXTRA) {
        taxableAmount = amount
        gstAmount = taxableAmount * gstPercentage / 100.0
        netAmount = taxableAmount + gstAmount
    } else {
        if (gstPercentage == 0.0) {
            taxableAmount = amount
            gstAmount = 0.0
            netAmount = amount
        } else {
            taxableAmount = amount * 100.0 / (100.0 + gstPercentage)
            gstAmount = amount - taxableAmount
            netAmount = amount
        }
    }

    val isAddEnabled = qtyValue > 0

    /* ------------------ UI ------------------ */

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Configure item before adding",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }

            // Inputs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                // Qty
                Column(modifier = Modifier.weight(1f)) {
                    Text("Qty", style = MaterialTheme.typography.labelSmall)
                    BorderedInput(
                        value = qtyN,
                        onValueChange = { qtyN = it.filter(Char::isDigit) },
                        keyboardType = KeyboardType.Number
                    )
                }

                // List Price
                Column(modifier = Modifier.weight(1f)) {
                    Text("List Price", style = MaterialTheme.typography.labelSmall)
                    BorderedInput(
                        value = listPriceN,
                        onValueChange = {
                            isAmountManuallyEdited = false
                            editMode = PriceEditMode.LIST_PRICE
                            listPriceN = it
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }

                // Discount
                Column(modifier = Modifier.weight(1f)) {
                    Text("Discount %", style = MaterialTheme.typography.labelSmall)
                    BorderedInput(
                        value = discountN,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            val value = filtered.toDoubleOrNull()
                            if (value == null || value <= 100.0) {
                                isAmountManuallyEdited = false
                                editMode = PriceEditMode.DISCOUNT
                                discountN = filtered
                            }
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }

                // Amount
                Column(modifier = Modifier.weight(1f)) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall)
                    BorderedInput(
                        value = if (isAmountManuallyEdited) amountN else formatTwo(amount),
                        onValueChange = {
                            isAmountManuallyEdited = true
                            editMode = PriceEditMode.AMOUNT
                            amountN = it
                        },
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            // Summary
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryCell("Taxable", taxableAmount)
                SummaryCell("GST %", gstPercentage, suffix = "%")
                SummaryCell("GST Amt", gstAmount)
                SummaryCell("Net", netAmount, highlight = true)
            }

            // Actions
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = isAddEnabled,
                    onClick = {
                        onAdd(
                            qtyValue,
                            unitPrice,
                            discountN.toDoubleOrNull() ?: 0.0,
                            listPriceN.toDoubleOrNull() ?: 0.0,
                            taxableAmount,
                            gstAmount,
                            netAmount, gstPercentage
                        )
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}


@Composable
fun RowScope.SummaryCell(
    label: String,
    value: Double,
    suffix: String = "",
    highlight: Boolean = false
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = buildString {
                append(formatTwo(value))
                append(suffix)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (highlight)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}


enum class PriceEditMode {
    LIST_PRICE,
    DISCOUNT,
    AMOUNT
}
