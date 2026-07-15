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

    val compInfo = db.companyInformationQueries
        .getCompanyInformation()
        .executeAsOneOrNull()

    html.append(
        """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">

            <meta
                name="viewport"
                content="width=device-width, initial-scale=1.0"
            >

            <style>
                @page {
                    size: A4;
                    margin: 10mm;
                }

                * {
                    box-sizing: border-box;
                }

                html,
                body {
                    margin: 0;
                    padding: 0;
                    width: 100%;

                    font-family: Arial, sans-serif;
                    font-size: 10pt;
                    line-height: 1.2;

                    color: #000;
                    background: #fff;

                    -webkit-print-color-adjust: exact;
                    print-color-adjust: exact;
                }

                /*
                 * Do not give this element height: 297mm.
                 *
                 * A fixed height traps all content inside a single A4-sized
                 * element and prevents normal document pagination.
                 */
                .page {
                    width: 100%;
                    margin: 0;
                    padding: 0;
                }

                /*
                 * A single outer border cannot reliably be repeated on every
                 * automatically generated page by Android WebView.
                 *
                 * The table itself keeps its borders and paginates correctly.
                 */
                .main-container {
                    width: 100%;
                    margin: 0;
                    padding: 0;
                }

                h1,
                h2,
                h3,
                h4 {
                    margin: 0;
                    padding: 0;
                    text-align: center;
                }

                .document-header {
                    width: 100%;
                    margin-bottom: 8px;
                }

                .gst-number {
                    margin: 0 0 5px 0;
                    padding: 0;
                    font-size: 9pt;
                    font-weight: bold;
                    text-align: left;
                }

                .company-info {
                    width: 100%;
                    margin: 0 0 8px 0;
                    text-align: center;
                }

                .company-name {
                    margin: 0 0 3px 0;
                    font-size: 16pt;
                    font-weight: bold;
                }

                .company-address {
                    margin: 0;
                    font-size: 9pt;
                    line-height: 1.25;
                }

                .report-title {
                    margin: 8px 0 5px 0;
                    font-size: 14pt;
                    font-weight: normal;
                    text-decoration: underline;
                }

                .ledger-info-table {
                    width: 100%;
                    margin: 6px 0 0 0;
                    border: none;
                    border-collapse: collapse;
                    table-layout: fixed;
                }

                .ledger-info-table tr {
                    break-inside: avoid;
                    page-break-inside: avoid;
                }

                .ledger-info-table td {
                    padding: 1px 2px;
                    border: none;
                    font-size: 9pt;
                    vertical-align: top;
                }

                .ledger-info-table .label {
                    width: 55px;
                    font-weight: bold;
                }

                .ledger-info-table .colon {
                    width: 8px;
                    text-align: center;
                }

                .opening-balance {
                    width: 100%;
                    margin: 4px 0 5px 0;
                    font-size: 9pt;
                    font-weight: bold;
                    text-align: right;
                    white-space: nowrap;
                }

                .ledger-table {
                    width: 100%;
                    margin: 0;
                    border-collapse: collapse;
                    table-layout: fixed;
                }

                /*
                 * This asks the print engine to repeat the column headings
                 * whenever the table continues onto another page.
                 */
                .ledger-table thead {
                    display: table-header-group;
                }

                .ledger-table tbody {
                    display: table-row-group;
                }

                /*
                 * The totals should remain at the end of the table.
                 * Do not use table-footer-group because that can repeat the
                 * totals on every page in some print engines.
                 */
                .ledger-table tfoot {
                    display: table-row-group;
                }

                .ledger-table tr {
                    break-inside: avoid;
                    page-break-inside: avoid;
                    -webkit-region-break-inside: avoid;
                }

                .ledger-table th,
                .ledger-table td {
                    padding: 3px 4px;
                    border: 1px solid #000;
                    vertical-align: middle;
                    overflow-wrap: anywhere;
                    word-wrap: break-word;
                }

                .ledger-table th {
                    background: #f5f5f5;
                    font-size: 8.5pt;
                    font-weight: bold;
                    text-align: center;
                }

                .ledger-table td {
                    font-size: 8.5pt;
                }

                .text {
                    text-align: left;
                }

                .center {
                    text-align: center;
                }

                .number {
                    text-align: right;
                    white-space: nowrap;
                }

                .total-label {
                    text-align: right;
                    font-weight: bold;
                }

                .totals-row th {
                    background: #f5f5f5;
                }

                @media print {
                    html,
                    body {
                        width: auto;
                        height: auto;
                        overflow: visible;
                    }

                    .page,
                    .main-container {
                        width: 100%;
                        height: auto;
                        min-height: 0;
                        max-height: none;
                        overflow: visible;
                    }

                    .ledger-table {
                        page-break-before: auto;
                        page-break-after: auto;
                    }

                    .ledger-table thead {
                        display: table-header-group;
                    }

                    .ledger-table tr {
                        break-inside: avoid;
                        page-break-inside: avoid;
                    }
                }
            </style>
        </head>

        <body>
            <div class="page">
                <div class="main-container">

                    <header class="document-header">
                        <div class="gst-number">
                            GST : ${compInfo?.T4.clean()}
                        </div>

                        <div class="company-info">
                            <h2 class="company-name">
                                ${CompanyName()}
                            </h2>

                            <div class="company-address">
                                ${compInfo?.T3.clean()}
                                <br>

                                Tel. : ${user?.Mobile.clean()}
                                &nbsp;&nbsp;
                                Email : ${user?.Email.clean()}
                            </div>

                            <h2 class="report-title">
                                Account Ledger
                            </h2>
                        </div>

                        <table class="ledger-info-table">
                            <tbody>
                                <tr>
                                    <td class="label">Account</td>
                                    <td class="colon">:</td>
                                    <td>${accountName.escapeHtml()}</td>
                                </tr>

                                <tr>
                                    <td class="label">Period</td>
                                    <td class="colon">:</td>
                                    <td>
                                        ${Tdate(startDate)} to ${Tdate(endDate)}
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="opening-balance">
                            Opening Bal. = Rs.
                            ${openingBalance.formatToAmtDec()}
                            ${openingBalanceType.escapeHtml()}
                        </div>
                    </header>

                    <table class="ledger-table">
                        <colgroup>
                            <col style="width: 11%;">
                            <col style="width: 10%;">
                            <col style="width: 14%;">
                            <col style="width: 28%;">
                            <col style="width: 12%;">
                            <col style="width: 12%;">
                            <col style="width: 13%;">
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
        val debit = if (row.debit > 0.0) {
            row.debit.formatToAmtDec()
        } else {
            ""
        }

        val credit = if (row.credit > 0.0) {
            row.credit.formatToAmtDec()
        } else {
            ""
        }

        val balance = buildString {
            append(row.balance.formatToAmtDec())

            if (row.balanceType.isNotBlank()) {
                append(" ")
                append(row.balanceType)
            }
        }

        html.append(
            """
                            <tr>
                                <td class="text">
                                    ${Tdate(row.date)}
                                </td>

                                <td class="center">
                                    ${row.type.escapeHtml()}
                                </td>

                                <td class="text">
                                    ${row.vchBillNo.escapeHtml()}
                                </td>

                                <td class="text">
                                    ${row.account.escapeHtml()}
                                </td>

                                <td class="number">
                                    $debit
                                </td>

                                <td class="number">
                                    $credit
                                </td>

                                <td class="number">
                                    ${balance.escapeHtml()}
                                </td>
                            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
                        </tbody>

                        <tfoot>
                            <tr class="totals-row">
                                <th
                                    colspan="4"
                                    class="total-label"
                                >
                                    Grand Total
                                </th>

                                <th class="number">
                                    ${totalDebit.formatToAmtDec()}
                                </th>

                                <th class="number">
                                    ${totalCredit.formatToAmtDec()}
                                </th>

                                <th></th>
                            </tr>

                            <tr class="totals-row">
                                <th
                                    colspan="6"
                                    class="total-label"
                                >
                                    Closing Balance
                                </th>

                                <th class="number">
                                    ${closingBalance.formatToAmtDec()}
                                    ${closingBalanceType.escapeHtml()}
                                </th>
                            </tr>
                        </tfoot>
                    </table>

                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}



private fun String.escapeHtml(): String {
    return buildString(length) {
        this@escapeHtml.forEach { character ->
            when (character) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(character)
            }
        }
    }
}