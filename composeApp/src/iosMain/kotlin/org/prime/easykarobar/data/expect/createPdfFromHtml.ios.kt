package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
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
actual suspend fun createPdfFromHtml(html: String, fileName: String): String {
    val printFormatter = UIMarkupTextPrintFormatter(html)

    val renderer = UIPrintPageRenderer()
    renderer.addPrintFormatter(printFormatter, 0L)

    val pdfData = NSMutableData()

    val bounds = CGRectMake(0.0, 0.0, 595.2, 841.8) // A4 size

    renderer.setValue(NSValue.valueWithCGRect(bounds), "paperRect")
    renderer.setValue(NSValue.valueWithCGRect(bounds), "printableRect")

    UIGraphicsBeginPDFContextToData(pdfData, bounds, null)

    for (i in 0 until renderer.numberOfPages) {
        UIGraphicsBeginPDFPage()
        renderer.drawPageAtIndex(i, inRect = bounds)
    }

    UIGraphicsEndPDFContext()

    val path = NSTemporaryDirectory() + "/$fileName.pdf"
    pdfData.writeToFile(path, atomically = true)

    return path
}