package org.prime.easykarobar.ui.screen.reports.outstanding

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.OutstandingRow
import org.prime.easykarobar.ui.printing.outstandingHtml
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportItemScreen
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.outstandingFilter
import org.prime.easykarobar.ui.shared.globalShared.parseToDoubleList
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.DueDays
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import smartSearch
import kotlin.math.absoluteValue

data class OutstandingReportScreen(
    val name: String, val startDate: String, val endDate: String, val cm1: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var receivableList by remember { mutableStateOf<List<DataList>>(emptyList()) }
        var payableList by remember { mutableStateOf<List<DataList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Name") }
        val nav = LocalNavigator.currentOrThrow

        val perms = SharedPrefs.Permissions.get()
        val filterBroker = if (perms?.FilterBroker == "Y") 1L else 0L
        val configBroker =
            if (filterBroker == 1L) perms?.ConfigBroker.parseToStringList() else emptyList()
        val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
        val filterAccounts = if (perms?.FilterAccounts == "Y") 1L else 0L
        val groupCodes = perms?.ConfigAGRP.parseToDoubleList()
        val excludeGuids = perms?.ConfigAccounts.parseToStringList()
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        var showGroupFilterSheet by remember { mutableStateOf(false) }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val productGroups = remember { db.ledgerGroupMasterQueries.selectAll().executeAsList() }

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                if (name == "Bill Receivable") {
                    if (cm1 != "") {
                        receivableList = db.voucherBillAllocationsQueries.billReceivableLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1,
                            filterCm3 = filterBroker,
                            cm3 = configBroker
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName
                            )
                        }
                    } else {
                        println(outstandingFilter())
                        receivableList = db.voucherBillAllocationsQueries.billReceivableList(
                            DATE = startDate,
                            DATE_ = endDate,
                            filterCm3 = filterBroker,
                            cm3 = configBroker,
                            groupFilter = filterAGRP,
                            GroupCode = groupCodes,
                            excludeFilter = filterAccounts,
                            GUID = excludeGuids
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName
                            )
                        }
                    }
                }
                if (name == "Pending Sale Order") {
                    if (cm1 != "") {
                        receivableList = db.vouchersPendingOrderQueries.pendingPurchaseOrderLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1,
                            filterCm3 = filterBroker,
                            cm3 = configBroker
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName,
                                itemName = it.ItemName
                            )
                        }
                    } else {
                        println(outstandingFilter())
                        receivableList = db.vouchersPendingOrderQueries.pendingPurchaseOrderList(
                            DATE = startDate,
                            DATE_ = endDate,
                            filterCm3 = filterBroker,
                            cm3 = configBroker,
                            groupFilter = filterAGRP,
                            GroupCode = groupCodes,
                            excludeFilter = filterAccounts,
                            GUID = excludeGuids
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName,
                                itemName = it.ItemName
                            )
                        }
                    }
                }
                if (name == "Pending Purchase Order") {
                    if (cm1 != "") {
                        payableList = db.vouchersPendingOrderQueries.pendingSaleOrderLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1,
                            filterCm3 = filterBroker,
                            cm3 = configBroker
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName,
                                itemName = it.ItemName
                            )
                        }
                    } else {
                        println(outstandingFilter())
                        payableList = db.vouchersPendingOrderQueries.pendingSaleOrderList(
                            DATE = startDate,
                            DATE_ = endDate,
                            filterCm3 = filterBroker,
                            cm3 = configBroker,
                            groupFilter = filterAGRP,
                            GroupCode = groupCodes,
                            excludeFilter = filterAccounts,
                            GUID = excludeGuids
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName,
                                itemName = it.ItemName
                            )
                        }
                    }
                }
                if (name == "Bill Payable") {
                    if (cm1 != "") {
                        payableList = db.voucherBillAllocationsQueries.billPayableLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1,
                            filterCm3 = filterBroker,
                            cm3 = configBroker
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName
                            )
                        }
                    } else {
                        payableList = db.voucherBillAllocationsQueries.billPayableList(
                            DATE = startDate,
                            DATE_ = endDate,
                            filterCm3 = filterBroker,
                            cm3 = configBroker,
                            groupFilter = filterAGRP,
                            GroupCode = groupCodes,
                            excludeFilter = filterAccounts,
                            GUID = excludeGuids
                        ).executeAsList().map {
                            DataList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount,
                                GroupName = it.GroupName
                            )
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
            isLoading = false
        }

        LaunchedEffect(showSearchBar) {
            if (showSearchBar) focusRequester.requestFocus()
        }

        // Apply group filter
        val groupFilteredReceivableList = if (selectedGroups.isEmpty()) {
            receivableList
        } else {
            receivableList.filter { it.GroupName in selectedGroups }
        }

        val filteredReceivableList = smartSearch(
            list = groupFilteredReceivableList.filter { it.adjustmentAmount?.toDouble() != 0.0 },
            query = searchQuery,
            selectors = listOf { item ->
                if (selectedOption == "Name") item.cm1 else item.billNumber
            })

        val groupFilteredPayableList = if (selectedGroups.isEmpty()) {
            payableList
        } else {
            payableList.filter { it.GroupName in selectedGroups }
        }

        val filteredPayableList = smartSearch(
            list = groupFilteredPayableList.filter { it.adjustmentAmount?.toDouble() != 0.0 },
            query = searchQuery,
            selectors = listOf { item ->
                if (selectedOption == "Name") item.cm1 else item.billNumber
            })

        // Calculate totals based on filtered data
        val totalRefAmt = if (name == "Bill Receivable") {
            groupFilteredReceivableList.sumOf { it.d1?.absoluteValue ?: 0.0 }
        } else {
            groupFilteredPayableList.sumOf { it.d1?.absoluteValue ?: 0.0 }
        }

        val totalPendingAmt = if (name == "Bill Receivable") {
            groupFilteredReceivableList.sumOf {
                it.adjustmentAmount?.toDouble()?.absoluteValue ?: 0.0
            }
        } else {
            groupFilteredPayableList.sumOf { it.adjustmentAmount?.toDouble()?.absoluteValue ?: 0.0 }
        }

        val ledgerBalType = if (name == "Bill Receivable") "Dr" else "Cr"

        fun generateOutstandingHtml(): String {
            val allRows = if (name == "Bill Receivable") {
                groupFilteredReceivableList.map { item ->
                    OutstandingRow(
                        date = item.date ?: "",
                        vchType = item.vchType ?: "",
                        refNo = item.billNumber ?: "",
                        refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                        pendingAmount = item.adjustmentAmount?.absoluteValue?.toDouble()
                            ?.formatToAmtDec()?.toDouble() ?: 0.0,
                        due = "Y",
                        dueDate = item.dueDate ?: "",
                        dueDays = DueDays(endDate, item.dueDate.toString())
                    )
                }
            } else {
                groupFilteredPayableList.map { item ->
                    OutstandingRow(
                        date = item.date ?: "",
                        vchType = item.vchType ?: "",
                        refNo = item.billNumber ?: "",
                        refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                        pendingAmount = item.adjustmentAmount?.absoluteValue?.toDouble()
                            ?.formatToAmtDec()?.toDouble() ?: 0.0,
                        due = "Y",
                        dueDate = item.dueDate ?: "",
                        dueDays = DueDays(endDate, item.dueDate.toString())
                    )
                }
            }

            val accountName = if (name == "Bill Receivable") {
                groupFilteredReceivableList.firstOrNull()?.cm1 ?: "All Accounts"
            } else {
                groupFilteredPayableList.firstOrNull()?.cm1 ?: "All Accounts"
            }

            return outstandingHtml(
                title = if (name == "Bill Receivable") "Bills Receivable" else "Bills Payable",
                accountName = accountName,
                onBasis = "Due Date",
                startDate = startDate,
                endDate = endDate,
                billStatusDate = endDate,
                rows = allRows,
                totalRefAmt = totalRefAmt,
                totalPendingAmt = totalPendingAmt,
                onAcc = 0.0,
                ledgerBal = totalPendingAmt,
                ledgerBalType = ledgerBalType
            )
        }

        val menuItems = listOf(
            MenuItemData(
            title = "Download", icon = Icons.Default.Download, onClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "${name.replace(" ", "_")}_Report",
                        htmlContent = generateOutstandingHtml(),
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it })
                }
            }), MenuItemData(
            title = "Share", icon = Icons.Default.Share, onClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "${name.replace(" ", "_")}_Report",
                        htmlContent = generateOutstandingHtml(),
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it })
                }
            }))

        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        // Group Filter Bottom Sheet
        GroupFilterBottomSheet(
            show = showGroupFilterSheet,
            items = productGroups,
            selectedItems = selectedGroups,
            itemNameSelector = { it.Name },
            onSelectedItemsChange = { selectedGroups = it },
            onDismiss = { showGroupFilterSheet = false },
            bottomSheetState = bottomSheetState
        )

        TallyReportScaffold(
            title = "$name Report",
            showBurgerMenu = true,
            menuItems = menuItems,
            showBottomBar = true,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${
                                if (name == "Bill Receivable"|| name == "Pending Sale Order") {
                                    filteredReceivableList.count()
                                } else filteredPayableList.count()
                            }", 1f, TextAlign.Start
                        ),
                        ReportColumn(
                            "Vch Amt: ${
                                if (name == "Bill Receivable" || name == "Pending Sale Order") {
                                    filteredReceivableList.sumOf { it.d1 ?: 0.0 }.absoluteValue.formatToAmtDec()
                                } else filteredPayableList.sumOf { it.d1 ?: 0.0 }.absoluteValue.formatToAmtDec()
                            }", 1f, TextAlign.End
                        ),
                        ReportColumn(
                            "Pen Amt: ${
                                if (name == "Bill Receivable" || name =="Pending Sale Order") {
                                    filteredReceivableList.sumOf { it.adjustmentAmount?.toDouble() ?: 0.0 }.absoluteValue.formatToAmtDec()
                                } else filteredPayableList.sumOf { it.adjustmentAmount?.toDouble() ?: 0.0 }.absoluteValue.formatToAmtDec()
                            }", 1f, TextAlign.End
                        ),
                    ),
                )
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                            .padding(horizontal = 8.dp)
                    ) {
                        if (showSearchBar) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.weight(0.8f).padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column {
                                        OutlinedButton(
                                            onClick = { expanded = true },
                                            modifier = Modifier.fillMaxWidth().height(56.dp)
                                        ) {
                                            Text(selectedOption)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }) {
                                            DropdownMenuItem(text = { Text("Name") }, onClick = {
                                                selectedOption = "Name"
                                                expanded = false
                                            })
                                            DropdownMenuItem(
                                                text = { Text("Bill No.") },
                                                onClick = {
                                                    selectedOption = "Bill No."
                                                    expanded = false
                                                })
                                        }
                                    }
                                }

                                TallySearchBar(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier.focusRequester(focusRequester).weight(2f)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "From ${Tdate(startDate)}  →  To ${Tdate(endDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showGroupFilterSheet = true }) {
                                    Text(
                                        if (selectedGroups.isEmpty()) "Group Filter"
                                        else "Group Filter (${selectedGroups.size})"
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.padding(vertical = 8.dp))

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (name == "Bill Receivable" || name =="Pending Sale Order") {
                                if (filteredReceivableList.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "No result found",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    items(filteredReceivableList) { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
                                                    nav.push(
                                                        LedgerReportItemScreen(
                                                            vchNo = item.billNumber.toString(),
                                                            date = item.date.toString(),
                                                            vchType = item.vchType.toString(),
                                                            guid = item.VCH_GUID.toString()
                                                        )
                                                    )
                                                }, colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ), elevation = CardDefaults.cardElevation(
                                                defaultElevation = 1.5.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp, vertical = 10.dp
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill No.:-  ${item.billNumber}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = true
                                                    )
                                                    TableCell(
                                                        "Date: ${Tdate(item.date.toString())}",
                                                        1f,
                                                        textAlign = TextAlign.End,
                                                        isHeader = true
                                                    )
                                                }

                                                Spacer(Modifier.height(4.dp))

                                                if(item.itemName!=null){
                                                    Row {
                                                        TableCell(
                                                            item.itemName,
                                                            1f,
                                                            textAlign = TextAlign.Start,
                                                            isHeader = false
                                                        )
                                                    }
                                                }
                                                Row {
                                                    TableCell(
                                                        item.cm1.toString(),
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                }

                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill: ${item.d1?.absoluteValue?.formatToAmtDec()}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Pending: ${
                                                            item.adjustmentAmount?.absoluteValue?.toDouble()
                                                                ?.formatToAmtDec()
                                                        }",
                                                        1f,
                                                        textAlign = TextAlign.End,
                                                        isHeader = false
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Row {
                                                    TableCell(
                                                        "Due: ${Tdate(item.dueDate.toString())} (${
                                                            DueDays(
                                                                endDate, item.dueDate.toString()
                                                            ) + " Days"
                                                        })",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (name == "Bill Payable" || name =="Pending Purchase Order") {
                                if (filteredPayableList.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "No result found",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                } else {
                                    items(filteredPayableList) { item ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
                                                    nav.push(
                                                        LedgerReportItemScreen(
                                                            vchNo = item.billNumber.toString(),
                                                            date = item.date.toString(),
                                                            vchType = item.vchType.toString(),
                                                            guid = item.VCH_GUID.toString()
                                                        )
                                                    )
                                                }, colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ), elevation = CardDefaults.cardElevation(
                                                defaultElevation = 1.5.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp, vertical = 10.dp
                                                )
                                            ) {
                                                Row {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        TableCell(
                                                            "Bill No. ${item.billNumber}",
                                                            1f,
                                                            textAlign = TextAlign.Start,
                                                            isHeader = true
                                                        )
                                                        TableCell(
                                                            "Date: ${Tdate(item.date.toString())}",
                                                            1f,
                                                            textAlign = TextAlign.End,
                                                            isHeader = true
                                                        )
                                                    }
                                                }
                                                if(item.itemName!=null){
                                                    Row {
                                                        TableCell(
                                                            item.itemName,
                                                            1f,
                                                            textAlign = TextAlign.Start,
                                                            isHeader = false
                                                        )
                                                    }
                                                }
                                                Row {
                                                    Spacer(Modifier.height(4.dp))
                                                    TableCell(
                                                        item.cm1.toString(),
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                }

                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill: ${item.d1?.absoluteValue?.formatToAmtDec()}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Pending: ${
                                                            item.adjustmentAmount?.absoluteValue?.toDouble()
                                                                ?.formatToAmtDec()
                                                        }",
                                                        1f,
                                                        textAlign = TextAlign.End,
                                                        isHeader = false
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Row {
                                                    TableCell(
                                                        "Due: ${Tdate(item.dueDate.toString())} (${
                                                            DueDays(
                                                                endDate, item.dueDate.toString()
                                                            ) + " Days"
                                                        })",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
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
            })
    }
}

data class DataList(
    val VCH_GUID: String?,
    val date: String?,
    val vchType: String?,
    val billNumber: String?,
    val cm1: String?,
    val dueDate: String?,
    val d1: Double?,
    val adjustmentAmount: Double?,
    val GroupName: String?,
    val itemName: String? = null  // Add this field
)