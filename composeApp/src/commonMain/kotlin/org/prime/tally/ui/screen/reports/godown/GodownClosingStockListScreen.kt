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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.expect.formatToQtyDec
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.printing.Quadruple
import org.prime.tally.ui.printing.fourHeaderHtml
import org.prime.tally.ui.printing.threeHeaderHtml
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.globalShared.StartDate
import org.prime.tally.ui.shared.globalShared.parseToStringList
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
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
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val column1Weight = 0.6f
        val column2Weight = 0.3f
        val column3Weight = 0.3f

//        val totalQty = list.sumOf { item ->
//            if ((item.Item_Qty ?: 0.0) < 0.0) item.Item_Qty ?: 0.0 else 0.0
//        }
        val totalQty = list.sumOf {
            it.Item_Qty ?: 0.0
        }
        println(list.toString())
        val totalAmt = list.sumOf {
            it.Item_Amt ?: 0.0
        }
        val perms = SharedPrefs.Permissions.get()
        val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L
        val godownCodes = if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                list = db.vouchersStockItemsQueries.godownWiseClosingStockList(filterGodown,godownCodes).executeAsList()
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
                it.Item_Godown?.startsWith(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        }

        val rows: List<Triple<String, String, String>> = filteredList.map { item ->
            Triple(
                item.Item_Godown ?: "",
                item.Item_Qty?.formatToQtyDec() ?: "-",
                item.Item_Amt?.formatToAmtDec() ?: "-",
            )
        }


        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "GodownClosingStock",
                            htmlContent = threeHeaderHtml(
                                title = "Godown Closing Stock",
                                headers = Triple("Account Name", "Qty", "Amount"),
                                rows = rows,
                                totalDebit = totalQty.formatToAmtDec().toDouble(),
                                totalCredit = totalAmt.formatToAmtDec().toDouble(),
                                date = StartDate()
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
                            fileName = "GodownClosingStock",
                            htmlContent = threeHeaderHtml(
                                title = "Godown Closing Stock",
                                headers = Triple("Account Name", "Debit", "Credit"),
                                rows = rows,
                                totalDebit = totalQty.formatToAmtDec().toDouble(),
                                totalCredit = totalAmt.formatToAmtDec().toDouble(),
                                date = StartDate()
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
            "Godown Closing Stock", showBottomBar = true,
            showBurgerMenu = true, menuItems = menuItems,
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
                            totalQty.absoluteValue.formatToQtyDec(),
                            column2Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            totalAmt.absoluteValue.formatToAmtDec(),
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
                                    "Location Name",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Qty",
                                    column2Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    "Amount",
                                    column3Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                nav.push(GodownClosingStockItemListScreen(item.Item_Godown))
                            },
                            content = { item ->
                                TableCell(
                                    text = item.Item_Godown ?: "",
                                    weight = column1Weight,
                                    isHeader = false
                                )

                                TableCell(
                                    text = item.Item_Qty?.formatToQtyDec() ?: "-",
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = item.Item_Amt?.formatToAmtDec() ?: "-",
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