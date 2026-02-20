package org.prime.easykarobar.ui.screen.reports.pendingOrder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.reports.outstanding.OutstandingReportScreen
import org.prime.easykarobar.ui.shared.composables.GroupFilterBottomSheet
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.filterGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.getPCGroupCodesByName
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportLazyList
import org.tally.PendingPurchaseOrderList
import smartSearch
import kotlin.math.absoluteValue

data class PendingOrderPartyList(
    val name: String, val startDate: String, val endDate: String, val cm1: String? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val perms = SharedPrefs.Permissions.get()
        val filterBroker = if (perms?.FilterBroker == "Y") 1L else 0L
        val configBroker =
            if (filterBroker == 1L) perms?.ConfigBroker.parseToStringList() else emptyList()
        val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
        val filterAccounts = if (perms?.FilterAccounts == "Y") 1L else 0L
        val excludeGuids = perms?.ConfigAccounts.parseToStringList()
        val column1Weight = 1f
        val nav = LocalNavigator.currentOrThrow
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedGroups by remember { mutableStateOf<List<String>>(emptyList()) }
        val focusRequester = remember { FocusRequester() }
        var list by remember { mutableStateOf<List<PendingPurchaseOrderList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        val db = DatabaseHolder.instance
        var showGroupFilterSheet by remember { mutableStateOf(false) }
        val productGroups = remember { db.ledgerGroupMasterQueries.selectAll().executeAsList() }
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)


        GroupFilterBottomSheet(
            show = showGroupFilterSheet,
            items = productGroups,
            selectedItems = selectedGroups,
            itemNameSelector = { it.Name },
            onSelectedItemsChange = { selectedGroups = it },
            onDismiss = { showGroupFilterSheet = false },
            bottomSheetState = bottomSheetState
        )


        LaunchedEffect(Unit) {
            isLoading = true
            if (name == "Pending Sale Order") {
                list = db.vouchersPendingOrderQueries.pendingSaleOrderList(
                    fromDate = startDate,
                    toDate = endDate,
                    filterCm3 = filterBroker,
                    cm3 = configBroker,
                    groupFilter = filterAGRP,
                    groupCodes = filterGroupCodes(),
                    excludeFilter = filterAccounts,
                    excludeGuids = excludeGuids
                ).executeAsList().map {
                    PendingPurchaseOrderList(
                        PartyName = it.PartyName,
                        GroupName = it.GroupName,
                        PendingQty = it.PendingQty
                    )
                }
            } else {
                list = db.vouchersPendingOrderQueries.pendingPurchaseOrderList(
                    fromDate = startDate,
                    toDate = endDate,
                    filterCm3 = filterBroker,
                    cm3 = configBroker,
                    groupFilter = filterAGRP,
                    groupCodes = filterGroupCodes(),
                    excludeFilter = filterAccounts,
                    excludeGuids = excludeGuids
                ).executeAsList()
            }
            isLoading = false
        }

        val filteredList = smartSearch(
            list = list,
            query = searchQuery,
            selectors = listOf { it.PartyName }
        )
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }
        // Apply group filter
        val groupFilteredReceivableList = if (selectedGroups.isEmpty()) {
            filteredList.filter { it.PendingQty!=0.0 }
        } else {
            filteredList.filter { it.GroupName in getPCGroupCodesByName(selectedGroups) && it.PendingQty!=0.0}
        }


        TallyReportScaffold(
            title = name,
            showBottomBar = true,
            showSearchAction = true,
            showBarcodeIcon = false,
            showBurgerMenu = false,
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            text = "Rows: ${list.count()}",
                            weight = column1Weight,
                            textAlign = TextAlign.Start
                        ),
                        ReportColumn(
                            text = "Total: ${list.sumOf { it.PendingQty?.absoluteValue ?: 0.0 }.formatToAmtDec()}",
                            weight = column1Weight,
                            textAlign = TextAlign.End
                        ),
                    ),
                )
            },
            onSearchClick = { showSearchBar = !showSearchBar },
        ) { paddingValues ->
            // Group Filter Bottom Sheet

            if (isLoading) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    TallyCircularLoader()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "From ${Tdate(startDate)}  →  To ${Tdate(endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showGroupFilterSheet = true }) {
                                Text(
                                    if (selectedGroups.isEmpty()) "Group Filter"
                                    else "Group Filter (${selectedGroups.size})"
                                )
                            }
                        }
                    }
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
                                "Party Name",
                                column1Weight,
                                TextAlign.Start
                            ),
                            ReportColumn(
                                "Pending Qty",
                                column1Weight,
                                TextAlign.End
                            )
                        )
                    )

                    TallyReportLazyList(
                        items = groupFilteredReceivableList,
                        onItemClick = { item ->
                            nav.push(
                                OutstandingReportScreen(
                                    name = name,
                                    startDate = startDate,
                                    endDate = endDate,
                                    cm1 = item.PartyName
                                )
                            )
                        },
                        key = {
                            "${it.PendingQty} ${it.GroupName} ${it.PartyName}"
                        },
                        content = { item ->
                            TableCell(
                                text = item.PartyName ?: "",
                                weight = column1Weight,
                                isHeader = false
                            )
                            TableCell(
                                text = item.PendingQty?.absoluteValue?.formatToAmtDec().toString(),
                                weight = column1Weight,
                                isHeader = false, textAlign = TextAlign.End
                            )

                        }
                    )
                }
            }
        }
    }
}
