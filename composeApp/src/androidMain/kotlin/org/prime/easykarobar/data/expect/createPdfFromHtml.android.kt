package org.prime.easykarobar.data.expect

import android.os.Environment
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.prime.easykarobar.AppContextHolder
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

actual suspend fun createPdfFromHtml(html: String, fileName: String): String {
    return withContext(Dispatchers.IO) {
        val appContext = AppContextHolder.appContext
        val fileNameWithTime = "$fileName${System.currentTimeMillis()}.pdf"

        val dir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        dir?.mkdirs()

        val file = File(dir, fileNameWithTime)

        val writerProps = WriterProperties()
            .useSmartMode()                    // deduplicates repeated resources (fonts, images)
            .setCompressionLevel(1)            // 0=none, 9=max — level 1 is fast with decent size

        BufferedOutputStream(FileOutputStream(file), 64 * 1024).use { buffered ->
            val writer = PdfWriter(buffered, writerProps)
            val pdfDoc = PdfDocument(writer)

            val props = ConverterProperties().apply {
                isImmediateFlush = false        // batch flushes instead of flushing every element
            }

            // Pre-convert HTML string to bytes once — avoids repeated charset detection
            val htmlBytes = html.toByteArray(StandardCharsets.UTF_8)
            HtmlConverter.convertToPdf(ByteArrayInputStream(htmlBytes), pdfDoc, props)

            pdfDoc.close()
        }

        file.absolutePath
    }
}