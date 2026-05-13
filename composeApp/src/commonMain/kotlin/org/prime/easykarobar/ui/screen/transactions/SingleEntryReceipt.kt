package org.prime.easykarobar.ui.screen.transactions

import CurrentDate
import TallyDatePickerRow
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.transactions.SingleEntryViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.hasSalesmanPermission
import org.prime.easykarobar.data.model.transactions.TranListResponse
import org.prime.easykarobar.data.model.transactions.TranRequest
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.EntryTypesHtml
import org.prime.easykarobar.ui.printing.entryTypesHtml
import org.prime.easykarobar.ui.screen.home.tabs.InfoRow
import org.prime.easykarobar.ui.screen.transactions.sale.makeNegativeConditional
import org.prime.easykarobar.ui.shared.composables.DownloadResultDialog
import org.prime.easykarobar.ui.shared.composables.TallyButton
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyScaffold
import org.prime.easykarobar.ui.shared.composables.TallyTextField
import org.prime.easykarobar.ui.shared.globalShared.filterGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.getLedgerMasters
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class SingleEntryReceipt(
    val name: String,
    val vchType: Int,
    val existingTransaction: TranListResponse? = null,
    val showPdc: Boolean = false
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow
        var selectedReferences by remember {
            mutableStateOf(listOf<BillByBillModel>())
        }
        var showBillModalSheet by remember { mutableStateOf(false) }
        var pdcType by remember {
            mutableStateOf(
                existingTransaction?.pdcType ?: PDCTYPE.REGULAR.name
            )
        }

        var selectedAccount by rememberSaveable { mutableStateOf(existingTransaction?.C1 ?: "") }
        var selectedAccountGUID by rememberSaveable {
            mutableStateOf(
                existingTransaction?.CM1 ?: ""
            )
        }
        var selectedSettlement by rememberSaveable { mutableStateOf(existingTransaction?.C2 ?: "") }
        var selectedSettlementGUID by rememberSaveable {
            mutableStateOf(
                existingTransaction?.CM2 ?: ""
            )
        }
        var amount by rememberSaveable { mutableStateOf(existingTransaction?.D2?.toString() ?: "") }
        var narration by rememberSaveable { mutableStateOf(existingTransaction?.Narration ?: "") }
        var showInstrumentSection by rememberSaveable { mutableStateOf(false) }
        var selectedInstrument by rememberSaveable { mutableStateOf(existingTransaction?.instrumentName?:"--N.A.--") }
        var instrumentNo by rememberSaveable { mutableStateOf(existingTransaction?.instrumentNumber?:"")}
        var instrumentDropdownExpanded by remember { mutableStateOf(false) }
        val instrumentOptions = listOf(
            "--N.A.--",
            "RTGS",
            "NEFT",
            "CHQ",
            "P.O",
            "D.D",
            "ECS",
            "A2A",
            "IMPS",
            "E-PYMT",
            "TRANSFER",
            "WALLET",
            "UPI",
            "TREDS",
            "OTHER"
        )
        var selectedDate by rememberSaveable {
            mutableStateOf(
                existingTransaction?.TranDate ?: CurrentDate()
            )
        }
        var selectedPdcDate by rememberSaveable {
            mutableStateOf(
                existingTransaction?.pdcDate ?: CurrentDate()
            )
        }
        println(existingTransaction)


        var showPopup by remember { mutableStateOf(false) }
        var showBottomSheet by rememberSaveable { mutableStateOf(false) }
        var showSettlementBottomSheet by rememberSaveable { mutableStateOf(false) }
        var showPdcDate by rememberSaveable { mutableStateOf(false) }
        val list = getLedgerMasters(db)


        val viewmodel: SingleEntryViewModel = viewModel { SingleEntryViewModel() }
        val state by viewmodel.dataState

        val isEdit = existingTransaction != null
        var uniqueId by remember { mutableStateOf("") }

        if (pdcType == PDCTYPE.PDC.name) showPdcDate = true
        if (pdcType == PDCTYPE.REGULAR.name) showPdcDate = false
        if (existingTransaction != null) {
            //TODO: make selectedbills from the list
            uniqueId = state.data?.uniqueID.toString()
            selectedReferences = existingTransaction.bills_collection
        }
        val compInfo = db.companyInformationQueries.selectAll().executeAsOne()

        if (state.isLoading) {
            if (isEdit) {

                TallyLoadingDialog("Editing your transaction")
            } else {

                TallyLoadingDialog("Creating your transaction")
            }
        }


        TallyScaffold(
            title = if (isEdit) "Edit $name Entry" else "Create $name Entry",
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    if (isEdit) {
                        InfoRow("Voucher No", existingTransaction?.VchNo ?: "")
                    }

                    InfoRow(
                        icon = Icons.Default.Badge,
                        label = "GST Number",
                        value = compInfo.T4.toString()
                    )


                    TallyDatePickerRow(
                        label = "Entry Date",
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        defaultDate = CurrentDate(),

                        )

                    if (showPdc) {
                        PdcTypeSelector(
                            pdcType = PDCTYPE.valueOf(pdcType),
                            onChange = {
                                pdcType = it.name
                            }
                        )
                    }
                    AnimatedVisibility(visible = showPdcDate) {
                        TallyDatePickerRow(
                            label = "PDC Date",
                            selectedDate = selectedPdcDate,
                            onDateSelected = { selectedPdcDate = it },
                            defaultDate = CurrentDate(),

                            )
                    }

                    SelectLedgerRow(
                        selectedAccount = selectedAccount,
                        onShowBottomSheet = { showBottomSheet = true },

                        title = "Ledger"
                    )


                    SelectLedgerRow(
                        selectedAccount = selectedSettlement,
                        onShowBottomSheet = { showSettlementBottomSheet = true },

                        title = "Settlement"
                    )

                    TallyAmountField(
                        value = amount, onValueChange = { amount = it }, label = "Amount"
                    )
                    TallyNarrationField(
                        value = narration, onValueChange = { narration = it }, label = "Narration"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bank Instruments",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showInstrumentSection = !showInstrumentSection }
                        ) {
                            Icon(
                                imageVector = if (showInstrumentSection) Icons.Default.RemoveCircleOutline
                                else Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showInstrumentSection) "Remove" else "Add",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    AnimatedVisibility(visible = showInstrumentSection) {
                        ExposedDropdownMenuBox(
                            expanded = instrumentDropdownExpanded,
                            onExpandedChange = { instrumentDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedInstrument,
                                    onValueChange = { },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    readOnly = true,
                                    label = { Text("Instrument Type") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = instrumentDropdownExpanded)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.5f
                                        ),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.3f
                                        ),
                                    ),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )

                                ExposedDropdownMenu(
                                    expanded = instrumentDropdownExpanded,
                                    onDismissRequest = { instrumentDropdownExpanded = false }
                                ) {
                                    instrumentOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedInstrument = option
                                                instrumentDropdownExpanded = false
                                            }
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = selectedInstrument != "Nil") {
                                    TallyTextField(
                                        value = instrumentNo,
                                        onValueChange = { instrumentNo = it },
                                        label = "Instrument No",
                                        placeholder = "Enter instrument number",
                                        isPassword = false,
                                        isNumber = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
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
                                enabled = (selectedAccount != "") && (amount != "")
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


                    println("Selected references $selectedReferences")
                    TransactionBillBottomSheet(
                        cm1 = selectedAccount,
                        showBottomSheet = showBillModalSheet,
                        ledgerGuid = selectedAccountGUID,
                        initialSelectedBills = selectedReferences,
                        onBillsSelected = { selectedReferences = it },
                        onDismiss = { showBillModalSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(
                            skipPartiallyExpanded = true
                        ),
                        title = "Bill by Bill",
                        totalAmount = amount.toDoubleOrNull() ?: 0.0,
                        isEdit = isEdit,
                        uniqueId = uniqueId,
                        vchType = vchType
                    )
                    TallyButton(
                        onClick = {

                            if (isEdit) {
                                viewmodel.updateSingleTran(

                                    TranRequest(
                                        TransactionID = existingTransaction.TransactionID,
                                        VchType = vchType,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        Narration = narration,
                                        bills_collection = selectedReferences,
                                        instrumentName = selectedInstrument,
                                        instrumentNumber = instrumentNo,
                                        pdcDate = if (showPdcDate) selectedPdcDate else null,
                                        pdcType = if (showPdcDate) PDCTYPE.PDC.name else PDCTYPE.REGULAR.name,
                                    ), onSuccess = {
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

                                                    vchtype = ref.vchType,

                                                    date = ref.date,

                                                    duedate = ref.dueDate,

                                                    billnumber = ref.billNumber,

                                                    // order matters more than you think later
                                                    srno = (index + 1).toLong(),

                                                    // you're already storing cm1 in data → don’t ignore it
                                                    cm1 = selectedAccount,

                                                    cm2 = "Agst Ref", // still hardcoded, your call

                                                    cm3 = "",

                                                    billid = ref.billId?.toDoubleOrNull(),

                                                    //TODO: logic for sale me + purc me minus
                                                    //d1 = ref.d1?.absoluteValue,
                                                    d1 = when (vchType) {
                                                        9 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        3 -> {
                                                            ref.d1
                                                        }

                                                        2 -> {
                                                            ref.d1
                                                        }

                                                        10 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        14 -> {
                                                            ref.d1?.absoluteValue
                                                        }

                                                        17 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        18, 19 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        16 -> {
                                                            ref.d1?.absoluteValue
                                                        }

                                                        else -> {
                                                            ref.d1?.absoluteValue
                                                        }
                                                    },

                                                    d2 = null,

//if pending >0
                                                    e2 = null
                                                )
                                            }
                                        }
                                        showPopup = true
                                    })
                            } else {
                                println(
                                    "THE SENT IS: " + TranRequest(
                                        VchType = vchType,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        Narration = narration,
                                        instrumentName = selectedInstrument,
                                        instrumentNumber = instrumentNo,
                                        bills_collection = selectedReferences,
                                        pdcDate = if (showPdcDate) selectedPdcDate else null,
                                        pdcType = if (showPdcDate) PDCTYPE.PDC.name else PDCTYPE.REGULAR.name,
                                    )
                                )
                                viewmodel.addSingleTran(
                                    TranRequest(
                                        VchType = vchType,
                                        TranDate = selectedDate,
                                        CM1 = selectedAccountGUID,
                                        CM2 = selectedSettlementGUID,
                                        CM3 = "",
                                        CM4 = "",
                                        C1 = selectedAccount,
                                        C2 = selectedSettlement,
                                        C3 = "",
                                        C4 = "",
                                        D1 = amount.toDouble(),
                                        D2 = amount.toDouble(),
                                        D3 = 0.0,
                                        D4 = 0.0,
                                        instrumentName = selectedInstrument,
                                        instrumentNumber = instrumentNo,
                                        Narration = narration,
                                        bills_collection = selectedReferences,
                                        pdcDate = if (showPdcDate) selectedPdcDate else null,
                                        pdcType = if (showPdcDate) PDCTYPE.PDC.name else PDCTYPE.REGULAR.name,
                                    ), onSuccess = {
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

                                                    vchtype = ref.vchType,

                                                    date = ref.date,

                                                    duedate = ref.dueDate,

                                                    billnumber = ref.billNumber,

                                                    // order matters more than you think later
                                                    srno = (index + 1).toLong(),

                                                    // you're already storing cm1 in data → don’t ignore it
                                                    cm1 = selectedAccount,

                                                    cm2 = "Agst Ref", // still hardcoded, your call

                                                    cm3 = "",

                                                    billid = ref.billId?.toDoubleOrNull(),

                                                    //TODO: logic for sale me + purc me minus
                                                    //d1 = ref.d1?.absoluteValue,
                                                    d1 = when (vchType) {
                                                        9 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        3 -> {
                                                            ref.d1
                                                        }

                                                        2 -> {
                                                            ref.d1
                                                        }

                                                        10 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        14 -> {
                                                            //makeNegativeConditional(ref.d1 ?: 0.0)
                                                            ref.d1?.absoluteValue
                                                        }

                                                        19 -> {
                                                            makeNegativeConditional(ref.d1 ?: 0.0)
                                                        }

                                                        16 -> {
                                                            ref.d1?.absoluteValue
                                                        }

                                                        else -> {
                                                            ref.d1?.absoluteValue
                                                        }
                                                    },

                                                    d2 = null,

//if pending >0
                                                    e2 = null
                                                )
                                            }
                                        }
                                        showPopup = true
                                    })
                            }


                        },
                        enabled = listOf(
                            selectedAccount,
                            selectedAccountGUID,
                            selectedSettlement,
                            selectedSettlementGUID,
                            amount,
                            selectedDate
                        ).all(String::isNotEmpty) && (!isEdit || hasSalesmanPermission("ED$vchType")) && !state.isLoading,
                        label = if (isEdit) "Modify" else "Create",
                        backgroundColor = MaterialTheme.colorScheme.primary
                    )

                    val filteredLedgerList = if (name == "Contra") {
                        list.filter { it.L2 == 1.0 || it.L3 == 1.0 }
                    } else if (vchType != 16) {
                        list.filter { it.L1 == 1.0 }
                    } else {
                        list
                    }

                    val filteredSettlementList = list.filter { it.L2 == 1.0 || it.L3 == 1.0 }

                    TransactionBottomSheet(
                        showBottomSheet = showBottomSheet,
                        list = filteredLedgerList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelected = {
                            it.let {
                                selectedAccount = it.first
                                selectedAccountGUID = it.second
                            }
                        },
                        onDismiss = { showBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        title = "Ledger Name",
                    )
                    TransactionBottomSheet(
                        showBottomSheet = showSettlementBottomSheet,
                        list = if (vchType != 16) filteredSettlementList.map {
                            Pair(
                                it.Name ?: "",
                                it.GUID ?: ""
                            )
                        } else filteredLedgerList.map { Pair(it.Name ?: "", it.GUID ?: "") },
                        onSelected = {
                            it.let {
                                selectedSettlement = it.first
                                selectedSettlementGUID = it.second
                            }
                        },
                        onDismiss = { showSettlementBottomSheet = false },
                        bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        title = "Settlement",
                    )
                }
                if (showPopup) {
                    DownloadResultDialog(
                        message = (state.message + " ${state.data?.VoucherNumber ?: ""}"),
                        onDone = {
                            if (state.success) {
                                showPopup = false
                                nav.pop()

                            } else {
                                showPopup = false
                            }

                        },
                        isSuccess = state.success,
                        confirmText = if (state.success) "Done" else "Try Again",
                        fileName = "${name}_${existingTransaction?.VchNo ?: state.data?.VoucherNumber}",
                        htmlContent = entryTypesHtml(
                            voucherNo = state.data?.VoucherNumber.toString(),
                            date = selectedDate,
                            data = EntryTypesHtml(
                                ledger = selectedAccount,
                                settlement = selectedSettlement,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                bills = selectedReferences
                            ),
                            title = name
                        ),
                    )
                }
            },
        )

    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label, style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ), modifier = Modifier.weight(1f, fill = false)
            )

            Text(
                text = value, style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary
                ), textAlign = TextAlign.End, modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun SelectLedgerRow(
    selectedAccount: String,
    onShowBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    enabled: Boolean = true
) {
    val db = DatabaseHolder.instance
    val perms = SharedPrefs.Permissions.get()
    val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
    val filterAccounts = if (perms?.FilterAccounts == "Y") 1L else 0L
    val excludeGuids = perms?.ConfigAccounts.parseToStringList()
    var balance by remember { mutableStateOf(0.0) }
    LaunchedEffect(selectedAccount) {
        if (selectedAccount.isNotEmpty()) {
            balance = db.vouchersLedgersQueries.getLedgerBalance(
                groupFilter = filterAGRP,
                GroupCode = filterGroupCodes(),
                excludeFilter = filterAccounts,
                GUID = excludeGuids,
                ledgername = selectedAccount
            ).executeAsOneOrNull()?.ClsnBal ?: 0.0
        }
    }

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        selectedAccount.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val bgColor = if (enabled) MaterialTheme.colorScheme.surfaceContainerLow
    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)

    val textColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        selectedAccount.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

        else -> MaterialTheme.colorScheme.onSurface
    }

    val iconColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        selectedAccount.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Select $title",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(1.dp, borderColor),
            enabled = enabled,
            onClick = {
                if (enabled) onShowBottomSheet()
            }) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Select $title",
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = selectedAccount.ifEmpty { "Select $title" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = if (selectedAccount.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = "Balance: ${balance.absoluteValue.formatToAmtDec()} ${if (balance < 0.0) "Dr" else "Cr"}",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

enum class PDCTYPE {
    REGULAR, PDC
}

@Composable
fun PdcTypeSelector(
    pdcType: PDCTYPE,
    onChange: (PDCTYPE) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PDCTYPE.entries.forEach { type ->
            val isSelected = pdcType == type

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onChange(type) },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant
                ),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            modifier = Modifier.size(18.dp),
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (type) {
                            PDCTYPE.REGULAR -> "Regular"
                            PDCTYPE.PDC -> "PDC"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}