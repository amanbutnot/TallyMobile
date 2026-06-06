package org.prime.easykarobar.ui.screen.reports.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.data.utils.showAmtToSalesman
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.threeHeaderHtml
import org.prime.easykarobar.ui.screen.reports.productReport.ProductReportScreen
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getProductParamStockItems
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.globalShared.perms
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportLazyList
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductParamStockList
import org.tally.ProductGroupMaster
import org.prime.easykarobar.ui.shared.composables.smartSearch
import kotlin.math.absoluteValue

object ParameterReportScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<GetProductParamStockList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var showGroupFilterSheet by remember { mutableStateOf(false) }
        var productGroups by remember { mutableStateOf(emptyList<ProductGroupMaster>()) }
        val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L

        val excludeGuids =
            if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()

        LaunchedEffect(Unit) {
            productGroups = withContext(Dispatchers.IO) {
                db.productGroupMasterQueries
                    .selectAllForReport(
                        filterGroup = filterItemGroups(),
                        groupCodes = itemGroupCodes(),
                        filterExclude = filterExclude,
                        excludeGuids = excludeGuids,
//                        filterGodown = filterGodown,
//                        godownCodes = godownCodes
                    )
                    .executeAsList()
            }
        }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val column1Weight = 0.5f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.4f



        LaunchedEffect(Unit) {
            isLoading = true

            list = withContext(Dispatchers.IO) {
                getProductParamStockItems(db)
            }
            if (!isActive) return@LaunchedEffect

            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }

        val groupFilteredList = remember(list, selectedGroups) {
            if (selectedGroups.isEmpty()) list
            else list.filter { it.GroupName in getProductsGroupCodesByName(selectedGroups) }
        }

        val filteredList = remember(groupFilteredList, searchQuery) {
            smartSearch(
                list = groupFilteredList,
                query = searchQuery,
                selectors = listOf { it.ProductName }
            )
        }

        val rows = remember(filteredList) {
            filteredList.map {
                Triple(
                    it.ProductName ?: "",
                    it.mvalue1?.formatToQtyDec() ?: "-",
                    it.mvalue2?.formatToAmtDec() ?: ""
                )
            }
        }
        val totals = remember(filteredList) {
            val qty = filteredList.sumOf { it.mvalue1 ?: 0.0 }
            val amt = filteredList.sumOf { it.mvalue2 ?: 0.0 }
            qty to amt
        }

        val totalQty = totals.first
        val totalAmt = totals.second

        val htmlContent = threeHeaderHtml(
            title = "Parameter",
            headers = Triple("Item Name", "Qty", "Amount"),
            rows = rows,
            totalDebit = totalQty.formatToQtyDec().toDouble(),
            totalCredit = totalAmt.formatToAmtDec().toDouble(),
            date = ""
        )
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        if (showGroupFilterSheet) {
            GroupFilterBottomSheet(
                show = showGroupFilterSheet,
                items = productGroups,
                selectedItems = selectedGroups,
                itemNameSelector = { it.Name },
                onSelectedItemsChange = { selectedGroups = it },
                onDismiss = { showGroupFilterSheet = false },
                bottomSheetState = bottomSheetState
            )
        }



        TallyReportScaffold(
            "Parameter Report", showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Parameter",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Parameter",
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onExcelClick = {
                scope.launch {
                    val excelRows = filteredList.map { item ->
                        listOf(
                            item.ProductName ?: "",
                            item.mvalue1?.formatToQtyDec() ?: "0.0",
                            item.mvalue2?.formatToAmtDec() ?: "0.0"
                        )
                    }
                    handlePdfAction(
                        fileName = "Parameter_Report",
                        htmlContent = htmlContent,
                        headers = listOf("Item Name", "Qty", "Amount"),
                        rows = excelRows,
                        action = PdfAction.DownloadExcel,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
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
                            if (showQtyToSalesman()) totalQty.absoluteValue.formatToQtyDec() else "",
                            column2Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            if (showAmtToSalesman()) totalAmt.absoluteValue.formatToAmtDec() else "",
                            column3Weight,
                            TextAlign.End
                        )
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
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        if (showSearchBar) {
                            TallySearchBar(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showGroupFilterSheet = true }) {
                                Text("Group Filter")
                            }
                        }
                        TallyReportHeaderCard(
                            columns = listOf(
                                ReportColumn(
                                    "Item Name",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    if (showQtyToSalesman())
                                        "Qty" else "",
                                    column3Weight,
                                    TextAlign.End
                                ),
//                                ReportColumn(
//                                    if(showAmtToSalesman())
//                                    "Amount" else "",
//                                    column4Weight,
//                                    TextAlign.End
//                                )
                            )
                        )

                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                //TODO: this is the StockItemReportListScreen
                                //   nav.push(StockItemReportScreen(item.Item_Name))
                                println(item.MasterCode1?.toInt())
                                nav.push(
                                    ProductReportScreen(
                                        item.MasterCode1?.toInt().toString(),
                                        isMain = false
                                    )
                                )

                            },
                            key = { item ->
                                buildString {
                                    append(item.ProductName)
                                    append('|')
                                    append(item.GroupName)
                                    append('|')
                                    append(item.mvalue1)
                                    append('|')
                                    append(item.MasterCode1)
                                }
                            },
                            content = { item ->
                                TableCell(
                                    text = item.ProductName ?: "",
                                    weight = column1Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = false
                                )
                                //TODO: ENABLE LATER
//
//                                TableCell(
//                                    text = item.Item_Unit.toString(),
//                                    weight = column2Weight,
//                                    textAlign = TextAlign.End,
//                                    isHeader = false
//                                )
                                TableCell(
                                    text = if (showQtyToSalesman()) {
                                        item.mvalue1?.formatToQtyDec() ?: "-"
                                    } else "",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )
//                                TableCell(
//                                    text = "-",
//                                    weight = column4Weight,
//                                    textAlign = TextAlign.End,
//                                    isHeader = false
//                                )
                            }
                        )
                    }
                }
            })
    }
}
