package org.prime.easykarobar.ui.screen.reports.ledger

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.ui.printing.InvoiceItem
import org.prime.easykarobar.ui.printing.InvoiceParticular
import org.prime.easykarobar.ui.printing.ReceiptPaymentRow
import org.prime.easykarobar.ui.printing.receiptPaymentHtml
import org.prime.easykarobar.ui.printing.salesInvoiceHtml
import org.prime.easykarobar.ui.shared.composables.MenuItemData
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.globalShared.isBusy
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import kotlin.math.absoluteValue

data class LedgerReportItemScreen(
    val vchNo: String,
    val date: String,
    val vchType: String,
    val guid: String
) : Screen {

    @Composable
    override fun Content() {
        val db = DatabaseHolder.instance
        val ledgerStockItemList = db.vouchersStockItemsQueries.ledgerStockItemList(guid).executeAsList()
        val ledgerStockBusyItemList = db.vouchersBsLedgersQueries.selectAll(guid).executeAsList()
        val ledgerReportItemList = db.vouchersLedgersQueries.ledgerReportItemList(guid).executeAsList()
        val vouchers = db.vouchersQueries.selectByGuid(guid).executeAsOneOrNull()

        var isLoading by remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                // simulate data fetch if needed
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }

        val stockColumn1Weight = 0.4f
        val stockColumn2Weight = 0.8f
        val stockColumn3Weight = 0.3f
        val stockColumn4Weight = 0.5f
        val stockColumn5Weight = 0.5f

        // Menu items for PDF generation
        val menuItems = listOf(
            MenuItemData(
                title = "Download",
                icon = Icons.Default.Download,
                onClick = {
                    scope.launch {
                        val htmlContent = withContext(Dispatchers.Default) {
                            if (ledgerStockItemList.isNotEmpty()) {
                                // 🔹 Sales Invoice HTML
                                val totalQty = ledgerStockItemList.sumOf { it.Qty ?: 0.0 }
                                val totalAmount = ledgerStockItemList.sumOf { it.Amt ?: 0.0 }
                                val grandTotal = totalAmount

                                salesInvoiceHtml(
                                    voucherNo = vchNo,
                                    date = date,
                                    partyName = ledgerReportItemList.firstOrNull()?.LedgerName
                                        ?: "",
                                    items = ledgerStockItemList.mapIndexed { index, it ->
                                        InvoiceItem(
                                            sn = index + 1,
                                            itemName = it.Item_Name ?: "",
                                            qty = it.Qty ?: 0.0,
                                            rate = it.Rate ?: 0.0,
                                            amount = it.Amt ?: 0.0
                                        )
                                    },
                                    particulars = ledgerReportItemList.map {
                                        InvoiceParticular(
                                            name = it.LedgerName ?: "",
                                            amount = it.CreditAmt ?: 0.0
                                        )
                                    },
                                    totalQty = totalQty,
                                    totalAmount = totalAmount,
                                    grandTotal = grandTotal,
                                    companyAddress = CompanyName(),
                                    companyContact = "",
                                    documentType = vchType
                                )
                            } else {
                                // 🔹 Receipt / Payment HTML
                                val rows = ledgerReportItemList.mapIndexed { index, it ->
                                    ReceiptPaymentRow(
                                        sn = index + 1,
                                        account = it.LedgerName ?: "",
                                        debit = it.DebitAmt,
                                        credit = it.CreditAmt
                                    )
                                }

                                val totalDebit = rows.sumOf { it.debit ?: 0.0 }
                                val totalCredit = rows.sumOf { it.credit ?: 0.0 }

                                receiptPaymentHtml(
                                    voucherNo = vchNo,
                                    date = date,
                                    rows = rows,
                                    totalDebit = totalDebit,
                                    totalCredit = totalCredit,
                                    companyAddress = CompanyName(),
                                    companyContact = "",
                                    documentType = vchType
                                )
                            }
                        }

                        handlePdfAction(
                            fileName = "$vchType Report",
                            htmlContent = htmlContent,
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
                        val htmlContent = withContext(Dispatchers.Default) {
                            if (ledgerStockItemList.isNotEmpty()) {
                                val totalQty = ledgerStockItemList.sumOf { it.Qty ?: 0.0 }
                                val totalAmount = ledgerStockItemList.sumOf { it.Amt ?: 0.0 }
                                val grandTotal = totalAmount

                                salesInvoiceHtml(
                                    voucherNo = vchNo,
                                    date = date,
                                    partyName = ledgerReportItemList.firstOrNull()?.LedgerName
                                        ?: "",
                                    items = ledgerStockItemList.mapIndexed { index, it ->
                                        InvoiceItem(
                                            sn = index + 1,
                                            itemName = it.Item_Name ?: "",
                                            qty = it.Qty ?: 0.0,
                                            rate = it.Rate ?: 0.0,
                                            amount = it.Amt ?: 0.0
                                        )
                                    },
                                    particulars = ledgerReportItemList.map {
                                        InvoiceParticular(
                                            name = it.LedgerName ?: "",
                                            amount = it.CreditAmt ?: 0.0
                                        )
                                    },
                                    totalQty = totalQty,
                                    totalAmount = totalAmount,
                                    grandTotal = grandTotal,
                                    companyAddress = CompanyName(),
                                    companyContact = "",
                                    documentType = vchType
                                )
                            } else {
                                val rows = ledgerReportItemList.mapIndexed { index, it ->
                                    ReceiptPaymentRow(
                                        sn = index + 1,
                                        account = it.LedgerName ?: "",
                                        debit = it.DebitAmt,
                                        credit = it.CreditAmt
                                    )
                                }

                                val totalDebit = rows.sumOf { it.debit ?: 0.0 }
                                val totalCredit = rows.sumOf { it.credit ?: 0.0 }

                                receiptPaymentHtml(
                                    voucherNo = vchNo,
                                    date = date,
                                    rows = rows,
                                    totalDebit = totalDebit,
                                    totalCredit = totalCredit,
                                    companyAddress = CompanyName(),
                                    companyContact = "",
                                    documentType = vchType
                                )
                            }
                        }

                        handlePdfAction(
                            fileName = "$vchType Report",
                            htmlContent = htmlContent,
                            action = PdfAction.Share,
                            onLoadingChange = { shareLoading = it }
                        )
                    }
                }
            )
        )

        if (shareLoading) TallyLoadingDialog("Generating Report")

        // UI Layout
        TallyReportScaffold(
            title = "$vchType Entry Details",
            showBurgerMenu = true,
            menuItems = menuItems,
            showBottomBar = false,
            showSearchAction = false,
            content = { paddingValues ->
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TallyCircularLoader()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 8.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Name: " + vouchers?.PARTYLEDGERNAME.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Date: ${Tdate(date)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                "Vch No.: $vchNo",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        val amtTotal = ledgerStockItemList.sumOf { it.Amt ?: 0.0 }

                        if (ledgerStockItemList.isNotEmpty()) {
                            Text(
                                "Voucher Item Details:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.height(4.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    TableCell("S No.", stockColumn1Weight, isHeader = true)
                                    TableCell("Item Name", stockColumn2Weight, isHeader = true)
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

                            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                itemsIndexed(ledgerStockItemList) { index, item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
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
                                                item.Qty?.absoluteValue?.formatToQtyDec()
                                                    .toString(),
                                                stockColumn3Weight,
                                                textAlign = TextAlign.End
                                            )
                                            println("RATE IS : ${item.Amt?.absoluteValue?.formatToAmtDec()}")
                                            TableCell(
                                                item.Rate?.absoluteValue?.formatToAmtDec()
                                                    .toString(),
                                                stockColumn4Weight,
                                                textAlign = TextAlign.End
                                            )
                                            TableCell(
                                                item.Amt?.absoluteValue?.formatToAmtDec()
                                                    .toString(),
                                                stockColumn5Weight,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }
                            }

                            TallyReportBottomBar(
                                columns = listOf(
                                    ReportColumn(
                                        "Total:",
                                        (stockColumn1Weight + stockColumn2Weight + stockColumn3Weight + stockColumn4Weight),
                                        TextAlign.End
                                    ),
                                    ReportColumn(
                                        amtTotal.absoluteValue.formatToAmtDec(),
                                        stockColumn5Weight,
                                        TextAlign.End
                                    )
                                )
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }

                        val indexedLedgerList = when {

                            ledgerStockItemList.isNotEmpty() -> ledgerReportItemList.drop(1)
                            else -> ledgerReportItemList
                        }


                        if (ledgerReportItemList.isNotEmpty()) {
                            val totalCredit = ledgerReportItemList.sumOf { it.CreditAmt ?: 0.0 }
                            val totalDebit = ledgerReportItemList.sumOf { it.DebitAmt ?: 0.0 }


                            Text(
                                if (isBusy()) "Bill Sundry" else "Ledger Details:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(Modifier.height(4.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                            {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    TableCell("S No.", stockColumn1Weight, isHeader = true)
                                    TableCell(
                                        "Account",
                                        stockColumn2Weight + stockColumn3Weight,
                                        isHeader = true
                                    )
                                    if (ledgerStockItemList.isEmpty()) {
                                        TableCell(
                                            "Credit",
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End,
                                            isHeader = true
                                        )
                                        TableCell(
                                            "Debit",
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End,
                                            isHeader = true
                                        )
                                    } else {
                                        if (isBusy()) {
                                            TableCell(
                                                "%",
                                                stockColumn5Weight,
                                                textAlign = TextAlign.End,
                                                isHeader = true
                                            )
                                        }
                                        TableCell(
                                            "Amount",
                                            stockColumn5Weight,
                                            textAlign = TextAlign.End,
                                            isHeader = true
                                        )
                                    }

                                }
                            }


                            if (isBusy()) {
                                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    itemsIndexed(ledgerStockBusyItemList) { index, item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TableCell(
                                                    (index + 1).toString(),
                                                    stockColumn1Weight
                                                )
                                                TableCell(
                                                    item.CM1 ?: "",
                                                    stockColumn2Weight + stockColumn3Weight
                                                )
                                                TableCell(
                                                    item.D1?.formatToAmtDec().toString(),
                                                    stockColumn5Weight,
                                                    textAlign = TextAlign.End,
                                                    isHeader = false
                                                )
//println("D3 iS : ${item.D3}")

                                                TableCell(
                                                    item.D3?.formatToAmtDec().toString()
                                                        .toString(),
                                                    stockColumn5Weight,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {

                                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    itemsIndexed(indexedLedgerList) { index, item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TableCell(
                                                    (index + 1).toString(),
                                                    stockColumn1Weight
                                                )
                                                TableCell(
                                                    item.LedgerName ?: "",
                                                    stockColumn2Weight + stockColumn3Weight
                                                )
                                                if (ledgerStockItemList.isEmpty()) {
                                                    TableCell(
                                                        item.CreditAmt?.absoluteValue?.formatToAmtDec()
                                                            .toString(),
                                                        stockColumn5Weight,
                                                        textAlign = TextAlign.End
                                                    )
                                                    TableCell(
                                                        item.DebitAmt?.absoluteValue?.formatToAmtDec()
                                                            .toString(),
                                                        stockColumn5Weight,
                                                        textAlign = TextAlign.End
                                                    )
                                                } else {
                                                    TableCell(
                                                        item.CreditAmt?.absoluteValue?.formatToAmtDec()
                                                            .toString(),
                                                        stockColumn5Weight,
                                                        textAlign = TextAlign.End
                                                    )

                                                }

                                            }
                                        }
                                    }
                                }
                            }


                            TallyReportBottomBar(
                                columns = listOf(
                                    ReportColumn(
                                        "Total:",
                                        (stockColumn1Weight + stockColumn2Weight + stockColumn3Weight + stockColumn4Weight),
                                        TextAlign.End
                                    ),
                                    if (isBusy()) {
                                        ReportColumn(
                                            vouchers?.D1?.absoluteValue?.formatToAmtDec()
                                                .toString(),
                                            stockColumn5Weight,
                                            TextAlign.End
                                        )
                                    } else {
                                        if (ledgerStockItemList.isEmpty()) {
                                            ReportColumn(
                                                totalCredit.formatToAmtDec(),
                                                stockColumn5Weight,
                                                TextAlign.End
                                            )
                                            ReportColumn(
                                                totalDebit.formatToAmtDec(),
                                                stockColumn5Weight,
                                                TextAlign.End
                                            )
                                        } else {
                                            ReportColumn(
                                                (totalDebit + totalCredit).absoluteValue.formatToAmtDec(),
                                                stockColumn5Weight,
                                                TextAlign.End
                                            )
                                        }
                                    },


                                    )
                            )
                        }
                    }
                }
            })
    }
}