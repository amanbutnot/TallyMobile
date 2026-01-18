package org.prime.tally.ui.screen.reports.outstanding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.data.utils.SharedPrefs
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.globalShared.parseToStringList
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.tally.OutstandingGroupList
import smartSearch
import kotlin.math.absoluteValue

data class OutstandingGroupListScreen(
    val name: String,
    val startDate: String,
    val endDate: String,
    val guid: Double,
    val account: String
) :
    Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<OutstandingGroupList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow

        val column1Weight = 0.6f
        val column2Weight = 0.4f

        val total = list.sumOf { item ->
            item.PenAmt ?: 0.0
        }

        val perms = SharedPrefs.Permissions.get()
        val filterAccounts = if (perms?.FilterAccounts == "Y") 1L else 0L
        val excludeGuids = perms?.ConfigAccounts.parseToStringList()
        val filterBroker = if (perms?.FilterBroker == "Y") 1L else 0L
        val configBroker =
            if (filterBroker == 1L) perms?.ConfigBroker.parseToStringList() else emptyList()
//        val menuItems = listOf(
//            MenuItemData(
//                title = "Download",
//                icon = Icons.Default.Download,
//                onClick = {
//                    scope.launch {
//                        handlePdfAction(
//                            fileName = "Trial Balance",
//                            htmlContent = threeHeaderHtml(
//                                title = "Trial Balance",
//                                headers = Triple("Account Name", "Debit", "Credit"),
//                                rows = rows,
//                                totalDebit = total.formatToAmtDec().toDouble(),
//                                totalCredit = totalCredit.formatToAmtDec().toDouble(),
//                                date = StartDate()
//                            ),
//                            action = PdfAction.Download,
//                            onLoadingChange = { shareLoading = it }
//                        )
//                    }
//                }
//            ),
//            MenuItemData(
//                title = "Share",
//                icon = Icons.Default.Share,
//                onClick = {
//                    scope.launch {
//                        handlePdfAction(
//                            fileName = "Trial Balance",
//                            htmlContent = threeHeaderHtml(
//                                title = "Trial Balance",
//                                headers = Triple("Account Name", "Debit", "Credit"),
//                                rows = rows,
//                                totalDebit = total,
//                                totalCredit = totalCredit, date = StartDate()
//                            ),
//                            action = PdfAction.Share,
//                            onLoadingChange = { shareLoading = it }
//                        )
//                    }
//                }
//            )
//        )
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }
        LaunchedEffect(Unit) {

            isLoading = true
            withContext(Dispatchers.IO) {
                println("${if (name == "Bill Receivable") 0 else 1} $startDate $endDate $filterBroker $configBroker $guid $filterAccounts $excludeGuids")
                list = db.voucherBillAllocationsQueries.outstandingGroupList(
                    mode = if (name == "Bill Receivable") 0 else 1,
                    DATE = startDate,
                    DATE_ = endDate,
                    filterCm3 = filterBroker,
                    cm3 = configBroker,
                    GroupCode = guid,
                    excludeFilter = filterAccounts,
                    GUID = excludeGuids,
                ).executeAsList()
                println(list)
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }
        val filteredList = smartSearch(
            list = list,
            query = searchQuery,
            selectors = listOf { it.Party }
        )




        TallyReportScaffold(
            name, showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = false,
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
                            total.absoluteValue.formatToAmtDec(),
                            column2Weight,
                            TextAlign.End
                        ),
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                            Text(
                                account,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                "${Tdate(startDate)} - ${Tdate(endDate)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        TallyReportHeaderCard(
                            columns = listOf(
                                ReportColumn(
                                    "Party",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Pending Amount",
                                    column2Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                nav.push(
                                    OutstandingReportScreen(
                                        name = name,
                                        startDate = startDate,
                                        endDate = endDate,
                                        cm1 = item.Party
                                    )
                                )
                            },
                            content = { item ->

                                TableCell(
                                    text = item.Party ?: "",
                                    weight = column1Weight,
                                    isHeader = false
                                )

                                TableCell(
                                    text = item.PenAmt?.formatToAmtDec().toString(),
                                    weight = column2Weight,
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
