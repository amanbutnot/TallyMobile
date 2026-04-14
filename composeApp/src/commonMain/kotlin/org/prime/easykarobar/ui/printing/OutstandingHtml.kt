package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.ui.shared.globalShared.CompanyGst
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
    val html = StringBuilder()

    html.append(
        """
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; margin: 20px; }
            table { width: 100%; border-collapse: collapse; margin-top: 16px; }
            th, td { border: 1px solid #000; padding: 6px; text-align: center; }
            th { background-color: #f5f5f5; font-weight: bold; }
            td.number { text-align: right; }
            td.text { text-align: left; }
            h2, h3, h4 { text-align: center; margin: 4px; }
            .summary { margin-top: 20px; font-weight: bold; text-align: right; }
        </style>
        </head>
        <body>
            <h2>${CompanyName()}</h2>
            <h3>GSTIN : </h3>
            <h2>$title</h2>
            <h3>On Basis of : $onBasis</h3>
            <h4>From ${Tdate(startDate)} to ${Tdate(endDate)} &nbsp;&nbsp;&nbsp;&nbsp; Bills Status as on : ${Tdate(billStatusDate)}</h4>

            <table>
                <tr>
                    <th>Dated</th>
                    <th>Type</th>
                    <th>Party Name</th>
                    <th>Ref. No.</th>
                    <th>Ref. Amt.</th>
                    <th>Adjusted Amt.</th>
                    <th>Pending Amt.</th>
                    <th>Due Date</th>
                    <th>Due Days</th>
                </tr>
        """.trimIndent()
    )

    rows.forEach { row ->
        html.append(
            """
            <tr>
                <td>${Tdate(row.date)}</td>
                <td>${row.vchType}</td>
                <td>${row.name}</td>
                <td>${row.refNo}</td>
                <td class="number">${row.refAmount}</td>
                <td class="number">${row.adjustedAmount}</td>
                <td class="number">${row.pendingAmount}</td>
                <td>${if (row.dueDate.isNotEmpty()) Tdate(row.dueDate) else ""}</td>
                <td>${row.dueDays}</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
            <tr>
                <th colspan="3">Grand Total</th>
                <th class="number">$totalRefAmt</th>
                <th class="number">${ totalPendingAmt}</th>
                <th colspan="3"></th>
            </tr>
            </table>
            <div class="summary">
                ( On Acc. : ${onAcc} ; Ledger Bal. : ${ledgerBal} $ledgerBalType )
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

    val html = StringBuilder()

    html.append(
        """
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; margin: 20px; }
            table { width: 100%; border-collapse: collapse; margin-top: 10px; margin-bottom: 20px; }
            th, td { border: 1px solid #000; padding: 6px; }
            th { background-color: #f5f5f5; font-weight: bold; }
            td.number { text-align: right; }
            td.text { text-align: left; }
            h2, h3, h4 { text-align: center; margin: 4px; }
            .party-title { text-align: left; font-weight: bold; margin-top: 20px; }
            .summary { margin-top: 20px; font-weight: bold; text-align: right; }
        </style>
        </head>
        <body>

            <h2>${CompanyName()}</h2>
            <h3>GSTIN : ${CompanyGst()}</h3>
            <h2>$title</h2>
            <h3>On Basis of : $onBasis</h3>
            <h4>
                From ${Tdate(startDate)} to ${Tdate(endDate)} 
                &nbsp;&nbsp;&nbsp;&nbsp; 
                Bills Status as on : ${Tdate(billStatusDate)}
            </h4>
        """.trimIndent()
    )

    // 🔥 PARTY-WISE LOOP
    parties.forEach { party ->

        html.append(
            """
            <div class="party-title">Account : ${party.partyName}</div>

            <table>
                <tr>
                    <th>Dated</th>
                    <th>Type</th>
                    <th>Ref. No.</th>
                    <th>Ref. Amt.</th>
                    <th>Adjusted Amt.</th>
                    <th>Pending Amt.</th>
                    <th>Due Date</th>
                    <th>Due Days</th>
                </tr>
            """.trimIndent()
        )

        party.rows.forEach { row ->
            html.append(
                """
                <tr>
                    <td>${Tdate(row.date)}</td>
                    <td>${row.vchType}</td>
                    <td>${row.refNo}</td>
                    <td class="number">${row.refAmount}</td>
                    <td class="number">${row.adjustedAmount}</td>
                    <td class="number">${row.pendingAmount}</td>
                    <td>${if (row.dueDate.isNotEmpty()) Tdate(row.dueDate) else ""}</td>
                    <td>${ DueDays(
                    endDate,
                    if (onBasis == "Due Date") row.dueDate else row.date
                ) + " Days"}</td>
                </tr>
                """.trimIndent()
            )
        }

        // ✅ Party Total
        html.append(
            """
                <tr>
                    <th colspan="3">Grand Total</th>
                    <th class="number">${party.totalRefAmt}</th>
                    <th class="number"></th>
                    <th class="number">${party.totalPendingAmt}</th>
                    <th colspan="2"></th>
                </tr>
            </table>
            """.trimIndent()
        )
    }

    // 🔥 Final Summary
    html.append(
        """
            <div class="summary">
                ( On Acc. : $onAcc ; Ledger Bal. : $ledgerBal $ledgerBalType )
            </div>

        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}