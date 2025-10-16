package org.prime.tally.ui.screen.reports.godown

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.expect.formatToQtyDec
import org.prime.tally.ui.printing.Quadruple
import org.prime.tally.ui.printing.fourHeaderHtml
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.GodownWiseOnEnterList
import kotlin.math.absoluteValue

data class GodownClosingStockItemListScreen(val itemName: String?) : Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<GodownWiseOnEnterList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val column1Weight = 0.6f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.3f

        val totalQty = list.sumOf { it.Item_Qty ?: 0.0 }
        val totalAmt = list.sumOf { it.Item_Amt ?: 0.0 }


        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                list = db.vouchersStockItemsQueries.godownWiseOnEnterList(itemName).executeAsList()
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
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
                it.Item_Name?.startsWith(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        }
        val rows: List<Quadruple<String, String, String, String>> = filteredList.map { item ->
            Quadruple(
                item.Item_Name ?: "",
                item.Item_Unit ?: "",
                item.Item_Qty?.formatToQtyDec()?:"0.0",
                item.Item_Amt?.absoluteValue?.formatToAmtDec() ?: ""
            )
        }


        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "GodownItem",
                            htmlContent = fourHeaderHtml(
                                title = "Godown Item $itemName",
                                headers = Quadruple("Item Name", "Unit", "Qty", "Amount"),
                                rows = rows,
                                total1 = totalQty.formatToQtyDec(),
                                total2 = totalAmt.formatToAmtDec(),
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
                            fileName = "GodownItem",
                            htmlContent = fourHeaderHtml(
                                title = "Godown Item",
                                headers = Quadruple("Date", "Name", "Vch No", "Amount"),
                                rows = rows,
                                total1 = totalQty.formatToQtyDec(),
                                total2 = totalAmt.formatToAmtDec(),
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
            "Godown Item Report",
            showBottomBar = true,
            showBurgerMenu = true,
            menuItems = menuItems,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {

                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${filteredList.count()}",
                            (column1Weight + column2Weight),
                            TextAlign.Start
                        ),
                        ReportColumn(
                            totalQty.absoluteValue.formatToQtyDec(),
                            column3Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            totalAmt.absoluteValue.formatToAmtDec(),
                            column4Weight,
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
                            onItemClick = {
                            },
                            content = { item ->
                                TableCell(
                                    text = item.Item_Name ?: "",
                                    weight = column1Weight,
                                    isHeader = false
                                )

                                TableCell(
                                    text = item.Item_Unit ?: "",
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = item.Item_Qty?.formatToQtyDec() ?: "-",
                                    weight = column3Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = item.Item_Amt?.formatToAmtDec() ?: "-",
                                    weight = column4Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                            })
                    }
                }
            })
    }
}
