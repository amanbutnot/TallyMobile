package org.prime.tally.ui.printing

import org.prime.tally.data.expect.formatToAmtDec
import org.prime.tally.ui.shared.globalShared.CompanyName
import org.prime.tally.ui.shared.globalShared.Tdate

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
                height: 297mm;
                margin: 0;
                padding: 0;
                font-family: Arial, sans-serif;
                font-size: 10pt;
                line-height: 1.2;
            }

            h2, h3, h4 {
                text-align: center;
                margin: 2px 0;
                font-weight: normal;
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

            td {
                font-size: 9pt;
            }

            td.number { text-align: right; }
            td.text { text-align: left; }
            td.center { text-align: center; }

            .opening-balance, .closing-balance {
                font-weight: bold;
                text-align: right;
                font-size: 9pt;
                margin: 4px 0;
            }

            .closing-balance { margin-top: 8px; }
        </style>
        </head>
        <body>
            <h2>${CompanyName()}</h2>
            <h3>GSTIN :</h3>
            <h2>Account Ledger</h2>
            <h3>Account: $accountName</h3> 
            <h3>From ${Tdate(startDate)} to ${Tdate(endDate)}</h3>

            <div class="opening-balance">
                Opening Bal. = Rs. ${openingBalance.formatToAmtDec()} $openingBalanceType
            </div>

            <table>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Vch/Bill No</th>
                    <th>Account</th>
                    <th>Debit (Rs.)</th>
                    <th>Credit (Rs.)</th>
                    <th>Balance (Rs.)</th>
                </tr>
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
                <td class="number">$debitStr</td>
                <td class="number">$creditStr</td>
                <td class="number">$balanceStr</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
            <tr>
                <th colspan="4" class="text">Grand Total</th>
                <th class="number">${totalDebit.formatToAmtDec()}</th>
                <th class="number">${totalCredit.formatToAmtDec()}</th>
                <th></th>
            </tr>
            </table>

            <div class="closing-balance">
                Closing Bal. = Rs. ${closingBalance.formatToAmtDec()} $closingBalanceType
            </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
