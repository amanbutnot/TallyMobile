package org.prime.tally.ui.screen.reports.godown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import org.prime.tally.ui.shared.SearchBar
import org.prime.tally.ui.shared.TallyCircularLoader
import org.prime.tally.ui.shared.TallyReportScaffold
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.tally.GodownWiseClosingStockList
import kotlin.math.absoluteValue

object GodownClosingStockListScreen : Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow


        var list by remember { mutableStateOf<List<GodownWiseClosingStockList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }


        val column1Weight = 0.6f
        val column2Weight = 0.3f
        val column3Weight = 0.3f

        val totalQty = list.sumOf { item ->
            if ((item.Item_Qty ?: 0.0) < 0.0) item.Item_Qty ?: 0.0 else 0.0
        }
        val totalAmt = list.sumOf { item ->
            if ((item.Item_Amt ?: 0.0) > 0.0) item.Item_Amt ?: 0.0 else 0.0
        }
        val totalRows = list.count()


        LaunchedEffect(Unit) {
            isLoading = true
            list = db.vouchersStockItemsQueries.godownWiseClosingStockList().executeAsList()
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }

        TallyReportScaffold(
            "Godown Closing Stock", showBottomBar = true,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                Card(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                )
                {
                    Row(
                        modifier = Modifier.Companion.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Companion.CenterVertically
                    ) {
                        Text(
                            text =
                                buildAnnotatedString {
                                    append("Rows: ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Companion.Bold)) {
                                        append("$totalRows")
                                    }
                                },
                            modifier = Modifier.Companion.weight(column1Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = totalQty.absoluteValue.toString(),
                            modifier = Modifier.Companion.weight(column2Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Companion.Medium,
                            textAlign = TextAlign.Companion.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = totalAmt.absoluteValue.toString(),
                            modifier = Modifier.Companion.weight(column3Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Companion.Medium,
                            textAlign = TextAlign.Companion.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(
                        Modifier.Companion.fillMaxSize(),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(modifier = Modifier.Companion.fillMaxSize().padding(paddingValues)) {
                        if (showSearchBar) {
                            SearchBar(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.Companion.focusRequester(focusRequester)
                            )
                        }
                        Card(
                            modifier = Modifier.Companion.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                        )
                        {
                            Row(
                                modifier = Modifier.Companion.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                TableCell(
                                    text = "Account Name",
                                    weight = column1Weight,
                                    textAlign = TextAlign.Companion.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    text = "Qty",
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = true
                                )
                                TableCell(
                                    text = "Amount",
                                    weight = column3Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = true
                                )
                            }
                        }


                        val filteredList = if (searchQuery.isEmpty()) {
                            list
                        } else {
                            list.filter {
                                it.Item_Godown?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true
                            }
                        }


                        LazyColumn(
                            modifier = Modifier.Companion.fillMaxSize()
                        ) {
                            items(filteredList) { item ->
                                Card(
                                    modifier = Modifier.Companion.fillMaxWidth()
                                        .padding(horizontal = 0.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.Companion.fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp).clickable {
                                                nav.push(GodownClosingStockItemListScreen(item.Item_Godown))
                                            },
                                        verticalAlignment = Alignment.Companion.CenterVertically
                                    ) {
                                        TableCell(
                                            text = item.Item_Godown ?: "",
                                            weight = column1Weight,
                                            isHeader = false
                                        )

                                        TableCell(
                                            text = item.Item_Qty.toString(),
                                            weight = column2Weight,
                                            textAlign = TextAlign.Companion.End,
                                            isHeader = false
                                        )
                                        TableCell(
                                            text = item.Item_Amt.toString(),
                                            weight = column3Weight,
                                            textAlign = TextAlign.Companion.End,
                                            isHeader = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }
}