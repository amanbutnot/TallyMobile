package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.model.transactions.SundryItem
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.transactions.sale.InvoiceItem
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import kotlin.math.absoluteValue

fun salesSlipHtml(
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
    val user = SharedPrefs.User.get()

    val isIgst =
        user?.State.clean() != partyDetails?.State.clean() &&
                partyDetails?.State.clean().isNotBlank() &&
                user?.State.clean().isNotBlank()

    val title = if (name == "Sale Invoice") "TAX INVOICE" else name.uppercase()

    val totalQty = items.sumOf { it.qty }.absoluteValue
    val totalGst = items.sumOf { it.gstAmt }
    val totalSale = items.sumOf { it.taxable }
    val totalSavings = items.sumOf {
        (it.listPrice * it.qty.absoluteValue) - it.taxable
    }.coerceAtLeast(0.0)

    return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
 @page {
    size: 3in auto;
    margin: 0.1in;
}

    * {
        box-sizing: border-box;
    }

    html, body {
        margin: 0;
        padding: 0;
        font-family: Arial, Helvetica, sans-serif;
        font-size: 10pt;
        color: #000;
        line-height: 1.18;
    }

.slip {
    width: 3in;
    margin: 0;
}

    .center { text-align: center; }
    .right { text-align: right; }
    .bold { font-weight: 700; }

    .company {
        font-size: 14pt;
        font-weight: 700;
        text-align: center;
        margin-top: 2mm;
    }

    .sub {
        text-align: center;
        font-size: 9.5pt;
    }

    .gap {
        height: 7mm;
    }

    .line {
        border-top: 1px solid #000;
        margin: 2px 0;
    }

    .title {
        text-align: center;
        font-size: 13pt;
        font-weight: 800;
        line-height: 1;
    }

    table {
        width: 100%;
        border-collapse: collapse;
    }

    td, th {
        padding: 1px 0;
        vertical-align: top;
        font-size: 9.5pt;
    }

    .info-label {
        width: 30%;
        white-space: nowrap;
    }

    .items th {
        font-weight: 400;
        text-align: left;
    }

    .items .head2 th {
        padding-top: 2px;
    }

    .item-main td {
        font-weight: 700;
        padding-top: 4px;
    }

    .item-sub td {
        padding-top: 3px;
    }

    .under-right {
        border-top: 1px solid #000;
    }

    .amount-words {
        border-top: 1px solid #000;
        border-bottom: 1px solid #000;
        font-size: 9pt;
        font-weight: 700;
        padding: 2px 0;
        margin-top: 2px;
    }

    .tax th {
        font-weight: 700;
        border-bottom: 1px solid #000;
    }

    .totals {
        width: 48mm;
        margin-top: 4px;
    }

    .totals td {
        font-size: 11pt;
        font-weight: 800;
        padding: 1px 0;
    }

    .footer {
        margin-top: 9px;
        border-top: 1px solid #000;
        border-bottom: 1px solid #000;
        text-align: center;
        font-size: 12pt;
        font-weight: 900;
        padding: 5px 0 3px;
    }
</style>
</head>

<body>
<div class="slip">

    <div class="company">${CompanyName().clean()}</div>
    <div class="sub">${compInfo?.T3.clean()}</div>

    <div class="gap"></div>

    <div class="sub">Phone : ${user?.Mobile.clean()}</div>
    <div class="sub bold">GSTIN : ${compInfo?.T4.clean()}</div>

    <div class="line"></div>
    <div class="title">$title</div>
    <div class="line"></div>

    <table>
        <tr>
            <td class="info-label">Invoice No/Date :</td>
            <td>${invoiceNo.clean()}</td>
            <td class="right">/ ${Tdate(date)}</td>
        </tr>
        <tr>
            <td class="info-label">Customer Name</td>
            <td colspan="2">${partyName.clean()}</td>
        </tr>
        <tr>
            <td class="info-label">Cust Mobile No :</td>
            <td colspan="2">${partyDetails?.MobileNo.clean()}</td>
        </tr>
    </table>

    <div class="line"></div>

    <table class="items">
        <thead>
        <tr>
            <th style="width: 7%;">Sl</th>
            <th style="width: 42%;">Product</th>
            <th style="width: 17%;" class="right">Price</th>
            <th style="width: 16%;" class="right">Disc(%)</th>
            <th style="width: 18%;" class="right">Amt.</th>
        </tr>
        <tr class="head2">
            <th></th>
            <th>Qty. &nbsp;&nbsp;&nbsp; HSN Code</th>
            <th class="right">GST %</th>
            <th colspan="2" class="right">GST Amt</th>
        </tr>
        </thead>

        <tbody>
            ${
        items.mapIndexed { index, item ->
            val unitTaxable = if (item.qty != 0) {
                item.taxable / item.qty.absoluteValue
            } else {
                0.0
            }

            val disc = when {
                item.CD.isNotBlank() -> item.CD
                item.discountPercentage != 0.0 -> item.discountPercentage.formatToAmtDec()
                else -> "--"
            }

            """
        <tr class="item-main">
            <td>${index + 1}</td>
            <td>${item.name.clean()}</td>
            <td class="right">${unitTaxable.formatToAmtDec()}</td>
            <td class="right">$disc</td>
            <td class="right">${item.net.formatToAmtDec()}</td>
        </tr>
        <tr class="item-sub">
            <td></td>
            <td>${item.qty.absoluteValue.toDouble().formatToAmtDec()} &nbsp;&nbsp;&nbsp; ${item.hsn.clean()}</td>
            <td class="right">${item.gstPercentage.formatToAmtDec()}%</td>
            <td colspan="2" class="right">${item.gstAmt.formatToAmtDec()}</td>
        </tr>
            """.trimIndent()
        }.joinToString("\n")
    }
        </tbody>
    </table>

    <table style="margin-top: 8px;">
        <tr>
            <td style="width: 67%;"></td>
            <td class="under-right"></td>
        </tr>
    </table>

    <div class="line" style="margin-top: 7px;"></div>

    <table>
        <tr>
            <td style="padding-left: 5mm;" class="bold">${totalQty.toDouble().formatToAmtDec()}</td>
            <td class="right bold">${grandTotal.formatToAmtDec()}</td>
        </tr>
    </table>

    <div class="amount-words">
        Rupees&nbsp;&nbsp; ${amountToWords(grandTotal)}
    </div>

    ${if (showTax) """
    <table class="tax">
        <thead>
        <tr>
            <th style="width: 22%;">Tax Rate</th>
            <th style="width: 36%;">Taxable Amt.</th>
            <th style="width: 42%;">${if (isIgst) "IGST" else "GST"} Amt.</th>
        </tr>
        </thead>
        <tbody>
        ${
        items.groupBy { it.gstPercentage }.map { (rate, group) ->
            """
            <tr>
                <td>${rate.formatToAmtDec()}%</td>
                <td>${group.sumOf { it.taxable }.formatToAmtDec()}</td>
                <td>${group.sumOf { it.gstAmt }.formatToAmtDec()}</td>
            </tr>
            """.trimIndent()
        }.joinToString("\n")
    }
        </tbody>
    </table>
    """ else ""}

    <table class="totals">
        <tr>
            <td>Total GST</td>
            <td class="center">:</td>
            <td>${totalGst.formatToAmtDec()}</td>
        </tr>
        <tr>
            <td>Total Sale</td>
            <td class="center">:</td>
            <td>${totalSale.formatToAmtDec()}</td>
        </tr>
        <tr>
            <td>Total Savings</td>
            <td class="center">:</td>
            <td>${totalSavings.formatToAmtDec()}</td>
        </tr>
        <tr>
            <td>Net Payable</td>
            <td class="center">:</td>
            <td>${grandTotal.formatToAmtDec()}</td>
        </tr>
    </table>

    <div class="line" style="margin-top: 8px;"></div>

    <div class="footer">
        THANK YOU. VISIT US AGAIN.
    </div>

</div>
</body>
</html>
""".trimIndent()
}



private fun amountToWords(amount: Double): String {
    val wholePart = amount.toInt()
    val decimalPart = ((amount - wholePart) * 100 + 0.5).toInt()

    val wholeWords = numberToWords(wholePart)
    val decimalWords =
        if (decimalPart > 0) " and Paisa ${numberToWords(decimalPart)}" else ""

    return "$wholeWords$decimalWords Only"
}

