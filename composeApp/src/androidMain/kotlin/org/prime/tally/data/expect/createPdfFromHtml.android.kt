package org.prime.tally.data.expect

import android.os.Environment
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.prime.tally.AppContextHolder
import java.io.File
import java.io.FileOutputStream

actual suspend fun createPdfFromHtml(html: String, fileName: String): String {
    return withContext(Dispatchers.IO) {
        val appContext = AppContextHolder.appContext
        val fileNameWithTime = "$fileName${System.currentTimeMillis()}.pdf"

        val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (dir != null && !dir.exists()) dir.mkdirs()

        val file = File(dir, fileNameWithTime)
        val props = ConverterProperties().apply {
            baseUri = null
            isImmediateFlush = true
        }

        FileOutputStream(file).use { output ->
            HtmlConverter.convertToPdf(html.byteInputStream(), output, props)
        }

        file.absolutePath
    }
}
