package org.prime.tally.ui.screen.reports.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.expect.formatToQtyDec
import org.prime.tally.ui.printing.threeHeaderHtml
import org.prime.tally.ui.screen.reports.productReport.ProductReportScreen
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.getProductParamStockItems
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductParamStockList
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
        val productGroups = remember { db.productGroupMasterQueries.selectAll().executeAsList() }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState()

        val column1Weight = 0.5f
        val column2Weight = 0.2f
        val column3Weight = 0.2f
        val column4Weight = 0.4f

        val totalQty = list.sumOf { it.mvalue1?.toDouble() ?: 0.0 }
        val totalAmt = list.sumOf { it.mvalue2?.toDouble() ?: 0.0 }

        LaunchedEffect(Unit) {
            isLoading = true
            list = getProductParamStockItems(db)

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

        val filteredList = if (searchQuery.isEmpty()) {
            groupFilteredList
        } else {
            val startsWith = groupFilteredList.filter {
                it.ProductName?.startsWith(searchQuery, ignoreCase = true) == true
            }

            val contains = groupFilteredList.filter {
                val value = it.ProductName
                value?.contains(searchQuery, ignoreCase = true) == true &&
                        !value.startsWith(searchQuery, ignoreCase = true)
            }

            startsWith + contains
        }


        val rows: List<Triple<String, String, String>> = filteredList.map { item ->
            Triple(
                item.ProductName ?: "",
                item.mvalue1?.formatToQtyDec() ?: "-",
                item.mvalue2?.formatToAmtDec() ?: ""
            )
        }

        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = "Parameter",
                            htmlContent = threeHeaderHtml(
                                title = "Parameter",
                                headers = Triple("Item Name", "Qty", "Amount"),
                                rows = rows,
                                totalDebit = totalQty.formatToQtyDec().toDouble(),
                                totalCredit = totalAmt.formatToAmtDec().toDouble(),
                                date = ""
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
                            fileName = "Parameter",
                            htmlContent = threeHeaderHtml(
                                title = "Parameter",
                                headers = Triple("Item Name", "Qty", "Amount"),
                                rows = rows,
                                totalDebit = totalQty.formatToQtyDec().toDouble(),
                                totalCredit = totalAmt.formatToAmtDec().toDouble(),
                                date = ""
                            ),
                            action = PdfAction.Download,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            )
        )
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        if (showGroupFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGroupFilterSheet = false },
                sheetState = bottomSheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filter by Group", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { selectedGroups = productGroups.mapNotNull { it.Name } }) {
                            Text("Select All")
                        }
                        TextButton(onClick = { selectedGroups = emptyList() }) {
                            Text("Clear")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn {
                        items(productGroups) { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val currentSelection = selectedGroups.toMutableList()
                                        group.Name?.let {
                                            if (currentSelection.contains(it)) {
                                                currentSelection.remove(it)
                                            } else {
                                                currentSelection.add(it)
                                            }
                                        }
                                        selectedGroups = currentSelection
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = group.Name in selectedGroups,
                                    onCheckedChange = { isChecked ->
                                        val currentSelection = selectedGroups.toMutableList()
                                        group.Name?.let { name ->
                                            if (isChecked) {
                                                currentSelection.add(name)
                                            } else {
                                                currentSelection.remove(name)
                                            }
                                        }
                                        selectedGroups = currentSelection
                                    }
                                )
                                Text(group.Name ?: "", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    Button(
                        onClick = { showGroupFilterSheet = false },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }


        TallyReportScaffold(
            "Parameter Report", showBottomBar = true,
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showGroupFilterSheet = true }) {
                                Text("Account Group Filter")
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
                                    text = item.mvalue1?.formatToQtyDec() ?: "-",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = "-",
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
