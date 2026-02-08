package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.transactions.sale.InvoiceItem
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate


fun salesHtml(
    name: String,
    partyName: String,
    invoiceNo: String,
    date: String,
    items: List<InvoiceItem>,
    sundries: List<SundryItem>,
    grandTotal: Double,transportDetails: TransportDetails
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val html = StringBuilder()

    val title = when (name) {
        "Sale Invoice" -> "Tax Invoice"
        "Sale Order" -> "Sale Order"
        "Sale Return" -> "Credit Note"
        "Purchase Invoice" -> "Purchase Invoice"
        "Purchase Order" -> "Purchase Order"
        "Purchase Return" -> "Debit Note"
        "Stock Transfer" -> "Stock Transfer"
        else -> ""
    }

    html.append(
        """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Tax Invoice</title>

<style>
@page {
    size: A4;
    margin: 12mm;
}

body {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
    color: #000;
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
}

.header .sub {
    font-size: 10pt;
}

.box {
    border: 1px solid #000;
    padding: 6px;
    margin-top: 6px;
}

.flex {
    display: flex;
    justify-content: space-between;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 8px;
}

th, td {
    border: 1px solid #000;
    padding: 5px;
    font-size: 9.5pt;
}

th {
    background: #f2f2f2;
    text-align: center;
}

.right { text-align: right; }
.center { text-align: center; }
.bold { font-weight: bold; }

.no-border td {
    border: none;
}

.amount-summary td {
    border-left: none;
    border-right: none;
}

.footer {
    margin-top: 12px;
    font-size: 9pt;
}

.signature {
    margin-top: 25px;
    display: flex;
    justify-content: space-between;
}

</style>
</head>

<body>

<!-- HEADER -->
<div class="header">
    <div class="bold">GSTIN : ${compInfo?.T4.toString()}</div>
    <div class="title">$title</div>

    <h2>${CompanyName()}</h2>
    <div>${compInfo?.T3.toString()}</div>
    <div>Pincode : 134003, Haryana</div>
    <div>Tel : ${SharedPrefs.User.get()?.Mobile}</div>
    <div>Email : ${SharedPrefs.User.get()?.Email}</div>
</div>

<!-- PARTY & INVOICE DETAILS -->
<div class="box flex">
    <div style="width:55%">
        <b>Party Details :</b><br>
        $partyName<br>
        C-186, GALI NO.7, BHAJAN PURA<br>
        North East Delhi, Delhi – 110053<br><br>
        <b>GSTIN / UIN :</b> 07ANZPS4239D1ZG
    </div>

    <div style="width:40%">
        <table class="no-border">
            <tr><td><b>Invoice No.</b></td><td>: $invoiceNo</td></tr>
            <tr><td><b>Dated</b></td><td>: ${Tdate(date)}</td></tr>
            <tr><td><b>Place of Supply</b></td><td>: Delhi (07)</td></tr>
            <tr><td><b>Reverse Charge</b></td><td>: N</td></tr>
            
            ${if(transportDetails.transportName!="") "<tr><td><b>Transport Name</b></td><td>: ${transportDetails.transportName}</td></tr>" else ""}
            ${if(transportDetails.station!="") "<tr><td><b>Station</b></td><td>: ${transportDetails.station}</td></tr>" else ""}
            ${if(transportDetails.gstRrNo!="") "<tr><td><b>GST/RR No.</b></td><td>: ${transportDetails.gstRrNo}</td></tr>" else ""}
            ${if(transportDetails.vehicleNo!="") "<tr><td><b>Vehicle No.</b></td><td>: ${transportDetails.vehicleNo}</td></tr>" else ""}
            ${if(transportDetails.pincode!="") "<tr><td><b>Pincode</b></td><td>: ${transportDetails.pincode}</td></tr>" else ""}
            ${if(transportDetails.gstRrDate!="") "<tr><td><b>GR/RR Date</b></td><td>: ${Tdate(transportDetails.gstRrDate)}</td></tr>" else ""}
        </table>
    </div>
</div>
            <!-- ITEM TABLE -->
             <table>
    <tr>
        <th>S.N.</th>
        <th>Description of Goods</th>
        <th>HSN / SAC</th>
        <th>Qty</th>
        <th>Rate (₹)</th>
        <th>Amount (₹)</th>
    </tr>""".trimIndent()
    )

    items.forEachIndexed { index, item ->
        html.append(
            """
    

    <tr>
        <td class="center">${index + 1}</td>
        <td>
            ${item.name}
        </td>
        <td class="center"></td>
        <td class="center">${item.qty}</td>
        <td class="right">${item.price.formatToAmtDec()}</td>
        <td class="right">${item.total.formatToAmtDec()}</td>
    </tr>

            """.trimIndent()
        )

    }
    html.append(

        """
            </table>
           <!-- TOTALS -->
<table class="amount-summary">
    <tr>
        <td style="width:70%"></td>
        <td class="right bold">${items.sumOf { it.total }.formatToAmtDec()}</td>
    </tr>""".trimIndent()
    )
    sundries.forEach { sun ->
        html.append(
            """
                 <tr>
        <td class="right bold">${if ((sun.i1 == 0 && sun.i2 == 0) || (sun.i1 == 0 && sun.i2 == 1)) "Less" else "Add"} : ${sun.name} ${if (sun.i2 == 1) sun.amount.formatToAmtDec() +"%" else ""}</td>
        <td class="right">${if (sun.i2 == 1) sun.percentValue.formatToAmtDec() else sun.amount.formatToAmtDec()}</td>
    </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
    <tr>
        <td class="right bold">Grand Total ₹</td>
        <td class="right bold">${grandTotal.formatToAmtDec()}</td>
    </tr>
</table>

<!-- TAX SUMMARY -->
<table>
    <tr>
        <th>Tax Rate</th>
        <th>Taxable Amt.</th>
        <th>IGST Amt.</th>
        <th>Total Tax</th>
    </tr>
    <tr>
        <td class="center">18%</td>
        <td class="right">9,000.00</td>
        <td class="right">1,620.00</td>
        <td class="right">1,620.00</td>
    </tr>
</table>

<!-- AMOUNT IN WORDS -->
<div class="box bold">
    ${numberToWords(grandTotal.toInt())} Only
</div>

<!-- BANK DETAILS -->
<div class="box">
    <b>Bank Details :</b><br>
    Bank Name : HDFC Bank<br>
    A/C No : 50200110813700<br>
    IFSC : HDFC0000654
</div>

<!-- FOOTER -->
<div class="footer flex">
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

</body>
</html>
 
        """.trimIndent()
    )





    return html.toString()
}

data class TransportDetails(
    val transportName : String,
    val gstRrNo : String,
    val vehicleNo : String,
    val station : String,
    val pincode : String,
    val  gstRrDate: String,
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
