package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.expect.formatToAmtDec
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate
import kotlin.math.absoluteValue

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
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val user = SharedPrefs.User.get()

    val title = documentType.uppercase()

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

    /* ── Info section ─────────────────────────────────────────────── */
    .info-section { display: flex; }
    .info-col { width: 50%; }
    .info-table { width: 100%; border-collapse: collapse; }
    .info-table td { padding: 1px 8px; font-size: 8.5pt; vertical-align: top; }
    .label-cell { width: 35%; }

    /* ── Items grow section ─────────────────────────────────────────── */
    .items-grow-section {
        border-bottom: 0.5pt solid #000;
        flex-grow: 1;
    }

    table.items-table {
        width: 100%;
        border-collapse: collapse;
    }
    table.items-table thead { height: 20px; }
    table.items-table th,
    table.items-table td {
        border-right: 0.5pt solid #000;
        border-bottom: 0.5pt solid #000;
        padding: 4px 8px;
        font-size: 9pt;
        vertical-align: top;
        line-height: 1.2;
    }
    table.items-table th {
        font-weight: bold;
        text-align: center;
        background: #fff;
    }
    table.items-table th:last-child,
    table.items-table td:last-child { border-right: none; }

    .filler-row td {
        min-height: 80mm;
        height: 80mm;
        border-bottom: none !important;
        border-right: none !important;
    }

    .right { text-align: right; }
    .center { text-align: center; }
    .bold { font-weight: bold; }

    /* ── Grand Total ────────────────────────────────────────────────── */
    .total-row {
        display: flex;
        align-items: center;
        padding: 0;
        font-weight: bold;
        font-size: 9pt;
        border-bottom: 0.5pt solid #000;
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
        <div class="title">${title}</div>
        <div class="company-name">${CompanyName()}</div>
        <div class="company-info">
            ${compInfo?.T3.clean()}<br>
            Tel. : ${user?.Mobile.clean()} &nbsp; email : ${user?.Email.clean()}
        </div>
    </div>

    <!-- Voucher info -->
    <div class="info-section border-b">
        <div class="info-col border-r">
            <table class="info-table">
                <tr><td class="label-cell">Voucher No.</td><td>: <b>${voucherNo.clean()}</b></td></tr>
                <tr><td class="label-cell">Dated</td><td>: <b>${Tdate(date)}</b></td></tr>
            </table>
        </div>
        <div class="info-col">
        </div>
    </div>

    <!-- Items table -->
    <div class="items-grow-section">
        <table class="items-table">
            <thead>
                <tr>
                    <th style="width:10%">S.N.</th>
                    <th style="width:50%">Account</th>
                    <th style="width:20%">Debit (Rs.)</th>
                    <th style="width:20%">Credit (Rs.)</th>
                </tr>
            </thead>
            <tbody>""".trimIndent()
    )

    rows.forEach { row ->
        val debitStr = if (row.debit != null && row.debit != 0.0) row.debit.absoluteValue.formatToAmtDec() else ""
        val creditStr = if (row.credit != null && row.credit != 0.0) row.credit.absoluteValue.formatToAmtDec() else ""

        html.append(
            """
            <tr>
                <td class="center">${row.sn}</td>
                <td>${row.account.clean()}</td>
                <td class="right">$debitStr</td>
                <td class="right">$creditStr</td>
            </tr>""".trimIndent()
        )
    }

    html.append(
        """
            <tr class="filler-row" style="height:100%;">
                <td colspan="4"></td>
            </tr>
            </tbody>
        </table>
    </div>

    <!-- Total Row -->
    <div class="total-row">
        <div style="width:60%; text-align:right; padding:4px 8px;">TOTAL</div>
        <div style="width:20%; text-align:right; padding:4px 8px; border-left:0.5pt solid #000;">${totalDebit.absoluteValue.formatToAmtDec()}</div>
        <div style="width:20%; text-align:right; padding:4px 8px; border-left:0.5pt solid #000;">${totalCredit.absoluteValue.formatToAmtDec()}</div>
    </div>

    <!-- Amount in Words -->
    <div class="amount-in-words">
        Rupees ${numberToWords(totalDebit.coerceAtLeast(totalCredit).toInt())} Only
    </div>

    <!-- Footer -->
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

</div>
</body>
</html>""".trimIndent()
    )

    return html.toString()
}

