package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.globalShared.CompanyGst
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun itemLedgerHtml(
    itemName: String,
    startDate: String,
    endDate: String,
    openingBalance: Double,
    openingAmount: Double,
    rows: List<LedgerRow>,
    totalInward: Double,
    totalOutward: Double,
    closingBalance: Double,
    closingAmount: Double,
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
            @page { size: A4; margin: 10mm; } 
            html, body {
                width: 210mm;
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
            <h3>GSTIN : ${CompanyGst()}</h3>
            <h2>Item Ledger</h2>
            <h3>Item: $itemName</h3> 
            <h3>From ${Tdate(startDate)} to ${Tdate(endDate)}</h3>

            <div class="opening-balance">
                Opening Qty: ${openingBalance.formatToAmtDec()} | Opening Amt: ${openingAmount.formatToAmtDec()}
            </div>

            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Vch No.</th>
                        <th>Particulars</th>
                        <th>Qty</th>
                        <th>Amount</th>
                        <th>Balance</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent()
    )

    rows.forEach { row ->
        val qtyStr = if (row.debit != 0.0) row.debit.formatToAmtDec() else ""
        val amountStr = if (row.credit != 0.0) row.credit.formatToAmtDec() else ""
        val balanceStr = row.balance.formatToAmtDec()

        html.append(
            """
            <tr>
                <td class="text">${Tdate(row.date)}</td>
                <td class="center">${row.type}</td>
                <td class="text">${row.vchBillNo}</td>
                <td class="text">${row.account}</td>
                <td class="number">$qtyStr</td>
                <td class="number">$amountStr</td>
                <td class="number">$balanceStr</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="4" class="text">Total</th>
                        <th class="number">${totalInward.formatToAmtDec()}</th>
                        <th class="number">${totalOutward.formatToAmtDec()}</th>
                        <th></th>
                    </tr>
                </tfoot>
            </table>

            <div class="closing-balance">
                Closing Qty: ${closingBalance.formatToAmtDec()} | Closing Amt: ${closingAmount.formatToAmtDec()}
            </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
