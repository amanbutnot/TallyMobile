package org.prime.easykarobar.data.expect

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.print.PrintManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.prime.easykarobar.AppContextHolder
import java.io.File

actual fun sharePdf(filePath: String) {
    val context = AppContextHolder.appContext
    val file = File(filePath)
    if (!file.exists()) {
        println("File Not Found")
        return
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    }
    Handler(Looper.getMainLooper()).post {
        val chooser = Intent.createChooser(intent, "Share PDF via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

}

actual fun printPdf(filePath: String) {
    val activity = AppContextHolder.activity ?: return
    val file = File(filePath)
    if (!file.exists()) return

    val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val printAdapter = object : android.print.PrintDocumentAdapter() {
        override fun onLayout(
            oldAttributes: android.print.PrintAttributes?,
            newAttributes: android.print.PrintAttributes?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: android.os.Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            val builder = android.print.PrintDocumentInfo.Builder(file.name)
            builder.setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(android.print.PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback?.onLayoutFinished(builder.build(), true)
        }

        override fun onWrite(
            pages: Array<out android.print.PageRange>?,
            destination: android.os.ParcelFileDescriptor?,
            cancellationSignal: android.os.CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            try {
                java.io.FileInputStream(file).use { input ->
                    java.io.FileOutputStream(destination?.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
                callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
            }
        }
    }
    printManager.print("Easy Karobar Document", printAdapter, null)
}

@Composable
actual fun shareText(text: String) {
    val context = LocalContext.current

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val chooser = Intent.createChooser(sendIntent, null)
    context.startActivity(chooser)
}