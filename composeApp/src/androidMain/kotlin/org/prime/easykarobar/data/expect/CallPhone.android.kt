package org.prime.easykarobar.data.expect

import android.content.Intent
import android.net.Uri
import org.prime.easykarobar.AppContextHolder

actual fun callPhone(number: String) {
    val uri = Uri.parse("tel:91${number}")
    val intent = Intent(Intent.ACTION_DIAL, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val context = AppContextHolder.appContext
    context.startActivity(intent)
}