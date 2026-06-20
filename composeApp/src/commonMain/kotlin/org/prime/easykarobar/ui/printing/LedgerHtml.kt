package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.globalShared.CompanyGst
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

data class LedgerRow(
    val date: String,
    val type: String,
    val vchBillNo: String,
    val account: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val balanceType: String
)

fun accountLedgerHtml(
    accountName: String,
    startDate: String,
    endDate: String,
    openingBalance: Double,
    openingBalanceType: String,
    rows: List<LedgerRow>,
    totalDebit: Double,
    totalCredit: Double,
    closingBalance: Double,
    closingBalanceType: String
): String {
    val html = StringBuilder()

    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            /* A4 full page setup */
            @page { size: A4; margin: 10mm; } /* use small margins for more usable space */
            html, body {
                width: 210mm;
                margin: 0;
                padding: 0;
                font-family: Arial, sans-serif;
                font-size: 10pt;
                line-height: 1.2;
            }

            .main-container {
                border: 2px solid #000;
                border-radius: 8px;
                padding: 12px;
                min-height: 270mm;
            }

            h2, h3, h4 {
                text-align: center;
                margin: 2px 0;
                font-weight: normal;
            }

            .company-info {
                text-align: center;
                margin-bottom: 10px;
            }

            .ledger-info {
                margin-top: 10px;
                font-size: 10pt;
                display: flex;
                justify-content: space-between;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 8px;
            }

            th, td {
                border: 1px solid #000;
                padding: 3px 4px;
            }

            th {
                background-color: #f5f5f5;
                text-align: center;
                font-size: 9pt;
            }

            thead {
                display: table-header-group;
            }

            td {
                font-size: 9pt;
            }

            .number { text-align: right; }
            .text { text-align: left; }
            .center { text-align: center; }

            .opening-balance {
                font-weight: bold;
                text-align: right;
                font-size: 9pt;
                margin: 4px 0;
            }
        </style>
        </head>
        <body>
            <div class="main-container">
            <div class="company-info">
                <h2>${CompanyName()}</h2>
                <div>GSTIN : ${CompanyGst()}</div>
                <h2 style="margin-top: 8px;">Account Ledger</h2>
            </div>

            <div class="ledger-info">
                <div><strong>Account:</strong> $accountName</div>
                <div style="text-align:right;"><strong>Period:</strong> ${Tdate(startDate)} to ${Tdate(endDate)}</div>
            </div>

            <div class="opening-balance">
                Opening Bal. = Rs. ${openingBalance.formatToAmtDec()} $openingBalanceType
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Vch/Bill No</th>
                        <th>Account</th>
                        <th>Debit (Rs.)</th>
                        <th>Credit (Rs.)</th>
                        <th>Balance (Rs.)</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent()
    )

    rows.forEach { row ->
        val debitStr = if (row.debit > 0) row.debit.formatToAmtDec() else ""
        val creditStr = if (row.credit > 0) row.credit.formatToAmtDec() else ""
        val balanceStr = row.balance.formatToAmtDec() + " ${row.balanceType}"

        html.append(
            """
            <tr>
                <td class="text">${Tdate(row.date)}</td>
                <td class="center">${row.type}</td>
                <td class="text">${row.vchBillNo}</td>
                <td class="text">${row.account}</td>
                <td class="number">$creditStr</td>
                <td class="number">$debitStr</td>
                <td class="number">$balanceStr</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
            </tbody>
            <tr>
                <th colspan="4" class="number">Grand Total</th>
                <th class="number">${totalCredit.formatToAmtDec()}</th>
                <th class="number">${totalDebit.formatToAmtDec()}</th>
                <th></th>
            </tr>
            <tr>
                <th colspan="6" class="number">Closing Balance</th>
                <th class="number">${closingBalance.formatToAmtDec()} $closingBalanceType</th>
            </tr>
            </table>
            </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
