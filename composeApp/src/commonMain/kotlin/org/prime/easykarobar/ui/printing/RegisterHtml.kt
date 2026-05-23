package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

fun registerHtml(
    title: String,
    startDate: String,
    endDate: String,
    rows: List<Quadruple<String, String, String, String>>,
    totalAmount: String
): String {
    val db = DatabaseHolder.instance
    val compInfo = db.companyInformationQueries.getCompanyInformation().executeAsOneOrNull()
    val user = SharedPrefs.User.get()

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
        padding: 2px 4px;
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

    /* Filler row: large min-height fills remaining page space after items. */
    .filler-row td {
        min-height: 80mm;
        height: 80mm;
        border-bottom: none !important;
        border-right: none !important;
    }

    /* ── Grand Total ────────────────────────────────────────────────── */
    .grand-total-row {
        display: flex;
        align-items: center;
        padding: 0;
        font-weight: bold;
        font-size: 9pt;
        border-bottom: 0.5pt solid #000;
    }
    .gt-label  { width: 75%; text-align: right; padding: 4px 8px; border-right: 0.5pt solid #000; }
    .gt-amt    { width: 25%; text-align: right; padding: 4px 8px; }

    /* ── Footer ─────────────────────────────────────────────────────── */
    .footer-section {
        display: flex;
        height: 80px;
    }
    .signature-section {
        width: 100%;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 4px 8px;
        text-align: right;
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
                <td style="padding:0; border:none; text-align:left;">GST : ${compInfo?.T4 ?: ""}</td>
                <td style="padding:0; border:none; text-align:right;">Period: ${Tdate(startDate)} to ${Tdate(endDate)}</td>
            </tr>
        </table>
    </div>

    <!-- Company header -->
    <div class="header-center border-b">
        <div class="title">${title.uppercase()}</div>
        <div class="company-name">${CompanyName()}</div>
        <div class="company-info">
            ${compInfo?.T3 ?: ""}<br>
            Tel. : ${user?.Mobile ?: ""} &nbsp; email : ${user?.Email ?: ""}
        </div>
    </div>

    <!-- Items table -->
    <div class="items-grow-section">
        <table class="items-table">
            <thead>
                <tr>
                    <th style="width:12%">Date</th>
                    <th style="width:50%">Account</th>
                    <th style="width:13%">Vch No.</th>
                    <th style="width:25%">Amount(₹)</th>
                </tr>
            </thead>
            <tbody>""".trimIndent()
    )

    rows.forEach { row ->
        html.append(
            """
            <tr>
                <td class="center">${row.first}</td>
                <td><b>${row.second}</b></td>
                <td class="center">${row.third}</td>
                <td class="right"><b>${row.fourth}</b></td>
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
    </div><!-- end items-grow-section -->

    <!-- Grand Total -->
    <div class="grand-total-row">
        <div class="gt-label">Grand Total</div>
        <div class="gt-amt">$totalAmount</div>
    </div>

    <!-- Footer -->
    <div class="footer-section">
        <div class="signature-section">
            <div style="text-align:right; padding-right: 20px;">
                For <b>${CompanyName()}</b><br><br><br>
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
