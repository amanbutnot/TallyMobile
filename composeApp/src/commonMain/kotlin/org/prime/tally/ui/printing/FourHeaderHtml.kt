package org.prime.tally.ui.printing

import org.prime.tally.ui.shared.globalShared.CompanyName
import org.prime.tally.ui.shared.globalShared.Tdate

fun fourHeaderHtml(
    title: String,
    headers: Quadruple<String, String, String, String>,
    rows: List<Quadruple<String, String, String, String>>,
    total1: String?="", total2: String? = "", startDate: String?=null, endDate: String?=null
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
            h2 { text-align: center; margin-bottom: 0; }
            h3,h1 { text-align: center; margin-bottom: 0; }
        </style>
        </head>
        <body>
        <h1>$title Report</h1>
        <h3>${startDate ?: ""} ${endDate ?: ""}</h3>
        <h3>${CompanyName()}</h3>
        <table>
        <tr>
            <th>${headers.first}</th>
            <th>${headers.second}</th>
            <th>${headers.third}</th>
            <th>${headers.fourth}</th>
        </tr>
    """.trimIndent()
    )

    rows.forEach { (col1, col2, col3, col4) ->
        html.append(
            """
            <tr>
                <td class="name">$col1</td>
                <td class="name">${col2}</td>
                <td class="number">${col3}</td>
                <td class="number">${col4}</td>
            </tr>
        """.trimIndent()
        )
    }

    // Footer totals
    html.append(
        """
        <tr>
            <th class="name">Total</th>
            <th class="name"></th>
            <th class="name">$total1</th>
            <th class="name">$total2</th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}


data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
