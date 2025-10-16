package org.prime.tally.ui.screen.reports.registers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.ui.printing.Quadruple
import org.prime.tally.ui.printing.fourHeaderHtml
import org.prime.tally.ui.screen.reports.ledger.LedgerReportItemScreen
import org.prime.tally.ui.shared.composables.MenuItemData
import org.prime.tally.ui.shared.composables.TallyCircularLoader
import org.prime.tally.ui.shared.composables.TallyLoadingDialog
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.composables.TallySearchBar
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.reportsShared.PdfAction
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.tally.ui.shared.reportsShared.TallyReportHeaderCard
import org.prime.tally.ui.shared.reportsShared.TallyReportLazyList
import org.prime.tally.ui.shared.reportsShared.handlePdfAction
import org.tally.RegisterReportList
import kotlin.math.absoluteValue

data class RegisterReportScreen(val name: String, val startDate: String, val endDate: String) :
    Screen {
    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<RegisterReportList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val nav = LocalNavigator.currentOrThrow
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()


        val column1Weight = 0.3f
        val column2Weight = 0.5f
        val column3Weight = 0.2f
        val column4Weight = 0.3f

        val totalAmt = list.sumOf { it.D1 ?: 0.0 }


        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                list = db.vouchersLedgersQueries.registerReportList(
                    VchType = name,
                    DATE = startDate,
                    DATE_ = endDate
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
        val filteredList = if (searchQuery.isEmpty()) {
            list
        } else {
            list.filter {
                it.CM1?.startsWith(
                    searchQuery,
                    ignoreCase = true
                ) == true
            }
        }

        val rows: List<Quadruple<String, String, String, String>> = filteredList.map { item ->
            Quadruple(
                item.DATE ?: "",
                item.CM1 ?: "",
                item.VOUCHERNUMBER?.trim() ?: "",
                item.D1?.absoluteValue?.formatToAmtDec() ?: ""
            )
        }


        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        handlePdfAction(
                            fileName = name,
                            htmlContent = fourHeaderHtml(
                                title = name,
                                headers = Quadruple("Date", "Name", "Vch No", "Amount"),
                                rows = rows,
                                total1 = totalAmt.formatToAmtDec(),
                                startDate = startDate,
                                endDate = endDate,
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
                            fileName = name,
                            htmlContent = fourHeaderHtml(
                                title = name,
                                headers = Quadruple("Date", "Name", "Vch No", "Amount"),
                                rows = rows,
                                total1 = totalAmt.formatToAmtDec(),
                                startDate = startDate,
                                endDate = endDate,
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


        TallyReportScaffold(
            "$name Report", showBottomBar = true,
            showSearchAction = true,
            showBurgerMenu = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            menuItems = menuItems,
            bottomBarContent = {
                TallyReportBottomBar(
                    columns = listOf(
                        ReportColumn(
                            "Rows: ${filteredList.count()}",
                            column1Weight + column2Weight + column3Weight,
                            TextAlign.Start
                        ),
                        ReportColumn(
                            totalAmt.absoluteValue.formatToAmtDec(),
                            column4Weight,
                            TextAlign.End
                        ),
//                        ReportColumn(
//                            totalAmt.absoluteValue.toString(),
//                            column3Weight,
//                            TextAlign.End
//                        )
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
                                    "Date",
                                    column1Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Name",
                                    column2Weight,
                                    TextAlign.Start
                                ),
                                ReportColumn(
                                    "Vch No",
                                    column3Weight,
                                    TextAlign.Start
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
                                nav.push(
                                    LedgerReportItemScreen(
                                        date = startDate,
                                        vchType = item.VchName.toString(),
                                        guid = item.VCH_GUID.toString(),
                                        vchNo = item.VOUCHERNUMBER.toString()
                                    )
                                )

                            },
                            content = { item ->
                                TableCell(
                                    text = Tdate(item.DATE ?: ""),
                                    weight = column1Weight,
                                    textAlign = TextAlign.Companion.Start,
                                    isHeader = false
                                )

                                TableCell(
                                    text = item.CM1.toString(),
                                    weight = column2Weight,
                                    textAlign = TextAlign.Companion.Start,
                                    isHeader = false
                                )
                                TableCell(
                                    text = item.VOUCHERNUMBER.toString().trim(),
                                    weight = column3Weight,
                                    textAlign = TextAlign.Companion.Start,
                                    isHeader = false
                                )
                                TableCell(
                                    text = item.D1?.absoluteValue?.formatToAmtDec() ?: "-",
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
