package org.prime.tally.data.expect

import android.os.Environment
import com.itextpdf.html2pdf.HtmlConverter
import org.prime.tally.AppContextHolder
import java.io.File
import java.io.FileOutputStream

actual fun createPdfFromHtml(html: String,fileName:String): String {

    val appContext = AppContextHolder.appContext

    val fileName = "$fileName${System.currentTimeMillis()}.pdf"

    val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    if (dir != null && !dir.exists()) dir.mkdirs()

    val file = File(dir, fileName)

    FileOutputStream(file).use { output ->
        HtmlConverter.convertToPdf(html, output)
    }

    return file.absolutePath

}