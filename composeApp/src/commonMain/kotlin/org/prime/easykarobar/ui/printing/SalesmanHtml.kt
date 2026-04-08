package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.expect.formatToQtyDec
import org.prime.easykarobar.ui.screen.reports.salesman.SalesmanData
import org.prime.easykarobar.ui.shared.globalShared.CompanyName

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

    val hasGroup = rows.any { it.groupName != null }

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
            th.number { text-align: right; }
        </style>
        </head>
        <body>
        <h1>$title</h1>
        <h3>${CompanyName()}</h3>

        <table>
            <tr>
                <th>Salesman</th>
                ${if (hasGroup) "<th>Group</th>" else ""}
                <th>Target Qty</th>
                <th>Achieved Qty</th>
                <th>Balance Qty</th>
                <th>Target Amt</th>
                <th>Achieved Amt</th>
                <th>Balance Amt</th>
            </tr>
        """.trimIndent()
    )

    // Rows
    rows.forEach {
        html.append(
            """
            <tr>
                <td class="name">${it.name}</td>
                ${if (hasGroup) "<td class=\"name\">${it.groupName ?: ""}</td>" else ""}
                <td class="number">${it.targetQty.formatToQtyDec()}</td>
                <td class="number">${it.achievedQty.formatToQtyDec()}</td>
                <td class="number">${it.balanceQty.formatToQtyDec()}</td>
                <td class="number">${it.targetAmt.formatToAmtDec()}</td>
                <td class="number">${it.achievedAmt.formatToAmtDec()}</td>
                <td class="number">${it.balanceAmt.formatToAmtDec()}</td>
            </tr>
            """.trimIndent()
        )
    }

    // Totals (aligned properly — no shifting bugs)
    html.append(
        """
        <tr>
            <th>Total</th>
            ${if (hasGroup) "<th></th>" else ""}
            <th class="number">${totalTargetQty.formatToQtyDec()}</th>
            <th class="number">${totalAchQty.formatToQtyDec()}</th>
            <th class="number">${totalBalQty.formatToQtyDec()}</th>
            <th class="number">${totalTargetAmt.formatToAmtDec()}</th>
            <th class="number">${totalAchAmt.formatToAmtDec()}</th>
            <th class="number">${totalBalAmt.formatToAmtDec()}</th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}