package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun fourHeaderHtml(
    title: String,
    headers: Quadruple<String, String, String, String>,
    rows: List<Quadruple<String, String, String, String>>,
    total1: String? = "",
    total2: String? = "",
    startDate: String? = null,
    endDate: String? = null
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val html = StringBuilder()
    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            @page { size: A4; margin: 12mm; }
            body {
                font-family: Arial, Helvetica, sans-serif;
                font-size: 10pt;
                color: #000;
            }

            .main-container {
                border: 2px solid #000;
                border-radius: 8px;
                padding: 12px;
            }

            h1, h2, h3 {
                margin: 2px 0;
                text-align: center;
            }

            .header {
                text-align: center;
                line-height: 1.4;
            }

            .header .title {
                font-size: 14pt;
                font-weight: bold;
                margin: 5px 0;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 12px;
            }

            th, td {
                border: 1px solid #000;
                padding: 6px;
                font-size: 9.5pt;
            }

            th {
                background-color: #f2f2f2;
                text-align: center;
            }

            thead { display: table-header-group; }

            td.number { text-align: right; }
            td.name { text-align: left; }
            
            .center { text-align: center; }
            .bold { font-weight: bold; }
        </style>
        </head>
        <body>
            <div class="main-container">
            
            <!-- HEADER -->
            <div class="header">
                <div class="bold">GSTIN : ${compInfo?.T4.toString()}</div>
                <div class="title">$title Report</div>

                <h2>${CompanyName()}</h2>
                <div>${compInfo?.T3.toString()}</div>
                <div>Tel : ${SharedPrefs.User.get()?.Mobile}</div>
                <div>Email : ${SharedPrefs.User.get()?.Email}</div>
                <div class="center" style="margin-top: 5px;">
                    ${if (startDate != null || endDate != null) "<b>Period:</b> ${Tdate(startDate?:"")} ${if (startDate != null && endDate != null) "to" else ""} ${Tdate(endDate?:"")}" else ""}
                </div>
            </div>

            <table>
            <thead>
            <tr>
                <th>${headers.first}</th>
                <th>${headers.second}</th>
                <th>${headers.third}</th>
                <th>${headers.fourth}</th>
            </tr>
            </thead>
            <tbody>
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
        </tbody>
        <tr>
            <th class="name">Total</th>
            <th class="name"></th>
            <th class="number"></th>
            <th class="number">$total1</th>
        </tr>
        </table>
        
        <!-- FOOTER SIGNATURE -->
        <div style="margin-top: 30px; display: flex; justify-content: flex-end;">
            <div style="width:35%; text-align:center;">
                For <b>${CompanyName()}</b><br><br><br><br>
                Authorised Signatory
            </div>
        </div>
        
        </div>
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
