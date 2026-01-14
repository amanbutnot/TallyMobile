package org.prime.tally.ui.screen.reports.outstanding

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.printing.OutstandingRow
import org.prime.tally.ui.printing.outstandingHtml
import org.prime.tally.ui.screen.reports.ledger.LedgerReportItemScreen
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.globalShared.outstandingFilter
import org.prime.tally.ui.shared.globalShared.parseToDoubleList
import org.prime.tally.ui.shared.globalShared.parseToStringList
import org.prime.tally.ui.shared.reportsShared.DueDays
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.BillPayableList
import org.tally.BillReceivableList
import kotlin.math.absoluteValue

data class OutstandingReportScreen(
    val name: String,
    val startDate: String,
    val endDate: String,
    val cm1: String? = null
) :
    Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var receivableList by remember { mutableStateOf<List<BillReceivableList>>(emptyList()) }
        var payableList by remember { mutableStateOf<List<BillPayableList>>(emptyList()) }
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
        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                if (name == "Bill Receivable") {
                    if (cm1 != "") {
                        receivableList = db.voucherBillAllocationsQueries.billReceivableLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1, filterCm3 = filterBroker, cm3 = configBroker
                        ).executeAsList().map {
                            BillReceivableList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount
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
                        ).executeAsList()
                    }

                }
                if (name == "Bill Payable") {
                    if (cm1 != "") {
                        payableList = db.voucherBillAllocationsQueries.billPayableLedgerList(
                            DATE = startDate,
                            DATE_ = endDate,
                            CM1 = cm1, filterCm3 = filterBroker, cm3 = configBroker
                        ).executeAsList().map {
                            BillPayableList(
                                VCH_GUID = it.VCH_GUID,
                                date = it.date,
                                vchType = it.vchType,
                                billNumber = it.billNumber,
                                cm1 = it.cm1,
                                dueDate = it.dueDate,
                                d1 = it.d1,
                                adjustmentAmount = it.adjustmentAmount
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
                        ).executeAsList()
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

        val filteredReceivableList = if (searchQuery.isEmpty()) {
            receivableList.filter { it.adjustmentAmount?.toDouble() !=0.0 }
            receivableList
        } else {
            val startsWith = receivableList.filter {
                it.adjustmentAmount?.toDouble()!=0.0
                if (selectedOption == "Name") {
                    it.cm1?.startsWith(searchQuery, ignoreCase = true) == true
                } else {
                    it.billNumber?.startsWith(searchQuery, ignoreCase = true) == true
                }
            }

            val contains = receivableList.filter {
                it.adjustmentAmount?.toDouble()!=0.0
                if (selectedOption == "Name") {
                    val value = it.cm1
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value?.startsWith(searchQuery, ignoreCase = true) == false
                } else {
                    val value = it.billNumber
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value?.startsWith(searchQuery, ignoreCase = true) == false
                }
            }

            startsWith + contains
        }

        val filteredPayableList = if (searchQuery.isEmpty()) {
            payableList.filter { it.adjustmentAmount?.toDouble()!=0.0 }
        } else {
            val startsWith = payableList.filter {
                it.adjustmentAmount?.toDouble()!=0.0
                if (selectedOption == "Name") {
                    it.cm1?.startsWith(searchQuery, ignoreCase = true) == true
                } else {
                    it.billNumber?.startsWith(searchQuery, ignoreCase = true) == true
                }
            }

            val contains = payableList.filter {
                it.adjustmentAmount?.toDouble()!=0.0
                if (selectedOption == "Name") {
                    val value = it.cm1
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value.startsWith(searchQuery, ignoreCase = true) == false
                } else {
                    val value = it.billNumber
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value.startsWith(searchQuery, ignoreCase = true) == false
                }
            }

            startsWith + contains
        }

        // Calculate totals
        val totalRefAmt = if (name == "Bill Receivable") {
            receivableList.sumOf { it.d1?.absoluteValue ?: 0.0 }
        } else {
            payableList.sumOf { it.d1?.absoluteValue ?: 0.0 }
        }

        val totalPendingAmt = if (name == "Bill Receivable") {
            receivableList.sumOf { it.adjustmentAmount?.toDouble()?.absoluteValue ?: 0.0 }
        } else {
            payableList.sumOf { it.adjustmentAmount?.toDouble()?.absoluteValue ?: 0.0 }
        }

        // Ledger balance type
        val ledgerBalType = if (name == "Bill Receivable") "Dr" else "Cr"

        // Group bills by account for PDF generation
        fun generateOutstandingRowsByAccount(): Map<String, List<OutstandingRow>> {
            return if (name == "Bill Receivable") {
                receivableList.groupBy { it.cm1 ?: "Unknown" }.mapValues { (_, items) ->
                    items.map { item ->
                        OutstandingRow(
                            date = item.date ?: "",
                            vchType = item.vchType ?: "",
                            refNo = item.billNumber ?: "",
                            refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                            pendingAmount = item.adjustmentAmount?.toDouble()?.absoluteValue?.formatToAmtDec()
                                ?.toDouble() ?: 0.0,
                            due = "Y",
                            dueDate = item.dueDate ?: "",
                            dueDays = ""
                        )
                    }
                }
            } else {
                payableList.groupBy { it.cm1 ?: "Unknown" }.mapValues { (_, items) ->
                    items.map { item ->
                        OutstandingRow(
                            date = item.date ?: "",
                            vchType = item.vchType ?: "",
                            refNo = item.billNumber ?: "",
                            refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                            pendingAmount = item.adjustmentAmount?.absoluteValue?.toDouble()?.formatToAmtDec()
                                ?.toDouble() ?: 0.0,
                            due = "Y",
                            dueDate = item.dueDate ?: "",
                            dueDays = ""
                        )
                    }
                }
            }
        }

        // Generate HTML for all accounts (for now showing first account or combined)
        fun generateOutstandingHtml(): String {
            val allRows = if (name == "Bill Receivable") {
                receivableList.map { item ->
                    OutstandingRow(
                        date = item.date ?: "",
                        vchType = item.vchType ?: "",
                        refNo = item.billNumber ?: "",
                        refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                        pendingAmount = item.adjustmentAmount?.absoluteValue?.toDouble()?.formatToAmtDec()
                            ?.toDouble() ?: 0.0,
                        due = "Y",
                        dueDate = item.dueDate ?: "",
                        dueDays = DueDays(
                            endDate,
                            item.dueDate.toString()
                        )
                    )
                }
            } else {
                payableList.map { item ->
                    OutstandingRow(
                        date = item.date ?: "",
                        vchType = item.vchType ?: "",
                        refNo = item.billNumber ?: "",
                        refAmount = item.d1?.absoluteValue?.formatToAmtDec()?.toDouble() ?: 0.0,
                        pendingAmount = item.adjustmentAmount?.absoluteValue?.toDouble()?.formatToAmtDec()
                            ?.toDouble() ?: 0.0,
                        due = "Y",
                        dueDate = item.dueDate ?: "",
                        dueDays = DueDays(
                            endDate,
                            item.dueDate.toString()
                        )
                    )
                }
            }

            val accountName = if (name == "Bill Receivable") {
                receivableList.firstOrNull()?.cm1 ?: "All Accounts"
            } else {
                payableList.firstOrNull()?.cm1 ?: "All Accounts"
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
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "${name.replace(" ", "_")}_Report",
                            htmlContent = generateOutstandingHtml(),
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            ),
            MenuItemData(
                title = "Share",
                icon = Icons.Default.Share,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "${name.replace(" ", "_")}_Report",
                            htmlContent = generateOutstandingHtml(),
                            action = PdfAction.Share,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            )
        )

        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

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
                                if (name == "Bill Receivable") {
                                    filteredReceivableList.count()
                                } else filteredPayableList.count()
                            }",
                            1f,
                            TextAlign.Start
                        ),
                        ReportColumn(
                            "Vch Amt: ${
                                if (name == "Bill Receivable") {
                                    filteredReceivableList.sumOf { it.d1 ?: 0.0 }.absoluteValue.formatToAmtDec()
                                } else filteredPayableList.sumOf { it.d1 ?: 0.0 }.absoluteValue.formatToAmtDec()
                            }",
                            1f,
                            TextAlign.End
                        ),
                        ReportColumn(
                            "Pen Amt: ${
                                if (name == "Bill Receivable") {
                                    filteredReceivableList.sumOf { it.adjustmentAmount?.toDouble() ?: 0.0 }.absoluteValue.formatToAmtDec()
                                } else filteredPayableList.sumOf { it.adjustmentAmount?.toDouble() ?: 0.0 }.absoluteValue.formatToAmtDec()
                            }",
                            1f,
                            TextAlign.End
                        ),
                    ),
                )
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
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
                                            modifier = Modifier.fillMaxWidth()
                                                .height(56.dp)
                                        ) {
                                            Text(selectedOption)
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Name") },
                                                onClick = {
                                                    selectedOption = "Name"
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Bill No.") },
                                                onClick = {
                                                    selectedOption = "Bill No."
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                TallySearchBar(
                                    searchQuery = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    modifier = Modifier
                                        .focusRequester(focusRequester)
                                        .weight(2f)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "From ${Tdate(startDate)}  →  To ${Tdate(endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.padding(vertical = 8.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        )
                        {
                            if (name == "Bill Receivable") {
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
                                                    nav.push(
                                                        LedgerReportItemScreen(
                                                            vchNo = item.billNumber.toString(),
                                                            date = item.date.toString(),
                                                            vchType = item.vchType.toString(),
                                                            guid = item.VCH_GUID.toString()
                                                        )
                                                    )
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 1.5.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 10.dp
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
                                                    println(item.adjustmentAmount)
                                                    TableCell(
                                                        "Pending: ${item.adjustmentAmount?.absoluteValue?.toDouble()?.formatToAmtDec()}",
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
                                                                endDate,
                                                                item.dueDate.toString()
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
                            } else {
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
                                                    nav.push(
                                                        LedgerReportItemScreen(
                                                            vchNo = item.billNumber.toString(),
                                                            date = item.date.toString(),
                                                            vchType = item.vchType.toString(),
                                                            guid = item.VCH_GUID.toString()
                                                        )
                                                    )
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            ),
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 1.5.dp
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 10.dp
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
                                                    println(item.adjustmentAmount)
                                                    TableCell(
                                                        "Pending: ${item.adjustmentAmount}",
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
                                                                endDate,
                                                                item.dueDate.toString()
                                                            )
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
            }
        )
    }
}