package org.prime.tally.ui.screen.reports.ledger

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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.TallyCircularLoader
import org.prime.tally.ui.shared.TallyReportScaffold
import org.prime.tally.ui.shared.TallySearchBar
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.tally.LedgerReportList

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
        var bal = 0.0
        bal = list.sumOf { it.D2!!+it.D3!! + bal }


        val columnSmallWeight = 2.5f
        val columnBigWeight = 7.5f

        LaunchedEffect(Unit) {
            isLoading = true
            list = db.vouchersLedgersQueries.ledgerReportList(
                CM1 = accountName,
                DATE = startDate,
                DATE_ = endDate
            ).executeAsList()
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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Closing Balance: ",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = bal.toString(),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("Rows: ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${filteredList.count()}")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
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
                            .padding(horizontal = 4.dp)
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
                            "From $startDate  →  To $endDate",
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
                                "Opening Balance: ",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }



                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filteredList.isEmpty()) {
                              item{
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
                                items(filteredList) { item ->


                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
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
                                                    item.DATE ?: "",
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
