package org.prime.easykarobar.ui.screen.transactions.sale

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.prime.easykarobar.business.viewmodel.transactions.InventoryVoucherViewModel
import org.prime.easykarobar.data.expect.BarcodeScanResult
import org.prime.easykarobar.data.expect.BarcodeScannerLauncher
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.data.expect.rememberBarcodeScanner
import org.prime.easykarobar.data.model.hasSalesmanPermission
import org.prime.easykarobar.data.model.transactions.BillingItem
import org.prime.easykarobar.data.model.transactions.InventoryVoucherRequest
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.model.transactions.TransportDetails
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.salesHtml
import org.prime.easykarobar.ui.screen.home.tabs.InfoRow
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
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.ProductsWithConfig
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getConfigItemMasters
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.isBusy
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.SerialNumberBottomSheet
import org.prime.easykarobar.ui.shared.reportsShared.TallyDatePickerRow
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.prime.easykarobar.ui.shared.reportsShared.yymmdd
import org.tally.BSMaster
import org.tally.CompanyInformation
import org.tally.LedgerMaster
import org.tally.ProductGroupMaster
import org.tally.Products_Pricing
import org.tally.SerialNoEnterReportSale
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class ProductPricing(
    val Guid: String,
    val ProductName: String,
    val SerialNo: String,
    val VchType: Int,
    val SalePrice: Double,
    val PurchasePrice: Double,
    val Discount: Double,
    val CompoundDiscount: String
)


fun formatTwo(value: Double): String {
    return value.formatToAmtDec()
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
    val taxable: Double,
    val gstAmt: Double,
    val net: Double,
    val guid: String = "",
    // Item descriptions
    val itemdesc1: String? = null,
    val CD: String,
    val itemdesc2: String? = null,
    val itemdesc3: String? = null,
    val itemdesc4: String? = null,
    val itemdesc5: String? = null,
    val itemdesc6: String? = null,
    val itemdesc7: String? = null,
    val itemdesc8: String? = null,
    val itemdesc9: String? = null,
    val itemdesc10: String? = null,
    val itemdesc11: String? = null,
    val itemdesc12: String? = null,
    val itemdesc13: String? = null,
    val itemdesc14: String? = null,
    val itemdesc15: String? = null,
    val itemdesc16: String? = null,
    val itemdesc17: String? = null,
    val itemdesc18: String? = null,
    val itemdesc19: String? = null,
    val itemdesc20: String? = null,
    val hsn: String? = null,
    val item_serial: List<@Contextual SerialNoEnterReportSale> = emptyList(),
    // Additional info
    val additionalinfo: String? = null,
    val conFactor: Double? = null,
    val conType: Double? = null,
    val selectedUnit: String? = null,
    val altQty: Double? = null,
    val mainUnit: String? = null,
    val altUnit: String? = null,
) {
    val total: Double get() = price * qty
}


enum class TaxType {
    INCLUSIVE, EXTRA, VOUCHER
}

data class SaleScreen(
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
        BackHandler(true) {
            showExitPopup = true
        }

        val isSale = name in listOf("Sale Order", "Sale Invoice", "Sale Return")

        val nav = LocalNavigator.currentOrThrow
        val db = DatabaseHolder.instance

        LaunchedEffect(Unit) {
            SharedPrefs.LastVchType.save(vchType)
        }
        var compInfo by remember { mutableStateOf<CompanyInformation?>(null) }
        var isInitialLoading by remember { mutableStateOf(true) }

        var selectedLedger by remember { mutableStateOf(selectedLedger ?: "") }
        var barcodeQty by remember { mutableStateOf("") }
        var selectedLedgerGUID by remember { mutableStateOf(selectedLedgerGUID ?: "") }
        var narration by remember { mutableStateOf("") }
        var selectedDate by remember { mutableStateOf(CurrentDate()) }
        var taxType by remember {
            mutableStateOf(
                TaxType.entries[SharedPrefs.LastTaxType.get(vchType)]
            )
        }

        var selectedItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
        var selectedInitialSerialNo by remember {
            mutableStateOf<List<SerialNoEnterReportSale>>(
                emptyList()
            )
        }
        var selectedSundries by remember { mutableStateOf<List<SundryItem>>(emptyList()) }

        var showLedgerSheet by remember { mutableStateOf(false) }
        var showQtyPopup by remember { mutableStateOf(false) }
        var showItemSheet by remember { mutableStateOf(false) }
        var showWarningMessage by remember { mutableStateOf(false) }
        var showSundrySheet by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }
        var showGroupFilterSheet by remember { mutableStateOf(false) }

        var showProductPricingSheet by remember { mutableStateOf(false) }
        var selectedProductForPricing by remember { mutableStateOf<ProductsWithConfig?>(null) }
        var selectedPricing by remember { mutableStateOf<ProductPricing?>(null) }

        var editingItem by remember { mutableStateOf<InvoiceItem?>(null) }

        var pendingSelectedProductName by remember { mutableStateOf<String?>(null) }
        var pendingSelectedProductGUID by remember { mutableStateOf<String?>(null) }

        var ledgerList by remember { mutableStateOf<List<LedgerMaster>>(emptyList()) }
        var busyLedgerList by remember { mutableStateOf<List<BSMaster>>(emptyList()) }
        var itemsList by remember { mutableStateOf<List<ProductsWithConfig>>(emptyList()) }
        var productGroups by remember { mutableStateOf<List<ProductGroupMaster>>(emptyList()) }
        var productPricingList by remember { mutableStateOf<List<Products_Pricing>>(emptyList()) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val info = db.companyInformationQueries.selectAll().executeAsOneOrNull()
                val ledgers = getLedgerMasters(db)
                val busyLedgers = db.bSMasterQueries.selectAll().executeAsList()
                val items = getConfigItemMasters(db, vchType)
                val groups = db.productGroupMasterQueries.selectAll(
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes()
                ).executeAsList()
                val pricing = db.productsPricingQueries.selectAll().executeAsList()

                withContext(Dispatchers.Main) {
                    compInfo = info
                    ledgerList = ledgers
                    busyLedgerList = busyLedgers
                    itemsList = items
                    productGroups = groups
                    productPricingList = pricing
                    isInitialLoading = false
                }
            }
        }

        println("item list is in sale order $itemsList")
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val groupFilteredList = remember(itemsList, selectedGroups) {
            if (selectedGroups.isEmpty()) {
                itemsList
            } else {
                itemsList.filter { it.GroupName in getProductsGroupCodesByName(selectedGroups) }
            }
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
        var scannerLauncher by remember {
            mutableStateOf<BarcodeScannerLauncher?>(null)
        }
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
        var editingItemIndex by remember { mutableStateOf<Int?>(null) }

        val optionalFields = remember {
            mutableStateListOf(*Array(20) { "" })
        }
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
        var selectedReferences by remember {
            mutableStateOf(listOf<BillByBillModel>())
        }
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

        LaunchedEffect(selectedLedger, ledgerList) {
            val found = ledgerList.find { it.Name == selectedLedger }?.GUID
            if (found != null) {
                selectedLedgerGUID = found
            }
        }

        scannerLauncher = rememberBarcodeScanner { result ->
            demoBarcodeName = result.toString()
            when (result) {
                is BarcodeScanResult.Cancelled -> {
                    demoBarcodeName = result.toString()
                    showQtyPopup = false
                    return@rememberBarcodeScanner
                }

                is BarcodeScanResult.Failure -> {
                    demoBarcodeName = result.toString()
                    showEmptyBarcode = true
                    return@rememberBarcodeScanner
                }

                is BarcodeScanResult.Success -> {
                    demoBarcodeName = result.toString()
                    val barcode = result.value.trim()

                    if (barcode.isEmpty()) {
                        demoBarcodeName = result.toString()
                        showEmptyBarcode = true
                        return@rememberBarcodeScanner
                    }

                    scope.launch(Dispatchers.IO) {
                        val product = db.productsQueries.getItemByName(
                            barcode, groupCodes = itemGroupCodes(),
                            filterGroup = filterItemGroups(),
                        ).executeAsOneOrNull()

                        if (product == null) {
                            withContext(Dispatchers.Main) {
                                demoBarcodeName = result.toString()
                                showEmptyBarcode = true
                            }
                            return@launch
                        }

                        val listPrice =
                            if (isSale) product.SalesPrice ?: 0.0 else product.PurcPrice ?: 0.0
                        val discount =
                            if (isSale) product.SaleDisc ?: 0.0 else product.PurcDisc ?: 0.0
                        val price = listPrice - (listPrice * discount / 100.0)
                        val qty = barcodeQty.toIntOrNull() ?: 1

                        val gstPercentage = try {
                            db.taxCategoryMastQueries.selectTaxRate(
                                product.TaxCategoryCode?.toInt().toString(), selectedDate
                            ).executeAsOneOrNull() ?: 0.0
                        } catch (e: Exception) {
                            println(e.message)
                            0.0
                        }

                        val taxableAmount: Double
                        val gstAmount: Double
                        val netAmount: Double

                        if (taxType == TaxType.EXTRA) {
                            taxableAmount = price * qty
                            gstAmount = taxableAmount * gstPercentage / 100.0
                            netAmount = taxableAmount + gstAmount
                        } else if (taxType == TaxType.VOUCHER) {
                            taxableAmount = price * qty
                            gstAmount = 0.0
                            netAmount = price * qty
                        } else {
                            if (gstPercentage == 0.0) {
                                taxableAmount = price * qty
                                gstAmount = 0.0
                                netAmount = price * qty
                            } else {
                                val amount = price * qty
                                taxableAmount = amount * 100.0 / (100.0 + gstPercentage)
                                gstAmount = amount - taxableAmount
                                netAmount = amount
                            }
                        }

                        val factor = product.ConFactor ?: 1.0
                        val conTypeVal = product.ConType ?: 1.0
                        val calculatedAltQty = if (conTypeVal == 1.0) {
                            qty.toDouble() * factor
                        } else {
                            qty.toDouble() / factor
                        }

                        withContext(Dispatchers.Main) {
                            showQtyPopup = false
                            selectedItems = selectedItems + InvoiceItem(
                                name = product.Name.orEmpty(),
                                price = price,
                                qty = qty,
                                discountPercentage = discount,
                                listPrice = listPrice,
                                taxable = taxableAmount,
                                gstAmt = gstAmount,
                                net = netAmount,
                                guid = product.GUID ?: pendingSelectedProductGUID.orEmpty(),
                                gstPercentage = gstPercentage,
                                taxCategoryCode = product.TaxCategoryCode?.toInt() ?: 0,
                                CD = if (discount != 0.0) discount.toString() else "",
                                conFactor = product.ConFactor,
                                conType = product.ConType,
                                selectedUnit = product.UnitName,
                                altQty = calculatedAltQty,
                                mainUnit = product.UnitName,
                                altUnit = product.AltUnit,
                                hsn = product.HSN
                            )

                            displayItemName = product.Name.orEmpty()
                            showAddMorePopup = true
                        }
                    }
                }
            }
        }
        GroupFilterBottomSheet(
            show = showGroupFilterSheet,
            items = productGroups, // can be any list
            selectedItems = selectedGroups,
            itemNameSelector = { it.Name },
            onSelectedItemsChange = { selectedGroups = it },
            onDismiss = { showGroupFilterSheet = false },
            bottomSheetState = bottomSheetState
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
                    println("delete serial number " + oneState.data?.uniqueID.toString())
                    scope.launch {
                        db.transaction {
                            selectedReferences.forEach {
                                db.voucherBillAllocationsQueries.deleteOldBillAllocation(
//                                    state.data?.uniqueID.toString()
                                    oneState.data?.uniqueID.toString()
                                )
                            }
                        }
                        db.transaction {
                            selectedItems.forEach {
                                it.item_serial.forEach {
                                    db.productSerialNoQueries.deleteSerialNo("${oneState.data?.uniqueID}_${it.SerialNo}")
                                }
                            }
                        }
                        tranId?.let {
                            viewmodel.deleteInventoryVch(
                                tranId = it, vchType = vchType
                            )
                        }
                    }
                },
                onCancel = { showDeleteDialog = false },
                onDismiss = { showDeleteDialog = false },
            )
        }

        var qtyError by remember { mutableStateOf(false) }
        if (showQtyPopup) {
            TallyAlertBox(
                title = "Enter Quantity",
                confirmButtonText = "OK",
                cancelButtonText = "Cancel",
                onConfirm = {
                    if (barcodeQty.isBlank()) {
                        qtyError = true
                    } else {
                        qtyError = false
                        showQtyPopup = false
                        scannerLauncher?.launch()
                    }
                },
                onCancel = { showQtyPopup = false },
                onDismiss = { showQtyPopup = false },
                content = {
                    Column {
                        TallyTextField(
                            value = barcodeQty,
                            onValueChange = {
                                barcodeQty = it
                                if (it.isNotBlank()) qtyError = false
                            },
                            placeholder = "Enter Quantity",
                            isPassword = false,
                            isNumber = true,
                            label = "Barcode Qty",
                        )
                        if (qtyError) {
                            Text(
                                text = "Quantity cannot be empty",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                })
        }

        val itemsTotal by derivedStateOf {
            selectedItems.sumOf { item ->
                // Apply compound discount if it exists
                val effectivePrice = if (item.CD.isNotBlank() && item.CD.contains("+")) {
                    applyCompoundDiscount(item.listPrice, item.CD)
                } else {
                    item.price
                }

                if (taxType == TaxType.EXTRA) {
                    val taxable = effectivePrice * item.qty
                    taxable + taxable * item.gstPercentage / 100.0
                } else if (taxType == TaxType.VOUCHER) {
                    effectivePrice * item.qty
                } else {
                    effectivePrice * item.qty
                }
            }
        }

        LaunchedEffect(tranId) {
            tranId?.let { viewmodel.getOneInventoryVoucher(it) }
        }

        LaunchedEffect(oneState.data, itemsList) {
            if (!isEdit || itemsList.isEmpty()) return@LaunchedEffect
            oneState.data?.let { data ->
                selectedDate = Tdate(data.created_at.take(10))
                selectedLedger = data.billing_name
                selectedLedgerGUID = data.billing_guid
                taxType = if (data.taxType == 1) TaxType.EXTRA else TaxType.INCLUSIVE
                selectedItems = data.items.map { itm ->
                    val prodFromDb = itemsList.find { it.ID == itm.product_id.toLongOrNull() }
                    InvoiceItem(
                        name = itm.product_name,
                        price = itm.price.toDouble(),
                        listPrice = itm.list_price.toDouble(),
                        qty = itm.quantity,
                        discountPercentage = itm.discount_percent.toDouble(),
                        taxable = itm.item_amount.toDouble(),
                        gstAmt = itm.taxamt1.toDouble(),
                        net = itm.total_amt.toDouble(),
                        gstPercentage = itm.tax_rate1.toDouble(),
                        taxCategoryCode = 0,
                        // Restore descriptions
                        itemdesc1 = itm.itemdesc1,
                        itemdesc2 = itm.itemdesc2,
                        itemdesc3 = itm.itemdesc3,
                        itemdesc4 = itm.itemdesc4,
                        itemdesc5 = itm.itemdesc5,
                        CD = itm.CD,
                        itemdesc6 = itm.itemdesc6,
                        itemdesc7 = itm.itemdesc7,
                        itemdesc8 = itm.itemdesc8,
                        itemdesc9 = itm.itemdesc9,
                        itemdesc10 = itm.itemdesc10,
                        itemdesc11 = itm.itemdesc11,
                        itemdesc12 = itm.itemdesc12,
                        itemdesc13 = itm.itemdesc13,
                        itemdesc14 = itm.itemdesc14,
                        itemdesc15 = itm.itemdesc15,
                        itemdesc16 = itm.itemdesc16,
                        itemdesc17 = itm.itemdesc17,
                        itemdesc18 = itm.itemdesc18,
                        itemdesc19 = itm.itemdesc19,
                        itemdesc20 = itm.itemdesc20,
                        additionalinfo = itm.additionalinfo,
                        conFactor = itm.conFactor ?: prodFromDb?.ConFactor,
                        conType = itm.conType ?: prodFromDb?.ConType,
                        selectedUnit = itm.selectedUnit ?: prodFromDb?.UnitName,
                        altQty = itm.altQty,
                        mainUnit = prodFromDb?.UnitName,
                        altUnit = prodFromDb?.AltUnit,
                        hsn = prodFromDb?.HSN,
                        item_serial = itm.item_serial.map { sn ->
                            SerialNoEnterReportSale(
                                SerialNo = sn,
                                MasterCode1 = itm.product_id.toDoubleOrNull(),
                                ProductName = itm.product_name,
                                UnitName = null,
                                GroupName = null,
                                Value1 = 1.0,
                                Value2 = 0.0,
                                Value3 = 0.0,
                                MasterCode2 = ""
                            )
                        }
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
                        srno = s.srno,
                        percentValue = s.percentValue
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
                data.bills_collection?.let {
                    selectedReferences = it
                }
                println(data.uniqueID.toString())
                uniqueId = data.uniqueID.toString()

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

        if (showEmptyBarcode) {
            TallyResultDialog(
                message = "Barcode not found \n Barcode Value $demoBarcodeName",
                onDone = { showEmptyBarcode = false },
                isSuccess = false
            )
        }

        val htmlContent = salesHtml(
            name = name,
            partyName = selectedLedger,
            partyGuid = selectedLedgerGUID,
            invoiceNo = oneState.data?.billed_vchno
                ?.takeIf { it.isNotBlank() }
                ?: oneState.data?.AutoVchNo
                    ?.takeIf { it != 0 }
                    ?.toString()
                ?: state.data?.VoucherNumber
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
                ?: "default_name",
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
                gstRrDate = gstRrDate,
                SpartyName = SpartyName,
                Saddress1 = Saddress1,
                Saddress2 = Saddress2,
                Saddress3 = Saddress3,
                Saddress4 = Saddress4,
                SshipState = SshipState,
                SgstIn = SgstIn
            )
        )

        val menuList = buildList {
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
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = oneState.data?.billed_vchno
                            ?.takeIf { it.isNotEmpty() }?.replace("/", "_")
                            ?: CompanyName(),
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it })
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = oneState.data?.billed_vchno
                            ?.takeIf { it.isNotEmpty() }?.replace("/", "_")
                            ?: CompanyName(),
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it })
                }
            },
            onExcelClick = {
                scope.launch {
                    val excelRows = selectedItems.mapIndexed { index, item ->
                        listOf(
                            (index + 1).toString(),
                            item.name,
                            item.qty.toString(),
                            item.listPrice.formatToAmtDec(),
                            item.CD,
                            item.taxable.formatToAmtDec(),
                            item.gstPercentage.toString(),
                            item.gstAmt.formatToAmtDec(),
                            item.net.formatToAmtDec()
                        )
                    }
                    handlePdfAction(
                        fileName = oneState.data?.billed_vchno
                            ?.takeIf { it.isNotEmpty() }?.replace("/", "_")
                            ?: CompanyName(),
                        htmlContent = htmlContent,
                        headers = listOf(
                            "S No.",
                            "Item Name",
                            "Qty",
                            "Price",
                            "Disc",
                            "Taxable",
                            "GST %",
                            "GST Amt",
                            "Total"
                        ),
                        rows = excelRows,
                        action = PdfAction.DownloadExcel,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            menuItems = menuList,
            title = if (isEdit) "Edit $name" else name,
            content = { paddingValues ->

                if (isInitialLoading || (isEdit && oneState.isLoading)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            InfoRow(
                                icon = Icons.Default.Badge,
                                label = "GST Number",
                                value = compInfo?.T4?.toString() ?: ""
                            )

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

                                    AssistChip(
                                        onClick = { showItemSheet = true },
                                        enabled = editingItem == null,
                                        label = {
                                            Text(
                                                "Add Item",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        border = null
                                    )
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val pending = editingItem
                                    val product = itemsList.find { it.Name == pending?.name }

                                    if (pending != null) {
                                        val gst = try {
                                            if (isEdit) {
                                                pending.gstPercentage
                                            } else {
                                                db.taxCategoryMastQueries.selectTaxRate(
                                                    pending.taxCategoryCode.toString(),
                                                    selectedDate
                                                ).executeAsOneOrNull() ?: 0.0
                                            }
                                        } catch (e: Exception) {
                                            println(e.message)
                                            0.0
                                        }

                                        println("This is value of gst ${pending.taxCategoryCode}")

                                        if (product != null) {
                                            ExpandedItemEditor1(
                                                name = product.Name ?: pending.name,
                                                defaultListPrice = if (pending.listPrice == 0.0)
                                                    if (isSale) product.SalesPrice ?: 0.0
                                                    else product.PurcPrice ?: 0.0
                                                else pending.listPrice,
                                                initialQuantity = pending.qty,
                                                initialDiscount = pending.CD.ifBlank { pending.discountPercentage.toString() },
                                                taxType = taxType,
                                                existingItem = pending,
                                                initialSerialNumbers = pending.item_serial.map {
                                                    it.SerialNo ?: ""
                                                },
                                                mainUnit = product.UnitName ?: "",
                                                altUnit = product.AltUnit,
                                                conFactor = product.ConFactor,
                                                conType = product.ConType,
                                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, itemDescs, additionalInfos, serialNumbers, cFactor, cType, sUnit, aQty ->
                                                    val newItem = InvoiceItem(
                                                        name = product.Name ?: pending.name,
                                                        price = unitPrice,
                                                        qty = qty,
                                                        discountPercentage = discount,
                                                        listPrice = listPriceText,
                                                        taxable = taxable,
                                                        CD = compoundDiscount ?: "",
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
                                                            SerialNoEnterReportSale(
                                                                SerialNo = sn,
                                                                MasterCode1 = product?.GUID?.toDoubleOrNull(),
                                                                ProductName = pending.name,
                                                                UnitName = null,
                                                                GroupName = null,
                                                                Value1 = 1.0,
                                                                Value2 = 0.0,
                                                                Value3 = 0.0, MasterCode2 = ""
                                                            )
                                                        },
                                                        conFactor = cFactor,
                                                        conType = cType,
                                                        selectedUnit = sUnit,
                                                        altQty = aQty,
                                                        mainUnit = product?.UnitName
                                                            ?: pending.mainUnit,
                                                        altUnit = product?.AltUnit
                                                            ?: pending.altUnit,
                                                        hsn = pending.hsn
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
                                                    if (editingItemIndex != null && editingItem != null) {
                                                        val list = selectedItems.toMutableList()
                                                        list.add(editingItemIndex!!, editingItem!!)
                                                        selectedItems = list
                                                    }
                                                    editingItemIndex = null
                                                    editingItem = null
                                                }
                                            )
                                        } else {
                                            val productFallback =
                                                itemsList.find { it.Name == pending.name }
                                            ExpandedItemEditor1(
                                                name = pending.name,
                                                defaultListPrice = pending.listPrice,
                                                initialDiscount = pending.CD.ifBlank { pending.discountPercentage.toString() },
                                                initialQuantity = pending.qty,
                                                taxType = taxType,
                                                existingItem = pending,
                                                initialSerialNumbers = pending.item_serial.map {
                                                    it.SerialNo ?: ""
                                                },
                                                mainUnit = productFallback?.UnitName ?: "",
                                                altUnit = productFallback?.AltUnit,
                                                conFactor = productFallback?.ConFactor,
                                                conType = productFallback?.ConType,
                                                onAdd = { qty, unitPrice, discount, compoundDiscount, listPriceText, taxable, gstAmount, net, gstPercentage, itemDescs, additionalInfos, serialNumbers, cFactor, cType, sUnit, aQty ->
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
                                                            SerialNoEnterReportSale(
                                                                SerialNo = sn,
                                                                MasterCode1 = product?.GUID?.toDoubleOrNull(),
                                                                ProductName = pending.name,
                                                                UnitName = null,
                                                                GroupName = null,
                                                                Value1 = 1.0,
                                                                Value2 = 0.0,
                                                                Value3 = 0.0, MasterCode2 = ""
                                                            )
                                                        },
                                                        conFactor = cFactor,
                                                        conType = cType,
                                                        selectedUnit = sUnit,
                                                        altQty = aQty,
                                                        mainUnit = productFallback?.UnitName
                                                            ?: pending.mainUnit,
                                                        altUnit = productFallback?.AltUnit
                                                            ?: pending.altUnit,
                                                        hsn = pending.hsn
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
                                                    if (editingItemIndex != null && editingItem != null) {
                                                        val list = selectedItems.toMutableList()
                                                        list.add(editingItemIndex!!, editingItem!!)
                                                        selectedItems = list
                                                    }
                                                    editingItemIndex = null
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
                                                        if (index1 == index) {
                                                            val newTaxableAmount: Double
                                                            val newGstAmount: Double
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
                                                                newGstAmount = 0.0
                                                                newNetAmount = item1.price * newQty
                                                            } else {
                                                                if (item1.gstPercentage == 0.0) {
                                                                    newTaxableAmount =
                                                                        item1.price * newQty
                                                                    newGstAmount = 0.0
                                                                    newNetAmount =
                                                                        item1.price * newQty
                                                                } else {
                                                                    val amount =
                                                                        item1.price * newQty
                                                                    newTaxableAmount =
                                                                        amount * 100.0 / (100.0 + item1.gstPercentage)
                                                                    newGstAmount =
                                                                        amount - newTaxableAmount
                                                                    newNetAmount = amount
                                                                }
                                                            }

                                                            val factor = item1.conFactor ?: 1.0
                                                            val conTypeVal = item1.conType ?: 1.0
                                                            val calculatedAltQty =
                                                                if (item1.selectedUnit == item1.altUnit) {
                                                                    newQty.toDouble()
                                                                } else {
                                                                    if (conTypeVal == 1.0) {
                                                                        newQty.toDouble() * factor
                                                                    } else {
                                                                        newQty.toDouble() / factor
                                                                    }
                                                                }

                                                            item1.copy(
                                                                qty = newQty,
                                                                taxable = newTaxableAmount,
                                                                gstAmt = newGstAmount,
                                                                net = newNetAmount,
                                                                altQty = calculatedAltQty
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
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = { showGroupFilterSheet = true },
                                            label = { Text("Group Filter") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.FilterList,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                labelColor = MaterialTheme.colorScheme.primary,
                                                leadingIconContentColor = MaterialTheme.colorScheme.primary
                                            )
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
                                headerAction = {}
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

                                        val sundryValue = if (sundry.i2 == 1) {
                                            cumulativeTotal * (sundry.amount / 100.0)
                                        } else {
                                            sundry.amount
                                        }

                                        cumulativeTotal = when (sundry.i1) {
                                            0 -> cumulativeTotal - sundryValue
                                            1 -> cumulativeTotal + sundryValue
                                            else -> cumulativeTotal + sundryValue
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SmallAddButton(
                                            label = if (isBusy()) "Add More Sundry" else "Add More Ledger",
                                            enabled =
                                                selectedItems.isNotEmpty()

                                        ) {
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
                            if (isBusy()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    Text(
                                        text = "Transport Details",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = { showTransportDetails = !showTransportDetails }
                                    ) {
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
                            }

                            AnimatedVisibility(
                                visible = showTransportDetails,
                                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                                    animationSpec = tween(300)
                                ),
                                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                                    animationSpec = tween(300)
                                )
                            )
                            {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
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
                                        Text(
                                            text = "Transportation Information",
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
                            if (isBusy()) {
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

                            }

                            if (vchType !in listOf(12, 13, 15)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bill By Bill Reference",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = { showBillModalSheet = true },
                                        enabled = (selectedLedgerGUID != "") && (!itemsList.isEmpty())
                                    ) {
                                        Icon(
                                            imageVector = if (showTransportDetails) Icons.Default.RemoveCircleOutline
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
                                title = "Bill by Bill", totalAmount = grandTotal,
                                isEdit = isEdit,
                                uniqueId = uniqueId, vchType = vchType
                            )

                            if (isBusy()) {
                                OptionalFieldCard(
                                    showOptionalField = showOptionalField,
                                    onShowChange = { showOptionalField = !showOptionalField },
                                    optionalFields = optionalFields,
                                    onFieldChange = { index, value ->
                                        optionalFields[index] = value
                                    }
                                )
                            }
                        }
                    }
                }

                SelectionSheet(
                    show = showLedgerSheet,
                    title = "Select Party Ledger",
                    options = ledgerList
                        .filter { it.L1 == 1.0 || it.L2 == 1.0 || it.L3 == 1.0 }
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
                    options = groupFilteredList,
                    onSelect = { itemName ->
                        val currentLedger = ledgerList.find { it.GUID == selectedLedgerGUID }
                        val pricingLevel =
                            if (isSale) currentLedger?.L6 ?: 100.0 else currentLedger?.L7 ?: 100.0

                        println("asdlkfj " + productPricingList.filter { it.GUID.toDouble() == itemName.GUID?.toDouble() })
                        selectedProductForPricing = itemName
                        val pricingForProduct =
                            productPricingList.filter { it.GUID.toDouble() == itemName.GUID?.toDouble() }

                        val autoPricing = if (pricingLevel != 100.0) {
                            pricingForProduct.find { it.Srno == pricingLevel.toLong() }
                        } else null

                        if (autoPricing != null) {
                            val prod = itemsList.find { it.Name == itemName.Name }
                            val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0
                            val listPrice = autoPricing.SalesPrice ?: 0.0
                            val discount = autoPricing.Disc ?: 0.0
                            editingItem = InvoiceItem(
                                name = itemName.Name.toString(),
                                price = listPrice - (listPrice * discount / 100.0),
                                qty = 1,
                                discountPercentage = discount,
                                listPrice = listPrice,
                                taxable = 0.0,
                                gstAmt = 0.0,
                                net = 0.0,
                                guid = itemName.GUID ?: "",
                                gstPercentage = 0.0,
                                taxCategoryCode = taxCategoryCode.toInt(),
                                CD = if (discount != 0.0) discount.toString() else "",
                                conFactor = prod?.ConFactor,
                                conType = prod?.ConType,
                                selectedUnit = prod?.UnitName,
                                altQty = null,
                                mainUnit = prod?.UnitName,
                                altUnit = prod?.AltUnit,
                                hsn = prod?.HSN
                            )
                            showItemSheet = false
                        } else if (pricingForProduct.isNotEmpty()) {
                            showProductPricingSheet = true
                        } else {
                            val prod = itemsList.find { it.Name == itemName.Name }
                            val listPrice =
                                if (isSale) prod?.SalesPrice ?: 0.0 else prod?.PurcPrice ?: 0.0
                            val discount =
                                if (isSale) prod?.SaleDisc ?: 0.0 else prod?.PurcDisc ?: 0.0
                            editingItem = InvoiceItem(
                                name = itemName.Name.toString(),
                                price = listPrice - (listPrice * discount / 100.0),
                                qty = 1,
                                discountPercentage = discount,
                                listPrice = listPrice,
                                taxable = 0.0,
                                gstAmt = 0.0,
                                net = 0.0,
                                guid = itemName.GUID ?: "",
                                gstPercentage = 0.0,
                                taxCategoryCode = (prod?.TaxCategoryCode ?: 0.0).toInt(),
                                CD = if (discount != 0.0) discount.toString() else "",
                                conFactor = prod?.ConFactor,
                                conType = prod?.ConType,
                                selectedUnit = prod?.UnitName,
                                altQty = null,
                                mainUnit = prod?.UnitName,
                                altUnit = prod?.AltUnit,
                                hsn = prod?.HSN
                            )
                            showItemSheet = false
                        }
                    },
                    onDismiss = { showItemSheet = false }
                )
                ProductPricingBottomSheet(
                    show = showProductPricingSheet,
                    productName = selectedProductForPricing?.Name ?: "",
                    pricingList = productPricingList.filter { it.GUID.toDouble() == selectedProductForPricing?.GUID?.toDouble() }
                        .map {
                            ProductPricing(
                                Guid = it.GUID,
                                ProductName = ('A'.code + (it.Srno - 101)).toInt().toChar()
                                    .toString(),
                                SerialNo = it.Srno.toString(),
                                SalePrice = it.SalesPrice ?: 0.0,
                                PurchasePrice = it.SalesPrice ?: 0.0,
                                Discount = it.Disc ?: 0.0,
                                CompoundDiscount = it.Disc.toString(), VchType = it.VchType.toInt()
                            )
                        },
                    onSelect = { pricing: ProductPricing ->
                        showProductPricingSheet = false
                        selectedPricing = pricing
                        val itemName = selectedProductForPricing
                        if (itemName != null) {
                            val prod = itemsList.find { it.Name == itemName.Name }
                            val taxCategoryCode = prod?.TaxCategoryCode ?: 0.0

                            val listPrice = pricing.SalePrice
                            val discount = pricing.Discount
                            editingItem = InvoiceItem(
                                name = itemName.Name.toString(),
                                price = if (pricing.CompoundDiscount.contains("+")) {
                                    applyCompoundDiscount(listPrice, pricing.CompoundDiscount)
                                } else {
                                    listPrice - (listPrice * discount / 100.0)
                                },
                                qty = 1,
                                discountPercentage = discount,
                                listPrice = listPrice,
                                taxable = 0.0,
                                gstAmt = 0.0,
                                net = 0.0,
                                guid = itemName.GUID ?: "",
                                gstPercentage = 0.0,
                                taxCategoryCode = taxCategoryCode.toInt(),
                                CD = pricing.CompoundDiscount,
                                conFactor = prod?.ConFactor,
                                conType = prod?.ConType,
                                selectedUnit = prod?.UnitName,
                                altQty = null,
                                mainUnit = prod?.UnitName,
                                altUnit = prod?.AltUnit,
                                hsn = prod?.HSN
                            )

                            showItemSheet = false
                        }
                    },
                    onDismiss = {
                        showProductPricingSheet = false
                        selectedProductForPricing = null
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
                        val pricePerUnit =
                            if (total > 0) total / selectedList.size
                            else if (selectedPricing != null) selectedPricing!!.SalePrice
                            else (editingItem?.price ?: 0.0)

                        val qty = selectedList.size.coerceAtLeast(1)

// ── SAME TAX LOGIC AS MULTI-SELECT ─────────────────────────────
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

                        val factor = prod?.ConFactor ?: editingItem?.conFactor ?: 1.0
                        val conTypeVal = prod?.ConType ?: editingItem?.conType ?: 1.0
                        val currentUnit = editingItem?.selectedUnit ?: prod?.UnitName
                        val altUnitName = prod?.AltUnit ?: editingItem?.altUnit

                        val calculatedAltQty =
                            if (currentUnit == altUnitName && altUnitName != null) {
                                qty.toDouble()
                            } else {
                                if (conTypeVal == 1.0) {
                                    qty.toDouble() * factor
                                } else {
                                    qty.toDouble() / factor
                                }
                            }

                        val updatedItem = InvoiceItem(
                            name = pendingSelectedProductName ?: "",
                            price = pricePerUnit,
                            qty = qty,
                            discountPercentage = selectedPricing?.Discount
                                ?: editingItem?.discountPercentage ?: 0.0,
                            listPrice = pricePerUnit,
                            taxable = taxableAmt,
                            gstAmt = gstAmt,
                            net = netAmt,
                            guid = pendingSelectedProductGUID ?: editingItem?.guid ?: "",
                            gstPercentage = gstPct,
                            taxCategoryCode = taxCategoryCode.toInt(),
                            CD = selectedPricing?.CompoundDiscount ?: editingItem?.CD ?: "",
                            item_serial = selectedList,
                            conFactor = prod?.ConFactor ?: editingItem?.conFactor,
                            conType = prod?.ConType ?: editingItem?.conType,
                            selectedUnit = currentUnit,
                            altQty = calculatedAltQty,
                            mainUnit = prod?.UnitName ?: editingItem?.mainUnit,
                            altUnit = altUnitName,
                            hsn = prod?.HSN ?: editingItem?.hsn
                        )

                        val list = selectedItems.toMutableList()
                        if (editingItemIndex != null) {
                            list.add(editingItemIndex!!, updatedItem)
                        } else {
                            list.add(updatedItem)
                        }
                        selectedItems = list

                        showSerialNumberBottomSheet = false
                        selectedInitialSerialNo = emptyList() // Reset after selection
                        editingItem = null
                        editingItemIndex = null
                        pendingSelectedProductName = null
                        pendingSelectedProductGUID = null
                    }
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
                                rate = 0.0,
                                srno = 0,
                                percentValue = 0.0
                            )
                        },
                        onSelect = { item ->
                            if (!selectedSundries.any { it.name == item.name }) {
                                val newItem = SundryItem(
                                    item.name, 0.0,
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
                        show = showSundrySheet,
                        title = "Select Ledger",
                        options = ledgerList.filter { it.L1 == 0.0 && it.L2 == 0.0 && it.L3 == 0.0 }
                            .map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelect = { sundryName, GUID ->
                            if (!selectedSundries.any { it.name == sundryName }) {
                                val newItem = SundryItem(
                                    sundryName, 0.0,
                                    guid = GUID,
                                    i1 = 0,
                                    i2 = 0,
                                    d2 = 0,
                                    rate = 0.0,
                                    srno = 0,
                                    percentValue = 0.0
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
                        onDone = {
                            showResultDialog = false
                            nav.pop()
                        },
                        isSuccess = state.success,
                        fileName = name,
                        htmlContent = htmlContent,
                        onLoadingChange = { shareLoading = it }
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
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

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
                                    guid = item.guid,
                                    CD = item.CD,
                                    // Pass through all description fields
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
                                    item_serial = item.item_serial.map { it.SerialNo ?: "" },
                                    conFactor = item.conFactor,
                                    conType = item.conType,
                                    selectedUnit = item.selectedUnit,
                                    altQty = item.altQty
                                )
                            }
println("selected date from sale invoice is $selectedDate")
println("using my function " +
        "selected date from sale invoice is ${selectedDate.yymmdd()}")
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
                                    total_amt = grandTotal,
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
                                    scope.launch(Dispatchers.IO) {
                                        SharedPrefs.LastTaxType.save(vchType, taxType.ordinal)
                                        db.transaction {
                                            selectedReferences.forEach {
                                                db.voucherBillAllocationsQueries.deleteOldBillAllocation(
                                                    state.data?.uniqueID.toString()
                                                )
                                            }
                                        }
                                        db.transaction {
                                            println(selectedReferences.size)
                                            selectedReferences.forEachIndexed { index, ref ->
                                                db.voucherBillAllocationsQueries.insertBillAllocation(
                                                    guid = "${state.data?.uniqueID}-${Uuid.random()}",
                                                    vch_guid = state.data?.uniqueID.toString(),
                                                    vchtype = ref.vchType,
                                                    date = ref.date,
                                                    duedate = ref.dueDate,
                                                    billnumber = ref.billNumber,
                                                    srno = (index + 1).toLong(),
                                                    cm1 = selectedLedger,
                                                    cm2 = "Agst Ref",
                                                    cm3 = "",
                                                    billid = ref.billId?.toDoubleOrNull(),
                                                    d1 = when (vchType) {
                                                        9, 10, 14 -> makeNegativeConditional(
                                                            ref.d1 ?: 0.0
                                                        )

                                                        3, 2 -> ref.d1
                                                        16 -> ref.d1?.absoluteValue
                                                        else -> ref.d1?.absoluteValue
                                                    },
                                                    d2 = null,
                                                    e2 = null
                                                )
                                            }
                                        }

                                        db.transaction {
                                            selectedItems.forEach { item ->
                                                println("in adding serial to db ${state.data?.uniqueID}")
                                                item.item_serial.forEach { serialObj ->
                                                    db.productSerialNoQueries.insertProductSerialNo(
                                                        serialNo = serialObj.SerialNo,
                                                        masterCode1 = item.guid.toDoubleOrNull(),
                                                        masterCode2 = serialObj.UnitName ?: "",
                                                        value1 = -1.0,
                                                        value2 = serialObj.Value2 ?: 0.0,
                                                        value3 = serialObj.Value3 ?: 0.0,
                                                        guid = "${state.data?.uniqueID}_${serialObj.SerialNo}"
                                                    )
                                                }
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            showResultDialog = true
                                        }
                                    }
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
                            enabled = if (editingItem != null) {
                                false
                            } else {
                                if (viewmodel.dataState.value.isLoading) {
                                    false
                                } else {
                                    if (isEdit) {
                                        enableUpdateButton && hasSalesmanPermission("ED$vchType")
                                    } else {
                                        selectedLedger.isNotEmpty() && selectedItems.isNotEmpty()
                                    }
                                }
                            },
                            label = if (isEdit) "Update" else "Create",
                            backgroundColor = MaterialTheme.colorScheme.primary
                        )

                        if (viewmodel.dataState.value.error != null) {
                            TallyResultDialog(
                                message = viewmodel.dataState.value.error ?: "Error",
                                onDone = { viewmodel.clearError() },
                                isSuccess = false,
                                confirmText = "Ok"
                            )
                        }

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


// ─────────────────────────────────────────────────────────────────────────────
// Small reusable composables (unchanged from original)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SmallAddButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = "")
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
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
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                if (headerAction != null) headerAction()
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
    onSerialNo: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    index: Int
) {
    val displayedTotal = remember(item, gstPercentage, taxType) {
        val effectivePrice = if (item.CD.isNotBlank() && item.CD.contains("+")) {
            applyCompoundDiscount(item.listPrice, item.CD)
        } else {
            item.price
        }

        if (taxType == TaxType.EXTRA) {
            val taxable = effectivePrice * item.qty
            taxable + taxable * gstPercentage / 100.0
        } else {
            effectivePrice * item.qty
        }
    }
    val effectivePrice = if (item.CD.isNotBlank() && item.CD.contains("+")) {
        applyCompoundDiscount(item.listPrice, item.CD)
    } else {
        item.price
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
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
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onSerialNo) {
                        Text("Serial No${if (item.item_serial.isNotEmpty()) " (${item.item_serial.size})" else ""}")
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(qty: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(24.dp)) {
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
            IconButton(onClick = onIncrease, modifier = Modifier.size(24.dp)) {
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
fun SundryCard(
    sundry: SundryItem,
    onAmountChange: (Double, Double, Int, Double) -> Unit,
    onRemove: () -> Unit,
    index: Int,
    runningTotal: Double,
    shouldFocus: Boolean,
    onFocusConsumed: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
    var isConfirmed by remember(sundry.name) {
        mutableStateOf(sundry.d2.takeIf { it.toDouble() != 0.0 } != null)
    }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(sundry.amount) {
        val amountStr = if (sundry.amount == 0.0) "" else sundry.amount.toString()
        if (amountStr != textValue) textValue = amountStr
    }

    val displayValue = textValue.toDoubleOrNull() ?: 0.0
    val calculatedAmount = if (isPercentage) runningTotal * (displayValue / 100.0) else displayValue

    fun fireAmountChange(value: String) {
        val parsedValue = value.toDoubleOrNull() ?: 0.0
        val finalAmount = if (isPercentage) runningTotal * (parsedValue / 100.0) else parsedValue
        val finalRate = if (isPercentage) parsedValue else 0.0
        onAmountChange(parsedValue, finalRate, index + 1, finalAmount)
    }

    val isInputActive = !isConfirmed || isEditing

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
                            color = if (isConfirmed) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(visible = isInputActive) {
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
                                            filtered.substringBefore('.') + "." + filtered.substringAfter(
                                                '.'
                                            ).replace(".", "")
                                        } else filtered
                                        textValue = validInput
                                        fireAmountChange(validInput)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                }

                if (isInputActive) {
                    IconButton(
                        onClick = {
                            if (textValue.isNotEmpty() && textValue.toDoubleOrNull() != null) {
                                fireAmountChange(textValue)
                                isConfirmed = true
                                isEditing = false
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm amount",
                            modifier = Modifier.size(20.dp),
                            tint = if (textValue.isNotEmpty()) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                val revertValue =
                                    if (sundry.amount == 0.0) "" else sundry.amount.toString()
                                textValue = revertValue
                                isEditing = false
                                focusManager.clearFocus()
                            } else {
                                onRemove()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEditing) "Cancel edit" else "Remove",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(
                        onClick = { isEditing = true; isConfirmed = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit amount",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 2.dp),
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
fun TaxTypeSelector(selectedTaxType: TaxType, onTaxTypeSelected: (TaxType) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.small).clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
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
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BorderedInput(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isEnabled: Boolean = true,
    showPlus: Boolean = false
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = value, selection = TextRange(value.length))
        }
    }
    LaunchedEffect(isFocused) {
        val endRange = if (isFocused) textFieldValue.text.length else 0
        textFieldValue = textFieldValue.copy(selection = TextRange(start = 0, end = endRange))
    }

    Box(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val filtered =
                    newValue.text.filter { it.isDigit() || it == '.' || if (showPlus) it == '+' else it == '.' }
                val dotCount = filtered.count { it == '.' }
                val validText = if (dotCount > 1) {
                    filtered.substringBefore('.') + "." + filtered.substringAfter('.')
                        .replace(".", "")
                } else filtered
                textFieldValue = newValue.copy(text = validText)
                onValueChange(validText)
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
    list: List<ProductsWithConfig>,
    onSelected: (ProductsWithConfig) -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState,
    title: String = "Select Account",
    itemContent: @Composable ((String) -> Unit)? = null
) {
    val listState = rememberSaveable(
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }
    var query by remember { mutableStateOf("") }

    val filteredList = remember(list, query) {
        smartSearch(
            list = list,
            query = query,
            selectors = listOf { it.Name }
        )
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
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
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
                    IconButton(onClick = { onDismiss() }, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            Icons.Default.Close,
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
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    TallySearchBar(searchQuery = query, onQueryChange = { query = it })
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), state = listState) {
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
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelected(item)
                                    onDismiss()
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (itemContent != null) {
                                    itemContent(item.Name.toString())
                                } else {
                                    ListItem(modifier = Modifier.fillMaxWidth(), headlineContent = {
                                        Text(
                                            text = item.Name.toString(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }, trailingContent = {
                                        if (showQtyToSalesman()) {
                                            Text(
                                                text = item.McOpening.formatToQtyDec(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    })

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ExpandedItemEditor1 — now with collapsible descriptions panel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ExpandedItemEditor1(
    name: String,
    defaultListPrice: Double,
    gstPercentage: Double,
    initialQuantity: Int? = 0,
    taxType: TaxType,
    initialDiscount: String,
    existingItem: InvoiceItem? = null,
    initialSerialNumbers: List<String> = emptyList(),
    mainUnit: String = "",
    altUnit: String? = null,
    conFactor: Double? = null,
    conType: Double? = null,
    onAdd: (
        qty: Int,
        unitPrice: Double,
        discount: Double,
        compoundDiscount: String?,
        listPrice: Double,
        taxable: Double,
        gstAmount: Double,
        net: Double,
        gstPercentage: Double,
        itemDescs: List<String?>,
        additionalInfos: List<String?>,
        serialNumbers: List<String>,
        conFactor: Double?,
        conType: Double?,
        selectedUnit: String?,
        altQty: Double?,
    ) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
) {

    var qtyN by remember { mutableStateOf(if (initialQuantity == 0) "" else initialQuantity.toString()) }
    var listPriceN by remember { mutableStateOf(defaultListPrice.formatToAmtDec()) }
    var discountN by remember { mutableStateOf(initialDiscount.toString()) }
    var amountN by remember { mutableStateOf("") }
    var serialNumbers by remember { mutableStateOf(initialSerialNumbers) }
    var selectedUnit by remember { mutableStateOf(existingItem?.selectedUnit ?: mainUnit) }

    var editMode by remember { mutableStateOf(PriceEditMode.LIST_PRICE) }
    var isAmountManuallyEdited by remember { mutableStateOf(false) }

    val isCompoundDiscount by derivedStateOf {
        discountN.contains("+")
    }

    var showDescriptions by remember(existingItem) {
        val hasAnyDesc = existingItem != null && listOf(
            existingItem.itemdesc1, existingItem.itemdesc2, existingItem.itemdesc3,
            existingItem.itemdesc4, existingItem.itemdesc5, existingItem.itemdesc6,
            existingItem.itemdesc7, existingItem.itemdesc8, existingItem.itemdesc9,
            existingItem.itemdesc10, existingItem.itemdesc11, existingItem.itemdesc12,
            existingItem.itemdesc13, existingItem.itemdesc14, existingItem.itemdesc15,
            existingItem.itemdesc16, existingItem.itemdesc17, existingItem.itemdesc18,
            existingItem.itemdesc19, existingItem.itemdesc20,
            existingItem.additionalinfo,
        ).any { !it.isNullOrBlank() }
        mutableStateOf(hasAnyDesc)
    }

    val itemDescs = remember(existingItem) {
        mutableStateListOf(
            existingItem?.itemdesc1 ?: "",
            existingItem?.itemdesc2 ?: "",
            existingItem?.itemdesc3 ?: "",
            existingItem?.itemdesc4 ?: "",
            existingItem?.itemdesc5 ?: "",
            existingItem?.itemdesc6 ?: "",
            existingItem?.itemdesc7 ?: "",
            existingItem?.itemdesc8 ?: "",
            existingItem?.itemdesc9 ?: "",
            existingItem?.itemdesc10 ?: "",
            existingItem?.itemdesc11 ?: "",
            existingItem?.itemdesc12 ?: "",
            existingItem?.itemdesc13 ?: "",
            existingItem?.itemdesc14 ?: "",
            existingItem?.itemdesc15 ?: "",
            existingItem?.itemdesc16 ?: "",
            existingItem?.itemdesc17 ?: "",
            existingItem?.itemdesc18 ?: "",
            existingItem?.itemdesc19 ?: "",
            existingItem?.itemdesc20 ?: "",
        )
    }

    val additionalInfos = remember(existingItem) {
        mutableStateListOf(
            existingItem?.additionalinfo ?: "",
        )
    }

    LaunchedEffect(existingItem) {
        existingItem ?: return@LaunchedEffect
        val descs = listOf(
            existingItem.itemdesc1, existingItem.itemdesc2, existingItem.itemdesc3,
            existingItem.itemdesc4, existingItem.itemdesc5, existingItem.itemdesc6,
            existingItem.itemdesc7, existingItem.itemdesc8, existingItem.itemdesc9,
            existingItem.itemdesc10, existingItem.itemdesc11, existingItem.itemdesc12,
            existingItem.itemdesc13, existingItem.itemdesc14, existingItem.itemdesc15,
            existingItem.itemdesc16, existingItem.itemdesc17, existingItem.itemdesc18,
            existingItem.itemdesc19, existingItem.itemdesc20,
        )
        descs.forEachIndexed { i, v -> itemDescs[i] = v ?: "" }
        additionalInfos[0] = existingItem.additionalinfo ?: ""
    }

    val qtyValue = qtyN.toIntOrNull()?.takeIf { it > 0 } ?: 0

    val unitPrice by derivedStateOf {
        when (editMode) {
            PriceEditMode.LIST_PRICE, PriceEditMode.DISCOUNT -> {
                val lp = listPriceN.replace(",", "").trim().toDoubleOrNull() ?: 0.0

                if (isCompoundDiscount) {
                    applyCompoundDiscount(lp, discountN)
                } else {
                    val dis = discountN.trim().toDoubleOrNull() ?: 0.0
                    lp - (lp * dis / 100.0)
                }
            }

            PriceEditMode.AMOUNT -> {
                if (!isAmountManuallyEdited || qtyValue == 0) return@derivedStateOf 0.0
                val amt = amountN.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                val price = amt / qtyValue
                discountN = "0"
                listPriceN = price.formatToAmtDec()
                price
            }
        }
    }

    val amount by derivedStateOf {
        if (qtyValue > 0) unitPrice * qtyValue else 0.0
    }

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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (altUnit != null && altUnit.isNotBlank() && altUnit != mainUnit) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit: ", style = MaterialTheme.typography.bodySmall)
                    AssistChip(
                        onClick = {
                            if (selectedUnit != mainUnit) {
                                val currentLP =
                                    listPriceN.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                                val factor = conFactor ?: 1.0
                                val newPrice =
                                    if (conType == 1.0) currentLP * factor else currentLP * factor
                                listPriceN =
                                    (kotlin.math.round(newPrice * 100.0) / 100.0).formatToAmtDec()
                                selectedUnit = mainUnit
                            }
                        },
                        label = { Text(mainUnit) },
                        colors = if (selectedUnit == mainUnit) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else AssistChipDefaults.assistChipColors()
                    )
                    AssistChip(
                        onClick = {
                            if (selectedUnit != altUnit) {
                                val currentLP =
                                    listPriceN.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                                val factor = conFactor ?: 1.0
                                val newPrice =
                                    if (conType == 1.0) currentLP / factor else currentLP / factor
                                listPriceN =
                                    (kotlin.math.round(newPrice * 100.0) / 100.0).formatToAmtDec()
                                selectedUnit = altUnit
                            }
                        },
                        label = { Text(altUnit) },
                        colors = if (selectedUnit == altUnit) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else AssistChipDefaults.assistChipColors()
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Column(modifier = Modifier.weight(1f)) {
                    Text("Qty")
                    BorderedInput(
                        value = qtyN,
                        onValueChange = {
                            isAmountManuallyEdited = false
                            editMode = PriceEditMode.LIST_PRICE
                            qtyN = it.filter(Char::isDigit)
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("List Price")
                    BorderedInput(
                        value = listPriceN,
                        isEnabled = hasSalesmanPermission("D36"),
                        onValueChange = {
                            isAmountManuallyEdited = false
                            editMode = PriceEditMode.LIST_PRICE
                            listPriceN = it
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Discount %")
                    BorderedInput(
                        value = discountN,
                        isEnabled = hasSalesmanPermission("D37"),
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() || c == '.' || c == '+' }

                            val parts = filtered.split("+").take(5)
                            val finalValue = parts.joinToString("+")

                            isAmountManuallyEdited = false
                            editMode = PriceEditMode.DISCOUNT
                            discountN = finalValue
                        }, keyboardType = KeyboardType.Text, showPlus = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Amount")
                    BorderedInput(
                        value = if (isAmountManuallyEdited) amountN else formatTwo(amount),
                        isEnabled = hasSalesmanPermission("D36"),
                        onValueChange = {
                            isAmountManuallyEdited = true
                            editMode = PriceEditMode.AMOUNT
                            amountN = it
                        }
                    )
                }
            }

            Row {
                SummaryCell("Taxable", taxableAmount)
                SummaryCell("GST %", gstPercentage, "%")
                SummaryCell("GST Amt", gstAmount)
                SummaryCell("Net", netAmount, highlight = true)
            }

            if (serialNumbers.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Serial Numbers (${serialNumbers.size})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = { serialNumbers = emptyList() }
                            ) {
                                Text("Clear")
                            }
                        }
                        serialNumbers.forEach { serial ->
                            Text(
                                text = "• $serial",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onCancel) { Text("Cancel") }

                Button(
                    enabled = isAddEnabled,
                    onClick = {
                        val factor = conFactor ?: 1.0
                        val calculatedAltQty = if (selectedUnit == altUnit) {
                            qtyValue.toDouble()
                        } else {
                            if (conType == 1.0) { // 1 Main = factor Alt
                                qtyValue.toDouble() * factor
                            } else { // 1 Alt = factor Main => 1 Main = 1/factor Alt
                                qtyValue.toDouble() / factor
                            }
                        }

                        onAdd(
                            qtyValue,
                            unitPrice,
                            if (isCompoundDiscount) 0.0 else discountN.trim().toDoubleOrNull()
                                ?: 0.0,
                            if (isCompoundDiscount) discountN else null,
                            listPriceN.replace(",", "").trim().toDoubleOrNull() ?: 0.0,
                            taxableAmount,
                            gstAmount,
                            netAmount,
                            gstPercentage,
                            itemDescs.map { it.ifBlank { null } },
                            additionalInfos.map { it.ifBlank { null } },
                            serialNumbers,
                            conFactor,
                            conType,
                            selectedUnit,
                            calculatedAltQty
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
    label: String, value: Double, suffix: String = "", highlight: Boolean = false
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
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class PriceEditMode {
    LIST_PRICE, DISCOUNT, AMOUNT
}


//GUID 999 (0-1)
//VCH GUID  same as GUID
//VCHTYPE (Sale Purchase r p)
//Date selected date
//billnumber (reference wala bill number)
//srno 0
//cm1 party select
//cm2 agst ref
//cm3 ""
//billid reference me aarha hai
//d1 amount reference me
//d2 e2 null


fun makeNegativeConditional(number: Double): Double {
    return if (number >= 0) {
        -number
    } else {
        number
    }
}

fun applyCompoundDiscount(basePrice: Double, compoundDiscountStr: String): Double {
    if (compoundDiscountStr.isBlank()) return basePrice

    val parts = compoundDiscountStr.split("+")
        .mapNotNull { it.trim().toDoubleOrNull() }
        .take(5)

    var result = basePrice
    for (discount in parts) {
        result -= result * discount / 100.0
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPricingBottomSheet(
    show: Boolean,
    productName: String,
    pricingList: List<ProductPricing>,
    onSelect: (ProductPricing) -> Unit,
    onDismiss: () -> Unit,
    bottomSheetState: SheetState
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Select Pricing for $productName",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                HorizontalDivider()

                LazyColumn {
                    items(pricingList) { pricing ->
                        ProductPricingItem(
                            pricing = pricing,
                            onSelect = { onSelect(pricing) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPricingItem(
    pricing: ProductPricing,
    onSelect: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onSelect),
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pricing.ProductName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatTwo(pricing.SalePrice),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        supportingContent = {
            Column {
                if (pricing.Discount > 0 || pricing.CompoundDiscount.isNotBlank()) {
                    Text(
                        text = "Discount: ${if (pricing.CompoundDiscount.isNotBlank()) pricing.CompoundDiscount else "${pricing.Discount}%"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}
