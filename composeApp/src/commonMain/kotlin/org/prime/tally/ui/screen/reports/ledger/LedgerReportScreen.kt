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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
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
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Name") }
        var openingBalance by remember { mutableStateOf<LedgerOpeningBalance?>(null) }
        val nav = LocalNavigator.currentOrThrow
        var closingBalance = 0.0
        closingBalance = list.sumOf { it.D2!! + it.D3!! + closingBalance }


        val columnSmallWeight = 2.5f
        val columnBigWeight = 7.5f

        LaunchedEffect(Unit) {
            isLoading = true
            list = db.vouchersLedgersQueries.ledgerReportList(
                CM1 = accountName,
                DATE = startDate,
                DATE_ = endDate
            ).executeAsList()
            openingBalance = db.vouchersLedgersQueries.ledgerOpeningBalance(accountName, startDate)
                .executeAsOne()
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) focusRequester.requestFocus()
        }
        val filteredList = if (searchQuery.isEmpty()) {
            list
        } else {
            if (selectedOption == "Name") {
                list.filter { it.AccountName?.contains(searchQuery, ignoreCase = true) == true }
            } else {
                list.filter { it.VOUCHERNUMBER?.contains(searchQuery, ignoreCase = true) == true }
            }
        }
        TallyReportScaffold(
            title = "Ledger Report",
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
                            "Closing Balance: $closingBalance",
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
                                "Opening Balance: ${openingBalance?.OpeningBal ?: 0.0}",
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
                                var bal = 0.0
                                items(filteredList) { item ->
                                    bal = item.D2!! + item.D3!! + bal

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp).clickable {
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
                                                    Tdate(item.DATE?:""),
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
                                                    text = if (item.D2 == 0.0) "${item.D3} Cr" else "${item.D2} Dr",
                                                    weight = columnSmallWeight,
                                                    textAlign = TextAlign.End,
                                                    textColor = if (item.D2 == 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    text = bal.toString(),
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
