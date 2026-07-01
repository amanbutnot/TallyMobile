package org.prime.easykarobar.data.expect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
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
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.NSEC_PER_SEC
import platform.darwin.NSObject
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun createPdfFromHtml(html: String, fileName: String): String =
    withContext(Dispatchers.Main) {
        // ✅ FIX: Declare these outside suspendCancellableCoroutine so they are
        // strongly retained for the entire duration of the coroutine.
        // Inside the lambda, Kotlin/Native's ARC cannot guarantee the objects
        // stay alive — the WKWebView and its delegate were being deallocated
        // before didFinishNavigation fired, killing the WebContent process.
        var webViewRef: WKWebView? = null
        var delegateRef: NSObject? = null

        suspendCancellableCoroutine { continuation ->

            val webView = WKWebView(
                frame = CGRectMake(0.0, 0.0, 595.2, 841.8),
                configuration = WKWebViewConfiguration()
            )
            webViewRef = webView // ✅ retain strongly

            val delegate = object : NSObject(), WKNavigationDelegateProtocol {
                override fun webView(
                    webView: WKWebView,
                    didFinishNavigation: WKNavigation?
                ) {
                    println("📄 [createPdfFromHtml] didFinishNavigation fired")
                    
                    // Add a small delay to ensure images are fully rendered/loaded before printing
                    dispatch_after(
                        dispatch_time(DISPATCH_TIME_NOW, (1.0 * NSEC_PER_SEC.toDouble()).toLong()),
                        dispatch_get_main_queue()
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
                            println("📄 [createPdfFromHtml] Rendering $numberOfPages page(s)")
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
                            println("📄 [createPdfFromHtml] PDF written to: $path")

                            // ✅ Clear refs before resuming
                            webViewRef = null
                            delegateRef = null

                            continuation.resume(path)
                        } catch (e: Exception) {
                            webViewRef = null
                            delegateRef = null
                            continuation.resumeWithException(e)
                        }
                    }
                }
                @ObjCSignatureOverride
                override fun webView(
                    webView: WKWebView,
                    didFailNavigation: WKNavigation?,
                    withError: NSError
                ) {
                    println("❌ [createPdfFromHtml] didFailNavigation: ${withError.localizedDescription}")
                    webViewRef = null
                    delegateRef = null
                    continuation.resumeWithException(
                        Exception("WKWebView failed: ${withError.localizedDescription}")
                    )
                }

                @ObjCSignatureOverride
                override fun webView(
                    webView: WKWebView,
                    didFailProvisionalNavigation: WKNavigation?,
                    withError: NSError
                ) {
                    println("❌ [createPdfFromHtml] didFailProvisionalNavigation: ${withError.localizedDescription}")
                    webViewRef = null
                    delegateRef = null
                    continuation.resumeWithException(
                        Exception("WKWebView provisional load failed: ${withError.localizedDescription}")
                    )
                }
            }

            delegateRef = delegate // ✅ retain strongly
            webView.navigationDelegate = delegate

            println("📄 [createPdfFromHtml] Loading HTML into WKWebView...")
            val baseUrl = NSURL(string = "https://images.easykarobar.in/")
            webView.loadHTMLString(html, baseURL = baseUrl)

            continuation.invokeOnCancellation {
                println("📄 [createPdfFromHtml] Coroutine cancelled — cleaning up")
                webView.navigationDelegate = null
                webViewRef = null
                delegateRef = null
            }
        }
    }