package org.prime.easykarobar.ui.screen.reports.stock

import CurrentDate
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.data.model.salesmanPermission
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.data.utils.showAmtToSalesman
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.Quadruple
import org.prime.easykarobar.ui.printing.fourHeaderHtml
import org.prime.easykarobar.ui.screen.reports.ledger.ItemLedgerScreen
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.PermissionDeniedDialog
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getProductStockItems
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportLazyList
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductStockItemList
import kotlin.math.absoluteValue

object StockReportScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<GetProductStockItemList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var showGroupFilterSheet by remember { mutableStateOf(false) }
        val productGroups = remember {
            db.productGroupMasterQueries.selectAll(
                filterGroup = filterItemGroups(),
                groupCodes = itemGroupCodes()
            ).executeAsList()
        }
        var showDeniedDialog by remember { mutableStateOf(false) }

        if (showDeniedDialog) {
            PermissionDeniedDialog { showDeniedDialog = false }
        }

        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


        val column1Weight = 0.5f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.4f




        LaunchedEffect(Unit) {
            isLoading = true
            list = getProductStockItems(db)
            println("Group list is : $list")

            isLoading = false


        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }
        val selectedGroupCodes = remember(selectedGroups) {
            getProductsGroupCodesByName(selectedGroups).toSet()
        }
        var filteredList by remember { mutableStateOf<List<GetProductStockItemList>>(emptyList()) }

        LaunchedEffect(list, selectedGroups, searchQuery) {
            withContext(Dispatchers.Default) {
                val selectedCodes = getProductsGroupCodesByName(selectedGroups).toSet()

                val groupFiltered = if (selectedCodes.isEmpty()) {
                    list
                } else {
                    list.filter { it.GroupName in selectedCodes }
                }

                val result = smartSearch(
                    list = groupFiltered,
                    query = searchQuery,
                    selectors = listOf { it.ProductName }
                )

                withContext(Dispatchers.Main) {
                    filteredList = result
                }
            }
        }

        val totalQty = filteredList.sumOf { it.Value1?.toDouble() ?: 0.0 }
        val totalAltQty = filteredList.sumOf { it.Value2?.toDouble() ?: 0.0 }
        val totalAmt = filteredList.sumOf { it.Value3?.toDouble() ?: 0.0 }
        val rows: List<Quadruple<String, String, String, String>> = filteredList.map { item ->
            Quadruple(
                item.ProductName ?: "",
                item.UnitName ?: "",
                item.Value1?.toDouble()?.formatToQtyDec() ?: "-",
                item.Value3?.toDouble()?.formatToAmtDec() ?: ""
            )
        }

        val htmlContent = fourHeaderHtml(
            title = "Stock",
            headers = Quadruple("Item Name", "Unit", "Qty", "Amount"),
            rows = rows,
            total1 = totalQty.formatToQtyDec(),
            total2 = totalAmt.formatToAmtDec()
        )
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        GroupFilterBottomSheet(
            show = showGroupFilterSheet,
            items = productGroups, // can be any list
            selectedItems = selectedGroups,
            itemNameSelector = { it.Name },
            onSelectedItemsChange = { selectedGroups = it },
            onDismiss = { showGroupFilterSheet = false },
            bottomSheetState = bottomSheetState
        )


        TallyReportScaffold(
            "Stock Report", showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Stock",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Stock",
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
                            item.UnitName ?: "",
                            item.Value1?.formatToQtyDec() ?: "0.0",
                            item.Value2?.formatToQtyDec() ?: "0.0",
                            item.Value3?.formatToAmtDec() ?: "0.0"
                        )
                    }
                    handlePdfAction(
                        fileName = "Stock_Report",
                        htmlContent = htmlContent,
                        headers = listOf("Item Name", "Unit", "M. Qty", "A. Qty", "Amount"),
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
                            if (showQtyToSalesman())
                                totalQty.absoluteValue.formatToQtyDec() else "",
                            column2Weight,
                            TextAlign.Start
                        ),
                        ReportColumn(
                            if (showQtyToSalesman())
                                totalAltQty.absoluteValue.formatToQtyDec() else "",
                            column2Weight,
                            TextAlign.Start
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
//                                ReportColumn(
//                                    "Unit",
//                                    column2Weight,
//                                    TextAlign.End
//                                ),
                                ReportColumn(
                                    "M. Qty",
                                    column3Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    "Alt. Qty",
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
                                //TODO: this is the StockItemReportListScreen
                                //   nav.push(StockItemReportScreen(item.Item_Name))
                                //   println(item.MasterCode1?.toInt())
                                salesmanPermission(
                                    "D52",
                                    accessDeniedBlock = { showDeniedDialog = true },
                                    successBlock = {
                                        nav.push(
                                            ItemLedgerScreen(
                                                accountName = item.ProductName.toString(),
                                                startDate = StartDate(),
                                                endDate = CurrentDate()
                                            )
                                        )
                                    }
                                )


                            },
                            key = { item ->
                                buildString {
                                    append(item.ProductName)
                                    append('|')
                                    append(item.GroupName)
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
                                    text = if (SharedPrefs.Permissions.get()?.FilterQty == "False" || SharedPrefs.Permissions.get()?.FilterQty == null) item.Value1
                                        ?.formatToQtyDec() ?: "-" else "",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )


                                TableCell(
                                    text = if (SharedPrefs.Permissions.get()?.FilterQty == "False" || SharedPrefs.Permissions.get()?.FilterQty == null) item.Value2
                                        ?.formatToQtyDec() ?: "-" else "",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )



                                TableCell(
                                    text = if (SharedPrefs.Permissions.get()?.FilterAmount == "False" || SharedPrefs.Permissions.get()?.FilterAmount == null) item.Value3
                                        ?.formatToAmtDec() ?: "-" else "",
                                    weight = column4Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )
                            }
                        )
                    }
                }
            })
    }
}
