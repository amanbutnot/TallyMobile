package org.prime.tally.ui.screen.reports.productReport

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.printing.productReportHtml
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.parseToDoubleList
import org.prime.tally.ui.shared.globalShared.parseToStringList
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
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
        val productGroups = remember { db.productGroupMasterQueries.selectAll().executeAsList() }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val bottomSheetState = rememberModalBottomSheetState()

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

                val groupCodes =
                    if (filterGroup == 1L) perms?.ConfigIGRP.parseToDoubleList() else emptyList()
                val excludeGuids =
                    if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
                val godownCodes =
                    if (filterGodown == 1L) perms?.ConfigGodown.parseToDoubleList() else emptyList()

                list = db.productParamStockQueries.getProductStockList(
                    productGuid,
                    enableParam,
                    paramFilters,
                    filterGroup = filterGroup,
                    groupCodes = groupCodes,
                    filterExclude = filterExclude,
                    excludeGuids = excludeGuids,
                    filterGodown = filterGodown,
                    godownCodes = godownCodes
                ).executeAsList()
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
            list.filter { it.GroupName in selectedGroups }
        }

        val filteredList = if (searchQuery.isEmpty()) {
            groupFilteredList
        } else {
            val startsWith = groupFilteredList.filter {
                it.ProductName?.startsWith(
                    searchQuery,
                    ignoreCase = true
                ) == true || it.GodownName?.startsWith(searchQuery, ignoreCase = true) == true
            }

            val contains = groupFilteredList.filter {
                val product = it.ProductName
                val godown = it.GodownName

                (product?.contains(searchQuery, ignoreCase = true) == true || godown?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true) && (product?.startsWith(
                    searchQuery,
                    ignoreCase = true
                ) != true && godown?.startsWith(searchQuery, ignoreCase = true) != true)
            }

            startsWith + contains
        }


        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }


        val menuItems = listOf(
            MenuItemData(
            title = "Download", icon = Icons.Default.Download, onClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Product Report", htmlContent = productReportHtml(
                            rows = list
                        ), action = PdfAction.Download, onLoadingChange = { shareLoading = it })
                }
            }), MenuItemData(
            title = "Share", icon = Icons.Default.Share, onClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Barcode Report", htmlContent = productReportHtml(
                            rows = list
                        ), action = PdfAction.Download, onLoadingChange = { shareLoading = it })
                }
            }))

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
            title = "Barcode Report",
            showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            menuItems = menuItems,
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
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showGroupFilterSheet = true }) {
                                Text("Account Group Filter")
                            }
                        }
//                        if (isMain) {
//                            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                                items(filteredList) {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 16.dp)
//                                            .padding(top = 16.dp, bottom = 20.dp)
//                                    ) {
//                                        Text(
//                                            text = it.ProductName.toString(),
//                                            style = MaterialTheme.typography.headlineLarge,
//                                            fontWeight = FontWeight.Bold,
//                                            modifier = Modifier.padding(bottom = 8.dp)
//                                        )
//
//                                        Row(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            horizontalArrangement = Arrangement.SpaceBetween
//                                        ) {
//                                            Text(
//                                                text = it.C1.toString(),
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                                            )
//                                            Text(
//                                                text = it.C2.toString(),
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                                            )
//                                            Text(
//                                                text = it.C3.toString(),
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                                            )
//                                            Text(
//                                                text = it.Value1.toString(),
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                fontWeight = FontWeight.SemiBold,
//                                                color = MaterialTheme.colorScheme.primary
//                                            )
//                                        }
//                                    }
//
//                                    HorizontalDivider(
//                                        modifier = Modifier.padding(horizontal = 16.dp),
//                                        thickness = 1.dp,
//                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
//                                    )
//
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                }
//                            }
//                        } else {
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
                                            text = "Total: ${items.sumOf { it.Value1 ?: 0.0 }}",
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
                                                text = it.Value1.toString(),
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

@Composable
fun TableCellFixed(
    text: String,
    width: Dp,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier.width(width).padding(horizontal = 8.dp),
        textAlign = textAlign,
        style = if (isHeader) {
            MaterialTheme.typography.titleSmall
        } else {
            MaterialTheme.typography.bodyMedium
        },
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        color = if (isHeader) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        maxLines = if (isHeader) 1 else 2,
        overflow = TextOverflow.Ellipsis
    )
}
