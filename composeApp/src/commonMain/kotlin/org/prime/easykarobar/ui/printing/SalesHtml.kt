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
    transportDetails: TransportDetails,
    showTax: Boolean = true
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val partyDetails = db.ledgerMasterQueries.selectByName(partyName).executeAsOneOrNull()
    println("party detail is $partyDetails and guid is $partyGuid and name is $partyName")
    val user = SharedPrefs.User.get()

    val isIgst = user?.State.clean() != partyDetails?.State.clean() &&
            partyDetails?.State.clean().isNotBlank() &&
            user?.State.clean().isNotBlank()

    val hasShipping = transportDetails.Saddress1.clean().isNotBlank() ||
            transportDetails.SpartyName.clean().isNotBlank()
    val shippedToName = if (hasShipping) transportDetails.SpartyName.clean().takeIf { it.isNotBlank() } ?: partyName.clean() else partyName.clean()
    val shippedToGstin = if (hasShipping)
        transportDetails.SgstIn.clean().takeIf { it.isNotBlank() } ?: (partyDetails?.GSTIN.clean())
    else
        partyDetails?.GSTIN.clean()
    val shippedToAddress = if (hasShipping) {
        listOfNotNull(
            transportDetails.Saddress1,
            transportDetails.Saddress2,
            transportDetails.Saddress3,
            transportDetails.Saddress4
        ).map { it.clean() }.filter { it.isNotBlank() }.joinToString("<br>")
    } else {
        listOfNotNull(
            partyDetails?.Address1,
            partyDetails?.Address2,
            partyDetails?.Address3,
            partyDetails?.Address4
        ).map { it.clean() }.filter { it.isNotBlank() }.joinToString("<br>")
    }

    val title = if (name == "Sale Invoice") "TAX INVOICE" else name.uppercase()
    val colSpan = if (showTax) (if (isIgst) 9 else 11) else 7

    val qtyStartWidth = if (showTax) "40%" else "66%"
    val labelWidth = if (showTax) "40%" else "14%"

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

    * {
        box-sizing: border-box;
    }

    html, body {
        margin: 0;
        padding: 0;
    }

    body {
        font-family: Arial, Helvetica, sans-serif;
        font-size: 8.5pt;
        color: #000;
    }

    /*
     * page-wrapper: just a border box, no fixed height.
     * The items-grow-section handles the stretching internally.
     */
    .page-wrapper {
        border: 1px solid #000;
        width: 100%;
        display: flex;
        flex-direction: column;
    }

    .border-b { border-bottom: 0.5pt solid #000; }
    .border-r { border-right: 0.5pt solid #000; }

    /* ── Header ─────────────────────────────────────────────────────── */
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

    /* ── Info / Billing ─────────────────────────────────────────────── */
    .info-section { display: flex; }
    .info-col { width: 50%; }
    .info-table { width: 100%; border-collapse: collapse; }
    .info-table td { padding: 1px 8px; font-size: 8.5pt; vertical-align: top; }
    .label-cell { width: 35%; }

    .billing-section { display: flex; min-height: 80px; }
    .billing-col { width: 50%; padding: 4px 8px; font-size: 8.5pt; line-height: 1.2; }

    /* ── Items grow section ─────────────────────────────────────────── */
    /*
     * No height/flex tricks on the table itself.
     * The filler row at the bottom gets a large min-height so it pushes
     * the footer down to fill the page — but only grows, never shrinks items.
     */
    .items-grow-section {
        border-bottom: 0.5pt solid #000;
    }

    table.items-table {
        width: 100%;
        border-collapse: collapse;
        /* NO height:100% — that was causing each row to expand */
    }
    table.items-table thead { height: 20px; }
    table.items-table th,
    table.items-table td {
        border-right: 0.5pt solid #000;
        border-bottom: 0.5pt solid #000;
        padding: 1px 4px;
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
    table.items-table td:last-child { border-right: none; }

    /* Filler row: large min-height fills remaining page space after items.
       If items already fill the page this row collapses to near-zero. */
    .filler-row td {
        min-height: 80mm;
        height: 80mm;
        border-bottom: none !important;
        border-right: none !important;
    }

    /* ── Summary (subtotal + sundries) ──────────────────────────────── */
    .summary-container {
        border-bottom: 0.5pt solid #000;
    }
    .summary-table {
        width: 100%;
        border-collapse: collapse;
    }
.summary-table .sum-spacer {
    border-right: 0.5pt solid #000;
    border-bottom: 0.5pt solid #000;
    padding: 2px 5px;
}
.summary-table .sum-label {
    white-space: nowrap;
    border-right: 0.5pt solid #000;
    border-bottom: 0.5pt solid #000;
    padding: 2px 6px;
    font-size: 8.5pt;
}
.summary-table .sum-amt {
    white-space: nowrap;
    text-align: right;
    border-bottom: 0.5pt solid #000;
    padding: 2px 6px;
    font-size: 8.5pt;
}
    .summary-table tr:last-child td { border-bottom: none; }

    /* ── Grand Total ────────────────────────────────────────────────── */
    .grand-total-row {
        display: flex;
        align-items: center;
        padding: 0;
        font-weight: bold;
        font-size: 9pt;
        border-bottom: 0.5pt solid #000;
    }
    .gt-label  { width: 45%; text-align: right; padding: 2px 8px; }
    .gt-qty    { width: 7%;  text-align: center; border-bottom: 1px solid #888; padding: 2px 0; }
    .gt-spacer { width: 35%; }
    .gt-amt    { width: 13%; text-align: right; padding: 2px 4px; }

    /* ── Tax Summary ────────────────────────────────────────────────── */
    .tax-summary-section {
        padding: 5px 8px;
        border-bottom: 0.5pt solid #000;
    }
    .tax-table { border-collapse: collapse; width: auto; }
    .tax-table th, .tax-table td {
        border: 0.5pt solid #000;
        padding: 1px 6px;
        font-size: 8pt;
    }

    /* ── Amount in Words ────────────────────────────────────────────── */
    .amount-in-words {
        padding: 4px 8px;
        font-weight: bold;
        font-size: 9pt;
        border-bottom: 0.5pt solid #000;
    }

    /* ── Footer ─────────────────────────────────────────────────────── */
    .footer-section {
        display: flex;
        height: 80px;
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
        overflow: hidden;
    }

    /* ── Utilities ──────────────────────────────────────────────────── */
    .right  { text-align: right; }
    .center { text-align: center; }
    .bold   { font-weight: bold; }
</style>
</head>
<body>
<div class="page-wrapper">

    <!-- GST / Copy line -->
    <div class="header-top border-b">
        <table style="width:100%; border-collapse:collapse; border:none; table-layout:fixed;">
            <tr>
                <td style="padding:0; border:none; text-align:left;">GST : ${compInfo?.T4.clean()}</td>
                <td style="padding:0; border:none; text-align:right;">Original Copy</td>
            </tr>
        </table>
    </div>

    <!-- Company header -->
    <div class="header-center border-b">
        <div class="title">$title</div>
        <div class="company-name">${CompanyName()}</div>
        <div class="company-info">
            ${compInfo?.T3.clean()}<br>
            Tel. : ${user?.Mobile.clean()} &nbsp; Email : ${user?.Email.clean()}
        </div>
    </div>

    <!-- Invoice / Transport info -->
    <div class="info-section border-b">
        <div class="info-col border-r">
            <table class="info-table">
                <tr><td class="label-cell">Invoice No.</td><td>: <b>${invoiceNo.clean()}</b></td></tr>
                <tr><td class="label-cell">Dated</td><td>: <b>${Tdate(date)}</b></td></tr>
                <tr><td class="label-cell">Place of Supply</td><td>: ${partyDetails?.State.clean()}</td></tr>
                <tr><td class="label-cell">Reverse Charge</td><td>: </td></tr>
                <tr><td class="label-cell">GR/RR No.</td><td>: ${transportDetails.gstRrNo.clean()}</td></tr>
            </table>
        </div>
        <div class="info-col">
            <table class="info-table">
                <tr><td class="label-cell">Transport</td><td>: ${transportDetails.transportName.clean()}</td></tr>
                <tr><td class="label-cell">Vehicle No.</td><td>: ${transportDetails.vehicleNo.clean()}</td></tr>
                <tr><td class="label-cell">Station</td><td>: ${transportDetails.station.clean()}</td></tr>
                <tr><td class="label-cell">E-Way Bill No.</td><td>: </td></tr>
            </table>
        </div>
    </div>

    <!-- Billing / Shipping -->
    <div class="billing-section border-b">
        <div class="billing-col border-r">
            <b>Billed to :</b><br>
            <b>${partyName.clean()}</b><br>
            ${
            listOfNotNull(
                partyDetails?.Address1,
                partyDetails?.Address2,
                partyDetails?.Address3,
                partyDetails?.Address4
            ).map { it.clean() }.filter { it.isNotBlank() }.joinToString("<br>")
        }<br>
            <b>GSTIN / UIN &nbsp;&nbsp;&nbsp; : ${partyDetails?.GSTIN.clean()}</b>
        </div>
        <div class="billing-col">
            <b>Shipped to :</b><br>
            <b>${shippedToName.clean()}</b><br>
            $shippedToAddress<br>
            <b>GSTIN / UIN &nbsp;&nbsp;&nbsp; : ${shippedToGstin.clean()}</b>
        </div>
    </div>

    <!-- Items table — grows to fill remaining page space -->
    <div class="items-grow-section">
        <table class="items-table">
            <thead>
                <tr>
                    <th style="width:3%">S.N.</th>
                    <th style="width:${if (showTax) 28 else 54}%">Description of Goods</th>
                    <th style="width:9%">HSN/SAC Code</th>
                    <th style="width:7%">Qty.</th>
                    <th style="width:5%">Unit</th>
                    <th style="width:9%">Price</th>
                    ${if (showTax) {
                        if (isIgst) """
                        <th style="width:10%">IGST Rate</th>
                        <th style="width:16%">IGST Amount</th>
                        """ else """
                        <th style="width:5%">CGST Rate</th>
                        <th style="width:8%">CGST Amount</th>
                        <th style="width:5%">SGST Rate</th>
                        <th style="width:8%">SGST Amount</th>
                        """
                    } else ""}
                    <th style="width:13%">Amount(₹)</th>
                </tr>
            </thead>
            <tbody>""".trimIndent()
    )

    // ── Item rows ────────────────────────────────────────────────────────────
    items.forEachIndexed { index, item ->
        val unitTaxable = if (item.qty != 0) item.taxable / item.qty.absoluteValue else 0.0

        val taxCells = if (showTax) {
            if (isIgst) {
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
        } else ""

        val serialList = item.item_serial.mapNotNull { it.SerialNo.clean().takeIf { s -> s.isNotBlank() } }
        val serials = if (serialList.isNotEmpty()) {
            "<br/><span style='font-size:7.5pt; color:#444;'>" +
                    serialList.joinToString() +
                    "</span>"
        } else ""

        html.append(
            """
            <tr>
                <td class="center">${index + 1}.</td>
                <td><b>${item.name.clean()}</b>$serials</td>
                <td class="center">${item.hsn.clean()}</td>
                <td class="center">${item.qty.absoluteValue}.00</td>
                <td class="center">${item.selectedUnit.clean()}</td>
                <td class="right">${unitTaxable.formatToAmtDec()}</td>
                $taxCells
                <td class="right"><b>${item.net.formatToAmtDec()}</b></td>
            </tr>""".trimIndent()
        )
    }

    // ── Single filler row — absorbs ALL remaining vertical space after items ──
    // height:100% works because the <table> itself has height:100% inside a
    // flex-grow:1 container.  The bottom border is suppressed so there is no
    // stray line at the bottom of the empty area.
    html.append(
        """
            <tr class="filler-row" style="height:100%;">
                <td colspan="$colSpan"></td>
            </tr>
            </tbody>
        </table>
    </div><!-- end items-grow-section -->""".trimIndent()
    )

    // ── Subtotal + Sundries ───────────────────────────────────────────────────
    val subtotal = items.sumOf { it.net }

    html.append(
        """
    <div class="summary-container">
        <table class="summary-table">
            <tr>
                <td class="sum-spacer" style="width:$qtyStartWidth; border-right:0.5pt solid #000;"></td>
                <td class="sum-amt bold" style="width:7%; text-align:center; border-right:0.5pt solid #000;">${items.sumOf { it.qty }.absoluteValue}.00</td>
                <td class="sum-label bold" style="width:$labelWidth; text-align:right;">Sub Total</td>
                <td class="sum-amt bold" style="width:13%">${subtotal.formatToAmtDec()}</td>
            </tr>""".trimIndent()
    )

    sundries.forEach { sun ->
        val label = if ((sun.i1 == 0 && sun.i2 == 0) || (sun.i1 == 0 && sun.i2 == 1)) "Less : ${sun.name}" else "Add : ${sun.name}"
        val amount = if (sun.i2 == 1) sun.percentValue else sun.amount
        html.append(
            """
            <tr>
                <td class="sum-spacer" colspan="2" style="border-right:0.5pt solid #000;"></td>
                <td class="sum-label" style="text-align:right;">$label</td>
                <td class="sum-amt">${amount.formatToAmtDec()}</td>
            </tr>""".trimIndent()
        )
    }

    html.append(
        """
        </table>
    </div><!-- end summary-container -->""".trimIndent()
    )

    // ── Grand Total ───────────────────────────────────────────────────────────
    html.append(
        """
    <div class="grand-total-row">
        <div class="gt-spacer" style="width:$qtyStartWidth;"></div>
        <div class="gt-qty" style="width:7%; text-align:center; border-bottom:1px solid #888; padding:2px 0;">${items.sumOf { it.qty }.absoluteValue}.00</div>
        <div class="gt-label" style="width:$labelWidth; text-align:right; padding:2px 8px;">Grand Total</div>
        <div class="gt-amt" style="width:13%; text-align:right; padding:2px 4px;">${grandTotal.formatToAmtDec()}</div>
    </div>""".trimIndent()
    )

    // ── Tax Summary ───────────────────────────────────────────────────────────
    html.append(
        """
    <div class="tax-summary-section" ${if (!showTax) "style='display:none;'" else ""}>
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
        val taxable  = groupItems.sumOf { it.taxable }
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
    </div><!-- end tax-summary-section -->""".trimIndent()
    )

    // ── Amount in Words ───────────────────────────────────────────────────────
    html.append(
        """
    <div class="amount-in-words">
        Total Qty : <b>${items.sumOf { it.qty }.absoluteValue}.00</b><br>
        Rupees ${numberToWords(grandTotal.toInt())} Only
    </div>""".trimIndent()
    )

    // ── Footer ────────────────────────────────────────────────────────────────
    html.append(
        """
    <div class="footer-section">
        <div class="terms border-r">
            <b>Terms &amp; Conditions</b><br>
            E.&amp; O.E.<br>
            Subject to '${user?.State ?: ""}' Jurisdiction only.
        </div>
        <div class="signature-section">
            <div style="font-size:8.5pt;">Receiver's Signature :</div>
            <div style="text-align:center;">
                For <b>${CompanyName()}</b><br><br>
                <b>Authorised Signatory</b>
            </div>
        </div>
    </div>

</div><!-- end page-wrapper -->
</body>
</html>""".trimIndent()
    )

    return html.toString()
}


// ─── Data class ──────────────────────────────────────────────────────────────

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


// ─── Number-to-words ─────────────────────────────────────────────────────────

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
        else   -> tens[n / 10] + if (n % 10 != 0) " ${ones[n % 10]}" else ""
    }

fun numberToWords(num: Int): String {
    if (num == 0) return "Zero"

    var n = num
    val result = StringBuilder()

    if (n >= 1_00_00_000) {
        result.append("${twoDigits(n / 1_00_00_000)} Crore ")
        n %= 1_00_00_000
    }

    if (n >= 1_00_000) {
        result.append("${twoDigits(n / 1_00_000)} Lakh ")
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

 fun String?.clean(): String = if (this == null || this.lowercase() == "null") "" else this
