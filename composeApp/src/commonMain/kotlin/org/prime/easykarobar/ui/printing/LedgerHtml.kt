package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

data class LedgerRow(
    val date: String,
    val type: String,
    val vchBillNo: String,
    val account: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val balanceType: String
)

fun accountLedgerHtml(
    accountName: String,
    startDate: String,
    endDate: String,
    openingBalance: Double,
    openingBalanceType: String,
    rows: List<LedgerRow>,
    totalDebit: Double,
    totalCredit: Double,
    closingBalance: Double,
    closingBalanceType: String
): String {
    val html = StringBuilder()
    val user = SharedPrefs.User.get()
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()

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

html, body {
    margin: 0;
    padding: 0;
    font-family: Arial, sans-serif;
    font-size: 10pt;
    line-height: 1.2;
}

.page {
    width: 210mm;
    height: 297mm;
    padding: 10mm;
    box-sizing: border-box;
}

.main-container {
    width: 100%;
    height: 100%;
    border: 2px solid #000;
    border-radius: 8px;
    padding: 12px;
    box-sizing: border-box;
}
            h2, h3, h4 {
                text-align: center;
                margin: 2px 0;
                font-weight: normal;
            }

            .company-info {
                text-align: center;
                margin-bottom: 10px;
            }

            .ledger-info-table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 10px;
            }

            .ledger-info-table td {
                border: none;
                padding: 1px 2px;
                font-size: 10pt;
            }

            .ledger-info-table .label {
                width: 55px;
            }

            .ledger-info-table .colon {
                width: 8px;
                text-align: center;
            }

            .opening-balance {
                font-weight: bold;
                text-align: right;
                font-size: 9pt;
                margin: 4px 0;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 8px;
                table-layout: fixed;
            }

            th, td {
                border: 1px solid #000;
                padding: 3px 4px;
                word-break: break-word;
            }

            th {
                background-color: #f5f5f5;
                text-align: center;
                font-size: 9pt;
                font-weight: bold;
            }

            thead {
                display: table-header-group;
            }

            td {
                font-size: 9pt;
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
        </style>
        </head>

<body>
<div class="page">
<div class="main-container">

            <div class="header-top">
                <table style="width:100%; border-collapse:collapse; border:none; table-layout:fixed; margin-top:0;">
                    <tr>
                        <td style="padding:0; border:none; text-align:left;">
                            GST : ${compInfo?.T4.clean()}
                        </td>
                    </tr>
                </table>
            </div>

            <div class="company-info">
                <h2 style="margin-top: 8px; font-weight:bold;">${CompanyName()}</h2>

                <div class="company-info">
                    ${compInfo?.T3.clean()}<br>
                    Tel. : ${user?.Mobile.clean()} &nbsp; Email : ${user?.Email.clean()}
                </div>

                <h2 style="margin-top: 8px; text-decoration-line: underline;">
                    Account Ledger
                </h2>
            </div>

            <table class="ledger-info-table">
                <tr>
                    <td class="label"><strong>Account</strong></td>
                    <td class="colon">:</td>
                    <td>${accountName}</td>
                </tr>
                <tr>
                    <td class="label"><strong>Period</strong></td>
                    <td class="colon">:</td>
                    <td>${Tdate(startDate)} to ${Tdate(endDate)}</td>
                </tr>
            </table>

            <div class="opening-balance">
                Opening Bal. = Rs. ${openingBalance.formatToAmtDec()} $openingBalanceType
            </div>

            <table>
                <colgroup>
                    <col style="width:11%">
                    <col style="width:10%">
                    <col style="width:14%">
                    <col style="width:28%">
                    <col style="width:12%">
                    <col style="width:12%">
                    <col style="width:13%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Type</th>
                        <th>Vch/Bill No</th>
                        <th>Account</th>
                        <th>Debit (Rs.)</th>
                        <th>Credit (Rs.)</th>
                        <th>Balance (Rs.)</th>
                    </tr>
                </thead>
                <tbody>
        """.trimIndent()
    )

    rows.forEach { row ->
        val debitStr = if (row.debit > 0) row.debit.formatToAmtDec() else ""
        val creditStr = if (row.credit > 0) row.credit.formatToAmtDec() else ""
        val balanceStr = row.balance.formatToAmtDec() + " ${row.balanceType}"

        html.append(
            """
                    <tr>
                        <td class="text">${Tdate(row.date)}</td>
                        <td class="center">${row.type}</td>
                        <td class="text">${row.vchBillNo}</td>
                        <td class="text">${row.account}</td>
                        <td class="number">$debitStr</td>
                        <td class="number">$creditStr</td>
                        <td class="number">$balanceStr</td>
                    </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
                </tbody>
                <tr>
                    <th colspan="4" class="number">Grand Total</th>
                    <th class="number">${totalDebit.formatToAmtDec()}</th>
                    <th class="number">${totalCredit.formatToAmtDec()}</th>
                    <th></th>
                </tr>
                <tr>
                    <th colspan="6" class="number">Closing Balance</th>
                    <th class="number">${closingBalance.formatToAmtDec()} $closingBalanceType</th>
                </tr>
            </table>

        </div>
        </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}