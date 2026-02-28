package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSValue
import platform.Foundation.setValue
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIPrintPageRenderer
import platform.UIKit.valueWithCGRect
import platform.UIKit.viewPrintFormatter
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun createPdfFromHtml(html: String, fileName: String): String =
    withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->

            val webView = WKWebView(
                frame = CGRectMake(0.0, 0.0, 595.2, 841.8)
            )

            // Load HTML and wait for it to finish rendering
            webView.loadHTMLString(html, baseURL = null)

            // Observe when loading finishes
            val delegate = object : NSObject(), WKNavigationDelegateProtocol {
                override fun webView(
                    webView: WKWebView,
                    didFinishNavigation: WKNavigation?
                ) {
                    try {
                        val printFormatter = webView.viewPrintFormatter()
                        val renderer = UIPrintPageRenderer()
                        renderer.addPrintFormatter(printFormatter, 0L)

                        val paperRect = CGRectMake(0.0, 0.0, 595.2, 841.8)
                        renderer.setValue(NSValue.valueWithCGRect(paperRect), "paperRect")
                        renderer.setValue(NSValue.valueWithCGRect(paperRect), "printableRect")

                        val pdfData = NSMutableData()
                        UIGraphicsBeginPDFContextToData(pdfData, paperRect, null)

                        val numberOfPages = renderer.numberOfPages
                        renderer.prepareForDrawingPages(
                            NSMakeRange(0u, numberOfPages.toULong())
                        )

                        for (i in 0L until numberOfPages) {
                            UIGraphicsBeginPDFPage()
                            renderer.drawPageAtIndex(i, inRect = paperRect)
                        }

                        UIGraphicsEndPDFContext()

                        val sanitizedFileName = fileName.replace(
                            "[^a-zA-Z0-9]".toRegex(), "_"
                        )
                        val tempDir = NSTemporaryDirectory()
                        val path = if (tempDir.endsWith("/"))
                            "$tempDir$sanitizedFileName.pdf"
                        else
                            "$tempDir/$sanitizedFileName.pdf"

                        pdfData.writeToFile(path, atomically = true)

                        continuation.resume(path)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun webView(
                    webView: WKWebView,
                    didFailNavigation: WKNavigation?,
                    withError: NSError
                ) {
                    continuation.resumeWithException(
                        Exception("WKWebView failed: ${withError.localizedDescription}")
                    )
                }
            }

            webView.navigationDelegate = delegate

            // Keep delegate alive until coroutine completes
            continuation.invokeOnCancellation {
                webView.navigationDelegate = null
            }
        }
    }