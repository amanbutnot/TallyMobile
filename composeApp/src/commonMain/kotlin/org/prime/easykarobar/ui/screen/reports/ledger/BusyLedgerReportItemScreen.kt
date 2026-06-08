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
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.ui.printing.ReceiptPaymentRow
import org.prime.easykarobar.ui.printing.TransportDetails
import org.prime.easykarobar.ui.printing.receiptPaymentHtml
import org.prime.easykarobar.ui.printing.salesHtml
import org.prime.easykarobar.ui.screen.transactions.sale.InvoiceItem
import org.prime.easykarobar.ui.shared.composables.TallyCircularLoader
import org.prime.easykarobar.ui.shared.composables.TallyLoadingDialog
import org.prime.easykarobar.ui.shared.composables.TallyReportScaffold
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.reportsShared.PdfAction
import org.prime.easykarobar.ui.shared.reportsShared.ReportColumn
import org.prime.easykarobar.ui.shared.reportsShared.TableCell
import org.prime.easykarobar.ui.shared.reportsShared.TallyReportBottomBar
import org.prime.easykarobar.ui.shared.reportsShared.handlePdfAction
import kotlin.math.absoluteValue

data class BusyLedgerReportItemScreen(
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


        println("alksfklasdf "+ledgerStockItemList)
        println(vouchers)
        var isLoading by remember { mutableStateOf(false) }
        var shareLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isLoading = true
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }

        val stockColumn1Weight = 0.4f
        val stockColumn2Weight = 0.8f
        val stockColumn3Weight = 0.3f
        val stockColumnUnitWeight = 0.3f
        val stockColumn4Weight = 0.5f
        val stockColumn5Weight = 0.5f

        // ── Shared HTML builder ───────────────────────────────────────────────
        fun buildHtmlContent(): String {
            return if (ledgerStockItemList.isNotEmpty()) {
                val invoiceItems = ledgerStockItemList.map { it ->
                    InvoiceItem(
                        name = it.Item_Name ?: "",
                        price = it.Rate ?: 0.0,
                        listPrice = it.Rate ?: 0.0,
                        qty = (it.Qty ?: 0.0).toInt(),
                        discountPercentage = 0.0,
                        taxCategoryCode = 0,
                        gstPercentage = 0.0,
                        taxable = it.Amt ?: 0.0,
                        gstAmt = 0.0,
                        net = it.Amt ?: 0.0,
                        CD = "",hsn =it.hsn,
                        selectedUnit = it.Unit
                    )
                }

                val sundries = ledgerStockBusyItemList.mapIndexed { index, it ->
                    SundryItem(
                        name = it.CM1 ?: "",
                        amount = it.D3 ?: 0.0,
                        rate = it.D1 ?: 0.0,
                        percentValue = it.D3 ?: 0.0,
                        srno = index + 1,
                        guid = "",
                        i1 = 1,
                        i2 = 0,
                        d2 = 0
                    )
                }

                salesHtml(
                    name = vchType,
                    partyName = ledgerReportItemList.firstOrNull()?.LedgerName ?: "",
                    partyGuid = guid,
                    invoiceNo = vchNo,
                    date = date,
                    items = invoiceItems,
                    sundries = sundries,
                    grandTotal = vouchers?.D1?.absoluteValue
                        ?: ledgerStockItemList.sumOf { it.Amt ?: 0.0 },
                    transportDetails = TransportDetails(
                        transportName = "",
                        gstRrNo = "",
                        vehicleNo = "",
                        station = "",
                        pincode = "",
                        gstRrDate = ""
                    ),
                    showTax = false
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
                receiptPaymentHtml(
                    voucherNo = vchNo,
                    date = date,
                    rows = rows,
                    totalDebit = rows.sumOf { it.debit ?: 0.0 },
                    totalCredit = rows.sumOf { it.credit ?: 0.0 },
                    companyAddress = "",
                    companyContact = "",
                    documentType = vchType
                )
            }
        }

        if (shareLoading) TallyLoadingDialog("Generating Report")

        val htmlContent = remember(ledgerStockItemList, ledgerStockBusyItemList, ledgerReportItemList, vouchers) {
            buildHtmlContent()
        }

        // ── UI Layout ─────────────────────────────────────────────────────────
        TallyReportScaffold(
            title = "$vchType Entry Details",
            showBurgerMenu = true,
            onDownloadClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "$vchType Report",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareText = "Share VchWise",
            onShareSecondText = "Share Itemwise",
            onDownloadText = "Download Vchwise",
            onDownloadSecondText = "Download Itemwise",
            onDownloadSecondClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "$vchType Report",
                        htmlContent = htmlContent,
                        action = PdfAction.Download,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareSecondClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "$vchType Report",
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onShareClick = {
                scope.launch {
                    handlePdfAction(
                        fileName = "$vchType Report",
                        htmlContent = htmlContent,
                        action = PdfAction.Share,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
            onExcelClick = {
                scope.launch {
                    val excelRows = if (ledgerStockItemList.isNotEmpty()) {
                        ledgerStockItemList.mapIndexed { index, item ->
                            listOf(
                                (index + 1).toString(),
                                item.Item_Name ?: "",
                                item.Qty?.absoluteValue?.formatToQtyDec().toString(),
                                item.Unit ?: "",
                                item.Rate?.absoluteValue?.formatToAmtDec().toString(),
                                item.Amt?.absoluteValue?.formatToAmtDec().toString()
                            )
                        }
                    } else {
                        ledgerReportItemList.mapIndexed { index, item ->
                            listOf(
                                (index + 1).toString(),
                                item.LedgerName ?: "",
                                item.DebitAmt?.absoluteValue?.formatToAmtDec() ?: "0.0",
                                item.CreditAmt?.absoluteValue?.formatToAmtDec() ?: "0.0"
                            )
                        }
                    }

                    val headers = if (ledgerStockItemList.isNotEmpty()) {
                        listOf("S No.", "Item Name", "Qty", "Unit", "Rate", "Amount")
                    } else {
                        listOf("S No.", "Account", "Debit", "Credit")
                    }

                    handlePdfAction(
                        fileName = "${vchType}_${vchNo}",
                        htmlContent = htmlContent,
                        headers = headers,
                        rows = excelRows,
                        action = PdfAction.DownloadExcel,
                        onLoadingChange = { shareLoading = it }
                    )
                }
            },
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
                                    TableCell("Qty", stockColumn3Weight, textAlign = TextAlign.End, isHeader = true)
                                    TableCell("Unit", stockColumnUnitWeight, textAlign = TextAlign.End, isHeader = true)
                                    TableCell("Rate", stockColumn4Weight, textAlign = TextAlign.End, isHeader = true)
                                    TableCell("Amount", stockColumn5Weight, textAlign = TextAlign.End, isHeader = true)
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
                                                item.Qty?.absoluteValue?.formatToQtyDec().toString(),
                                                stockColumn3Weight,
                                                textAlign = TextAlign.End
                                            )
                                            TableCell(
                                                item.Unit ?: "",
                                                stockColumnUnitWeight,
                                                textAlign = TextAlign.End
                                            )
                                            TableCell(
                                                item.Rate?.absoluteValue?.formatToAmtDec().toString(),
                                                stockColumn4Weight,
                                                textAlign = TextAlign.End
                                            )
                                            TableCell(
                                                item.Amt?.absoluteValue?.formatToAmtDec().toString(),
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
                                        (stockColumn1Weight + stockColumn2Weight + stockColumn3Weight + stockColumnUnitWeight + stockColumn4Weight),
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
                                if (ledgerStockItemList.isNotEmpty()) "Bill Sundry" else "Ledger Details:",
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
                                    TableCell("Account", stockColumn2Weight + stockColumn3Weight, isHeader = true)
                                    if (ledgerStockItemList.isEmpty()) {
                                        TableCell("Credit", stockColumn5Weight, textAlign = TextAlign.End, isHeader = true)
                                        TableCell("Debit", stockColumn5Weight, textAlign = TextAlign.End, isHeader = true)
                                    } else {
                                        TableCell("%", stockColumn5Weight, textAlign = TextAlign.End, isHeader = true)
                                        TableCell("Amount", stockColumn5Weight, textAlign = TextAlign.End, isHeader = true)
                                    }
                                }
                            }

                            if (ledgerStockItemList.isNotEmpty()) {
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
                                                TableCell((index + 1).toString(), stockColumn1Weight)
                                                TableCell(item.CM1 ?: "", stockColumn2Weight + stockColumn3Weight)
                                                TableCell(
                                                    item.D1?.formatToAmtDec().toString(),
                                                    stockColumn5Weight,
                                                    textAlign = TextAlign.End,
                                                    isHeader = false
                                                )
                                                TableCell(
                                                    item.D3?.formatToAmtDec().toString(),
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
                                                TableCell((index + 1).toString(), stockColumn1Weight)
                                                TableCell(item.LedgerName ?: "", stockColumn2Weight + stockColumn3Weight)
                                                TableCell(
                                                    item.CreditAmt?.absoluteValue?.formatToAmtDec().toString(),
                                                    stockColumn5Weight,
                                                    textAlign = TextAlign.End
                                                )
                                                TableCell(
                                                    item.DebitAmt?.absoluteValue?.formatToAmtDec().toString(),
                                                    stockColumn5Weight,
                                                    textAlign = TextAlign.End
                                                )
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
                                    if (ledgerStockItemList.isNotEmpty()) {
                                        ReportColumn(
                                            vouchers?.D1?.absoluteValue?.formatToAmtDec().toString(),
                                            stockColumn5Weight,
                                            TextAlign.End
                                        )
                                    } else {
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
                                    }
                                )
                            )
                        }
                    }
                }
            })
    }
}