package org.prime.tally.ui.screen.reports.stock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.tally.StockReportList
import kotlin.math.absoluteValue

object StockReportScreen : Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<StockReportList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow


        val column1Weight = 0.6f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.3f

        val totalQty = list.sumOf { it.Item_Qty ?: 0.0 }
        val totalAmt = list.sumOf { it.Item_Amt ?: 0.0 }


        LaunchedEffect(Unit) {
            isLoading = true
            list = db.vouchersStockItemsQueries.stockReportList().executeAsList()
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }
        val filteredList = if (searchQuery.isEmpty()) {
            list
        } else {
            list.filter {
                it.Item_Name?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        }
        TallyReportScaffold(
            "Stock Report", showBottomBar = true,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${filteredList.count()}",
                            column1Weight,
                            TextAlign.Start
                        ),
                        ReportColumn(
                            totalQty.absoluteValue.toString(),
                            column2Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            totalAmt.absoluteValue.toString(),
                            column3Weight,
                            TextAlign.End
                        )
                    ),
                )
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
                            TallySearchBar(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.Companion.focusRequester(focusRequester)
                            )
                        }
                        TallyReportHeaderCard(
                            columns = listOf(
                                ReportColumn(
                                    "Item Name",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Unit",
                                    column2Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    "Qty",
                                    column3Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    "Amount",
                                    column4Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                nav.push(StockItemReportScreen(item.Item_Name))

                            },
                            content = { item ->
                                TableCell(
                                    text = item.Item_Name ?: "",
                                    weight = column1Weight,
                                    textAlign = TextAlign.Companion.Start,
                                    isHeader = false
                                )

                                TableCell(
                                    text = item.Item_Unit.toString(),
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
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
                        )
                    }
                }
            })
    }
}