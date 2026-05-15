package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.transactions.sale.InvoiceItem
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import kotlin.math.absoluteValue


fun salesHtml(
    name: String,
    partyName: String,
    partyGuid: String,
    invoiceNo: String,
    date: String,
    items: List<InvoiceItem>,
    sundries: List<SundryItem>,
    grandTotal: Double,
    transportDetails: TransportDetails
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val partyDetails = db.ledgerMasterQueries.selectByGuid(partyGuid).executeAsOneOrNull()
    val user = SharedPrefs.User.get()

    val isIgst = user?.State != partyDetails?.State && partyDetails?.State?.isNotBlank() == true && user?.State?.isNotBlank() == true

    val hasShipping = transportDetails.Saddress1?.isNotBlank() == true || transportDetails.SpartyName?.isNotBlank() == true
    val shippedToName = if (hasShipping) transportDetails.SpartyName ?: partyName else partyName
    val shippedToGstin = if (hasShipping) transportDetails.SgstIn ?: (partyDetails?.GSTIN ?: "") else (partyDetails?.GSTIN ?: "")
    val shippedToAddress = if (hasShipping) {
        listOfNotNull(
            transportDetails.Saddress1,
            transportDetails.Saddress2,
            transportDetails.Saddress3,
            transportDetails.Saddress4
        ).filter { it.isNotBlank() }.joinToString("<br>")
    } else {
        listOfNotNull(
            partyDetails?.Address1,
            partyDetails?.Address2,
            partyDetails?.Address3,
            partyDetails?.Address4
        ).filter { it.isNotBlank() }.joinToString("<br>")
    }

    val title = if (name == "Sale Invoice") "TAX INVOICE" else name.uppercase()

    val html = StringBuilder()

    html.append(
        """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
    @page {
        size: A4;
        margin: 10mm;
    }
    body {
        font-family: Arial, Helvetica, sans-serif;
        font-size: 8.5pt;
        color: #000;
        margin: 0;
        padding: 0;
    }
    .main-container {
        border: 1px solid #000;
        width: 100%;
        display: flex;
        flex-direction: column;
        min-height: 275mm;
    }
    .border-b { border-bottom: 0.5pt solid #000; }
    .border-r { border-right: 0.5pt solid #000; }
    
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
    
    .info-section {
        display: flex;
    }
    .info-col {
        width: 50%;
    }
    .info-table {
        width: 100%;
        border-collapse: collapse;
    }
    .info-table td {
        padding: 1px 8px;
        font-size: 8.5pt;
        vertical-align: top;
    }
    .label-cell { width: 35%; }
    
    .billing-section {
        display: flex;
        min-height: 80px;
    }
    .billing-col {
        width: 50%;
        padding: 4px 8px;
        font-size: 8.5pt;
        line-height: 1.2;
    }
    
    .irn-section {
        padding: 2px 8px;
        font-size: 8pt;
        display: flex;
        justify-content: space-between;
    }
    
    .items-wrapper {
        flex-grow: 1;
        border-bottom: 0.5pt solid #000;
    }
    table.items-table {
        width: 100%;
        border-collapse: collapse;
        height: 100%;
    }
    table.items-table thead {
        height: 25px;
    }
    table.items-table th, table.items-table td {
        border-right: 0.5pt solid #000;
        border-bottom: 0.5pt solid #000;
        padding: 3px 4px;
        font-size: 8.5pt;
        vertical-align: top;
    }
    table.items-table th {
        font-weight: bold;
        text-align: center;
        background: #fff;
    }
    table.items-table th:last-child, table.items-table td:last-child {
        border-right: none;
    }
    .filler-row td {
        border-bottom: none !important;
    }
    
    .right { text-align: right; }
    .center { text-align: center; }
    .bold { font-weight: bold; }
    
    .summary-container {
        display: flex;
    }
    .summary-left {
        width: 78.5%;
        position: relative;
    }
    .summary-right {
        width: 21.5%;
    }
    .summary-right table {
        width: 100%;
        border-collapse: collapse;
    }
    .summary-right td {
        border-left: 0.5pt solid #000;
        border-bottom: 0.5pt solid #000;
        padding: 2px 5px;
        font-size: 8.5pt;
    }
    .summary-right tr:last-child td { border-bottom: none; }

    .grand-total-row {
        display: flex;
        align-items: center;
        padding: 0;
        font-weight: bold;
        font-size: 9pt;
    }
    .gt-label { width: 45%; text-align: right; padding: 2px 8px; }
    .gt-qty { width: 7%; text-align: center; border-bottom: 1px solid #888; padding: 2px 0; }
    .gt-spacer { width: 35%; }
    .gt-amt { width: 13%; text-align: right; padding: 2px 4px; }

    .tax-summary-section {
        padding: 5px 8px;
    }
    .tax-table {
        border-collapse: collapse;
        width: auto;
    }
    .tax-table th, .tax-table td {
        border: 0.5pt solid #000;
        padding: 1px 6px;
        font-size: 8pt;
    }
    
    .amount-in-words {
        padding: 4px 8px;
        font-weight: bold;
        font-size: 9pt;
    }
    .bank-details {
        padding: 4px 8px;
        font-size: 8.5pt;
    }
    .footer-section {
        display: flex;
        min-height: 100px;
    }
    .terms {
        width: 55%;
        padding: 4px 8px;
        font-size: 7.5pt;
        line-height: 1.1;
    }
    .signature-section {
        width: 45%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 4px 8px;
    }
</style>
</head>
<body>
<div class="main-container">
<div class="header-top border-b">
    <table style="width: 100%; border-collapse: collapse; border: none; table-layout: fixed;">
        <tr>
            <td style="padding: 0; border: none; text-align: left;">GST : ${compInfo?.T4 ?: ""}</td>
            <td style="padding: 0; border: none; text-align: right;">Original Copy</td>
        </tr>
    </table>
</div>
    
    <div class="header-center border-b">
        <div class="title">$title</div>
        <div class="company-name">${CompanyName()}</div>
        <div class="company-info">
            ${compInfo?.T3 ?: ""}<br>
            Tel. : ${user?.Mobile ?: ""} &nbsp; email : ${user?.Email ?: ""}
        </div>
    </div>

    <div class="info-section border-b">
        <div class="info-col border-r">
            <table class="info-table">
                <tr><td class="label-cell">Invoice No.</td><td>: <b>$invoiceNo</b></td></tr>
                <tr><td class="label-cell">Dated</td><td>: <b>${Tdate(date)}</b></td></tr>
                <tr><td class="label-cell">Place of Supply</td><td>: ${partyDetails?.State ?: ""}</td></tr>
                <tr><td class="label-cell">Reverse Charge</td><td>: </td></tr>
                <tr><td class="label-cell">GR/RR No.</td><td>: ${transportDetails.gstRrNo}</td></tr>
            </table>
        </div>
        <div class="info-col">
            <table class="info-table">
                <tr><td class="label-cell">Transport</td><td>: ${transportDetails.transportName}</td></tr>
                <tr><td class="label-cell">Vehicle No.</td><td>: ${transportDetails.vehicleNo}</td></tr>
                <tr><td class="label-cell">Station</td><td>: ${transportDetails.station}</td></tr>
                <tr><td class="label-cell">E-Way Bill No.</td><td>: </td></tr>
            </table>
        </div>
    </div>

    <div class="billing-section border-b">
        <div class="billing-col border-r">
            <b>Billed to :</b><br>
            <b>$partyName</b><br>
            ${
            listOfNotNull(
                partyDetails?.Address1,
                partyDetails?.Address2,
                partyDetails?.Address3,
                partyDetails?.Address4
            ).filter { it.isNotBlank() }.joinToString("<br>")
        }<br>
            <b>GSTIN / UIN &nbsp;&nbsp;&nbsp; : ${partyDetails?.GSTIN ?: ""}</b>
        </div>
        <div class="billing-col">
            <b>Shipped to :</b><br>
            <b>$shippedToName</b><br>
            $shippedToAddress<br>
            <b>GSTIN / UIN &nbsp;&nbsp;&nbsp; : $shippedToGstin</b>
        </div>
    </div>

    <div class="items-wrapper">
    <table class="items-table">
        <thead>
            <tr>
                <th style="width:3%">S.N.</th>
                <th style="width:33%">Description of Goods</th>
                <th style="width:9%">HSN/SAC Code</th>
                <th style="width:7%">Qty.</th>
                <th style="width:9%">Price</th>
                ${if (isIgst) """
                <th style="width:10%">IGST Rate</th>
                <th style="width:16%">IGST Amount</th>
                """ else """
                <th style="width:5%">CGST Rate</th>
                <th style="width:8%">CGST Amount</th>
                <th style="width:5%">SGST Rate</th>
                <th style="width:8%">SGST Amount</th>
                """}
                <th style="width:13%">Amount(₹)</th>
            </tr>
        </thead>
        <tbody style="vertical-align: top;">""".trimIndent()
    )

    items.forEachIndexed { index, item ->
        val unitTaxable = if (item.qty != 0) item.taxable / item.qty.absoluteValue else 0.0
        
        val taxCells = if (isIgst) {
            """
                <td class="right">${item.gstPercentage.formatToAmtDec()}%</td>
                <td class="right">${item.gstAmt.formatToAmtDec()}</td>
            """.trimIndent()
        } else {
            val cgstRate = item.gstPercentage / 2
            val sgstRate = item.gstPercentage / 2
            val cgstAmt = item.gstAmt / 2
            val sgstAmt = item.gstAmt / 2
            """
                <td class="right">${cgstRate.formatToAmtDec()}%</td>
                <td class="right">${cgstAmt.formatToAmtDec()}</td>
                <td class="right">${sgstRate.formatToAmtDec()}%</td>
                <td class="right">${sgstAmt.formatToAmtDec()}</td>
            """.trimIndent()
        }

        val serials = if (item.item_serial.isNotEmpty()) {
            "<br/><span style='font-size:7.5pt; color:#444;'>${item.item_serial.joinToString { it.SerialNo.toString() }}</span>"
        } else ""

        html.append(
            """
            <tr style="height: 1px;">
                <td class="center">${index + 1}.</td>
                <td><b>${item.name}</b>$serials</td>
                <td class="center"></td>
                <td class="center">${item.qty.absoluteValue}.00</td>
                <td class="right">${unitTaxable.formatToAmtDec()}</td>
                $taxCells
                <td class="right"><b>${item.net.formatToAmtDec()}</b></td>
            </tr>""".trimIndent()
        )
    }

    // Add filler row that takes up remaining space
    html.append(
        """
            <tr class="filler-row">
                <td></td><td></td><td></td><td></td><td></td>
                ${if (isIgst) "<td></td><td></td>" else "<td></td><td></td><td></td><td></td>"}
                <td></td>
            </tr>
        </tbody>
    </table>
    </div>

    <div class="summary-container border-b">
        <div class="summary-left"></div>
        <div class="summary-right">
            <table>""".trimIndent()
    )

    // Add subtotal before sundries
    val subtotal = items.sumOf { it.net }
    html.append(
        """
        <tr>
            <td class="right bold" style="border-bottom: 0.5pt solid #000;">${subtotal.formatToAmtDec()}</td>
        </tr>
        """.trimIndent()
    )

    sundries.forEach { sun ->
        val label = if ((sun.i1 == 0 && sun.i2 == 0) || (sun.i1 == 0 && sun.i2 == 1)) "Less : " else "Add : "
        val amount = if (sun.i2 == 1) sun.percentValue else sun.amount
        html.append(
            """
                <tr>
                    <td class="right" style="font-size: 8pt;">$label ${sun.name} <span style="float:right;">${amount.formatToAmtDec()}</span></td>
                </tr>""".trimIndent()
        )
    }

    html.append(
        """
            </table>
        </div>
    </div>

    <div class="grand-total-row border-b">
        <div class="gt-label">Grand Total</div>
        <div class="gt-qty">${items.sumOf { it.qty }.absoluteValue}.00</div>
        <div class="gt-spacer"></div>
        <div class="gt-amt">${grandTotal.formatToAmtDec()}</div>
    </div>

    <div class="tax-summary-section border-b">
        <table class="tax-table">
            <thead>
                <tr>
                    <th>Tax Rate</th>
                    <th>Taxable Amt.</th>
                    ${if (isIgst) "<th>IGST Amt.</th>" else "<th>CGST Amt.</th><th>SGST Amt.</th>"}
                    <th>Total Tax</th>
                </tr>
            </thead>
            <tbody>""".trimIndent()
    )

    val taxGroups = items.groupBy { it.gstPercentage }
    taxGroups.forEach { (rate, groupItems) ->
        val taxable = groupItems.sumOf { it.taxable }
        val gstTotal = groupItems.sumOf { it.gstAmt }
        
        val taxSumCells = if (isIgst) {
            """<td class="right">${gstTotal.formatToAmtDec()}</td>"""
        } else {
            """
                <td class="right">${(gstTotal / 2).formatToAmtDec()}</td>
                <td class="right">${(gstTotal / 2).formatToAmtDec()}</td>
            """.trimIndent()
        }

        html.append(
            """
                <tr>
                    <td class="center">${rate.formatToAmtDec()}%</td>
                    <td class="right">${taxable.formatToAmtDec()}</td>
                    $taxSumCells
                    <td class="right">${gstTotal.formatToAmtDec()}</td>
                </tr>""".trimIndent()
        )
    }

    html.append(
        """
            </tbody>
        </table>
    </div>

    <div class="amount-in-words border-b">
        Rupees ${numberToWords(grandTotal.toInt())} Only
    </div>


    <div class="footer-section">
        <div class="terms border-r">
            <b>Terms & Conditions</b><br>
            E.& O.E.<br>
            Subject to '${user?.State ?: ""}' Jurisdiction only.
        </div>
        <div class="signature-section">
            <div style="font-size: 8.5pt;">Receiver's Signature :</div><br><br><br>
            <div style="text-align: center;">
                For <b>${CompanyName()}</b><br><br><br>
                <b>Authorised Signatory</b>
            </div>
        </div>
    </div>
</div>
</body>
</html>""".trimIndent()
    )

    return html.toString()
}

data class TransportDetails(
    val transportName: String,
    val gstRrNo: String,
    val vehicleNo: String,
    val station: String,
    val pincode: String,
    val gstRrDate: String,
    val SpartyName: String? = null,
    val Saddress1: String? = null,
    val Saddress2: String? = null,
    val Saddress3: String? = null,
    val Saddress4: String? = null,
    val SshipState: String? = null,
    val SgstIn: String? = null
)

private val ones = arrayOf(
    "", "One", "Two", "Three", "Four", "Five",
    "Six", "Seven", "Eight", "Nine", "Ten",
    "Eleven", "Twelve", "Thirteen", "Fourteen",
    "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
)

private val tens = arrayOf(
    "", "", "Twenty", "Thirty", "Forty",
    "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
)

private fun twoDigits(n: Int): String =
    when {
        n < 20 -> ones[n]
        else -> tens[n / 10] + if (n % 10 != 0) " ${ones[n % 10]}" else ""
    }

fun numberToWords(num: Int): String {
    if (num == 0) return "Zero"

    var n = num
    val result = StringBuilder()

    if (n >= 1_00_00_000) {
        result.append("${twoDigits(n / 1_00_00_000)} Crore ")
        n %= 1_00_00_000
    }

    if (n >= 1_00_00_000) {
        result.append("${twoDigits(n / 1_00_00_000)} Crore ")
        n %= 1_00_00_000
    }

    if (n >= 1_00_000) {
        result.append("${twoDigits(n / 1_00_00_000)} Lakh ")
        n %= 1_00_000
    }

    if (n >= 1000) {
        result.append("${twoDigits(n / 1000)} Thousand ")
        n %= 1000
    }

    if (n >= 100) {
        result.append("${ones[n / 100]} Hundred ")
        n %= 100
    }

    if (n > 0) {
        result.append(twoDigits(n))
    }

    return result.toString().trim()
}
