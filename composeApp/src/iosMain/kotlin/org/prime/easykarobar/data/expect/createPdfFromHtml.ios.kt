package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIMarkupTextPrintFormatter
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.valueWithCGRect

@OptIn(ExperimentalForeignApi::class)
actual suspend fun createPdfFromHtml(html: String, fileName: String): String = withContext(Dispatchers.Main) {
    val printFormatter = UIMarkupTextPrintFormatter(html)

    val renderer = UIPrintPageRenderer()
    renderer.addPrintFormatter(printFormatter, 0L)

    // A4 size: 595.2 x 841.8 points
    val paperRect = CGRectMake(0.0, 0.0, 595.2, 841.8)

    // Use KVC to set paperRect and printableRect as they are read-only properties
    renderer.setValue(NSValue.valueWithCGRect(paperRect), "paperRect")
    renderer.setValue(NSValue.valueWithCGRect(paperRect), "printableRect")

    val pdfData = NSMutableData()

    // Start the PDF context
    UIGraphicsBeginPDFContextToData(pdfData, paperRect, null)

    // Accessing numberOfPages triggers the layout. 
    // We also call prepareForDrawingPages to ensure the renderer is ready.
    val numberOfPages = renderer.numberOfPages
    renderer.prepareForDrawingPages(NSMakeRange(0u, numberOfPages.toULong()))

    for (i in 0L until numberOfPages) {
        UIGraphicsBeginPDFPage()
        renderer.drawPageAtIndex(i, inRect = paperRect)
    }

    UIGraphicsEndPDFContext()

    // Sanitize the filename and construct the temporary path
    val sanitizedFileName = fileName.replace("[^a-zA-Z0-9]".toRegex(), "_")
    val tempDir = NSTemporaryDirectory()
    val path = if (tempDir.endsWith("/")) "$tempDir$sanitizedFileName.pdf" else "$tempDir/$sanitizedFileName.pdf"
    
    pdfData.writeToFile(path, atomically = true)

    path
}
