package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.transactions.BillByBillModel
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun entryTypesHtml(
    voucherNo: String,
    date: String,
    data: EntryTypesHtml,
    title: String,
): String {
    val html = StringBuilder()
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries
        .getCompanyInformation()
        .executeAsOneOrNull()

    // Receipt: Ledger = Credit, Settlement = Debit
    // Payment: Ledger = Debit, Settlement = Credit
    val ledgerDebit = when (title) {
        "Payment" -> data.amount.formatToAmtDec()
        else -> ""
    }

    val ledgerCredit = when (title) {
        "Receipt" -> data.amount.formatToAmtDec()
        else -> ""
    }

    val settlementDebit = when (title) {
        "Receipt" -> data.amount.formatToAmtDec()
        else -> ""
    }

    val settlementCredit = when (title) {
        "Payment" -> data.amount.formatToAmtDec()
        else -> ""
    }

    val billsHtml = if (data.bills.isNotEmpty()) {
        val rows = data.bills.joinToString("") { bill ->
            """
            <tr>
                <td>${bill.cm2 ?: ""}</td>
                <td>${bill.billNumber ?: ""}</td>
                <td class="number">${bill.d1?.formatToAmtDec() ?: ""}</td>
            </tr>
            """
        }
        """
        <div style="margin-top: 20px;">
            <strong>Bill wise Details:</strong>
            <table style="margin-top: 5px;">
                <tr>
                    <th>Ref Type</th>
                    <th>Ref Number</th>
                    <th>Amount</th>
                </tr>
                $rows
            </table>
        </div>
        """
    } else ""

    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                @page {
                    size: A4;
                    margin: 12mm;
                }

                body {
                    font-family: Arial, sans-serif;
                    font-size: 10pt;
                    color: #000;
                }

                .page {
                    border: 2px solid #000;
                    padding: 12px;
                }

                .header {
                    text-align: center;
                    margin-bottom: 12px;
                }

                .header .bold {
                    font-weight: bold;
                }

                .header .title {
                    font-size: 14pt;
                    font-weight: bold;
                    margin: 6px 0;
                }

                h2 {
                    margin: 4px 0;
                    font-weight: normal;
                }

                .invoice-info {
                    margin-top: 10px;
                    font-size: 10pt;
                    display: flex;
                    justify-content: space-between;
                }

                .invoice-info div {
                    width: 48%;
                }

                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 10px;
                }

                th, td {
                    border: 1px solid #000;
                    padding: 5px;
                    font-size: 9pt;
                }

                th {
                    background-color: #f5f5f5;
                    text-align: center;
                }

                td.center {
                    text-align: right;
                }

                td.number {
                    text-align: right;
                }
               

                .total-row {
                    font-weight: bold;
                    background-color: #f9f9f9;
                }
            </style>
        </head>

        <body>
        <div class="page">

            <!-- HEADER -->
            <div class="header">
                <div class="bold">GSTIN : ${compInfo?.T4 ?: ""}</div>
                <div class="title">$title</div>

                <h2>${CompanyName()}</h2>
                <div>${compInfo?.T3 ?: ""}</div>
                <div>Tel : ${SharedPrefs.User.get()?.Mobile ?: ""}</div>
                <div>Email : ${SharedPrefs.User.get()?.Email ?: ""}</div>
            </div>

            <!-- VOUCHER INFO -->
            <div class="invoice-info">
                <div><strong>Date:</strong> ${Tdate(date)}</div>
                <div style="text-align:right;">
                    <strong>Voucher No:</strong> $voucherNo
                </div>
            </div>

            <!-- TABLE -->
            <table>
                <tr>
                    <th>S.N</th>
                    <th>Account</th>
                    <th>Debit (Rs.)</th>
                    <th>Credit (Rs.)</th>
                </tr>

                <tr>
                    <td class="center">1</td>
                    <td>${data.ledger}</td>
                    <td class="number">$ledgerDebit</td>
                    <td class="number">$ledgerCredit</td>
                </tr>

                <tr>
                    <td class="center">2</td>
                    <td>${data.settlement}</td>
                    <td class="number">$settlementDebit</td>
                    <td class="number">$settlementCredit</td>
                </tr>

                <tr class="total-row">
                    <th colspan="2">TOTAL</th>
                    <th class="number">${data.amount.formatToAmtDec()}</th>
                    <th class="number">${data.amount.formatToAmtDec()}</th>
                </tr>
            </table>

            $billsHtml

        </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}

data class EntryTypesHtml(
    val ledger: String,
    val settlement: String,
    val amount: Double,
    val bills: List<BillByBillModel> = emptyList()
)
