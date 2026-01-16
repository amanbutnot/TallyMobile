package org.prime.tally.ui.screen.reports.stock

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
import kotlinx.coroutines.launch
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.expect.formatToQtyDec
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.printing.Quadruple
import org.prime.tally.ui.printing.fourHeaderHtml
import org.prime.tally.ui.screen.reports.productReport.ProductReportScreen
import org.prime.tally.ui.shared.composables.GroupFilterBottomSheet
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.getProductStockItems
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductStockItemList
import smartSearch
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
        val productGroups = remember { db.productGroupMasterQueries.selectAll().executeAsList() }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)


        val column1Weight = 0.5f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.4f




        LaunchedEffect(Unit) {
            isLoading = true
            list = getProductStockItems(db)

            isLoading = false


        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }
        val groupFilteredList = if (selectedGroups.isEmpty()) {
            list
        } else {
            list.filter { it.GroupName in selectedGroups }
        }
        val filteredList = smartSearch(
            list = groupFilteredList,
            query = searchQuery,
            selectors = listOf(
                { it.ProductName },
            )
        )


        val totalQty = filteredList.sumOf { it.Value1?.toDouble() ?: 0.0 }
        val totalAmt = filteredList.sumOf { it.Value3?.toDouble() ?: 0.0 }
        val rows: List<Quadruple<String, String, String, String>> = filteredList.map { item ->
            Quadruple(
                item.ProductName ?: "",
                item.UnitName ?: "",
                item.Value1?.toDouble()?.formatToQtyDec() ?: "-",
                item.Value3?.toDouble()?.formatToAmtDec() ?: ""
            )
        }

        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "Stock",
                            htmlContent = fourHeaderHtml(
                                title = "Stock",
                                headers = Quadruple("Item Name", "Unit", "Qty", "Amount"),
                                rows = rows,
                                total1 = totalQty.formatToQtyDec(),
                                total2 = totalAmt.formatToAmtDec()
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
                            fileName = "Stock",
                            htmlContent = fourHeaderHtml(
                                title = "Stock",
                                headers = Quadruple("Item Name", "Unit", "Qty", "Amount"),
                                rows = rows,
                                total1 = totalQty.formatToQtyDec(),
                                total2 = totalAmt.formatToAmtDec()

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
            menuItems = menuItems,
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
                                    text = item.Value1?.toDouble()?.formatToQtyDec() ?: "-",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )
                                if(SharedPrefs.Permissions.get()?.FilterAmount=="N" || SharedPrefs.Permissions.get()?.FilterAmount==null)
                                TableCell(
                                    text = item.Value3?.toDouble()?.formatToAmtDec() ?: "-",
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
