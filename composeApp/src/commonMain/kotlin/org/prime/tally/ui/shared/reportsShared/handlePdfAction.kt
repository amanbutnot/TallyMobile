package org.prime.tally.ui.shared.reportsShared

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.prime.tally.data.expect.createPdfFromHtml
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class PdfAction {
    Download,
    Share
}

suspend fun handlePdfAction(
    fileName: String,
    htmlContent: String,
    action: PdfAction,
    onLoadingChange: (Boolean) -> Unit
) {
    onLoadingChange(true)
    delay(100)
    try {
        val platformFile = PlatformFile(
            createPdfFromHtml(htmlContent, fileName)
        )


        when (action) {
            PdfAction.Download -> {
                val file = FileKit.openFileSaver(
                    suggestedName = generateUniqueFileName(fileName),
                    extension = "pdf"
                )
                file?.write(platformFile)
            }
            PdfAction.Share -> {
                FileKit.shareFile(platformFile)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        onLoadingChange(false)
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

    return "${baseName}_$timestamp"
}