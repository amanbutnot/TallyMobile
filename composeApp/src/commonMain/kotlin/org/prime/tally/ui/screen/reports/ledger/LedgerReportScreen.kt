package org.prime.tally.ui.screen.reports.ledger

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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.expect.formatToQtyDec
import org.prime.tally.ui.printing.LedgerRow
import org.prime.tally.ui.printing.accountLedgerHtml
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.LedgerOpeningBalance
import org.tally.LedgerReportList
import kotlin.math.absoluteValue

data class LedgerReportScreen(val accountName: String, val startDate: String, val endDate: String) :
    Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<LedgerReportList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Name") }
        var openingBalance by remember { mutableStateOf<LedgerOpeningBalance?>(null) }
        val nav = LocalNavigator.currentOrThrow

        val columnSmallWeight = 2.5f
        val columnBigWeight = 7.5f

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                println("DB Started")
                val reportList = db.vouchersLedgersQueries.ledgerReportList(
                    CM1 = accountName,
                    DATE = startDate,
                    DATE_ = endDate
                ).executeAsList()
                println("DB Ended")

                val opening = db.vouchersLedgersQueries
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

        val filteredList = if (searchQuery.isEmpty()) {
            list
        } else {
            val startsWith = list.filter {
                if (selectedOption == "Name") {
                    it.AccountName?.startsWith(searchQuery, ignoreCase = true) == true
                } else {
                    it.VOUCHERNUMBER?.startsWith(searchQuery, ignoreCase = true) == true
                }
            }

            val contains = list.filter {
                if (selectedOption == "Name") {
                    val value = it.AccountName
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value?.startsWith(searchQuery, ignoreCase = true) == false
                } else {
                    val value = it.VOUCHERNUMBER
                    value?.contains(searchQuery, ignoreCase = true) == true &&
                            value?.startsWith(searchQuery, ignoreCase = true) == false
                }
            }

            startsWith + contains
        }


        // Calculate totals and closing balance
        var totalDebit = 0.0
        var totalCredit = 0.0
        var runningBalance = openingBalance?.OpeningBal?.toDouble() ?: 0.0

        list.forEach { item ->
            totalDebit += item.D2 ?: 0.0
            totalCredit += item.D3 ?: 0.0
            runningBalance += (item.D2 ?: 0.0) - (item.D3 ?: 0.0)
        }

        val closingBalance = runningBalance
        val closingBalanceType = if (closingBalance >= 0) "Cr" else "Dr"
        val openingBalType = if ((openingBalance?.OpeningBal?.toDouble() ?: 0.0) >= 0) "Cr" else "Dr"

        // Generate ledger rows for PDF
        fun generateLedgerRows(): List<LedgerRow> {
            val rows = mutableListOf<LedgerRow>()
            var bal = openingBalance?.OpeningBal?.toDouble() ?: 0.0

            list.forEach { item ->
                val debit = item.D2 ?: 0.0
                val credit = item.D3 ?: 0.0
                bal += debit - credit
                val balType = if (bal >= 0) "Cr" else "Dr"

                rows.add(
                    LedgerRow(
                        date = item.DATE ?: "",
                        type = item.VchType ?: "",
                        vchBillNo = item.VOUCHERNUMBER ?: "",
                        account = item.AccountName ?: "",
                        debit = debit,
                        credit = credit,
                        balance = kotlin.math.abs(bal),
                        balanceType = balType
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
                            fileName = "Ledger_Report_$accountName",
                            htmlContent = accountLedgerHtml(
                                accountName = accountName,
                                startDate = startDate,
                                endDate = endDate,
                                openingBalance = kotlin.math.abs(openingBalance?.OpeningBal?.toDouble() ?: 0.0),
                                openingBalanceType = openingBalType,
                                rows = generateLedgerRows(),
                                totalDebit = totalDebit,
                                totalCredit = totalCredit,
                                closingBalance = kotlin.math.abs(closingBalance),
                                closingBalanceType = closingBalanceType
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
                            fileName = "Ledger_Report_$accountName",
                            htmlContent = accountLedgerHtml(
                                accountName = accountName,
                                startDate = startDate,
                                endDate = endDate,
                                openingBalance = kotlin.math.abs(openingBalance?.OpeningBal?.toDouble() ?: 0.0),
                                openingBalanceType = openingBalType,
                                rows = generateLedgerRows(),
                                totalDebit = totalDebit,
                                totalCredit = totalCredit,
                                closingBalance = kotlin.math.abs(closingBalance),
                                closingBalanceType = closingBalanceType
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
            title = "Ledger Report",
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
                            "Closing: ${closingBalance.absoluteValue.formatToAmtDec()} $closingBalanceType",
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
                            "Account: $accountName",
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
                                        "Cr/Dr",
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
                                "Opening: ${openingBalance?.OpeningBal?.absoluteValue?.formatToAmtDec()} $openingBalType",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
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
                                var bal = openingBalance?.OpeningBal?.formatToAmtDec()?.toDouble() ?: 0.0
                                items(filteredList) { item ->
                                    bal += (item.D2 ?: 0.0) - (item.D3 ?: 0.0)
                                    val balType = if (bal >= 0) "Cr" else "Dr"

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
                                                TableCell(
                                                    text = if (item.D2 == 0.0) "${item.D3?.absoluteValue?.formatToAmtDec()} Dr" else "${item.D2?.absoluteValue?.formatToAmtDec()} Cr",
                                                    weight = columnSmallWeight,
                                                    textAlign = TextAlign.End,
                                                    textColor = if (item.D2 == 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    text = "${kotlin.math.abs(bal).absoluteValue.formatToAmtDec()} $balType",
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