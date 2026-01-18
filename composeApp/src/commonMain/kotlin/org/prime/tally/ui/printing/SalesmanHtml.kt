package org.prime.tally.ui.printing

import org.prime.tally.ui.screen.reports.salesman.SalesmanData
import org.prime.tally.ui.shared.globalShared.CompanyName
import org.prime.tally.ui.shared.globalShared.Tdate

fun salesmanReportHtml(
    title: String,
    rows: List<SalesmanData>,
    totalTargetQty: Double,
    totalAchQty: Double,
    totalBalQty: Double,
    totalTargetAmt: Double,
    totalAchAmt: Double,
    totalBalAmt: Double,

): String {
    val html = StringBuilder()

    html.append(
        """
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; margin: 16px; }
            table { width: 100%; border-collapse: collapse; margin-top: 12px; }
            th, td { border: 1px solid #000; padding: 6px; }
            th { background-color: #f5f5f5; text-align: center; }
            td.number { text-align: right; }
            td.name { text-align: left; }
            h1, h3 { text-align: center; margin-bottom: 4px; }
            th.number {text-align: right}
        </style>
        </head>
        <body>
        <h1>$title</h1>
        <h3>${CompanyName()}</h3>

        <table>
            <tr>
                <th>Salesman</th>
                <th>Target Qty</th>
                <th>Achieved Qty</th>
                <th>Balance Qty</th>
                <th>Target Amt</th>
                <th>Achieved Amt</th>
                <th>Balance Amt</th>
            </tr>
        """.trimIndent()
    )

    rows.forEach {
        html.append(
            """
            <tr>
                <td class="name">${it.name}</td>
                <td class="number">${it.targetQty}</td>
                <td class="number">${it.achievedQty}</td>
                <td class="number">${it.balanceQty}</td>
                <td class="number">${it.targetAmt}</td>
                <td class="number">${it.achievedAmt}</td>
                <td class="number">${it.balanceAmt}</td>
            </tr>
            """.trimIndent()
        )
    }

    // Footer totals
    html.append(
        """
        <tr>
            <th>Total</th>
            <th class="number">$totalTargetQty</th>
            <th class="number">$totalAchQty</th>
            <th class="number">$totalBalQty</th>
            <th class="number">$totalTargetAmt</th>
            <th class="number">$totalAchAmt</th>
            <th class="number">$totalBalAmt</th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
