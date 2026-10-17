package org.prime.easykarobar

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.messaging.FirebaseMessaging
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.prime.easykarobar.data.expect.subscribeUserToFcmTopic
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.reports.ledger.LedgerReportFilterScreen
import java.lang.ref.WeakReference


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCMToken", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("FCMToken", "Current FCM Token: $token")
            if (!token.isNullOrBlank()) {
                SharedPrefs.FcmToken.save(token)
                subscribeUserToFcmTopic()
            }
        }
        createChannel(this)
        AppContextHolder.appContext = applicationContext
        AppContextHolder.activityRef = WeakReference(this)
        FileKit.init(this)

        val initialScreen = intent.data?.let { uri ->
            when {
                uri.scheme == "https" && uri.host == "easykarobar.app" -> {
                    when (uri.pathSegments.firstOrNull()) {
                        "ledger-filter" -> {
                            val showAccount = uri.getQueryParameter("showAccount")?.toBoolean() ?: true
                            val isItemLedger = uri.getQueryParameter("isItemLedger")?.toBoolean() ?: false
                            LedgerReportFilterScreen(showAccount = showAccount, isItemLedger = isItemLedger)
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }

        setContent {
            App(initialScreen)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppContextHolder.activityRef = null
    }
}

object AppContextHolder{
    lateinit var appContext: Context
    var activityRef: WeakReference<Activity>? = null
    val activity: Activity? get() = activityRef?.get()
}
