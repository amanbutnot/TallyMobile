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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.reportsShared.DueDays
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.tally.BillPayableList
import org.tally.BillReceivableList
import kotlin.math.absoluteValue

data class OutstandingReportScreen(val name: String, val startDate: String, val endDate: String) :
    Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance


        var receivableList by remember { mutableStateOf<List<BillReceivableList>>(emptyList()) }
        var payableList by remember { mutableStateOf<List<BillPayableList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Name") }


        val columnSmallWeight = 2.5f
        val columnBigWeight = 7.5f

        LaunchedEffect(Unit) {
            isLoading = true
            if (name == "Bill Receivable") {
                receivableList = db.voucherBillAllocationsQueries.billReceivableList(
                    DATE = startDate,
                    DATE_ = endDate
                ).executeAsList()
            }
            if (name == "Bill Payable") {
                payableList = db.voucherBillAllocationsQueries.billPayableList(
                    DATE = startDate,
                    DATE_ = endDate
                ).executeAsList()
            }
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) focusRequester.requestFocus()
        }
        val filteredReceivableList = if (searchQuery.isEmpty()) {
            receivableList
        } else {
            if (selectedOption == "Name") {
                receivableList.filter { it.cm1?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                receivableList.filter {
                    it.billNumber?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }
            }
        }

        val filteredPayableList = if (searchQuery.isEmpty()) {
            payableList
        } else {
            if (selectedOption == "Name") {
                payableList.filter { it.cm1?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                payableList.filter {
                    it.billNumber?.contains(
                        searchQuery,
                        ignoreCase = true
                    ) == true
                }
            }
        }



        TallyReportScaffold(
            title = "$name Report",
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
                                    filteredReceivableList.sumOf { it.d1 ?: 0.0 }.absoluteValue
                                } else filteredPayableList.sumOf { it.d1 ?: 0.0 }.absoluteValue
                            }",
                            1f,
                            TextAlign.End
                        ),
                        ReportColumn(
                            "Pen Amt: ${
                                if (name == "Bill Receivable") {
                                    filteredReceivableList.sumOf { it.adjustmentAmount ?: 0.0 }.absoluteValue
                                } else filteredPayableList.sumOf { it.adjustmentAmount ?: 0.0 }.absoluteValue
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

                                // --- Search Field ---
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
                        ) {

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
                                        // bal = item.D2!! + item.D3!! + bal

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
//                                                nav.push(
//                                                    LedgerReportItemScreen(
//                                                        vchNo = item.VOUCHERNUMBER.toString(),
//                                                        date = item.DATE.toString(),
//                                                        vchType = item.VchType.toString(),
//                                                        guid = item.VCH_GUID.toString()
//                                                    )
//                                                )
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
                                                    TableCell(
                                                        item.cm1.toString(),
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = true
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill No. ${item.billNumber}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Date: ${Tdate(item.date.toString())}",
                                                        1f,
                                                        textAlign = TextAlign.End,
                                                        isHeader = false
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill: ${item.d1?.absoluteValue}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Pending: ${item.adjustmentAmount?.absoluteValue}",
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
                                        // bal = item.D2!! + item.D3!! + bal

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp).clickable {
//                                                nav.push(
//                                                    LedgerReportItemScreen(
//                                                        vchNo = item.VOUCHERNUMBER.toString(),
//                                                        date = item.DATE.toString(),
//                                                        vchType = item.VchType.toString(),
//                                                        guid = item.VCH_GUID.toString()
//                                                    )
//                                                )
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
                                                    TableCell(
                                                        item.cm1.toString(),
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = true
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill No. ${item.billNumber}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Date: ${Tdate(item.date.toString())}",
                                                        1f,
                                                        textAlign = TextAlign.End,
                                                        isHeader = false
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    TableCell(
                                                        "Bill: ${item.d1?.absoluteValue}",
                                                        1f,
                                                        textAlign = TextAlign.Start,
                                                        isHeader = false
                                                    )
                                                    TableCell(
                                                        "Pending: ${item.adjustmentAmount?.absoluteValue}",
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
