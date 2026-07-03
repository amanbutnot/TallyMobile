package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun registerHtml(
    title: String,
    startDate: String,
    endDate: String,
    rows: List<Quadruple<String, String, String, String>>,
    totalAmount: String
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val user = SharedPrefs.User.get()

    val html = StringBuilder()

    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            @page {
                size: A4;
                margin: 0;
            }

            * {
                box-sizing: border-box;
            }

            html, body {
                margin: 0;
                padding: 0;
                font-family: Arial, Helvetica, sans-serif;
                font-size: 8.5pt;
                color: #000;
            }

            .page {
                width: 210mm;
                height: 297mm;
                padding: 10mm;
                box-sizing: border-box;
            }

            .page-wrapper {
                width: 100%;
                height: 277mm;
                border: 1px solid #000;
                display: flex;
                flex-direction: column;
            }

            .border-b {
                border-bottom: 0.5pt solid #000;
            }

            .header-top {
                padding: 2px 8px;
                font-size: 8.5pt;
            }

            .header-center {
                text-align: center;
                padding: 2px 5px 4px 5px;
            }

            .title {
                font-weight: bold;
                text-decoration: underline;
                font-size: 9.5pt;
            }

            .company-name {
                font-size: 16pt;
                font-weight: bold;
                margin: 0;
            }

            .company-info {
                font-size: 8.5pt;
                line-height: 1.2;
            }

            .period-line {
                text-align: left;
                font-size: 8.5pt;
                font-weight: bold;
                padding: 3px 0 0 0;
            }

            .items-grow-section {
                flex-grow: 1;
                border-bottom: 0.5pt solid #000;
            }

            table.items-table {
                width: 100%;
                border-collapse: collapse;
                table-layout: fixed;
            }

            table.items-table th,
            table.items-table td {
                border-right: 0.5pt solid #000;
                border-bottom: 0.5pt solid #000;
                padding: 2px 4px;
                font-size: 8pt;
                vertical-align: top;
                line-height: 1.1;
            }

            table.items-table th {
                font-weight: bold;
                text-align: center;
                background: #fff;
            }

            table.items-table th:last-child,
            table.items-table td:last-child {
                border-right: none;
            }

            .right {
                text-align: right;
            }

            .center {
                text-align: center;
            }

            .bold {
                font-weight: bold;
            }
        </style>
        </head>

        <body>
        <div class="page">
        <div class="page-wrapper">

            <div class="header-top border-b">
                GST : ${compInfo?.T4 ?: ""}
            </div>

            <div class="header-center border-b">
                <div class="title">${title.uppercase()}</div>
                <div class="company-name">${CompanyName()}</div>
                <div class="company-info">
                    ${compInfo?.T3 ?: ""}<br>
                    Tel. : ${user?.Mobile ?: ""} &nbsp; email : ${user?.Email ?: ""}
                </div>

                <div class="period-line">
                    Period : ${Tdate(startDate)} to ${Tdate(endDate)}
                </div>
            </div>

            <div class="items-grow-section">
                <table class="items-table">
                    <thead>
                        <tr>
                            <th style="width:12%">Date</th>
                            <th style="width:50%">Account</th>
                            <th style="width:13%">Vch No.</th>
                            <th style="width:25%">Amount(₹)</th>
                        </tr>
                    </thead>
                    <tbody>
        """.trimIndent()
    )

    rows.forEach { row ->
        html.append(
            """
                        <tr>
                            <td class="center">${row.first}</td>
                            <td class="bold">${row.second}</td>
                            <td class="center">${row.third}</td>
                            <td class="right bold">${row.fourth}</td>
                        </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
                        <tr>
                            <th colspan="3" class="right">Grand Total</th>
                            <th class="right">$totalAmount</th>
                        </tr>
                    </tbody>
                </table>
            </div>

        </div>
        </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}