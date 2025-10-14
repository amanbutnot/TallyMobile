package org.prime.tally.ui.printing

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
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; margin: 20px; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; }
            th, td { border: 1px solid #000; padding: 6px; }
            th { background-color: #f5f5f5; text-align: center; }
            td.number { text-align: right; }
            td.text { text-align: left; }
            td.center { text-align: center; }
            h2, h3, h4 { text-align: center; margin: 4px; }
            .opening-balance { margin: 8px 0; font-weight: bold; }
            .closing-balance { margin-top: 16px; font-weight: bold; text-align: right; }
        </style>
        </head>
        <body>
            <h2>${CompanyName()}</h2>
            <h3>GSTIN : </h3>
            <h2>Account Ledger</h2>
            <h3>Account : $accountName &nbsp;&nbsp;&nbsp;&nbsp; From ${Tdate(startDate)} to ${Tdate(endDate)}</h3>
            
            <div class="opening-balance">Opening Bal. = Rs. ${openingBalance} $openingBalanceType</div>

            <table>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Vch/Bill No</th>
                    <th>Account</th>
                    <th>Debit(Rs.)</th>
                    <th>Credit(Rs.)</th>
                    <th>Balance(Rs.)</th>
                </tr>
        """.trimIndent()
    )

    rows.forEach { row ->
        val debitStr = if (row.debit > 0)row.debit else ""
        val creditStr = if (row.credit > 0) row.credit else ""
        val balanceStr = "${row.balance} ${row.balanceType}"

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
                <th class="number">${totalDebit}</th>
                <th class="number">${totalCredit}</th>
                <th></th>
            </tr>
            </table>
            <div class="closing-balance">
                Closing Bal. = Rs. ${closingBalance} $closingBalanceType
            </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}