package org.prime.tally.ui.screen.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.SearchBar
import org.prime.tally.ui.shared.TallyCircularLoader
import org.prime.tally.ui.shared.TallyReportScaffold
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.tally.TrialBalanceList
import kotlin.math.absoluteValue

object TrialBalanceScreen : Screen {
    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance

        var list by remember { mutableStateOf<List<TrialBalanceList>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var showSearchBar by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }


        val column1Weight = 0.6f
        val column2Weight = 0.3f
        val column3Weight = 0.3f

        val totalDebit = list.sumOf { item ->
            if ((item.ClsnBal ?: 0.0) < 0.0) item.ClsnBal ?: 0.0 else 0.0
        }
        val totalCredit = list.sumOf { item ->
            if ((item.ClsnBal ?: 0.0) > 0.0) item.ClsnBal ?: 0.0 else 0.0
        }
        val totalRows = list.count()


        LaunchedEffect(Unit) {
            isLoading = true
            list = db.vouchersLedgersQueries.trialBalanceList().executeAsList()
            isLoading = false
        }
        LaunchedEffect(showSearchBar) {
            if (showSearchBar) {
                focusRequester.requestFocus()
            }
        }

        TallyReportScaffold(
            "Trial Balance", showBottomBar = true,
            showSearchAction = true,
            onSearchClick = { showSearchBar = !showSearchBar },
            bottomBarContent = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                )
                {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                buildAnnotatedString {
                                    append("Rows: ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("$totalRows")
                                    }
                                },
                            modifier = Modifier.weight(column1Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = totalDebit.absoluteValue.toString(),
                            modifier = Modifier.weight(column2Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = totalCredit.absoluteValue.toString(),
                            modifier = Modifier.weight(column3Weight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            content = { paddingValues ->
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        if (showSearchBar) {
                            SearchBar(
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                        )
                        {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                TableCell(
                                    text = "Account Name",
                                    weight = column1Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    text = "Debit",
                                    weight = column2Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                                TableCell(
                                    text = "Credit",
                                    weight = column3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                            }
                        }


                        val filteredList = if (searchQuery.isEmpty()) {
                            list
                        } else {
                            list.filter { it.CM1?.contains(searchQuery, ignoreCase = true) == true }
                        }


                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredList) { item ->
                                val debitAmount =
                                    if ((item.ClsnBal ?: 0.0) < 0.0) item.ClsnBal
                                        ?: 0.0 else 0.0
                                val creditAmount =
                                    if ((item.ClsnBal ?: 0.0) > 0.0) item.ClsnBal
                                        ?: 0.0 else 0.0

                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 0.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell(
                                            text = item.CM1 ?: "",
                                            weight = column1Weight,
                                            isHeader = false
                                        )

                                        TableCell(
                                            text = if (debitAmount < 0) debitAmount.absoluteValue.toString() else "-",
                                            weight = column2Weight,
                                            textAlign = TextAlign.End,
                                            isHeader = false
                                        )
                                        TableCell(
                                            text = if (creditAmount > 0) creditAmount.absoluteValue.toString() else "-",
                                            weight = column3Weight,
                                            textAlign = TextAlign.End,
                                            isHeader = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }
}