package org.prime.easykarobar.ui.screen.reports.ledger


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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
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
import org.prime.easykarobar.ui.printing.LedgerRow
import org.prime.easykarobar.ui.printing.itemLedgerHtml
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import smartSearch

data class ItemLedgerScreen(val accountName: String, val startDate: String, val endDate: String) :
    Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember {
            mutableStateOf<List<org.tally.vouchersStockItems.LedgerReportList>>(
                emptyList()
            )
        }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Name") }
        var openingBalance by remember {
            mutableStateOf<org.tally.vouchersStockItems.LedgerOpeningBalance?>(
                null
            )
        }
        val nav = LocalNavigator.currentOrThrow

        val columnSmallWeight = 2.5f
        val columnBigWeight = 7.5f

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                println("DB Started")
                val reportList = db.vouchersStockItemsQueries.ledgerReportList(
                    CM1 = accountName,
                    DATE = startDate,
                    DATE_ = endDate
                ).executeAsList().filter { it.VchType != "Opening" }
                println("DB Ended")

                val opening = db.vouchersStockItemsQueries
                    .ledgerOpeningBalance(accountName, startDate)
                    .executeAsOne()

                // Now switch back to main thread to update Compose states
                withContext(Dispatchers.Main) {
                    list = reportList
                    openingBalance = opening
                    isLoading = false
                }
            }
        }


        LaunchedEffect(showSearchBar) {
            if (showSearchBar) focusRequester.requestFocus()
        }

        val filteredList = smartSearch(
            list = list,
            query = searchQuery,
            selectors = listOf { item ->
                if (selectedOption == "Name") {
                    item.AccountName
                } else {
                    item.VOUCHERNUMBER
                }
            }
        )


        // Calculate totals and closing balance
        val (totalInward, totalOutward, closingBalance) = remember(list, openingBalance) {
            var tIn = 0.0
            var tOut = 0.0
            var bal = openingBalance?.OpeningBal ?: 0.0
            list.forEach { item ->
                tIn += item.D2 ?: 0.0
                tOut += item.D3 ?: 0.0
                bal += (item.D2 ?: 0.0) - (item.D3 ?: 0.0)
            }
            Triple(tIn, tOut, bal)
        }

        // Generate ledger rows for PDF
        fun generateLedgerRows(): List<LedgerRow> {
            val rows = mutableListOf<LedgerRow>()
            var bal = openingBalance?.OpeningBal ?: 0.0

            list.forEach { item ->
                val debit = item.D2 ?: 0.0
                val credit = item.D3 ?: 0.0
                bal += debit - credit

                rows.add(
                    LedgerRow(
                        date = item.DATE ?: "",
                        type = item.VchType ?: "",
                        vchBillNo = item.VOUCHERNUMBER ?: "",
                        account = item.AccountName ?: "",
                        debit = debit,
                        credit = credit,
                        balance = bal,
                        balanceType = ""
                    )
                )
            }
            return rows
        }

        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "Item_Ledger_Report_$accountName",
                            htmlContent = itemLedgerHtml(
                                itemName = accountName,
                                startDate = startDate,
                                endDate = endDate,
                                openingBalance = openingBalance?.OpeningBal ?: 0.0,
                                rows = generateLedgerRows(),
                                totalInward = totalInward,
                                totalOutward = totalOutward,
                                closingBalance = closingBalance
                            ),
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
                            fileName = "Item_Ledger_Report_$accountName",
                            htmlContent = itemLedgerHtml(
                                itemName = accountName,
                                startDate = startDate,
                                endDate = endDate,
                                openingBalance = openingBalance?.OpeningBal ?: 0.0,
                                rows = generateLedgerRows(),
                                totalInward = totalInward,
                                totalOutward = totalOutward,
                                closingBalance = closingBalance
                            ),
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
            title = "Item Ledger Report",
            showBurgerMenu = true,
            menuItems = menuItems,
            showBottomBar = true,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${filteredList.count()}",
                            columnSmallWeight,
                            TextAlign.Start
                        ),
                        ReportColumn(
                            "Closing: ${closingBalance.formatToAmtDec()}",
                            columnBigWeight,
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
                                                text = { Text("Vch No") },
                                                onClick = {
                                                    selectedOption = "Vch No"
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

                        Spacer(Modifier.padding(top = 8.dp))
                        Text(
                            "Item: $accountName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "From ${Tdate(startDate)}  →  To ${Tdate(endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.padding(vertical = 8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    TableCell(
                                        "Date",
                                        columnSmallWeight,
                                        textAlign = TextAlign.Start,
                                        isHeader = true
                                    )
                                    TableCell(
                                        "Type",
                                        columnSmallWeight,
                                        textAlign = TextAlign.Start,
                                        isHeader = true
                                    )
                                    TableCell(
                                        "In/Out",
                                        columnSmallWeight,
                                        textAlign = TextAlign.End,
                                        isHeader = true
                                    )
                                    TableCell(
                                        "Balance",
                                        columnSmallWeight,
                                        textAlign = TextAlign.End,
                                        isHeader = true
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    TableCell(
                                        "Vch No.",
                                        columnSmallWeight,
                                        textAlign = TextAlign.Start,
                                        isHeader = true
                                    )
                                    TableCell(
                                        "Particulars",
                                        columnBigWeight,
                                        textAlign = TextAlign.Start,
                                        isHeader = true
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "Opening: ${openingBalance?.OpeningBal?.formatToAmtDec() ?: "0.0"}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        val runningBalances = remember(filteredList, openingBalance) {
                            var bal = openingBalance?.OpeningBal ?: 0.0

                            filteredList.map { item ->
                                bal += (item.D2 ?: 0.0) - (item.D3 ?: 0.0)
                                bal
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filteredList.isEmpty()) {
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

                                itemsIndexed(filteredList) { index, item ->
                                    val bal = runningBalances[index]

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp).clickable {
                                                println("GUID: ${item.VCH_GUID}")
                                                nav.push(
                                                    LedgerReportItemScreen(
                                                        vchNo = item.VOUCHERNUMBER.toString(),
                                                        date = item.DATE.toString(),
                                                        vchType = item.VchType.toString(),
                                                        guid = item.VCH_GUID.toString()
                                                    )
                                                )
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                    )
                                    {
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
                                                    Tdate(item.DATE ?: ""),
                                                    columnSmallWeight,
                                                    textAlign = TextAlign.Start,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    item.VchType.toString(),
                                                    columnSmallWeight,
                                                    textAlign = TextAlign.Start,
                                                    isHeader = false
                                                )
                                                val qtyValue = if ((item.D2 ?: 0.0) != 0.0) (item.D2 ?: 0.0) else -(item.D3 ?: 0.0)
                                                TableCell(
                                                    text = qtyValue.formatToAmtDec(),
                                                    weight = columnSmallWeight,
                                                    textAlign = TextAlign.End,
                                                    textColor = if ((item.D2 ?: 0.0) > 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    text = bal.formatToAmtDec(),
                                                    weight = columnSmallWeight,
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
                                                    item.VOUCHERNUMBER ?: "",
                                                    columnSmallWeight,
                                                    textAlign = TextAlign.Start,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    item.AccountName.toString(),
                                                    columnBigWeight,
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
        )
    }
}

//in out