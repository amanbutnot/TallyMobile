package org.prime.easykarobar.data.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
actual fun rememberBarcodeScanner(onResult: (String?) -> Unit): BarcodeScannerLauncher {
    val context = LocalContext.current
    val scanner = remember { GmsBarcodeScanning.getClient(context) }

    return remember {
        BarcodeScannerLauncher {
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    onResult(barcode.rawValue)
                }
                .addOnFailureListener {
                    onResult(null)
                }
                .addOnCanceledListener {
                    onResult(null)
                }
        }
    }
}

actual class BarcodeScannerLauncher(
    private val onLaunch: () -> Unit
) {
    actual fun launch() {
        onLaunch()
    }
}