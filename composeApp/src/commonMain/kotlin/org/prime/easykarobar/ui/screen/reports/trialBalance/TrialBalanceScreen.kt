package org.prime.easykarobar.ui.screen.reports.trialBalance

import org.prime.easykarobar.ui.shared.reportsShared.CurrentDate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import org.prime.easykarobar.data.expect.stringToDouble
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.printing.threeHeaderHtml
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportScreen
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.composables.TallySearchBar
import org.prime.easykarobar.ui.shared.composables.smartSearch
import org.prime.easykarobar.ui.shared.globalShared.StartDate
import org.prime.easykarobar.ui.shared.globalShared.filterGroupCodes
import org.prime.easykarobar.ui.shared.globalShared.parseToDoubleList
import org.prime.easykarobar.ui.shared.globalShared.parseToStringList
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportLazyList
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import org.tally.TrialBalanceList
import kotlin.math.absoluteValue

object TrialBalanceScreen : Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<TrialBalanceList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow

        val column1Weight = 0.6f
        val column2Weight = 0.3f
        val column3Weight = 0.3f

        val totalDebit = list.sumOf { item ->
            if ((item.ClsnBal ?: 0.0) < 0.0) item.ClsnBal ?: 0.0 else 0.0
        }
        val totalCredit = list.sumOf { item ->
            if ((item.ClsnBal ?: 0.0) > 0.0) item.ClsnBal ?: 0.0 else 0.0
        }
        val rows = list.map { item ->
            val debit = if ((item.ClsnBal ?: 0.0) < 0.0)
                (item.ClsnBal ?: 0.0).absoluteValue.formatToAmtDec()
            else ""

            val credit = if ((item.ClsnBal ?: 0.0) > 0.0)
                (item.ClsnBal ?: 0.0).absoluteValue.formatToAmtDec()
            else ""

            Triple(item.CM1 ?: "", debit, credit)
        }
        val rowsExcel: List<List<String>> = list.map { item ->
            val balance = item.ClsnBal ?: 0.0

            val debit = if (balance < 0.0)
                balance.absoluteValue.formatToAmtDec()
            else ""

            val credit = if (balance > 0.0)
                balance.absoluteValue.formatToAmtDec()
            else ""

            listOf(
                item.CM1 ?: "",
                debit,
                credit
            )
        }
        val perms = SharedPrefs.Permissions.get()
        val filterAGRP = if (perms?.FilterAGRP == "Y") 1L else 0L
        val filterAccounts = if (perms?.FilterAccounts == "Y") 1L else 0L
        val groupCodes = perms?.ConfigAGRP.parseToDoubleList()
        val excludeGuids = perms?.ConfigAccounts.parseToStringList()
        if (shareLoading) {
            TallyLoadingDialog("Generating Report")
        }
        LaunchedEffect(Unit) {

            isLoading = true
            withContext(Dispatchers.IO) {
                list = db.vouchersLedgersQueries.trialBalanceList(
                    groupFilter = filterAGRP,
                    GroupCode = filterGroupCodes(),
                    excludeFilter = filterAccounts,
                    GUID = excludeGuids,
//                    startDate = StartDate(),
//                    endDate = PreviousDate()
                ).executeAsList()
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
            selectors = listOf { it.CM1 }
        )




        TallyReportScaffold(
            title = "Trial Balance",
            showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Trial Balance",
                        htmlContent = threeHeaderHtml(
                            title = "Trial Balance",
                            headers = Triple("Account Name", "Debit", "Credit"),
                            rows = rows,
                            totalDebit = totalDebit.formatToAmtDec().stringToDouble(),
                            totalCredit = totalCredit.formatToAmtDec().stringToDouble(),
                            date = StartDate()
                        ),
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Trial Balance",
                        htmlContent = threeHeaderHtml(
                            title = "Trial Balance",
                            headers = Triple("Account Name", "Debit", "Credit"),
                            rows = rows,
                            totalDebit = totalDebit,
                            totalCredit = totalCredit, date = StartDate()
                        ),
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onExcelClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "Trial Balance",
                        headers = listOf("Account Name", "Debit", "Credit"),
                        rows = rowsExcel,
                        htmlContent = threeHeaderHtml(
                            title = "Trial Balance",
                            headers = Triple("Account Name", "Debit", "Credit"),
                            rows = rows,
                            totalDebit = totalDebit.formatToAmtDec().stringToDouble(),
                            totalCredit = totalCredit.formatToAmtDec().stringToDouble(),
                            date = StartDate()
                        ),
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
                            totalDebit.absoluteValue.formatToAmtDec(),
                            column2Weight,
                            TextAlign.End
                        ),
                        ReportColumn(
                            totalCredit.absoluteValue.formatToAmtDec(),
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
                                    "Account Name",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Debit",
                                    column2Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    "Credit",
                                    column3Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        TallyReportLazyList(
                            items = filteredList,
                            onItemClick = { item ->
                                nav.push(
                                    LedgerReportScreen(
                                        accountName = item.CM1.toString(),
                                        startDate = StartDate(),
                                        endDate = CurrentDate()
                                    )
                                )
                            }, key = { item ->
                                buildString {
                                    append(item.CM1)
                                    append('|')
                                    append(item.ClsnBal)

                                }
                            },
                            content = { item ->
                                val debitAmount =
                                    if ((item.ClsnBal ?: 0.0) < 0.0) item.ClsnBal
                                        ?: 0.0 else 0.0
                                val creditAmount =
                                    if ((item.ClsnBal ?: 0.0) > 0.0) item.ClsnBal
                                        ?: 0.0 else 0.0

                                TableCell(
                                    text = item.CM1 ?: "",
                                    weight = column1Weight,
                                    isHeader = false
                                )

                                TableCell(
                                    text = if (debitAmount < 0) debitAmount.absoluteValue.formatToAmtDec() else "-",
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.End,
                                    isHeader = false
                                )
                                TableCell(
                                    text = if (creditAmount > 0) creditAmount.absoluteValue.formatToAmtDec() else "-",
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

