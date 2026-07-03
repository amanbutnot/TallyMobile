package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import org.prime.easykarobar.ui.shared.reportsShared.DueDays

data class OutstandingRow(
    val name: String,
    val date: String,
    val vchType: String,
    val refNo: String,
    val refAmount: Double,
    val pendingAmount: Double,
    val dueDate: String,
    val dueDays: String,
    val adjustedAmount: String
)

private fun outstandingCss(): String = """
<style>
    @page {
        size: A4;
        margin: 0;
    }

    html, body {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
        font-size: 10pt;
        line-height: 1.2;
    }

    .page {
        width: 210mm;
        min-height: 297mm;
        padding: 10mm;
        box-sizing: border-box;
    }

    .main-container {
        width: 100%;
        min-height: 277mm;
        border: 2px solid #000;
        border-radius: 8px;
        padding: 12px;
        box-sizing: border-box;
    }

    h2, h3, h4 {
        text-align: center;
        margin: 2px 0;
    }

    .company-info {
        text-align: center;
        margin-bottom: 8px;
    }

    .top-table {
        width: 100%;
        border-collapse: collapse;
        margin: 0;
    }

    .top-table td {
        border: none;
        padding: 0;
        font-size: 8pt;
    }

    .report-meta {
        width: 100%;
        border-collapse: collapse;
        margin-top: 8px;
        margin-bottom: 6px;
    }

    .report-meta td {
        border: none;
        padding: 2px 0;
        font-size: 9pt;
        font-weight: bold;
    }

    .report-table {
        width: 100%;
        border-collapse: collapse;
        table-layout: fixed;
        margin-top: 6px;
    }

    .report-table th,
    .report-table td {
        border: 1px solid #000;
        padding: 3px 4px;
        font-size: 8pt;
        word-break: normal;
        overflow-wrap: normal;
    }

    .report-table th {
        background-color: #f5f5f5;
        font-weight: bold;
        text-align: center;
    }

    .number {
        text-align: right;
        white-space: nowrap;
    }

    .text {
        text-align: left;
    }

    .center {
        text-align: center;
    }

    .date {
        white-space: nowrap;
        text-align: center;
    }

    .summary {
        margin-top: 12px;
        font-weight: bold;
        text-align: right;
        font-size: 9pt;
    }

    thead {
        display: table-header-group;
    }
</style>
""".trimIndent()

fun outstandingHtml(
    title: String,
    accountName: String,
    onBasis: String,
    startDate: String,
    endDate: String,
    billStatusDate: String,
    rows: List<OutstandingRow>,
    totalRefAmt: String,
    totalPendingAmt: String,
    onAcc: Double,
    ledgerBal: String,
    ledgerBalType: String = "Dr"
): String {
    val user = SharedPrefs.User.get()
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val html = StringBuilder()

    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        ${outstandingCss()}
        </head>
        <body>
        <div class="page">
        <div class="main-container">

            <table class="top-table">
                <tr>
                    <td>GST : ${compInfo?.T4.clean()}</td>
                </tr>
            </table>

            <div class="company-info">
                <h2 style="font-weight:bold;">${CompanyName()}</h2>
                <div>
                    ${compInfo?.T3.clean()}<br>
                    Tel. : ${user?.Mobile.clean()} &nbsp; Email : ${user?.Email.clean()}
                </div>
                <h2 style="text-decoration-line: underline;">$title</h2>
                <h3>On Basis of : $onBasis</h3>
            </div>

            <table class="report-meta">
                <tr>
                    <td class="text">From ${Tdate(startDate)} to ${Tdate(endDate)}</td>
                    <td style="text-align:right;">Bills Status as on : ${Tdate(billStatusDate)}</td>
                </tr>
                <tr>
                    <td colspan="2" class="text">Account : $accountName</td>
                </tr>
            </table>

            <table class="report-table">
                <colgroup>
                    <col style="width:11%">
                    <col style="width:9%">
                    <col style="width:16%">
                    <col style="width:12%">
                    <col style="width:13%">
                    <col style="width:13%">
                    <col style="width:13%">
                    <col style="width:8%">
                    <col style="width:5%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Dated</th>
                        <th>Type</th>
                        <th>Party Name</th>
                        <th>Ref. No.</th>
                        <th>Ref. Amt.</th>
                        <th>Adjusted Amt.</th>
                        <th>Pending Amt.</th>
                        <th>Bill Date</th>
                        <th>Bill Days</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent()
    )

    rows.forEach { row ->
        html.append(
            """
                    <tr>
                        <td class="date">${Tdate(row.date)}</td>
                        <td class="center">${row.vchType}</td>
                        <td class="text">${row.name}</td>
                        <td class="text">${row.refNo}</td>
                        <td class="number">${row.refAmount.formatToAmtDec()}</td>
                        <td class="number">${row.adjustedAmount}</td>
                        <td class="number">${row.pendingAmount.formatToAmtDec()}</td>
                        <td class="date">${if (row.dueDate.isNotEmpty()) Tdate(row.dueDate) else ""}</td>
                        <td class="center">${row.dueDays}</td>
                    </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
                    <tr>
                        <th colspan="4" class="number">Grand Total</th>
                        <th class="number">$totalRefAmt</th>
                        <th></th>
                        <th class="number">$totalPendingAmt</th>
                        <th colspan="2"></th>
                    </tr>
                </tbody>
            </table>

            <div class="summary">
                ( On Acc. : ${onAcc.formatToAmtDec()} ; Ledger Bal. : $ledgerBal $ledgerBalType )
            </div>

        </div>
        </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}

data class PartyOutstanding(
    val partyName: String,
    val rows: List<OutstandingRow>,
    val totalRefAmt: String,
    val totalPendingAmt: String
)

fun partyWiseOutstanding(
    title: String,
    accountName: String,
    onBasis: String,
    startDate: String,
    endDate: String,
    billStatusDate: String,
    parties: List<PartyOutstanding>,
    onAcc: Double,
    ledgerBal: String,
    ledgerBalType: String = "Dr"
): String {
    val user = SharedPrefs.User.get()
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val html = StringBuilder()

    html.append(
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        ${outstandingCss()}
        <style>
            .party-title {
                text-align: left;
                font-weight: bold;
                margin-top: 12px;
                font-size: 9pt;
            }
        </style>
        </head>
        <body>
        <div class="page">
        <div class="main-container">

            <table class="top-table">
                <tr>
                    <td>GST : ${compInfo?.T4.clean()}</td>
                </tr>
            </table>

            <div class="company-info">
                <h2 style="font-weight:bold;">${CompanyName()}</h2>
                <div>
                    ${compInfo?.T3.clean()}<br>
                    Tel. : ${user?.Mobile.clean()} &nbsp; Email : ${user?.Email.clean()}
                </div>
                <h2 style="text-decoration-line: underline;">$title</h2>
                <h3>On Basis of : $onBasis</h3>
            </div>

            <table class="report-meta">
                <tr>
                    <td class="text">From ${Tdate(startDate)} to ${Tdate(endDate)}</td>
                    <td style="text-align:right;">Bills Status as on : ${Tdate(billStatusDate)}</td>
                </tr>
                <tr>
                    <td colspan="2" class="text">Account : $accountName</td>
                </tr>
            </table>
        """.trimIndent()
    )

    parties.forEach { party ->
//        <div class="party-title">Account : ${party.partyName}</div>
        html.append(
            """


            <table class="report-table">
                <colgroup>
                    <col style="width:11%">
                    <col style="width:10%">
                    <col style="width:15%">
                    <col style="width:15%">
                    <col style="width:15%">
                    <col style="width:15%">
                    <col style="width:10%">
                    <col style="width:9%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Dated</th>
                        <th>Type</th>
                        <th>Ref. No.</th>
                        <th>Ref. Amt.</th>
                        <th>Adjusted Amt.</th>
                        <th>Pending Amt.</th>
                        <th>$onBasis</th>
                        <th>${if (onBasis == "Bill Date") "Bill Days" else "Due Days"}</th>
                    </tr>
                </thead>
                <tbody>
            """.trimIndent()
        )

        party.rows.forEach { row ->
            html.append(
                """
                    <tr>
                        <td class="date">${Tdate(row.date)}</td>
                        <td class="center">${row.vchType}</td>
                        <td class="text">${row.refNo}</td>
                        <td class="number">${row.refAmount.formatToAmtDec()}</td>
                        <td class="number">${row.adjustedAmount}</td>
                        <td class="number">${row.pendingAmount.formatToAmtDec()}</td>
                        <td class="date">${if (row.dueDate.isNotEmpty()) Tdate(row.dueDate) else ""}</td>
                        <td class="center">${
                    DueDays(
                        endDate,
                        if (onBasis == "Due Date") row.dueDate else row.date
                    )
                } Days</td>
                    </tr>
                """.trimIndent()
            )
        }

        html.append(
            """
                    <tr>
                        <th colspan="3" class="number">Grand Total</th>
                        <th class="number">${party.totalRefAmt}</th>
                        <th></th>
                        <th class="number">${party.totalPendingAmt}</th>
                        <th colspan="2"></th>
                    </tr>
                </tbody>
            </table>
            """.trimIndent()
        )
    }

    html.append(
        """
            <div class="summary">
                ( On Acc. : ${onAcc.formatToAmtDec()} ; Ledger Bal. : $ledgerBal $ledgerBalType )
            </div>

        </div>
        </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}