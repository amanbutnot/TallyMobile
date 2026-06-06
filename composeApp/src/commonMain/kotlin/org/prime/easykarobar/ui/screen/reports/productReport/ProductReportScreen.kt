package org.prime.easykarobar.ui.screen.reports.productReport

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.data.utils.showQtyToSalesman
import org.prime.easykarobar.ui.printing.productReportHtml
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToDoubleList
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.GetProductStockList

data class ProductReportScreen(val productGuid: String? = null, val isMain: Boolean) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<GetProductStockList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var showGroupFilterSheet by remember { mutableStateOf(false) }
        val productGroups = remember { db.productGroupMasterQueries.selectAll(   filterGroup = filterItemGroups(),
            groupCodes = itemGroupCodes()).executeAsList() }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                val perms = SharedPrefs.Permissions.get()
                val enableParam = if (perms?.FilterParam1 == "Y") 1L else 0L
                val paramFilters =
                    if (enableParam == 1L) perms?.ConfigParam1.parseToStringList() else emptyList()
                val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
                val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
                val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

                val excludeGuids =
                    if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
                val godownCodes =
                    if (filterGodown == 1L) perms?.ConfigGodown.parseToDoubleList() else emptyList()
                list = db.productParamStockQueries.getProductStockList(
                    productGuid?.toDouble()?.toInt()?.toString(),
                    enableParam,
                    paramFilters,
                    filterGroup = filterGroup,
                    groupCodes = filterItemGroupCodes(),
                    filterExclude = filterExclude,
                    excludeGuids = excludeGuids,
                    filterGodown = filterGodown,
                    godownCodes = godownCodes
                ).executeAsList()
                println("this list is "+list)
            }
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
            list.filter { it.GroupName in getProductsGroupCodesByName(selectedGroups) }
        }

        val filteredList = smartSearch(
            list = groupFilteredList,
            query = searchQuery,
            selectors = listOf(
                { it.ProductName },
                { it.GodownName }
            )
        )


        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }


        val htmlContent = productReportHtml(
            rows = list
        )

        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }

        if (showGroupFilterSheet) {
            GroupFilterBottomSheet(
                show = showGroupFilterSheet,
                items = productGroups,
                selectedItems = selectedGroups,
                itemNameSelector = { it.Name }, // assuming productGroups is List<ProductGroup>
                onSelectedItemsChange = { selectedGroups = it },
                onDismiss = { showGroupFilterSheet = false },
                bottomSheetState = bottomSheetState
            )
        }


        TallyReportScaffold(
            title = "Barcode Report",
            showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Product Report",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Product Report",
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
                            item.GodownName ?: "",
                            item.Value1?.toString() ?: "0.0",
                            item.Value2?.toString() ?: "0.0",
                            item.C1 ?: "",
                            item.C2 ?: "",
                            item.C3 ?: ""
                        )
                    }
                    handlePdfAction(
                        fileName = "Barcode_Report",
                        htmlContent = htmlContent,
                        headers = listOf("Product Name", "Location", "Main Qty", "Alt Qty", "C1", "C2", "C3"),
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
                            "Rows: ${filteredList.count()}", 1f, TextAlign.Start
                        )
                    )
                )
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    ) {
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
                        val groupId = filteredList.groupBy { it.ProductName }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            groupId.forEach { (name, items) ->
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name.toString(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Text(
                                            text = if (showQtyToSalesman()) {
                                                "Total: " + items.sumOf { it.Value1 ?: 0.0 }.formatToAmtDec()
                                            } else "",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                    }
                                }
                                items(items) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 16.dp, bottom = 20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = it.C1.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = it.C2.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = it.C3.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (showQtyToSalesman()) it.Value1.toString() else "",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            })
    }
}

@Composable
private fun ColumnScope.ScrollableScreen(
    horizontalScrollState: ScrollState,
    columnWidths: List<Dp>,
    filteredList: List<GetProductStockList>
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TableCellFixed(
                text = "Product Name",
                width = columnWidths[0],
                textAlign = TextAlign.Start,
                isHeader = true
            )
            TableCellFixed(
                text = "Location",
                width = columnWidths[1],
                textAlign = TextAlign.Start,
                isHeader = true
            )
            TableCellFixed(
                text = "Main Qty",
                width = columnWidths[2],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "Alt Qty",
                width = columnWidths[3],
                textAlign = TextAlign.End,
                isHeader = true
            )
            TableCellFixed(
                text = "C1", width = columnWidths[4], textAlign = TextAlign.End, isHeader = true
            )
            TableCellFixed(
                text = "C2", width = columnWidths[5], textAlign = TextAlign.End, isHeader = true
            )
            TableCellFixed(
                text = "C3", width = columnWidths[6], textAlign = TextAlign.End, isHeader = true
            )
        }
    }
    val state = rememberLazyListState()
    // Data rows with vertical scroll
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f), state = state
    ) {
        itemsIndexed(items = filteredList) { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                    containerColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    TableCellFixed(
                        text = item.ProductName ?: "-",
                        width = columnWidths[0],
                        textAlign = TextAlign.Start,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.GodownName ?: "-",
                        width = columnWidths[1],
                        textAlign = TextAlign.Start,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.Value1?.toString() ?: "-",
                        width = columnWidths[2],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.Value2?.toString() ?: "-",
                        width = columnWidths[3],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C1 ?: "-",
                        width = columnWidths[4],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C2 ?: "-",
                        width = columnWidths[5],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                    TableCellFixed(
                        text = item.C3 ?: "-",
                        width = columnWidths[6],
                        textAlign = TextAlign.End,
                        isHeader = false
                    )
                }
            }
            if (index < filteredList.lastIndex) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
