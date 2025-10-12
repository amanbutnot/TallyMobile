package org.prime.tally.ui.printing

import org.tally.TrialBalanceList
import kotlin.math.absoluteValue

fun generateTrialBalanceHtml(
    list: List<TrialBalanceList>,
    totalDebit: Double,
    totalCredit: Double
): String {
    val html = StringBuilder()
    html.append(
        """
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; }
            table { width: 100%; border-collapse: collapse; }
            th, td { border: 1px solid #000; padding: 4px; }
            th { background-color: #f2f2f2; }
            td.number { text-align: right; }
            h2 { text-align: center; }
            td.name { text-align: left; }
        </style>
        </head>
        <body>
        <h2>Trial Balance</h2>
        <table>
        <tr>
            <th class="name">Account Name</th>
            <th class="number">Debit</th>
            <th class="number">Credit</th>
        </tr>
    """.trimIndent()
    )

    list.forEach { item ->
        val debit = if ((item.ClsnBal ?: 0.0) < 0.0) (item.ClsnBal ?: 0.0).absoluteValue else 0.0
        val credit = if ((item.ClsnBal ?: 0.0) > 0.0) (item.ClsnBal ?: 0.0).absoluteValue else 0.0

        html.append(
            """
            <tr>
                <td>${item.CM1 ?: ""}</td>
                <td class="number">${if (debit > 0) debit else "-"}</td>
                <td class="number">${if (credit > 0) credit else "-"}</td>
            </tr>
        """.trimIndent()
        )
    }

    // Footer totals
    html.append(
        """
        <tr>
            <th class="number">Total</th>
            <th class="number">$totalDebit</th>
            <th class="number">$totalCredit</th>
        </tr>
    """.trimIndent()
    )

    html.append("</table></body></html>")

    return html.toString()
}
