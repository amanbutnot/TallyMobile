package org.prime.easykarobar.ui.shared.reportsShared

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.prime.easykarobar.data.expect.createExcel
import org.prime.easykarobar.data.expect.createPdfFromHtml
import org.prime.easykarobar.data.expect.sharePdf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class PdfAction {
    Download,
    Share, DownloadExcel, ShareExcel
}

suspend fun handlePdfAction(
    fileName: String,
    htmlContent: String,
    action: PdfAction,
    headers: List<String>? = null,
    rows: List<List<String>>? = null,
    onLoadingChange: (Boolean) -> Unit
) {
    println("📄 [handlePdfAction] Started — fileName: $fileName | action: $action")
    println("📄 [handlePdfAction] HTML content length: ${htmlContent.length} chars")

    onLoadingChange(true)
    println("📄 [handlePdfAction] Loading state set to true")

    delay(100)
    var filePath = ""

    try {
        println("📄 [handlePdfAction] Calling createPdfFromHtml...")
        filePath = if (action == PdfAction.Download || action == PdfAction.Share) {
            createPdfFromHtml(htmlContent, fileName)
        } else {
            createExcel(
                fileName,
                headers as List<String>, rows as List<List<String>>
            )
        }
        println("📄 [handlePdfAction] createPdfFromHtml returned path: $filePath")

        if (filePath.isEmpty()) {
            println("❌ [handlePdfAction] filePath is empty — PDF creation likely failed")
            return
        }

        when (action) {
            PdfAction.Download -> {
                println("📄 [handlePdfAction] Action: Download — opening file saver")
                val uniqueName = generateUniqueFileName(fileName)
                println("📄 [handlePdfAction] Suggested file name: $uniqueName")
                val file = FileKit.openFileSaver(
                    suggestedName = uniqueName,
                    extension = "pdf"
                )
                if (file == null) {
                    println("⚠️ [handlePdfAction] File saver returned null — user may have cancelled")
                } else {
                    println("📄 [handlePdfAction] Writing to file: $file")
                    file.write(PlatformFile(filePath))
                    println("✅ [handlePdfAction] File written successfully")
                }
            }

            PdfAction.Share -> {
                println("📄 [handlePdfAction] Action: Share — calling sharePdf")
                sharePdf(filePath)
                println("📄 [handlePdfAction] sharePdf call returned")
            }

            PdfAction.ShareExcel -> {}
            PdfAction.DownloadExcel -> {
                val uniqueName = generateUniqueFileName(fileName)
                val file = FileKit.openFileSaver(
                    suggestedName = uniqueName,
                    extension = "xlsx"
                )
                if (file == null) {
                    println("⚠️ [handlePdfAction] File saver returned null — user may have cancelled")
                } else {
                    println("📄 [handlePdfAction] Writing to file: $file")
                    file.write(PlatformFile(filePath))
                    println("✅ [handlePdfAction] File written successfully")
                }
            }
        }
    } catch (e: Exception) {
        println("❌ [handlePdfAction] Exception caught: ${e::class.simpleName} — ${e.message}")
        e.printStackTrace()
    } finally {
        onLoadingChange(false)
        println("📄 [handlePdfAction] Loading state set to false — done")
    }
}

@OptIn(ExperimentalTime::class)
fun generateUniqueFileName(baseName: String): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    val timestamp =
        (now.year % 100).toString().padStart(2, '0') +
                now.month.number.toString().padStart(2, '0') +
                now.day.toString().padStart(2, '0') +
                now.hour.toString().padStart(2, '0') +
                now.minute.toString().padStart(2, '0') +
                now.second.toString().padStart(2, '0')

    val result = "${baseName}_$timestamp"
    println("📄 [generateUniqueFileName] Generated: $result")
    return result
}