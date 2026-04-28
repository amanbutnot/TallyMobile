package org.prime.easykarobar.ui.screen.transactions.sale


// ─────────────────────────────────────────────────────────────────────────────
// CHANGED: SaleScreen2 — Multi-select item bottom sheet
// All other functions (formatTwo, InvoiceItem, TaxType, SmallAddButton,
// SectionCard, CompactItemCard, QuantitySelector, SundryCard, SubtotalRow,
// TaxTypeSelector, TaxTypeOption, BorderedInput, TransactionItemBottomList,
// ExpandedItemEditor1, SummaryCell, PriceEditMode, makeNegativeConditional,
// applyCompoundDiscount) are UNCHANGED — do not re-declare them.
// ─────────────────────────────────────────────────────────────────────────────

import CurrentDate
import TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.launch
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.expect.BarcodeScanResult
import org.prime.easykarobar.data.expect.BarcodeScannerLauncher
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.rememberBarcodeScanner
import org.prime.easykarobar.data.model.hasSalesmanPermission
import org.prime.easykarobar.data.model.transactions.BillingItem
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.model.transactions.TransportDetails
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.salesHtml
import org.prime.easykarobar.ui.screen.transactions.BillByBillModel
import org.prime.easykarobar.ui.screen.transactions.SelectLedgerRow
import org.prime.easykarobar.ui.screen.transactions.TransactionBillBottomSheet
import org.prime.easykarobar.ui.shared.composables.DownloadResultDialog
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyAlertBox
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallyResultDialog
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getItemMasters
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.isBusy
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.SerialNumberBottomSheet
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.Products
import org.tally.Products_Pricing
import org.tally.SerialNoEnterReport
import yymmdd
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


// ─────────────────────────────────────────────────────────────────────────────
// NEW: Data class for a pending multi-select item (holds qty before confirm)
// ─────────────────────────────────────────────────────────────────────────────
data class PendingMultiItem(
    val product: Products,
    var qty: String = "1"
)

// ─────────────────────────────────────────────────────────────────────────────
// SaleScreen2 — identical to SaleScreen except item-selection uses multi-select
// ─────────────────────────────────────────────────────────────────────────────
data class SaleScreen2(
    val name: String,
    val vchType: Int,
    val tranId: Int? = null,
    val selectedLedger: String? = null,
    val selectedLedgerGUID: String? = null,
    val isEdit: Boolean = false,
    val enableUpdateButton: Boolean = true
) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class, InternalVoyagerApi::class, ExperimentalUuidApi::class,
        ExperimentalMaterial3ExpressiveApi::class
    )
    @Composable
    override fun Content() {
        var showExitPopup by remember { mutableStateOf(false) }
        var showSerialNumberBottomSheet by remember { mutableStateOf(false) }
        BackHandler(true) { showExitPopup = true }

        val isSale = name in listOf("Sale Order", "Sale Invoice", "Sale Return")
        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow

        var selectedLedger by remember { mutableStateOf(selectedLedger ?: "") }
        var barcodeQty by remember { mutableStateOf("") }
        var selectedLedgerGUID by remember { mutableStateOf(selectedLedgerGUID ?: "") }
        var narration by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf(CurrentDate()) }
        var taxType by remember { mutableStateOf(TaxType.INCLUSIVE) }

        var selectedItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var selectedSundries by remember { mutableStateOf<List<SundryItem>>(emptyList()) }

        var showLedgerSheet by remember { mutableStateOf(false) }
        var showQtyPopup by remember { mutableStateOf(false) }
        var showItemSheet by remember { mutableStateOf(false) }
        var showWarningMessage by remember { mutableStateOf(false) }
        var showSundrySheet by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }
        var showGroupFilterSheet by remember { mutableStateOf(false) }

        var editingItem by remember { mutableStateOf<InvoiceItem?>(null) }

        var showProductPricingSheet by remember { mutableStateOf(false) }
        var selectedPricing by remember { mutableStateOf<ProductPricing?>(null) }
        var productPricingList by remember { mutableStateOf<List<Products_Pricing>>(emptyList()) }
        var selectedProductForPricing by remember { mutableStateOf("") }
        var pendingItemsAfterMultiSelect by remember {
            mutableStateOf<List<PendingMultiItem>>(
                emptyList()
            )
        }
        productPricingList = db.productsPricingQueries.selectAll().executeAsList()

        var pendingSelectedProductName by remember { mutableStateOf<String?>(null) }
        var pendingSelectedProductGUID by remember { mutableStateOf<String?>(null) }
        var selectedInitialSerialNo by remember {
            mutableStateOf<List<SerialNoEnterReport>>(
                emptyList()
            )
        }
        var editingItemIndex by remember { mutableStateOf<Int?>(null) }

        val ledgerList = getLedgerMasters(db)
        val busyLedgerList = db.bSMasterQueries.selectAll().executeAsList()
        val itemsList = getItemMasters(db)
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        var selectedSerialNo by remember { mutableStateOf<List<String>>(emptyList()) }
        var serialNoTotal by remember { mutableStateOf(0.0) }
        val groupFilteredList = if (selectedGroups.isEmpty()) itemsList
        else itemsList.filter { it.GroupName in getProductsGroupCodesByName(selectedGroups) }

        val productGroups = remember {
            db.productGroupMasterQueries.selectAll(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes()
            ).executeAsList()
        }

        val viewmodel: InventoryVoucherViewModel = viewModel { InventoryVoucherViewModel() }
        val state by viewmodel.dataState
        val oneState by viewmodel.oneState
        val deleteState by viewmodel.deleteState
        val scope = rememberCoroutineScope()
        var uniqueId by remember { mutableStateOf("") }

        var showDeleteDialog by remember { mutableStateOf(false) }
        var showEmptyBarcode by remember { mutableStateOf(false) }
        var displayItemName by remember { mutableStateOf("") }
        var showAddMorePopup by remember { mutableStateOf(false) }
        var scannerLauncher by remember { mutableStateOf<BarcodeScannerLauncher?>(null) }
        var focusedSundryGuid by remember { mutableStateOf<String?>(null) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var showTransportDetails by remember { mutableStateOf(false) }
        var transportName by remember { mutableStateOf("") }
        var gstRrNo by remember { mutableStateOf("") }
        var vehicleNo by remember { mutableStateOf("") }
        var station by remember { mutableStateOf("") }
        var pincode by remember { mutableStateOf("") }
        var demoBarcodeName by remember { mutableStateOf("") }
        var gstRrDate by remember { mutableStateOf(CurrentDate()) }
        var SshowShippingDetails by remember { mutableStateOf(false) }
        var SbillingShipping by remember { mutableStateOf(false) }
        var SselectedBilling by remember { mutableStateOf("") }

        val optionalFields = remember { mutableStateListOf(*Array(20) { "" }) }
        var showOptionalField by remember { mutableStateOf(false) }

        var SpartyName by remember { mutableStateOf("") }
        var Saddress1 by remember { mutableStateOf("") }
        var Saddress2 by remember { mutableStateOf("") }
        var Saddress3 by remember { mutableStateOf("") }
        var Saddress4 by remember { mutableStateOf("") }
        var SshipState by remember { mutableStateOf("") }
        var SmobileNo by remember { mutableStateOf("") }
        var Semail by remember { mutableStateOf("") }
        var SitPan by remember { mutableStateOf("") }
        var SadharNo by remember { mutableStateOf("") }
        var SgstIn by remember { mutableStateOf("") }
        var shareLoading by remember { mutableStateOf(false) }
        var selectedReferences by remember { mutableStateOf(listOf<BillByBillModel>()) }
        var showBillModalSheet by remember { mutableStateOf(false) }

        if (showExitPopup) TallyAlertBox(
            title = "Exit?",
            message = "Do you want to exit",
            confirmButtonText = "Yes",
            cancelButtonText = "No",
            onConfirm = { nav.pop() },
            onCancel = { showExitPopup = false },
            onDismiss = { showExitPopup = false },
        )

        selectedLedgerGUID = ledgerList.find { l -> l.Name == selectedLedger }?.GUID ?: ""

        scannerLauncher = rememberBarcodeScanner { result ->
            demoBarcodeName = result.toString()
            when (result) {
                is BarcodeScanResult.Cancelled -> {
                    showQtyPopup = false; return@rememberBarcodeScanner
                }

                is BarcodeScanResult.Failure -> {
                    showEmptyBarcode = true; return@rememberBarcodeScanner
                }

                is BarcodeScanResult.Success -> {
                    val barcode = result.value.trim()
                    if (barcode.isEmpty()) {
                        showEmptyBarcode = true; return@rememberBarcodeScanner
                    }
                    val product = db.productsQueries.getItemByName(
                        barcode, groupCodes = itemGroupCodes(), filterGroup = filterItemGroups()
                    ).executeAsOneOrNull()
                    if (product == null) {
                        showEmptyBarcode = true; return@rememberBarcodeScanner
                    }
                    showQtyPopup = false
                    val price = if (isSale) product.SalesPrice ?: 0.0 else product.PurcPrice ?: 0.0
                    val qty = barcodeQty.toIntOrNull() ?: 1
                    val gstPercentage = try {
                        db.taxCategoryMastQueries.selectTaxRate(
                            product.TaxCategoryCode?.toInt().toString(), selectedDate
                        ).executeAsOneOrNull() ?: 0.0
                    } catch (e: Exception) {
                        0.0
                    }
                    val taxableAmount: Double;
                    val gstAmount: Double;
                    val netAmount: Double
                    if (taxType == TaxType.EXTRA) {
                        taxableAmount = price * qty
                        gstAmount = taxableAmount * gstPercentage / 100.0
                        netAmount = taxableAmount + gstAmount
                    } else if (taxType == TaxType.VOUCHER) {
                        taxableAmount = price * qty; gstAmount = 0.0; netAmount = price * qty
                    } else {
                        if (gstPercentage == 0.0) {
                            taxableAmount = price * qty; gstAmount = 0.0; netAmount = price * qty
                        } else {
                            val amount = price * qty
                            taxableAmount = amount * 100.0 / (100.0 + gstPercentage)
                            gstAmount = amount - taxableAmount; netAmount = amount
                        }
                    }
                    selectedItems = selectedItems + InvoiceItem(
                        name = product.Name.orEmpty(), price = price, qty = qty,
                        discountPercentage = 0.0,
                        listPrice = if (isSale) product.SalesPrice ?: 0.0 else product.PurcPrice
                            ?: 0.0,
                        taxable = taxableAmount, gstAmt = gstAmount, net = netAmount,
                        guid = product.GUID ?: pendingSelectedProductGUID.orEmpty(),
                        gstPercentage = gstPercentage,
                        taxCategoryCode = product.TaxCategoryCode?.toInt() ?: 0, CD = ""
                    )
                    displayItemName = product.Name.orEmpty()
                    showAddMorePopup = true
                }
            }
        }

        GroupFilterBottomSheet(
            show = showGroupFilterSheet, items = productGroups,
            selectedItems = selectedGroups, itemNameSelector = { it.Name },
            onSelectedItemsChange = { selectedGroups = it },
            onDismiss = { showGroupFilterSheet = false }, bottomSheetState = bottomSheetState
        )

        if (showAddMorePopup) TallyAlertBox(
            title = "Item Added Successfully ($displayItemName)",
            message = "Do you want to add more?",
            confirmButtonText = "Yes", cancelButtonText = "No",
            onConfirm = { showAddMorePopup = false; scannerLauncher?.launch() },
            onCancel = { showAddMorePopup = false }, onDismiss = { showAddMorePopup = false },
        )

        if (deleteState.message != null) TallyResultDialog(
            message = deleteState.message ?: "Error Occurred",
            onDone = { nav.pop() }, isSuccess = deleteState.success, confirmText = "Done"
        )

        if (showDeleteDialog) TallyAlertBox(
            title = "Delete",
            message = "Are you sure you want to delete this voucher",
            onConfirm = {
                scope.launch {
                    db.transaction {
                        selectedReferences.forEach {
                            db.voucherBillAllocationsQueries.deleteOldBillAllocation(
                                oneState.data?.uniqueID.toString()
                            )
                        }
                    }
                    tranId?.let { viewmodel.deleteInventoryVch(tranId = it, vchType = vchType) }
                }
            },
            onCancel = { showDeleteDialog = false }, onDismiss = { showDeleteDialog = false },
        )

        var qtyError by remember { mutableStateOf(false) }
        if (showQtyPopup) TallyAlertBox(
            title = "Enter Quantity",
            confirmButtonText = "OK", cancelButtonText = "Cancel",
            onConfirm = {
                if (barcodeQty.isBlank()) qtyError = true
                else {
                    qtyError = false; showQtyPopup = false; scannerLauncher?.launch()
                }
            },
            onCancel = { showQtyPopup = false }, onDismiss = { showQtyPopup = false },
            content = {
                Column {
                    TallyTextField(
                        value = barcodeQty,
                        onValueChange = { barcodeQty = it; if (it.isNotBlank()) qtyError = false },
                        placeholder = "Enter Quantity", isPassword = false,
                        isNumber = true, label = "Barcode Qty",
                    )
                    if (qtyError) Text(
                        text = "Quantity cannot be empty",
                        color = MaterialTheme.colorScheme.error, fontSize = 12.sp
                    )
                }
            })

        val itemsTotal by derivedStateOf {
            selectedItems.sumOf { item ->
                val effectivePrice = if (item.CD.isNotBlank() && item.CD.contains("+"))
                    applyCompoundDiscount(item.listPrice, item.CD) else item.price
                if (taxType == TaxType.EXTRA) {
                    val taxable = effectivePrice * item.qty
                    taxable + taxable * item.gstPercentage / 100.0
                } else effectivePrice * item.qty
            }
        }

        LaunchedEffect(tranId) { tranId?.let { viewmodel.getOneInventoryVoucher(it) } }

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
                        net = it.total_amt.toDouble(),
                        gstPercentage = it.tax_rate1.toDouble(),
                        taxCategoryCode = 0,
                        itemdesc1 = it.itemdesc1,
                        itemdesc2 = it.itemdesc2,
                        itemdesc3 = it.itemdesc3,
                        itemdesc4 = it.itemdesc4,
                        itemdesc5 = it.itemdesc5,
                        CD = it.CD,
                        itemdesc6 = it.itemdesc6,
                        itemdesc7 = it.itemdesc7,
                        itemdesc8 = it.itemdesc8,
                        itemdesc9 = it.itemdesc9,
                        itemdesc10 = it.itemdesc10,
                        itemdesc11 = it.itemdesc11,
                        itemdesc12 = it.itemdesc12,
                        itemdesc13 = it.itemdesc13,
                        itemdesc14 = it.itemdesc14,
                        itemdesc15 = it.itemdesc15,
                        itemdesc16 = it.itemdesc16,
                        itemdesc17 = it.itemdesc17,
                        itemdesc18 = it.itemdesc18,
                        itemdesc19 = it.itemdesc19,
                        itemdesc20 = it.itemdesc20,
                        additionalinfo = it.additionalinfo,
                        item_serial = it.item_serial.map { sn ->
                            SerialNoEnterReport(
                                SerialNo = sn,
                                MasterCode1 = it.product_id.toDoubleOrNull(),
                                MasterCode2 = "",
                                ProductName = it.product_name,
                                UnitName = null,
                                GroupName = null,
                                Value1 = 1.0,
                                Value2 = 0.0,
                                Value3 = 0.0
                            )
                        }
                    )
                }
                selectedSundries = data.sundries.map { s ->
                    SundryItem(
                        name = s.name, amount = s.amount, guid = s.guid,
                        i1 = s.i1, i2 = s.i2, d2 = s.d2, rate = s.rate,
                        srno = s.srno, percentValue = s.percentValue
                    )
                }
                transportName = data.other_info?.transportName ?: ""
                gstRrNo = data.other_info?.gstNum ?: ""
                vehicleNo = data.other_info?.vehicleNum ?: ""
                station = data.other_info?.station ?: ""
                pincode = data.other_info?.pincode ?: ""
                gstRrDate = data.other_info?.grDate ?: CurrentDate()
                SpartyName = data.other_info?.SpartyName ?: ""
                SadharNo = data.other_info?.Saadhar ?: ""
                Saddress1 = data.other_info?.Saddress1 ?: ""
                Saddress2 = data.other_info?.Saddress2 ?: ""
                Saddress3 = data.other_info?.Saddress3 ?: ""
                Saddress4 = data.other_info?.Saddress4 ?: ""
                SshipState = data.other_info?.SshipState ?: ""
                SmobileNo = data.other_info?.SmobileNo ?: ""
                Semail = data.other_info?.Semail ?: ""
                SitPan = data.other_info?.SitPan ?: ""
                optionalFields[0] = data.other_info?.OptionalField1 ?: ""
                optionalFields[1] = data.other_info?.OptionalField2 ?: ""
                optionalFields[2] = data.other_info?.OptionalField3 ?: ""
                optionalFields[3] = data.other_info?.OptionalField4 ?: ""
                optionalFields[4] = data.other_info?.OptionalField5 ?: ""
                optionalFields[5] = data.other_info?.OptionalField6 ?: ""
                optionalFields[6] = data.other_info?.OptionalField7 ?: ""
                optionalFields[7] = data.other_info?.OptionalField8 ?: ""
                optionalFields[8] = data.other_info?.OptionalField9 ?: ""
                optionalFields[9] = data.other_info?.OptionalField10 ?: ""
                optionalFields[10] = data.other_info?.OptionalField11 ?: ""
                optionalFields[11] = data.other_info?.OptionalField12 ?: ""
                optionalFields[12] = data.other_info?.OptionalField13 ?: ""
                optionalFields[13] = data.other_info?.OptionalField14 ?: ""
                optionalFields[14] = data.other_info?.OptionalField15 ?: ""
                optionalFields[15] = data.other_info?.OptionalField16 ?: ""
                optionalFields[16] = data.other_info?.OptionalField17 ?: ""
                optionalFields[17] = data.other_info?.OptionalField18 ?: ""
                optionalFields[18] = data.other_info?.OptionalField19 ?: ""
                optionalFields[19] = data.other_info?.OptionalField20 ?: ""
                SgstIn = data.other_info?.SgstIn ?: ""
                SbillingShipping = data.other_info?.SbillingShipping ?: false
                data.bills_collection?.let { selectedReferences = it }
                uniqueId = data.uniqueID.toString()
                selectedSerialNo = data.item_serial
            }
        }

        val sundriesTotal = selectedSundries.fold(0.0) { runningTotal, sundry ->
            val baseAmount = itemsTotal + runningTotal
            val sundryValue =
                if (sundry.i2 == 1) baseAmount * (sundry.amount / 100.0) else sundry.amount
            when (sundry.i1) {
                0 -> runningTotal - sundryValue; else -> runningTotal + sundryValue
            }
        }

        val grandTotal = itemsTotal + sundriesTotal

        LaunchedEffect(Unit) {
            productPricingList = db.productsPricingQueries.selectAll().executeAsList()
        }

        if (state.isLoading) TallyLoadingDialog(
            if (isEdit) "Editing transaction" else "Creating transaction"
        )

        if (showEmptyBarcode) TallyResultDialog(
            message = "Barcode not found \n Barcode Value $demoBarcodeName",
            onDone = { showEmptyBarcode = false }, isSuccess = false
        )

        val htmlContent = salesHtml(
            name = name, partyName = selectedLedger, partyGuid = selectedLedgerGUID,
            invoiceNo = oneState.data?.AutoVchNo.toString(), date = selectedDate,
            items = selectedItems, sundries = selectedSundries, grandTotal = grandTotal,
            transportDetails = org.prime.easykarobar.ui.printing.TransportDetails(
                transportName = transportName, gstRrNo = gstRrNo, vehicleNo = vehicleNo,
                station = station, pincode = pincode, gstRrDate = gstRrDate
            )
        )
        val menuList = buildList {
            add(MenuItemData(Icons.Default.Download, "Download", {
                scope.launch {
                    handlePdfAction(
                        fileName = CompanyName(),
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it })
                }
            }))
            add(MenuItemData(Icons.Default.Share, "Share", {
                scope.launch {
                    handlePdfAction(
                        fileName = CompanyName(),
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it })
                }
            }))
            if (enableUpdateButton || SharedPrefs.User.get()?.role == "admin") {
                add(
                    MenuItemData(Icons.Default.Delete, "Delete") { showDeleteDialog = true }

                )
            }
        }
        TallyReportScaffold(
            showBurgerMenu = isEdit,
            onBackClick = { showExitPopup = true },
            showBarcodeIcon = true,
            onBarcodeClick = { showQtyPopup = true },
            menuItems = menuList,
            title = if (isEdit) "Edit $name" else name,
            content = { paddingValues ->
                if (isEdit && oneState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

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
                                    SelectLedgerRow(
                                        selectedAccount = selectedLedger,
                                        onShowBottomSheet = { showLedgerSheet = true },
                                        title = "Party Ledger"
                                    )
                                }
                            }

                            TaxTypeSelector(
                                selectedTaxType = taxType,
                                onTaxTypeSelected = { taxType = it })

                            SectionCard(
                                title = "ITEMS", count = selectedItems.size,
                                headerAction = {
                                    TextButton(
                                        onClick = { showItemSheet = true },
                                        enabled = editingItem == null,
                                        contentPadding = PaddingValues(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Add more items",
                                            style = MaterialTheme.typography.labelLarge,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    }
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val pending = editingItem
                                    val product = itemsList.find { it.Name == pending?.name }

                                    if (pending != null) {
                                        val gst = try {
                                            if (isEdit) pending.gstPercentage
                                            else db.taxCategoryMastQueries.selectTaxRate(
                                                pending.taxCategoryCode.toString(), selectedDate
                                            ).executeAsOneOrNull() ?: 0.0
                                        } catch (e: Exception) {
                                            0.0
                                        }

                                        if (product != null) {
                                            ExpandedItemEditor1(
                                                name = product.Name ?: pending.name,
                                                defaultListPrice = if (pending.listPrice == 0.0)
                                                    if (isSale) product.SalesPrice
                                                        ?: 0.0 else product.PurcPrice ?: 0.0
                                                else pending.listPrice,
                                                initialQuantity = pending.qty,
                                                initialDiscount = pending.CD.ifBlank { pending.discountPercentage.toString() },
                                                taxType = taxType, existingItem = pending,
                                                initialSerialNumbers = pending.item_serial.map {
                                                    it.SerialNo ?: ""
                                                },
                                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, itemDescs, additionalInfos, serialNumbers ->
                                                    val newItem = InvoiceItem(
                                                        name = product.Name ?: pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        CD = compoundDiscount.toString(),
                                                        gstAmt = gstAmount,
                                                        net = net,
                                                        guid = product.GUID
                                                            ?: pendingSelectedProductGUID ?: "",
                                                        gstPercentage = gstPercentage,
                                                        taxCategoryCode = product.TaxCategoryCode?.toInt()
                                                            ?: 0,
                                                        itemdesc1 = itemDescs.getOrNull(0),
                                                        itemdesc2 = itemDescs.getOrNull(1),
                                                        itemdesc3 = itemDescs.getOrNull(2),
                                                        itemdesc4 = itemDescs.getOrNull(3),
                                                        itemdesc5 = itemDescs.getOrNull(4),
                                                        itemdesc6 = itemDescs.getOrNull(5),
                                                        itemdesc7 = itemDescs.getOrNull(6),
                                                        itemdesc8 = itemDescs.getOrNull(7),
                                                        itemdesc9 = itemDescs.getOrNull(8),
                                                        itemdesc10 = itemDescs.getOrNull(9),
                                                        itemdesc11 = itemDescs.getOrNull(10),
                                                        itemdesc12 = itemDescs.getOrNull(11),
                                                        itemdesc13 = itemDescs.getOrNull(12),
                                                        itemdesc14 = itemDescs.getOrNull(13),
                                                        itemdesc15 = itemDescs.getOrNull(14),
                                                        itemdesc16 = itemDescs.getOrNull(15),
                                                        itemdesc17 = itemDescs.getOrNull(16),
                                                        itemdesc18 = itemDescs.getOrNull(17),
                                                        itemdesc19 = itemDescs.getOrNull(18),
                                                        itemdesc20 = itemDescs.getOrNull(19),
                                                        additionalinfo = additionalInfos.getOrNull(0),
                                                        item_serial = serialNumbers.map { sn ->
                                                            SerialNoEnterReport(
                                                                SerialNo = sn,
                                                                MasterCode1 = product?.GUID?.toDoubleOrNull(),
                                                                MasterCode2 = "",
                                                                ProductName = pending.name,
                                                                UnitName = null,
                                                                GroupName = null,
                                                                Value1 = 1.0,
                                                                Value2 = 0.0,
                                                                Value3 = 0.0
                                                            )
                                                        }
                                                    )
                                                    val insertAt =
                                                        editingItemIndex ?: selectedItems.size
                                                    val mutable = selectedItems.toMutableList()
                                                    mutable.add(insertAt, newItem)
                                                    selectedItems = mutable
                                                    editingItemIndex = null
                                                    editingItem = null
                                                },
                                                onBack = {
                                                    val insertAt =
                                                        editingItemIndex ?: selectedItems.size
                                                    val mutable = selectedItems.toMutableList()
                                                    mutable.add(insertAt, pending)
                                                    selectedItems = mutable
                                                    editingItemIndex = null
                                                    editingItem = null
                                                },
                                                gstPercentage = gst,
                                                onCancel = {
                                                    editingItemIndex = null
                                                    editingItem = null
                                                }
                                            )
                                        } else {
                                            ExpandedItemEditor1(
                                                name = pending.name,
                                                defaultListPrice = pending.listPrice,
                                                initialDiscount = pending.CD.ifBlank { pending.discountPercentage.toString() },
                                                initialQuantity = pending.qty,
                                                taxType = taxType, existingItem = pending,
                                                initialSerialNumbers = pending.item_serial.map {
                                                    it.SerialNo ?: ""
                                                },
                                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, itemDescs, additionalInfos, serialNumbers ->
                                                    val newItem = InvoiceItem(
                                                        name = pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        CD = compoundDiscount.toString(),
                                                        gstAmt = gstAmount,
                                                        net = net,
                                                        guid = pendingSelectedProductGUID ?: "",
                                                        gstPercentage = gstPercentage,
                                                        taxCategoryCode = product?.TaxCategoryCode?.toInt()
                                                            ?: 0,
                                                        itemdesc1 = itemDescs.getOrNull(0),
                                                        itemdesc2 = itemDescs.getOrNull(1),
                                                        itemdesc3 = itemDescs.getOrNull(2),
                                                        itemdesc4 = itemDescs.getOrNull(3),
                                                        itemdesc5 = itemDescs.getOrNull(4),
                                                        itemdesc6 = itemDescs.getOrNull(5),
                                                        itemdesc7 = itemDescs.getOrNull(6),
                                                        itemdesc8 = itemDescs.getOrNull(7),
                                                        itemdesc9 = itemDescs.getOrNull(8),
                                                        itemdesc10 = itemDescs.getOrNull(9),
                                                        itemdesc11 = itemDescs.getOrNull(10),
                                                        itemdesc12 = itemDescs.getOrNull(11),
                                                        itemdesc13 = itemDescs.getOrNull(12),
                                                        itemdesc14 = itemDescs.getOrNull(13),
                                                        itemdesc15 = itemDescs.getOrNull(14),
                                                        itemdesc16 = itemDescs.getOrNull(15),
                                                        itemdesc17 = itemDescs.getOrNull(16),
                                                        itemdesc18 = itemDescs.getOrNull(17),
                                                        itemdesc19 = itemDescs.getOrNull(18),
                                                        itemdesc20 = itemDescs.getOrNull(19),
                                                        additionalinfo = additionalInfos.getOrNull(0),
                                                        item_serial = serialNumbers.map { sn ->
                                                            SerialNoEnterReport(
                                                                SerialNo = sn,
                                                                MasterCode1 = product?.GUID?.toDoubleOrNull(),
                                                                MasterCode2 = "",
                                                                ProductName = pending.name,
                                                                UnitName = null,
                                                                GroupName = null,
                                                                Value1 = 1.0,
                                                                Value2 = 0.0,
                                                                Value3 = 0.0
                                                            )
                                                        }
                                                    )
                                                    val insertAt =
                                                        editingItemIndex ?: selectedItems.size
                                                    val mutable = selectedItems.toMutableList()
                                                    mutable.add(insertAt, newItem)
                                                    selectedItems = mutable
                                                    editingItemIndex = null
                                                    editingItem = null
                                                },
                                                onBack = {
                                                    val insertAt =
                                                        editingItemIndex ?: selectedItems.size
                                                    val mutable = selectedItems.toMutableList()
                                                    mutable.add(insertAt, pending)
                                                    selectedItems = mutable
                                                    editingItemIndex = null
                                                    editingItem = null
                                                },
                                                gstPercentage = gst,
                                                onCancel = {
                                                    editingItemIndex = null
                                                    editingItem = null
                                                }
                                            )
                                        }
//                                                onCancel = { selectedItems = selectedItems - pending; editingItem = null }
//                                            )
//                                        }
                                    }

                                    selectedItems.forEachIndexed { index, item ->
                                        CompactItemCard(
                                            index = index, item = item,
                                            gstPercentage = item.gstPercentage, taxType = taxType,
                                            onQuantityChange = { newQty ->
                                                selectedItems =
                                                    selectedItems.mapIndexed { index1, item1 ->
                                                        if (index1 == index) {
                                                            val newTaxableAmount: Double;
                                                            val newGstAmount: Double;
                                                            val newNetAmount: Double
                                                            if (taxType == TaxType.EXTRA) {
                                                                newTaxableAmount =
                                                                    item1.price * newQty
                                                                newGstAmount =
                                                                    newTaxableAmount * item1.gstPercentage / 100.0
                                                                newNetAmount =
                                                                    newTaxableAmount + newGstAmount
                                                            } else if (taxType == TaxType.VOUCHER) {
                                                                newTaxableAmount =
                                                                    item1.price * newQty
                                                                newGstAmount = 0.0; newNetAmount =
                                                                    item1.price * newQty
                                                            } else {
                                                                if (item1.gstPercentage == 0.0) {
                                                                    newTaxableAmount =
                                                                        item1.price * newQty
                                                                    newGstAmount =
                                                                        0.0; newNetAmount =
                                                                        item1.price * newQty
                                                                } else {
                                                                    val amount =
                                                                        item1.price * newQty
                                                                    newTaxableAmount =
                                                                        amount * 100.0 / (100.0 + item1.gstPercentage)
                                                                    newGstAmount =
                                                                        amount - newTaxableAmount; newNetAmount =
                                                                        amount
                                                                }
                                                            }
                                                            item1.copy(
                                                                qty = newQty,
                                                                taxable = newTaxableAmount,
                                                                gstAmt = newGstAmount,
                                                                net = newNetAmount
                                                            )
                                                        } else item1
                                                    }
                                            },
                                            onRemove = { selectedItems = selectedItems - item },
                                            onEdit = {
                                                if (editingItem == null) {
                                                    editingItemIndex = index
                                                    editingItem = item
                                                    selectedItems = selectedItems - item
                                                }
                                            },
                                            onSerialNo = {
                                                if (editingItem == null) {
                                                    editingItemIndex = index
                                                    editingItem = item
                                                    selectedItems = selectedItems - item
                                                    pendingSelectedProductGUID = item.guid
                                                    pendingSelectedProductName = item.name
                                                    selectedInitialSerialNo = item.item_serial
                                                    showSerialNumberBottomSheet = true
                                                }
                                            }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = {
                                            showGroupFilterSheet = true
                                        }) { Text("Group Filter") }
                                    }
                                    if (selectedItems.isNotEmpty()) SubtotalRow(
                                        "Subtotal",
                                        itemsTotal
                                    )
                                }
                            }

                            SectionCard(
                                title = if (isBusy()) "SUNDRIES" else "Ledgers",
                                count = selectedSundries.size, headerAction = {}
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    var cumulativeTotal = itemsTotal
                                    selectedSundries.forEachIndexed { index, sundry ->
                                        SundryCard(
                                            index = index, sundry = sundry,
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
                                        val sundryValue = if (sundry.i2 == 1)
                                            cumulativeTotal * (sundry.amount / 100.0) else sundry.amount
                                        cumulativeTotal = when (sundry.i1) {
                                            0 -> cumulativeTotal - sundryValue; else -> cumulativeTotal + sundryValue
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
                                    if (selectedSundries.isNotEmpty()) SubtotalRow(
                                        "Subtotal",
                                        sundriesTotal
                                    )
                                }
                            }

                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    TallyTextField(
                                        value = narration,
                                        onValueChange = { narration = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        imeAction = ImeAction.Done,
                                        label = "Narration",
                                        isNumber = false,
                                        isPassword = false,
                                        placeholder = "Narration"
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Transport Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = {
                                    showTransportDetails = !showTransportDetails
                                }) {
                                    Icon(
                                        imageVector = if (showTransportDetails) Icons.Default.RemoveCircleOutline
                                        else Icons.Default.AddCircleOutline,
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
                                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                            ) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                        .padding(bottom = 16.dp),
                                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            "Transportation Information",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 1.dp
                                        )
                                        TallyTextField(
                                            value = transportName,
                                            onValueChange = { transportName = it },
                                            label = "Transport Name",
                                            placeholder = "Enter transport name",
                                            isPassword = false,
                                            isNumber = false,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        TallyTextField(
                                            value = station,
                                            onValueChange = { station = it },
                                            label = "Station",
                                            placeholder = "Enter station name",
                                            isPassword = false,
                                            isNumber = false,
                                            modifier = Modifier.fillMaxWidth()
                                        )
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
                                                isNumber = false,
                                                imeAction = ImeAction.Done,
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

                            ShippingCard(
                                showShippingDetails = SshowShippingDetails,
                                onShowChange = { SshowShippingDetails = !SshowShippingDetails },
                                billingShipping = SbillingShipping,
                                onBillingShippingChange = { SbillingShipping = it },
                                partyName = SpartyName,
                                onPartyNameChange = { SpartyName = it },
                                address1 = Saddress1,
                                onAddressChange1 = { if (it.length < 40) Saddress1 = it },
                                address2 = Saddress2,
                                onAddressChange2 = { if (it.length < 40) Saddress2 = it },
                                address3 = Saddress3,
                                onAddressChange3 = { if (it.length < 40) Saddress3 = it },
                                address4 = Saddress4,
                                onAddressChange4 = { if (it.length < 40) Saddress4 = it },
                                state = SshipState,
                                onStateChange = { SshipState = it },
                                mobileNo = SmobileNo,
                                onMobileChange = { SmobileNo = it },
                                email = Semail,
                                onEmailChange = { Semail = it },
                                itPan = SitPan,
                                onPanChange = { SitPan = it },
                                gstIn = SgstIn,
                                onGstChange = { SgstIn = it },
                                adharNo = SadharNo,
                                onAdharChange = { SadharNo = it },
                                onbillingShippingSelected = { SselectedBilling = it },
                                selectedBilling = SselectedBilling
                            )

                            if (vchType !in listOf(12, 13, 15)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Bill By Bill Reference",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = { showBillModalSheet = true },
                                        enabled = selectedLedgerGUID != "" && itemsList.isNotEmpty()
                                    ) {
                                        Icon(
                                            imageVector = if (showBillModalSheet) Icons.Default.RemoveCircleOutline
                                            else Icons.Default.AddCircleOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showBillModalSheet) "Remove" else "Add",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }

                            TransactionBillBottomSheet(
                                cm1 = selectedLedger,
                                showBottomSheet = showBillModalSheet,
                                ledgerGuid = selectedLedgerGUID,
                                initialSelectedBills = selectedReferences,
                                onBillsSelected = { selectedReferences = it },
                                onDismiss = { showBillModalSheet = false },
                                bottomSheetState = rememberModalBottomSheetState(
                                    skipPartiallyExpanded = true
                                ),
                                title = "Bill by Bill",
                                totalAmount = grandTotal,
                                isEdit = isEdit,
                                uniqueId = uniqueId,
                                vchType = vchType
                            )

                            OptionalFieldCard(
                                showOptionalField = showOptionalField,
                                onShowChange = { showOptionalField = !showOptionalField },
                                optionalFields = optionalFields,
                                onFieldChange = { index, value -> optionalFields[index] = value }
                            )
                        }
                    }
                }

                // ── Ledger sheet (unchanged) ──────────────────────────────────────
                SelectionSheet(
                    show = showLedgerSheet, title = "Select Party Ledger",
                    options = ledgerList.filter { it.L1 == 1.0 || it.L2 == 1.0 || it.L3 == 1.0 }
                        .map { it.Name ?: "" },
                    onSelect = { selected ->
                        selectedLedger = selected
                        selectedLedgerGUID = ledgerList.find { l -> l.Name == selected }?.GUID ?: ""
                    },
                    onDismiss = { showLedgerSheet = false }
                )

                // ── CHANGED: Multi-select item bottom sheet ───────────────────────
                MultiSelectItemSheet(
                    show = showItemSheet,
                    title = "Select Items",
                    options = groupFilteredList,
                    isSale = isSale,
                    onConfirm = { pendingItems ->
                        showItemSheet = false
                        pendingItemsAfterMultiSelect = pendingItems
                    },
                    onDismiss = { showItemSheet = false }
                )

                LaunchedEffect(pendingItemsAfterMultiSelect) {
                    if (pendingItemsAfterMultiSelect.isNotEmpty()) {
                        val pending = pendingItemsAfterMultiSelect.first()
                        val prod = itemsList.find { it.Name == pending.product.Name }
                        val perms = SharedPrefs.Permissions.get()
                        val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
                        val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
                        val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L
                        val excludeGuids =
                            if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
                        val godownCodes =
                            if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()

                        val serialList = db.productSerialNoQueries.serialNoEnterReport(
                            filterGroup = filterGroup, groupCodes = filterItemGroupCodes(),
                            filterExclude = filterExclude, excludeGuids = excludeGuids,
                            filterGodown = filterGodown, godownCodes = godownCodes,
                            filterSingle = 1L,
                            includeSingle = pending.product.GUID?.toDoubleOrNull() ?: 0.0,
                            filterSingleG = 0L, includeSingleG = ""
                        ).executeAsList()

                        val pricing =
                            productPricingList.filter { it.GUID.toDouble() == pending.product.GUID?.toDoubleOrNull() }

                        val currentLedger = ledgerList.find { it.GUID == selectedLedgerGUID }
                        val pricingLevel = if (isSale) currentLedger?.L6 ?: 100.0 else currentLedger?.L7 ?: 100.0
                        val autoPricing = if (pricingLevel != 100.0) {
                            pricing.find { it.Srno == pricingLevel.toLong() }
                        } else null

                        if (autoPricing != null) {
                            val price = if (isSale) autoPricing.SalesPrice ?: 0.0 else autoPricing.SalesPrice ?: 0.0
                            val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
                            val gstPct = try {
                                db.taxCategoryMastQueries.selectTaxRate(
                                    taxCategoryCode.toInt().toString(), selectedDate
                                ).executeAsOneOrNull() ?: 0.0
                            } catch (e: Exception) {
                                0.0
                            }
                            val qty = pending.qty.toIntOrNull()?.coerceAtLeast(1) ?: 1

                            val taxableAmt: Double
                            val gstAmt: Double
                            val netAmt: Double
                            if (taxType == TaxType.EXTRA) {
                                taxableAmt = price * qty
                                gstAmt = taxableAmt * gstPct / 100.0
                                netAmt = taxableAmt + gstAmt
                            } else if (taxType == TaxType.VOUCHER) {
                                taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                            } else {
                                if (gstPct == 0.0) {
                                    taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                                } else {
                                    val amount = price * qty
                                    taxableAmt = amount * 100.0 / (100.0 + gstPct)
                                    gstAmt = amount - taxableAmt; netAmt = amount
                                }
                            }

                            selectedItems = selectedItems + InvoiceItem(
                                name = pending.product.Name.orEmpty(),
                                price = price, qty = qty,
                                discountPercentage = autoPricing.Disc ?: 0.0, listPrice = price,
                                taxable = taxableAmt, gstAmt = gstAmt, net = netAmt,
                                guid = pending.product.GUID.orEmpty(),
                                gstPercentage = gstPct,
                                taxCategoryCode = taxCategoryCode.toInt(), CD = autoPricing.Disc.toString()
                            )
                            pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                        } else if (pricing.isNotEmpty()) {
                            selectedProductForPricing = pending.product.Name.orEmpty()
                            pendingSelectedProductGUID = pending.product.GUID
                            showProductPricingSheet = true
                        } else if (serialList.isNotEmpty()) {
                            pendingSelectedProductName = pending.product.Name
                            pendingSelectedProductGUID = pending.product.GUID
                            showSerialNumberBottomSheet = true
                        } else {
                            // No special handling needed, add directly
                            val price =
                                if (isSale) prod?.SalesPrice ?: 0.0 else prod?.PurcPrice ?: 0.0
                            val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
                            val gstPct = try {
                                db.taxCategoryMastQueries.selectTaxRate(
                                    taxCategoryCode.toInt().toString(), selectedDate
                                ).executeAsOneOrNull() ?: 0.0
                            } catch (e: Exception) {
                                0.0
                            }
                            val qty = pending.qty.toIntOrNull()?.coerceAtLeast(1) ?: 1

                            val taxableAmt: Double
                            val gstAmt: Double
                            val netAmt: Double
                            if (taxType == TaxType.EXTRA) {
                                taxableAmt = price * qty
                                gstAmt = taxableAmt * gstPct / 100.0
                                netAmt = taxableAmt + gstAmt
                            } else if (taxType == TaxType.VOUCHER) {
                                taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                            } else {
                                if (gstPct == 0.0) {
                                    taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                                } else {
                                    val amount = price * qty
                                    taxableAmt = amount * 100.0 / (100.0 + gstPct)
                                    gstAmt = amount - taxableAmt; netAmt = amount
                                }
                            }

                            selectedItems = selectedItems + InvoiceItem(
                                name = pending.product.Name.orEmpty(),
                                price = price, qty = qty,
                                discountPercentage = 0.0, listPrice = price,
                                taxable = taxableAmt, gstAmt = gstAmt, net = netAmt,
                                guid = pending.product.GUID.orEmpty(),
                                gstPercentage = gstPct,
                                taxCategoryCode = taxCategoryCode.toInt(), CD = ""
                            )
                            pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                        }
                    }
                }

                ProductPricingBottomSheet(
                    show = showProductPricingSheet,
                    productName = selectedProductForPricing,
                    pricingList = productPricingList.filter { it.GUID.toDouble() == pendingSelectedProductGUID?.toDoubleOrNull() }
                        .map {
                            ProductPricing(
                                Guid = it.GUID,
                                ProductName = if (it.Srno >= 101) ('A'.code + (it.Srno - 101).toInt()).toChar()
                                    .toString() else it.GUID,
                                SerialNo = it.Srno.toString(),
                                VchType = it.VchType.toInt(),
                                SalePrice = it.SalesPrice ?: 0.0,
                                PurchasePrice = it.SalesPrice ?: 0.0,
                                Discount = it.Disc ?: 0.0,
                                CompoundDiscount = it.Disc.toString()
                            )
                        },
                    onSelect = { pricing ->
                        selectedPricing = pricing
                        showProductPricingSheet = false

                        val pending = pendingItemsAfterMultiSelect.first()
                        val prod = itemsList.find { it.Name == pending.product.Name }
                        val perms = SharedPrefs.Permissions.get()
                        val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
                        val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
                        val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L
                        val excludeGuids =
                            if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
                        val godownCodes =
                            if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()

                        val serialList = db.productSerialNoQueries.serialNoEnterReport(
                            filterGroup = filterGroup, groupCodes = filterItemGroupCodes(),
                            filterExclude = filterExclude, excludeGuids = excludeGuids,
                            filterGodown = filterGodown, godownCodes = godownCodes,
                            filterSingle = 1L,
                            includeSingle = pending.product.GUID?.toDoubleOrNull() ?: 0.0,
                            filterSingleG = 0L, includeSingleG = ""
                        ).executeAsList()

                        if (serialList.isNotEmpty()) {
                            pendingSelectedProductName = pending.product.Name
                            pendingSelectedProductGUID = pending.product.GUID
                            showSerialNumberBottomSheet = true
                        } else {
                            val price = if (isSale) pricing.SalePrice else pricing.PurchasePrice
                            val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
                            val gstPct = try {
                                db.taxCategoryMastQueries.selectTaxRate(
                                    taxCategoryCode.toInt().toString(), selectedDate
                                ).executeAsOneOrNull() ?: 0.0
                            } catch (e: Exception) {
                                0.0
                            }
                            val qty = pending.qty.toIntOrNull()?.coerceAtLeast(1) ?: 1

                            val taxableAmt: Double
                            val gstAmt: Double
                            val netAmt: Double
                            if (taxType == TaxType.EXTRA) {
                                taxableAmt = price * qty
                                gstAmt = taxableAmt * gstPct / 100.0
                                netAmt = taxableAmt + gstAmt
                            } else if (taxType == TaxType.VOUCHER) {
                                taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                            } else {
                                if (gstPct == 0.0) {
                                    taxableAmt = price * qty; gstAmt = 0.0; netAmt = price * qty
                                } else {
                                    val amount = price * qty
                                    taxableAmt = amount * 100.0 / (100.0 + gstPct)
                                    gstAmt = amount - taxableAmt; netAmt = amount
                                }
                            }

                            selectedItems = selectedItems + InvoiceItem(
                                name = pending.product.Name.orEmpty(),
                                price = price, qty = qty,
                                discountPercentage = pricing.Discount,
                                listPrice = price,
                                taxable = taxableAmt, gstAmt = gstAmt, net = netAmt,
                                guid = pending.product.GUID.orEmpty(),
                                gstPercentage = gstPct,
                                taxCategoryCode = taxCategoryCode.toInt(),
                                CD = pricing.CompoundDiscount
                            )
                            selectedPricing = null
                            pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                        }
                    },
                    onDismiss = {
                        showProductPricingSheet = false
                        selectedPricing = null
                        pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                    },
                    bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                )

                SerialNumberBottomSheet(
                    productGuid = pendingSelectedProductGUID.toString(),
                    show = showSerialNumberBottomSheet,
                    initialSelectedSerialNumbers = selectedInitialSerialNo.map {
                        it.SerialNo ?: ""
                    },
                    onDismiss = {
                        if (editingItemIndex != null && editingItem != null) {
                            val list = selectedItems.toMutableList()
                            list.add(editingItemIndex!!, editingItem!!)
                            selectedItems = list
                        }
                        showSerialNumberBottomSheet = false
                        selectedInitialSerialNo = emptyList()
                        editingItemIndex = null
                        editingItem = null
                        pendingSelectedProductName = null
                        pendingSelectedProductGUID = null
                        selectedPricing = null
                        if (pendingItemsAfterMultiSelect.isNotEmpty()) {
                            pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                        }
                    },
                    onSerialNumbersSelected = { selectedList ->
                        val total = selectedList.sumOf { it.Value3 ?: 0.0 }
                        val prod = itemsList.find { it.Name == pendingSelectedProductName }
                        val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
                        val gstPct = try {
                            db.taxCategoryMastQueries.selectTaxRate(
                                taxCategoryCode.toInt().toString(), selectedDate
                            ).executeAsOneOrNull() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }

                        val pricePerUnit = if (selectedPricing != null) {
                            if (isSale) selectedPricing!!.SalePrice else selectedPricing!!.PurchasePrice
                        } else if (total > 0) {
                            total / selectedList.size
                        } else {
                            editingItem?.price ?: (if (isSale) prod?.SalesPrice
                                ?: 0.0 else prod?.PurcPrice ?: 0.0)
                        }

                        val discPct =
                            selectedPricing?.Discount ?: editingItem?.discountPercentage ?: 0.0
                        val compDisc = selectedPricing?.CompoundDiscount ?: editingItem?.CD ?: ""

                        val qty = selectedList.size.coerceAtLeast(1)

                        val taxableAmt: Double
                        val gstAmt: Double
                        val netAmt: Double

                        if (taxType == TaxType.EXTRA) {
                            taxableAmt = pricePerUnit * qty
                            gstAmt = taxableAmt * gstPct / 100.0
                            netAmt = taxableAmt + gstAmt
                        } else if (taxType == TaxType.VOUCHER) {
                            taxableAmt = pricePerUnit * qty
                            gstAmt = 0.0
                            netAmt = pricePerUnit * qty
                        } else {
                            if (gstPct == 0.0) {
                                taxableAmt = pricePerUnit * qty
                                gstAmt = 0.0
                                netAmt = pricePerUnit * qty
                            } else {
                                val amount = pricePerUnit * qty
                                taxableAmt = amount * 100.0 / (100.0 + gstPct)
                                gstAmt = amount - taxableAmt
                                netAmt = amount
                            }
                        }

                        val updatedItem = InvoiceItem(
                            name = pendingSelectedProductName ?: "",
                            price = pricePerUnit,
                            qty = qty,
                            discountPercentage = discPct,
                            listPrice = pricePerUnit,
                            taxable = taxableAmt,
                            gstAmt = gstAmt,
                            net = netAmt,
                            guid = pendingSelectedProductGUID ?: editingItem?.guid ?: "",
                            gstPercentage = gstPct,
                            taxCategoryCode = taxCategoryCode.toInt(),
                            CD = compDisc,
                            item_serial = selectedList
                        )

                        val list = selectedItems.toMutableList()
                        if (editingItemIndex != null) {
                            list.add(editingItemIndex!!, updatedItem)
                        } else {
                            list.add(updatedItem)
                        }
                        selectedItems = list

                        showSerialNumberBottomSheet = false
                        selectedInitialSerialNo = emptyList()
                        editingItem = null
                        editingItemIndex = null
                        pendingSelectedProductName = null
                        pendingSelectedProductGUID = null
                        selectedPricing = null
                        if (pendingItemsAfterMultiSelect.isNotEmpty()) {
                            pendingItemsAfterMultiSelect = pendingItemsAfterMultiSelect.drop(1)
                        }
                    }
                )

                if (isBusy()) {
                    SelectionSheetThree(
                        show = showSundrySheet, title = "Select Sundry",
                        options = busyLedgerList.map {
                            SundryItem(
                                name = it.Name.toString(), amount = 0.0, guid = it.GUID.toString(),
                                i1 = it.I1?.toInt() ?: 0, i2 = it.I2?.toInt() ?: 0,
                                d2 = it.D2?.toInt() ?: 0, rate = 0.0, srno = 0, percentValue = 0.0
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
                                focusedSundryGuid = newItem.guid
                            }
                        },
                        onDismiss = { showSundrySheet = false }
                    )
                } else {
                    SelectionSheetTwo(
                        show = showSundrySheet, title = "Select Ledger",
                        options = ledgerList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelect = { sundryName, GUID ->
                            if (!selectedSundries.any { it.name == sundryName }) {
                                val newItem = SundryItem(
                                    sundryName, 0.0, guid = GUID,
                                    i1 = 0, i2 = 0, d2 = 0, rate = 0.0, srno = 0, percentValue = 0.0
                                )
                                selectedSundries = selectedSundries + newItem
                                focusedSundryGuid = GUID
                            }
                        },
                        onDismiss = { showSundrySheet = false }
                    )
                }

                if (showResultDialog) {
                    DownloadResultDialog(
                        message = "${state.message}\n${state.data?.VoucherNumber}",
                        onDone = { showResultDialog = false; nav.pop() },
                        isSuccess = state.success, fileName = name,
                        htmlContent = htmlContent, onLoadingChange = { shareLoading = it }
                    )
                }
            },
            showBottomBar = true,
            bottomBarContent = {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shadowElevation = 12.dp,
                    tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "TOTAL",
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
                                    guid = item.guid,
                                    CD = item.CD,
                                    itemdesc1 = item.itemdesc1,
                                    itemdesc2 = item.itemdesc2,
                                    itemdesc3 = item.itemdesc3,
                                    itemdesc4 = item.itemdesc4,
                                    itemdesc5 = item.itemdesc5,
                                    itemdesc6 = item.itemdesc6,
                                    itemdesc7 = item.itemdesc7,
                                    itemdesc8 = item.itemdesc8,
                                    itemdesc9 = item.itemdesc9,
                                    itemdesc10 = item.itemdesc10,
                                    itemdesc11 = item.itemdesc11,
                                    itemdesc12 = item.itemdesc12,
                                    itemdesc13 = item.itemdesc13,
                                    itemdesc14 = item.itemdesc14,
                                    itemdesc15 = item.itemdesc15,
                                    itemdesc16 = item.itemdesc16,
                                    itemdesc17 = item.itemdesc17,
                                    itemdesc18 = item.itemdesc18,
                                    itemdesc19 = item.itemdesc19,
                                    itemdesc20 = item.itemdesc20,
                                    additionalinfo = item.additionalinfo,
                                    item_serial = item.item_serial.map { it.SerialNo ?: "" }
                                )
                            }
                            viewmodel.createEditInventoryResponse(
                                inventoryVoucherRequest = InventoryVoucherRequest(
                                    billing_guid = selectedLedgerGUID,
                                    vch_type = vchType, billing_name = selectedLedger,
                                    billing_mobile = "", billing_state = "",
                                    billing_country = "", billing_address = "",
                                    taxType = if (taxType == TaxType.EXTRA) 1 else 2,
                                    items = billingItems, sundries = selectedSundries,
                                    TranDate = selectedDate.yymmdd(), Narration = narration,
                                    TransactionID = tranId, total_amt = grandTotal,
                                    transportDetails = TransportDetails(
                                        transportName = transportName,
                                        station = station,
                                        gstNum = gstRrNo,
                                        vehicleNum = vehicleNo,
                                        pincode = pincode,
                                        grDate = gstRrDate,
                                        SpartyName = SpartyName,
                                        Saddress1 = Saddress1,
                                        Saddress2 = Saddress2,
                                        Saddress3 = Saddress3,
                                        Saddress4 = Saddress4,
                                        SshipState = SshipState,
                                        SmobileNo = SmobileNo,
                                        Saadhar = SadharNo,
                                        Semail = Semail,
                                        SitPan = SitPan,
                                        SgstIn = SgstIn,
                                        SbillingShipping = SbillingShipping,
                                        OptionalField1 = optionalFields[0],
                                        OptionalField2 = optionalFields[1],
                                        OptionalField3 = optionalFields[2],
                                        OptionalField4 = optionalFields[3],
                                        OptionalField5 = optionalFields[4],
                                        OptionalField6 = optionalFields[5],
                                        OptionalField7 = optionalFields[6],
                                        OptionalField8 = optionalFields[7],
                                        OptionalField9 = optionalFields[8],
                                        OptionalField10 = optionalFields[9],
                                        OptionalField11 = optionalFields[10],
                                        OptionalField12 = optionalFields[11],
                                        OptionalField13 = optionalFields[12],
                                        OptionalField14 = optionalFields[13],
                                        OptionalField15 = optionalFields[14],
                                        OptionalField16 = optionalFields[15],
                                        OptionalField17 = optionalFields[16],
                                        OptionalField18 = optionalFields[17],
                                        OptionalField19 = optionalFields[18],
                                        OptionalField20 = optionalFields[19],
                                    ),
                                    bills_collection = selectedReferences,
                                ),
                                onSuccess = {
                                    db.transaction {
                                        selectedReferences.forEach {
                                            db.voucherBillAllocationsQueries.deleteOldBillAllocation(
                                                state.data?.uniqueID.toString()
                                            )
                                        }
                                    }
                                    db.transaction {
                                        selectedReferences.forEachIndexed { index, ref ->
                                            db.voucherBillAllocationsQueries.insertBillAllocation(
                                                guid = "${state.data?.uniqueID}-${Uuid.random()}",
                                                vch_guid = state.data?.uniqueID.toString(),
                                                vchtype = ref.vchType, date = ref.date,
                                                duedate = ref.dueDate, billnumber = ref.billNumber,
                                                srno = (index + 1).toLong(), cm1 = selectedLedger,
                                                cm2 = "Agst Ref", cm3 = "",
                                                billid = ref.billId?.toDoubleOrNull(),
                                                d1 = when (vchType) {
                                                    9 -> makeNegativeConditional(ref.d1 ?: 0.0)
                                                    3 -> ref.d1
                                                    2 -> ref.d1
                                                    10 -> makeNegativeConditional(ref.d1 ?: 0.0)
                                                    14 -> makeNegativeConditional(ref.d1 ?: 0.0)
                                                    16 -> ref.d1?.absoluteValue
                                                    else -> ref.d1?.absoluteValue
                                                },
                                                d2 = null, e2 = null
                                            )
                                        }
                                    }
                                    db.transaction {
                                        selectedItems.forEach { item ->
                                            item.item_serial.forEach { serial ->
                                                db.productSerialNoQueries.insertProductSerialNo(
                                                    serialNo = serial.SerialNo,
                                                    masterCode1 = serial.MasterCode1,
                                                    masterCode2 = serial.MasterCode2
                                                        ?: "", // Godown/MasterCode2
                                                    value1 = -1.0,
                                                    value2 = serial.Value2 ?: 0.0,
                                                    value3 = serial.Value3 ?: 0.0,
                                                    guid = "${state.data?.uniqueID}-$serial-${Uuid.random()}"
                                                )
                                            }
                                        }
                                    }
                                    showResultDialog = true
                                },
                                url = if (isEdit) "updateInventory" else "addInventoryVch"
                            )
                        }

                        TallyButton(
                            onClick = {
                                if (grandTotal < 0.0) showWarningMessage = true else createO()
                            },
                            enabled = if (isEdit) enableUpdateButton && hasSalesmanPermission("ED$vchType")
                            else selectedLedger.isNotEmpty() && selectedItems.isNotEmpty(),
                            label = if (isEdit) "Update" else "Create",
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )

                        if (viewmodel.dataState.value.error != null) TallyResultDialog(
                            message = viewmodel.dataState.value.error ?: "Error",
                            onDone = { viewmodel.clearError() },
                            isSuccess = false,
                            confirmText = "Ok"
                        )

                        if (showWarningMessage) TallyAlertBox(
                            title = "Warning",
                            message = "Do you want to save negative voucher??",
                            confirmButtonText = "Yes", cancelButtonText = "No",
                            onConfirm = { showWarningMessage = false; createO() },
                            onCancel = { showWarningMessage = false },
                            onDismiss = { showWarningMessage = false },
                        )
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Multi-select item bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bottom sheet that lets the user:
 *  1. Search and tap items to add them to a pending cart
 *  2. Adjust qty inline for each pending item (inline +/- stepper + type-in)
 *  3. Tap "Add X Items" to confirm and dismiss
 *
 * Items already in the cart show a filled checkbox; tapping again removes them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectItemSheet(
    show: Boolean,
    title: String,
    options: List<Products>,
    isSale: Boolean,
    onConfirm: (List<PendingMultiItem>) -> Unit,
    onDismiss: () -> Unit,
) {
    // Pending cart: guid → PendingMultiItem
    val pendingMap = remember { mutableStateOf<Map<String, PendingMultiItem>>(emptyMap()) }

    // Reset cart when sheet opens
    LaunchedEffect(show) {
        if (show) pendingMap.value = emptyMap()
    }

    var query by remember(show) { mutableStateOf("") }

    val filteredList = remember(options, query) {
        if (query.isBlank()) options
        else {
            val q = query.trim()

            val (startsWith, rest) = options.partition {
                it.Name.orEmpty().startsWith(q, true) ||
                        it.Alias.orEmpty().startsWith(q, true)
            }

            val contains = rest.filter {
                it.Name.orEmpty().contains(q, true) ||
                        it.Alias.orEmpty().contains(q, true)
            }

            startsWith + contains
        }
    }

    if (!show) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        sheetGesturesEnabled = false,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (pendingMap.value.isNotEmpty()) {
                        Text(
                            text = "${pendingMap.value.size} item${if (pendingMap.value.size > 1) "s" else ""} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close, contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // ── Search bar ────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                TallySearchBar(searchQuery = query, onQueryChange = { query = it })
            }

            // ── Item list ─────────────────────────────────────────────────────
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (query.isBlank()) "No items available" else "No results found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                items(filteredList, key = { it.GUID ?: it.Name.orEmpty() }) { product ->
                    val guid = product.GUID.orEmpty()
                    val isSelected = guid in pendingMap.value
                    val pendingItem = pendingMap.value[guid]
                    val listPrice =
                        if (isSale) product.SalesPrice ?: 0.0 else product.PurcPrice ?: 0.0

                    MultiSelectItemRow(
                        product = product,
                        listPrice = listPrice,
                        stock = product.N1?.formatToAmtDec() ?: "-",
                        isSelected = isSelected,
                        pendingQty = pendingItem?.qty ?: "1",
                        onToggle = {
                            pendingMap.value = if (isSelected) {
                                pendingMap.value - guid
                            } else {
                                pendingMap.value + (guid to PendingMultiItem(
                                    product = product,
                                    qty = "1"
                                ))
                            }
                        },
                        onQtyChange = { newQty ->
                            if (isSelected) {
                                pendingMap.value = pendingMap.value.toMutableMap().also {
                                    it[guid] = it[guid]!!.copy(qty = newQty)
                                }
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) } // bottom padding for FAB
            }

            // ── Sticky bottom confirm bar ─────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onConfirm(pendingMap.value.values.toList())
                        onDismiss()
                    },
                    enabled = pendingMap.value.isNotEmpty(),
                    modifier = Modifier.weight(2f)
                ) {
                    val count = pendingMap.value.size
                    Text(
                        text = if (count == 0) "Add Items"
                        else "Add $count Item${if (count > 1) "s" else ""}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Single row inside MultiSelectItemSheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MultiSelectItemRow(
    product: Products,
    listPrice: Double,
    isSelected: Boolean,
    pendingQty: String,
    onToggle: () -> Unit,
    onQtyChange: (String) -> Unit,
    stock: String,
) {
    val focusRequester = remember { FocusRequester() }

    // Auto-focus qty field when row becomes selected
    LaunchedEffect(isSelected) {
        if (isSelected) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox indicator
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )

            // Name + price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.Name.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (listPrice > 0.0) {
                    Text(
                        text = formatTwo(listPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showQtyToSalesman()) {
                    Text(
                        text = "Stock: $stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Qty stepper — only visible when selected
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Decrement
                    IconButton(
                        onClick = {
                            val current = pendingQty.toIntOrNull() ?: 1
                            if (current > 1) onQtyChange((current - 1).toString())
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove, contentDescription = "Decrease",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Qty text field
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .border(
                                1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicQtyField(
                            value = pendingQty,
                            onValueChange = { onQtyChange(it.filter(Char::isDigit)) },
                            focusRequester = focusRequester
                        )
                    }

                    // Increment
                    IconButton(
                        onClick = {
                            val current = pendingQty.toIntOrNull() ?: 1
                            onQtyChange((current + 1).toString())
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Increase",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEW: Minimal qty text field used inside MultiSelectItemRow
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BasicQtyField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = value,
        onValueChange = { new ->
            val filtered = new.filter(Char::isDigit)
            if (filtered.isEmpty() || filtered.toIntOrNull() != null) {
                onValueChange(filtered)
            }
        },
        modifier = Modifier
            .width(40.dp)
            .focusRequester(focusRequester),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(
                        "1", style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
                inner()
            }
        }
    )
}