package org.prime.tally.ui.printing

import org.prime.tally.ui.shared.globalShared.CompanyName
import org.tally.GetProductStockList

fun productReportHtml(
    title: String = "Product",
    rows: List<GetProductStockList>,
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
            h1 { text-align: center; margin-bottom: 0; }
            h3 { text-align: center; margin-bottom: 0; }
        </style>
        </head>
        <body>
        <h1>$title Report</h1>
        <h3>${CompanyName()}</h3>
        <table>
        <tr>
            <th>Product Name</th>
            <th>Location</th>
            <th>Main Qty</th>
            <th>Alt Qty</th>
            <th>C1</th>
            <th>C2</th>
            <th>C3</th>
        </tr>
        """.trimIndent()
    )

    rows.forEach { row ->
        html.append(
            """
            <tr>
                <td class="name">${row.ProductName}</td>
                <td class="name">${row.GodownName}</td>
                <td class="number">${row.Value1}</td>
                <td class="number">${row.Value2}</td>
                <td class="number">${row.C1}</td>
                <td class="number">${row.C2}</td>
                <td class="number">${row.C3}</td>
            </tr>
            """.trimIndent()
        )
    }

    // Footer with total rows
    html.append(
        """
        <tr>
            <th class="name">Total Rows: ${rows.size}</th>
            <th class="name"></th>
            <th class="number"></th>
            <th class="number"></th>
            <th class="number"></th>
            <th class="number"></th>
            <th class="number"></th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}

