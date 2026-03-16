package org.prime.easykarobar.ui.printing

import org.prime.easykarobar.ui.shared.globalShared.CompanyName
import org.prime.easykarobar.ui.shared.globalShared.Tdate

data class AttendanceRow(
    val date: String,
    val time: String,
    val salesmen: String,
    val party: String,
    val status: String,
    val address: String,
    val photoUrl: String
)

fun attendanceHtml(
    title: String,
    rows: List<AttendanceRow>,
    startDate: String,
    endDate: String,
    isCheckIn: Boolean,
): String {
    val colSpan = if (isCheckIn) 7 else 5
    val html = StringBuilder()
    html.append(
        """
        <html>
        <head>
        <style>
            body { font-family: Arial, sans-serif; font-size: 12pt; margin: 16px; }
            table { width: 100%; border-collapse: collapse; margin-top: 12px; }
            th, td { border: 1px solid #000; padding: 6px; }
            th { background-color: #f5f5f5; text-align: center; }
            td.number { text-align: right; }
            td.name { text-align: left; }
            td.center { text-align: center; }
            td.photo { text-align: center; padding: 4px; }
            img.attendance-photo { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
            h2, h1, h3 { text-align: center; margin-bottom: 0; }
        </style>
        </head>
        <body>
        <h1>$title</h1>
        <h3>${Tdate(startDate)} To ${Tdate(endDate)}</h3>
        <h3>${CompanyName()}</h3>
        <table>
        <tr>
            <th style="min-width: 100px; white-space: nowrap;">Date</th>
            <th style="min-width: 80px; white-space: nowrap;">Time</th>
            ${if (isCheckIn) "<th>Salesmen</th><th>Party</th>" else ""}
            <th>Status</th>
            <th>Address</th>
     
        </tr>
        """.trimIndent()
    )

    rows.forEach { row ->
//        val photoTag = if (row.photoUrl.isNotBlank())
//            """<img class="attendance-photo" src="${row.photoUrl}" alt="photo" />"""
//        else
//            "-"

        html.append(
            """
            <tr>
                <td class="center" style="white-space: nowrap;">${Tdate(row.date)}</td>
                <td class="center" style="white-space: nowrap;">${row.time.ifBlank { "-" }}</td>
                ${
                if (isCheckIn) """
                <td class="name">${row.salesmen.ifBlank { "-" }}</td>
                <td class="name">${row.party.ifBlank { "-" }}</td>
                """ else ""
            }
                <td class="center">${row.status.ifBlank { "-" }}</td>
                <td class="name">${row.address.ifBlank { "-" }}</td>
                
            </tr>
            """.trimIndent()
        )
    }

    html.append(
        """
        <tr>
            <th colspan="$colSpan" style="text-align: center;">Total Records: ${rows.size}</th>
        </tr>
        </table>
        </body>
        </html>
        """.trimIndent()
    )

    return html.toString()
}

//       <th>Photo</th>
//<td class="photo">$photoTag</td>