package org.prime.easykarobar

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import java.lang.ref.WeakReference


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppContextHolder.appContext = applicationContext
        AppContextHolder.activityRef = WeakReference(this)
        FileKit.init(this)
        setContent {
            App()
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
