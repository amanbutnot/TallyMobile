package org.prime.easykarobar.data.expect

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.prime.easykarobar.AppContextHolder
import java.io.File

actual fun sharePdf(filePath: String) {
    val context = AppContextHolder.appContext
    val file = File(filePath)
    if(!file.exists()){
    println("File Not Found")
        return
    }
    val uri = FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM,uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    }
    Handler(Looper.getMainLooper()).post {
        context.startActivity(Intent.createChooser(intent, "Share PDF via"))
    }

}