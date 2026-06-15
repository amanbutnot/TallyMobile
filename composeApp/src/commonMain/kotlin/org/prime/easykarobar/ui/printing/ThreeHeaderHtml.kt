package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun threeHeaderHtml(
    title: String,
    headers: Triple<String, String, String>,
    rows: List<Triple<String, String, String>>,
    totalDebit: Double,
    totalCredit: Double,
    date: String
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
            h2,h1,h3 { text-align: center; margin-bottom: 0; }
        </style>
        </head>
        <body>
        <h1>$title</h1>
                ${
            if (date.isNotBlank()) {
                "<h3>As on -> ${ Tdate(date) }</h3>"
            } else ""
        }
        <h3>${CompanyName()}</h3>
        <table>
        <tr>
            <th>${headers.first}</th>
            <th>${headers.second}</th>
            <th>${headers.third}</th>
        </tr>
    """.trimIndent()
    )

    rows.forEach { (col1, col2, col3) ->
        html.append(
            """
            <tr>
                <td class="name">$col1</td>
                <td class="number">${col2.ifBlank { "-" }}</td>
                <td class="number">${col3.ifBlank { "-" }}</td>
            </tr>
        """.trimIndent()
        )
    }

    // Footer totals
    html.append(
        """
        <tr>
            <th class="name">Total</th>
            <th class="number">$totalDebit</th>
            <th class="number">$totalCredit</th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
