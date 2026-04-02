package org.prime.easykarobar.data.expect

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.prime.easykarobar.AppContextHolder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


actual suspend fun createExcel(
    fileName: String,
    headers: List<String>,
    rows: List<List<String>>
): String {
    return withContext(Dispatchers.IO) {

        val context = AppContextHolder.appContext
        val fileNameWithTime = "$fileName${System.currentTimeMillis()}.xlsx"

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        dir?.mkdirs()

        val file = File(dir, fileNameWithTime)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Trial Balance")

        // 🔹 Styles
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
            alignment = org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
        }

        val textLeftStyle = workbook.createCellStyle().apply {
            alignment = org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT
        }

        val textRightStyle = workbook.createCellStyle().apply {
            alignment = org.apache.poi.ss.usermodel.HorizontalAlignment.RIGHT
        }

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { colIndex, value ->
            val cell = headerRow.createCell(colIndex)
            cell.setCellValue(value)
            cell.cellStyle = headerStyle
        }

        rows.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)

            rowData.forEachIndexed { colIndex, cellValue ->
                val cell = row.createCell(colIndex)
                cell.setCellValue(cellValue)

                cell.cellStyle = if (colIndex == 0) textLeftStyle else textRightStyle
            }
        }


        sheet.setColumnWidth(0, 25 * 256) // Account Name
        sheet.setColumnWidth(1, 15 * 256) // Debit
        sheet.setColumnWidth(2, 15 * 256) // Credit

        BufferedOutputStream(FileOutputStream(file), 64 * 1024).use { output ->
            workbook.write(output)
        }

        workbook.close()

        file.absolutePath
    }
}