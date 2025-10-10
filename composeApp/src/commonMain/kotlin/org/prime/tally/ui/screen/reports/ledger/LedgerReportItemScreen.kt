package org.prime.tally.ui.screen.reports.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.prime.tally.data.expect.DatabaseHolder
import org.prime.tally.ui.shared.composables.TallyReportScaffold
import org.prime.tally.ui.shared.globalShared.Tdate
import org.prime.tally.ui.shared.reportsShared.ReportColumn
import org.prime.tally.ui.shared.reportsShared.TableCell
import org.prime.tally.ui.shared.reportsShared.TallyReportBottomBar

data class LedgerReportItemScreen(
    val vchNo: String,
    val date: String,
    val vchType: String,
    val guid: String
) : Screen {

    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val ledgerStockItemList =
            db.vouchersStockItemsQueries.ledgerStockItemList(guid).executeAsList()
        val ledgerReportItemList =
            db.vouchersLedgersQueries.ledgerReportItemList(guid).executeAsList()

        val stockColumn1Weight = 0.4f
        val stockColumn2Weight = 0.8f
        val stockColumn3Weight = 0.3f
        val stockColumn4Weight = 0.5f
        val stockColumn5Weight = 0.5f

        TallyReportScaffold(
            title = "$vchType Entry Details",
            showBottomBar = false,
            showSearchAction = false, content = { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        // .navigationBarsPadding()
                        .padding(horizontal = 8.dp)
                ) {

                    Spacer(Modifier.height(8.dp))

                    // Top info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Date: ${Tdate(date)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Vch No.: $vchNo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ----------------------- STOCK ITEM DETAILS -----------------------
                    val amtTotal = ledgerStockItemList.sumOf { it.Amt ?: 0.0 }
                    if (ledgerStockItemList.isNotEmpty()) {
                        Text(
                            "Stock Item Details:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))

                        // Header
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                TableCell(
                                    "S No.",
                                    stockColumn1Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    "Item Name",
                                    stockColumn2Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    "Qty",
                                    stockColumn3Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                                TableCell(
                                    "Rate",
                                    stockColumn4Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                                TableCell(
                                    "Amount",
                                    stockColumn5Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                            }
                        }


                        // Scrollable list of stock items
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(ledgerStockItemList) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell((index + 1).toString(), stockColumn1Weight)
                                        TableCell(item.Item_Name ?: "", stockColumn2Weight)
                                        TableCell(
                                            item.Qty.toString(),
                                            stockColumn3Weight,
                                            textAlign = TextAlign.End
                                        )
                                        TableCell(
                                            item.Rate.toString(),
                                            stockColumn4Weight,
                                            textAlign = TextAlign.End
                                        )
                                        TableCell(
                                            item.Amt.toString(),
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Stock total


                        // Total row - stays visible
                        TallyReportBottomBar(
                            columns = listOf(
                                ReportColumn(
                                    "Total:",
                                    (stockColumn1Weight + stockColumn2Weight + stockColumn3Weight + stockColumn4Weight),
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    text = amtTotal.toString(),
                                    stockColumn5Weight,
                                    TextAlign.End
                                )
                            )
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                    val indexedLedgerList =
                        if (ledgerStockItemList.isNotEmpty()) ledgerReportItemList.drop(2) else ledgerReportItemList


                    // ----------------------- LEDGER DETAILS -----------------------
                    if (ledgerReportItemList.isNotEmpty()) {
                        val totalDebit = ledgerReportItemList.sumOf { it.DebitAmt ?: 0.0 }
                        val totalCredit = ledgerReportItemList.sumOf { it.CreditAmt ?: 0.0 }

                        Text(
                            "Ledger Details:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        // Header
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                TableCell(
                                    "S No.",
                                    stockColumn1Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    "Account",
                                    stockColumn2Weight + stockColumn3Weight,
                                    textAlign = TextAlign.Start,
                                    isHeader = true
                                )
                                TableCell(
                                    "Debit Amt",
                                    stockColumn5Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                                TableCell(
                                    "Credit Amt",
                                    stockColumn5Weight,
                                    textAlign = TextAlign.End,
                                    isHeader = true
                                )
                            }
                        }

                        // Scrollable list of ledger items
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(indexedLedgerList) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell((index + 1).toString(), stockColumn1Weight)
                                        TableCell(
                                            item.LedgerName ?: "",
                                            stockColumn2Weight + stockColumn3Weight
                                        )
                                        TableCell(
                                            item.DebitAmt.toString(),
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End
                                        )
                                        TableCell(
                                            item.CreditAmt.toString(),
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }

                        // Total row - stays visible
                        TallyReportBottomBar(
                            columns = listOf(
                                ReportColumn(
                                    "Total:",
                                    (stockColumn1Weight + stockColumn2Weight + stockColumn3Weight + stockColumn4Weight),
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    text = totalDebit.toString(),
                                    stockColumn5Weight,
                                    TextAlign.End
                                ),
                                ReportColumn(
                                    text = totalCredit.toString(),
                                    stockColumn5Weight,
                                    TextAlign.End
                                ),

                                )
                        )
                    }
                }
            }
        )

    }
}
