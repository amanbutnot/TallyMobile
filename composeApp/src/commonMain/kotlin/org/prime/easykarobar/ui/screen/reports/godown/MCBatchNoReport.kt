package org.prime.easykarobar.ui.screen.reports.godown

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
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.data.utils.showAmtToSalesman
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.threeHeaderHtml
import org.prime.easykarobar.ui.screen.reports.stock.BatchNumberStockReport
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportLazyList
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.McBatchNoReport
import org.prime.easykarobar.ui.shared.composables.smartSearch
import kotlin.math.absoluteValue

object MCBatchNoReport : Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val nav = LocalNavigator.currentOrThrow
        var list by remember { mutableStateOf<List<McBatchNoReport>>(emptyList()) }
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
        val godownCodes =
            if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()

        val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
        val excludeGuids =
            if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                list = db.productBatchNoQueries.mcBatchNoReport(
                    filterGodown = filterGodown,
                    godownCodes = godownCodes,
                    filterGroup = filterItemGroups(),
                    groupCodes = itemGroupCodes(),
                    filterExclude = filterExclude,
                    excludeGuids = excludeGuids,
                ).executeAsList()
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
        val filteredList = smartSearch(
            list = list,
            query = searchQuery,
            selectors = listOf { it.Item_Godown }
        )

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
                            fileName = "MC Wise Batch Number",
                            htmlContent = threeHeaderHtml(
                                title = "MC Wise Batch Number",
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
                            fileName = "MC Wise Batch Number",
                            htmlContent = threeHeaderHtml(
                                title = "MC Wise Batch Number",
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
            "MC Wise Batch Number", showBottomBar = true,
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
                            if (showQtyToSalesman())
                                totalQty.absoluteValue.formatToQtyDec() else "",
                            column2Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            if (showAmtToSalesman())
                                totalAmt.absoluteValue.formatToAmtDec() else "",
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
                                    if (showQtyToSalesman())
                                        "Qty" else "",
                                    column2Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    if (showAmtToSalesman())
                                        "Amount" else "",
                                    column3Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                nav.push(
                                    BatchNumberStockReport(
                                        false,
                                        item.Item_GodownCode?.toInt().toString(),
                                        false
                                    )
                                )
                            }, key = { item ->
                                buildString {
                                    append(item.Item_Godown)
                                    append('|')
                                    append(item.Item_Qty)
                                    append('|')
                                    append(item.Item_Amt)
                                }
                            },
                            content = { item ->
                                TableCell(
                                    text = item.Item_Godown ?: "",
                                    weight = column1Weight,
                                    isHeader = false
                                )

                                TableCell(
                                    text = if (showQtyToSalesman()) {
                                        item.Item_Qty?.formatToQtyDec() ?: "-"
                                    } else "",
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = if (showAmtToSalesman()) {
                                        item.Item_Amt?.formatToAmtDec() ?: "-"
                                    } else "",
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