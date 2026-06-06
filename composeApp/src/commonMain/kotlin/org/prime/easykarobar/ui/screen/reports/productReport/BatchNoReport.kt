package org.prime.easykarobar.ui.screen.reports.productReport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.filterItemGroups
import org.prime.easykarobar.ui.shared.globalShared.getProductsGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.itemGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.BatchNoEnterReport
import org.prime.easykarobar.ui.shared.composables.smartSearch
import kotlin.math.absoluteValue

data class BatchNoReport(
    val productGuid: String? = null,
    val isMain: Boolean,
    val isDirect: Boolean,
    val godownCode: String?=null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<BatchNoEnterReport>>(emptyList()) }
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
                val filterGroup = if (perms?.FilterIGRP == "Y") 1L else 0L
                val filterExclude = if (perms?.FilterItems == "Y") 1L else 0L
                val filterGodown = if (perms?.FilterGodown == "Y") 1L else 0L

                val excludeGuids =
                    if (filterExclude == 1L) perms?.ConfigItems.parseToStringList() else emptyList()
                val godownCodes =
                    if (filterGodown == 1L) perms?.ConfigGodown.parseToStringList() else emptyList()
                list = db.productBatchNoQueries.batchNoEnterReport(
                    filterGroup = filterGroup,
                    groupCodes = filterItemGroupCodes(),
                    filterExclude = filterExclude,
                    excludeGuids = excludeGuids,
                    filterGodown = filterGodown,
                    godownCodes = godownCodes,
                    filterSingle = if (isMain) 0L else 1L,
                    includeSingle = productGuid,
                    filterSingleG = if (isDirect) 0L else 1L,
                    includeSingleG = godownCode
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
            list.filter { it.GroupName in getProductsGroupCodesByName(selectedGroups) }
        }

        val filteredList = smartSearch(
            list = groupFilteredList,
            query = searchQuery,
            selectors = listOf(
                { it.ProductName },
            )
        )
        val totalAmt = filteredList.sumOf { it.Value3?.toDouble() ?: 0.0 }


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
            title = "Batch No. Report",
            showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onDownloadClick = {},
            onShareClick = {},
            onExcelClick = {
                scope.launch {
                    val excelRows = filteredList.map { item ->
                        listOf(
                            item.ProductName ?: "",
                            item.BatchNo ?: "",
                            item.Value3?.toString() ?: "0.0"
                        )
                    }
                    handlePdfAction(
                        fileName = "Batch_No_Report",
                        htmlContent = "",
                        headers = listOf("Product Name", "Batch No", "Qty"),
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
                            "Total Qty: ${filteredList.count()}", 1f, TextAlign.Start
                        ),
                        ReportColumn(
                            totalAmt.absoluteValue.formatToAmtDec(),
                            1f,
                            TextAlign.End
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
                                                "Total: " + items.sumOf { it.Value1 ?: 0.0 }.formatToAmtDec() + " (${items.sumOf { it.Value3?:0.0 }.formatToAmtDec()})"
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
                                                text = it.BatchNo.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (showQtyToSalesman()) it.Value3.toString() else "",
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
