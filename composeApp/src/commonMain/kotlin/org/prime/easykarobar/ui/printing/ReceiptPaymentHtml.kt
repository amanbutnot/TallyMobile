package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.ui.shared.globalShared.CompanyName

data class ReceiptPaymentRow(
    val sn: Int,
    val account: String,
    val debit: Double?,
    val credit: Double?
)

fun receiptPaymentHtml(
    voucherNo: String,
    date: String,
    rows: List<ReceiptPaymentRow>,
    totalDebit: Double,
    totalCredit: Double,
    companyAddress: String,
    companyContact: String,
    documentType: String
): String {
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
                font-family: Arial, sans-serif;
                font-size: 10pt;
                color: #000;
            }

            h2, h3 {
                text-align: center;
                margin: 4px 0;
                font-weight: normal;
            }

            .company-info {
                text-align: center;
                margin-bottom: 10px;
            }

            .invoice-info {
                margin-top: 10px;
                font-size: 10pt;
                display: flex;
                justify-content: space-between;
            }

            .invoice-info div {
                width: 48%;
            }

            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 10px;
            }

            th, td {
                border: 1px solid #000;
                padding: 5px;
                font-size: 9pt;
            }

            th {
                background-color: #f5f5f5;
                text-align: center;
            }

            td.center { text-align: center; }
            td.number { text-align: right; }

            .total-row {
                font-weight: bold;
                background-color: #f9f9f9;
            }

            .footer-note {
                font-size: 9pt;
                text-align: center;
                margin-top: 20px;
            }
        </style>
        </head>
        <body>
            <div class="company-info">
                <h2>${CompanyName()}</h2>
                <div>$companyAddress</div>
                <div>$companyContact</div>
                <h3>$documentType</h3>
            </div>

            <div class="invoice-info">
                <div><strong>Date:</strong> $date</div>
                <div style="text-align:right;"><strong>Voucher No:</strong> $voucherNo</div>
            </div>

            <table>
                <tr>
                    <th>S.N</th>
                    <th>Account</th>
                    <th>Debit (Rs.)</th>
                    <th>Credit (Rs.)</th>
                </tr>
        """.trimIndent()
    )

    rows.forEach { row ->
        val debitStr = if (row.debit != null && row.debit > 0) row.debit.formatToAmtDec() else ""
        val creditStr =
            if (row.credit != null && row.credit > 0) row.credit.formatToAmtDec() else ""

        html.append(
            """
            <tr>
                <td class="center">${row.sn}</td>
                <td>${row.account}</td>
                <td class="number">$debitStr</td>
                <td class="number">$creditStr</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
            <tr class="total-row">
                <th colspan="2" class="text">TOTAL</th>
                <th class="number">${totalDebit.formatToAmtDec()}</th>
                <th class="number">${totalCredit.formatToAmtDec()}</th>
            </tr>
            </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}
