package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import kotlin.math.absoluteValue

data class InvoiceItem(
    val sn: Int,
    val itemName: String,
    val qty: Double,
    val rate: Double,
    val amount: Double
)

data class InvoiceParticular(
    val name: String,
    val amount: Double
)

fun salesInvoiceHtml(
    voucherNo: String,
    date: String,
    partyName: String,
    items: List<InvoiceItem>,
    particulars: List<InvoiceParticular>,
    totalQty: Double,
    totalAmount: Double,
    grandTotal: Double,
    companyAddress: String,
    companyContact: String,
    documentType: String = "SALES DOCUMENT"
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

            .main-container {
                border: 2px solid #000;
                border-radius: 8px;
                padding: 12px;
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

            .summary {
                margin-top: 10px;
                font-weight: bold;
                text-align: right;
            }

            .particulars {
                margin-top: 12px;
                width: 100%;
            }

            .particulars td:first-child { text-align: left; }
            .particulars td:last-child { text-align: right; }

            .grand-total {
                font-weight: bold;
                font-size: 11pt;
                text-align: right;
                margin-top: 10px;
            }

            .footer {
                margin-top: 20px;
                font-size: 9pt;
                display: flex;
                justify-content: space-between;
            }

            .flex {
                display: flex;
                justify-content: space-between;
            }

            .bold { font-weight: bold; }
        </style>
        </head>
        <body>
            <div class="main-container">
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

            <div style="margin-top:4px;"><strong>Party:</strong> $partyName</div>

            <table>
                <tr>
                    <th>S.N</th>
                    <th>Item Name</th>
                    <th>Qty</th>
                    <th>Rate (Rs.)</th>
                    <th>Amount (Rs.)</th>
                </tr>
        """.trimIndent()
    )

    items.forEach { item ->
        html.append(
            """
            <tr>
                <td class="center">${item.sn}</td>
                <td>${item.itemName}</td>
                <td class="number">${item.qty.absoluteValue.formatToAmtDec()}</td>
                <td class="number">${item.rate.formatToAmtDec()}</td>
                <td class="number">${item.amount.formatToAmtDec()}</td>
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
            <tr>
                <th colspan="2" class="text">TOTAL</th>
                <th class="number">${totalQty.absoluteValue.formatToAmtDec()}</th>
                <th></th>
                <th class="number">${totalAmount.formatToAmtDec()}</th>
            </tr>
            </table>
        """.trimIndent()
    )

//    // Particulars table (Discount, GST, etc.)
//    html.append(
//        """
//            <table class="particulars">
//                <tr>
//                    <th>Particulars</th>
//                    <th>Amount (Rs.)</th>
//                </tr>
//        """.trimIndent()
//    )
//
//    particulars.forEach { p ->
//        html.append(
//            """
//            <tr>
//                <td>${p.name}</td>
//                <td class="number">${p.amount.formatToAmtDec()}</td>
//            </tr>
//            """.trimIndent()
//        )
//    }

    html.append(
        """
            </table>
            <div class="grand-total">
                Grand Total = Rs. ${grandTotal.formatToAmtDec()}
            </div>

            <div class="footer">
                <div style="width:60%">
                    <b>Terms & Conditions</b><br>
                    E.& O.E.<br>
                    1. Goods once sold will not be taken back.<br>
                    2. Interest @ 18% p.a. will be charged if payment is delayed.<br>
                    3. Subject to '${SharedPrefs.User.get()?.State}' Jurisdiction only.
                </div>

                <div style="width:35%; text-align:center;">
                    For <b>${CompanyName()}</b><br><br><br>
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